package jp.ac.u_tokyo.slamwithcameraimu;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class ProcessingActivity extends Activity {

	private final String TAG = "SLAM";

	SharedPreferences sp;

	String server, user, pass, clientId;
	int port;
	MqttClientService MCS;

	PublishSensorData publishSensorData;

	TextView text;
	String log = "";

	//Camera
	Preview mPreview;
	Camera mCamera;
	int numberOfCameras;
	int cameraCurrentlyLocked;
	int defaultCameraId;
	private int sw, sh;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_processing);

		Log.d("SLAM", "OnCreate");

		//Preferences
		sp = PreferenceManager.getDefaultSharedPreferences(this);

		//Button
//		View buttonStop = findViewById(R.id.button_stop);
//		buttonStop.setOnClickListener(this);
//
//		//TextView
//		text = (TextView) findViewById(R.id.textView1);

		//Init
		init();
	}

	private void init(){
		initMCS();
		initPublishSensorData();
		initCamera();
	}

	private void initMCS(){
		//Mqtt login data
		server = sp.getString("server", "");
		port = Integer.parseInt(sp.getString("port", ""));
		user = sp.getString("user", "");
		pass = sp.getString("pass", "");
		clientId = sp.getString("clientId", "");

		//MqttClientService
		MCS = new MqttClientServiceEx(getApplicationContext(),server,port,user,pass,clientId);
		MCS.setConf(Conf.qos,Conf.retain);

		//Mqtt connect
		MCS.connect();
//		log("Mqtt connecting...");
	}

	private void initPublishSensorData(){
		//PublishSensorData (This is a thread.)
		publishSensorData = new PublishSensorData(getApplicationContext());
		publishSensorData.setMCS(MCS);
		publishSensorData.setRate(Integer.parseInt(sp.getString("rate", "20")));
		publishSensorData.setAccelType(Integer.parseInt(sp.getString("accel_g", "2")));
		publishSensorData.setAccelThreshold(Float.parseFloat(sp.getString("accelThreshold", "0.1")));
		publishSensorData.setAlpha(Float.parseFloat(sp.getString("alpha", "0.85")));
		publishSensorData.setAlphaLPF(Float.parseFloat(sp.getString("alpha_LPF", "0.8")));
		publishSensorData.start();
	}

	private void initCamera(){

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Create a RelativeLayout container that will hold a SurfaceView,
		// and set it as the content of our activity.
		String detector = sp.getString("detector", "");
		mPreview = new Preview(this,MCS);
		mPreview.setDetector(detector);
		mPreview.setThreshold(Float.parseFloat(sp.getString("threshold", "0.0")));
		setContentView(mPreview);

		// Find the total number of cameras available
		numberOfCameras = Camera.getNumberOfCameras();

		// Find the ID of the default camera
		CameraInfo cameraInfo = new CameraInfo();
		for (int i = 0; i < numberOfCameras; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
				defaultCameraId = i;
			}
		}

		//Screen size
		WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
		// Create instance of display ディスプレイのインスタンス生成
		Display disp = wm.getDefaultDisplay();
		sw = disp.getWidth();
		sh = disp.getHeight();
	}

	private void log(String str){
		log += str+"\n";
		text.setText(log);
	}

	@Override
	protected void onResume(){
		super.onResume();
		Log.d("SLAM", "OnResume");

		if(!publishSensorData.isAlive()){
			publishSensorData.start();
		}

		//OpenCV
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this,
				mLoaderCallback);

		try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }

		// Open the default i.e. the first rear facing camera.
		mCamera = Camera.open();
		cameraCurrentlyLocked = defaultCameraId;
		mPreview.setCamera(mCamera);
	}

	@Override
	protected void onPause(){
		super.onPause();
		Log.d("SLAM", "OnPause");

		// Because the Camera object is a shared resource, it's very
		// important to release it when the activity is paused.
		if (mCamera != null) {
			mPreview.setCamera(null);
			mCamera.setPreviewCallback(null);
			mPreview.getHolder().removeCallback(mPreview);
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	protected void onStop(){
		super.onStop();
		Log.d("SLAM", "OnStop");
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		Log.d("SLAM", "OnDestroy");
		publishSensorData.halt();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if(keyCode == KeyEvent.KEYCODE_BACK) {
	        //Back Key
	    	//何もしない
	    	return true;
//	    	publishSensorData.halt();
//	       return super.onKeyDown(keyCode, event);
	    } else {
	        return super.onKeyDown(keyCode, event);
	    }
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Log.d(TAG, "X:" + event.getX() + ",Y:" + event.getY());
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (event.getX() > (int) ((float) sw * 0.4f)
					&& event.getX() < sw
					&& event.getY() > (int) ((float) sh * 0.12f)
					&& event.getY() < (int) ((float) sh * 0.88f))
			{
				publishSensorData.halt();
		    	Intent intent = new Intent(ProcessingActivity.this, MainActivity.class);
		    	startActivity(intent);
			}
			break;
		}
		return true;
	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

//	@Override
//	public void onClick(View v) {
//		Intent intent;
//	    switch(v.getId()){
//	    case R.id.button_stop:
//			publishSensorData.halt();
//	    	intent = new Intent(ProcessingActivity.this, MainActivity.class);
//	    	startActivity(intent);
//	    	break;
//	    }
//	}
}
