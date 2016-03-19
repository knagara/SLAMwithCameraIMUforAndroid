package jp.ac.u_tokyo.slamwithcameraimu;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple wrapper around a Camera and a SurfaceView that renders a centered
 * preview of the Camera to the surface. We need to center the SurfaceView
 * because not all devices have cameras that support preview sizes at the same
 * aspect ratio as the device's display.
 */
class Preview extends ViewGroup implements SurfaceHolder.Callback {
	private final String TAG = "SLAM";

	MqttClientService MCS;
	String detectorStr;
	float threshold;

	Context mContext;
	SurfaceView mSurfaceView;
	SurfaceHolder mHolder;
	Size mPreviewSize;
	List<Size> mSupportedPreviewSizes;
	Camera mCamera;
	private boolean mProgressFlag = false;

	int count = 0, frame = 0, prevFrame = -1;
	boolean isFirst = true;

	String path = "";
	SimpleDateFormat dateFormat;
	Size prevSize;
	private Mat mGray;
	private FeatureDetector detector;
	private DescriptorExtractor extractor;

	Mat image01, image02;
//	Mat image01KP, image02KP;
	MatOfKeyPoint keyPoint01, keyPoint02;
	Mat descripters01, descripters02;
	MatOfDMatch matches, matches_reverse;
	DescriptorMatcher matcher;
	Mat matchedImage;


	Preview(Context context, MqttClientService MCS) {
		super(context);

		this.MCS = MCS;
		mContext = context;

		mSurfaceView = new SurfaceView(context);
		addView(mSurfaceView);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = mSurfaceView.getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
	}

	public void setDetector(String detector_){
		detectorStr = detector_;
	}

	public void setThreshold(float threshold_){
		threshold = threshold_;
	}

	public SurfaceHolder getHolder(){
		return mHolder;
	}

	/**
	 * Intialize Open CV
	 */
	public void initOpenCV(){
		// Initialize Mat
		mGray = new Mat(prevSize.height, prevSize.width, CvType.CV_8U);
		image02 = new Mat(prevSize.width, prevSize.height, CvType.CV_8U);
		image01 = image02;
		descripters02 = new Mat(image02.rows(), image02.cols(),image02.type());
		descripters01 = descripters02;
		keyPoint02 = new MatOfKeyPoint();
		keyPoint01 = new MatOfKeyPoint();
		matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
		matchedImage = new Mat(image02.rows(),image02.cols() * 2, image02.type());

		// Features2d detector
		if(detectorStr.equals("FAST")){
			detector = FeatureDetector.create(FeatureDetector.FAST);
		}else if(detectorStr.equals("ORB")){
			detector = FeatureDetector.create(FeatureDetector.ORB);
		}else if(detectorStr.equals("BRISK")){
			detector = FeatureDetector.create(FeatureDetector.BRISK);
		}else{
			throw new IllegalArgumentException("Error: Please set the type of feature detector!");
		}

		// Features2d extractor
		extractor = DescriptorExtractor.create(DescriptorExtractor.BRISK);

		// read conf of detector
		if(detectorStr.equals("FAST")){
			path = Environment.getExternalStorageDirectory().getPath()+"/DCIM/SLAMwithCameraIMU/conf/FASTdetector.txt";
		}else if(detectorStr.equals("ORB")){
			path = Environment.getExternalStorageDirectory().getPath()+"/DCIM/SLAMwithCameraIMU/conf/ORBdetector.txt";
		}else if(detectorStr.equals("BRISK")){
			path = Environment.getExternalStorageDirectory().getPath()+"/DCIM/SLAMwithCameraIMU/conf/BRISKdetector.txt";
		}else{
			throw new IllegalArgumentException("Error: Please set the type of feature detector!");
		}
		detector.read(path);

		// write conf of detector
//		path = Environment.getExternalStorageDirectory()
//				.getPath()
//				+ "/DCIM/SLAMwithCameraIMU/file/detector.txt";
//		detector.write(path);

//		// read conf of extractor
//		path = Environment.getExternalStorageDirectory()
//				.getPath()
//				+ "/DCIM/SLAMwithCameraIMU/conf/extractor.txt";
//		extractor.read(path);
	}

	private final Camera.PreviewCallback editPreviewImage = new Camera.PreviewCallback() {

		public void onPreviewFrame(byte[] data, Camera camera) {

//			count++;
//			Log.d(TAG, "count = " + count);
//			if (count >= 10) {
//				count = 0;
//				mCamera.stopPreview();

//				Log.d(TAG, "onPreviewFrame");

				if(isFirst){
					isFirst = false;
					initOpenCV();
					new QuickToastTask(mContext, "Camera start", 100).execute();
				}

				if(prevFrame != frame){
					prevFrame = frame;

					//new QuickToastTask(mContext, "captured", 10).execute();

					mGray.put(0, 0, data); // Create gray scale image  プレビュー画像NV21のYデータをコピーすればグレースケール画像になる
					Core.flip(mGray.t(), image02, 0); // Rotate image  ポートレイト＋フロントなので回転
					Core.flip(image02, image02, -1);

					new FeatureDetectTask().execute(image02);
				}

//				mCamera.setPreviewCallback(editPreviewImage);
//				mCamera.startPreview();
			}
//		}
	};

	public void setCamera(Camera camera) {
		mCamera = camera;
		if (mCamera != null) {
			mSupportedPreviewSizes = mCamera.getParameters()
					.getSupportedPreviewSizes();
			requestLayout();
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		try {
			if (mCamera != null) {
				mCamera.setPreviewDisplay(holder);
			}
		} catch (IOException exception) {
			Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		try{
			// Now that the size is known, set up the camera parameters and begin
			// the preview.
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
			requestLayout();

			prevSize = parameters.getPreviewSize();

			mCamera.setParameters(parameters);
			mCamera.setPreviewCallback(editPreviewImage);
			mCamera.startPreview();
		}catch(Exception e){

		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		if (mCamera != null) {
			mCamera.stopPreview();
		}
	}

	public void stopPreview(){
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			setCamera(null);
		}
	}

	public void switchCamera(Camera camera) {
		setCamera(camera);
		try {
			camera.setPreviewDisplay(mHolder);
		} catch (IOException exception) {
			Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
		}
		Camera.Parameters parameters = camera.getParameters();
		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		requestLayout();

		camera.setParameters(parameters);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// We purposely disregard child measurements because act as a
		// wrapper to a SurfaceView that centers the camera preview instead
		// of stretching it.
		final int width = resolveSize(getSuggestedMinimumWidth(),
				widthMeasureSpec);
		final int height = resolveSize(getSuggestedMinimumHeight(),
				heightMeasureSpec);
		setMeasuredDimension(width, height);

		if (mSupportedPreviewSizes != null) {
			mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width,
					height);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed && getChildCount() > 0) {
			final View child = getChildAt(0);

			final int width = r - l;
			final int height = b - t;

			int previewWidth = width;
			int previewHeight = height;
			if (mPreviewSize != null) {
				previewWidth = mPreviewSize.width;
				previewHeight = mPreviewSize.height;
			}

			// Center the child SurfaceView within the parent.
			if (width * previewHeight > height * previewWidth) {
				final int scaledChildWidth = previewWidth * height
						/ previewHeight;
				child.layout((width - scaledChildWidth) / 2, 0,
						(width + scaledChildWidth) / 2, height);
			} else {
				final int scaledChildHeight = previewHeight * width
						/ previewWidth;
				child.layout(0, (height - scaledChildHeight) / 2, width,
						(height + scaledChildHeight) / 2);
			}
		}
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public class FeatureDetectTask extends AsyncTask<Mat, Integer, Mat> {

		public FeatureDetectTask() {
			super();
		}

	    protected Mat doInBackground(Mat... mat) {

			long currentTimeMillis = System.currentTimeMillis();

	    	image02 = mat[0].clone();

	    	// Calc processing time  処理時間計測
//	    	long start = System.nanoTime();

	    	// Detect keypoints  特徴点抽出
			detector.detect(image02, keyPoint02);

			// Delete nearest neighbor keypoints  近傍にある特徴点の削除
			// New keypoint list  新しいkeyPointリスト
			LinkedList<KeyPoint> newKeyPointList = new LinkedList<KeyPoint>(keyPoint02.toList());
			// Substitute (x,y) for array from Mat  Matから特徴点の(x,y)抜き出して配列に代入
			int KPtotal = keyPoint02.rows();
			int[][] KP = new int[KPtotal][3];
			for(int i=0;i<KPtotal;i++){
				double[] xy = keyPoint02.get(i, 0);
				KP[i][0] = 0; // 0 = false  delete flag false  削除フラグfalse
				KP[i][1] = (int) (xy[0]);
				KP[i][2] = (int) (xy[1]);
			}
			// If near keypoint is found, delete it  近傍点が存在する場合，それを削除する
			for(int i=0;i<KPtotal;i++){
				if(KP[i][0] == 1){ // 1 = true  delete flag ture  削除フラグtrue
					//newKeyPointList.remove(i);
				}else{
					// Search near keypoints  近傍点探索
					for(int j=(i+1);j<KPtotal;j++){
						if(KP[i][1]-2 <= KP[j][1] && KP[j][1] <= KP[i][1]+2 &&
							KP[i][2]-2 <= KP[j][2] && KP[j][2] <= KP[i][2]+2){
							KP[j][0] = 1; // 1 = true  delete flag ture  削除フラグtrue
						}
					}
				}
			}
			// If delete flag is true, delete it  削除フラグtrueになっている場所を削除
			int ii = 0;
			Iterator<KeyPoint> itr = newKeyPointList.iterator();
			while(itr.hasNext()){
			    itr.next();
			    if(KP[ii][0] == 1){
			    	itr.remove();
			    }
			    ii++;
			}
			// newKeyPointList -> keyPoint02
			keyPoint02.release();
			keyPoint02.fromList(newKeyPointList);

			// Descript keypoint's feature  特徴量記述
			extractor.compute(image02, keyPoint02, descripters02);

			// Draw keypoints  特徴点の描画
//			Features2d.drawKeypoints(image02, keyPoint02, image02);
//			path = Environment.getExternalStorageDirectory()
//					.getPath()
//					+ "/DCIM/SLAMwithCameraIMU/img/"
//					+ dateFormat.format(new Date()) + "_KP.jpg";
//			Highgui.imwrite(path, image02);

			if (frame > 0) {

				// Matching  マッチング
				matches = new MatOfDMatch();
				matches_reverse = new MatOfDMatch();
				if(descripters01.empty() == false &&
					descripters02.empty() == false){
					matcher.match(descripters01, descripters02, matches);
					matcher.match(descripters02, descripters01, matches_reverse);
				}

				// Filtering of matching  マッチングのフィルタリング
				// (1) Cross check  クロスチェック
				// (2) Delete matching which has too big distance above threshold  しきい値以上のdistanceを持つマッチングを除去
				DMatch[] match12 = matches.toArray();
				DMatch[] match21 = matches_reverse.toArray();
				ArrayList<DMatch> listMatch = new ArrayList<DMatch>();
				for (int i = 0; i < match12.length; i++) {
					DMatch forward = match12[i];
					DMatch backward = match21[forward.trainIdx];
					if (backward.trainIdx == forward.queryIdx &&
							forward.distance < threshold){
						listMatch.add(forward);
					}
				}
				if(listMatch.size() > 0){
					// Sort by distance ascending order  distanceで昇順にソート（マッチング度が高い順で上から並ぶ）
					Collections.sort(listMatch, new DMatchComparator());
					// ArrayList -> MatOfDMatch
					matches.fromList(listMatch);
				}else{
					matches = new MatOfDMatch();
				}

//				// Save image  画像を保存
//				path = Environment.getExternalStorageDirectory().getPath()
//						+ "/DCIM/SLAMwithCameraIMU/img/"
//						+ dateFormat.format(new Date()) + "_Match.jpg";
//				matchedImage = new Mat(image01.rows(), image01.cols()*2, image01.type());
//			    Features2d.drawMatches(image01, keyPoint01, image02, keyPoint02, matches, matchedImage);
//				Highgui.imwrite(path, matchedImage);


				// MQTT Publish
				// Matching, image coordinate of keypoints, マッチング結果，キーポイントの画像座標
				//long currentTimeMillis = System.currentTimeMillis();
				StringBuilder sb = new StringBuilder();
				sb.append(currentTimeMillis);
				sb.append("$");
				if(listMatch.size() > 0){
					for(DMatch match : listMatch){

						/// index_of_keypoint_on_previous_frame:index_of_keypoint_on_current_frame:image_coordinate[x]_on_previous_frame:image_coordinate[y]_on_previous_frame:image_coordinate[x]_on_current_frame:image_coordinate[y]_on_current_frame&
						/// 時刻t-1のインデックス:時刻tのインデックス:時刻t-1の画像座標x:時刻t-1の画像座標y:時刻tの画像座標x:時刻tの画像座標y&
						sb.append(match.queryIdx);
						sb.append(":");
						sb.append(match.trainIdx);
						sb.append(":");
						double[] keypoint = keyPoint01.get(match.queryIdx, 0);
						sb.append(keypoint[0]);
						sb.append(":");
						sb.append(keypoint[1]);
						sb.append(":");
						double[] keypoint2 = keyPoint02.get(match.trainIdx, 0);
						sb.append(keypoint2[0]);
						sb.append(":");
						sb.append(keypoint2[1]);
						sb.append("&");

/*
 * 						/// Add descripter
						sb.append(match.queryIdx);
						sb.append(":");
						sb.append(match.trainIdx);
						sb.append(":");
						double[] keypoint = keyPoint01.get(match.queryIdx, 0);
						sb.append(keypoint[0]);
						sb.append(":");
						sb.append(keypoint[1]);
						sb.append(":");
						double[] keypoint2 = keyPoint02.get(match.trainIdx, 0);
						sb.append(keypoint2[0]);
						sb.append(":");
						sb.append(keypoint2[1]);
						sb.append(":");
						for(int j=0;j<descripters02.cols();j++){
//							double[] descripter = descripters02.get(match.trainIdx, j);
							sb.append((int)descripters02.get(match.trainIdx, j)[0]);
							sb.append(":");
						}
						sb.append("&");
*/
					}
				}else{
					sb.append("nomatch");
				}

				MCS.publish("SLAM/input/camera", new String(sb));


				//Mat -> binary  Matをバイナリに変換
//				byte buff[] = new byte[(int) (descripters02.total() * descripters02.channels())];
//				descripters02.get(0, 0, buff);
//				//MQTT Publish
//				MCS.publishBinary("SLAM/input/camera", buff);

			}

//			long end = System.nanoTime();
//			Log.d(TAG,"Time (ms):" + (end - start) / 1000000f);

			return image02;
	    }

	    @Override
	    protected void onPostExecute(Mat mat) {

//	    	Log.d(TAG,"task finished. frame = "+frame);

			image01 = mat.clone();
			keyPoint02.copyTo(keyPoint01);
			descripters01 = descripters02.clone();

//			isFirst = false;
			frame++;
	    }
	}

}
