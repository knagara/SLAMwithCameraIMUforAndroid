package jp.ac.u_tokyo.slamwithcameraimu;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class PublishSensorData extends Thread implements SensorEventListener {

	private boolean halt_ = false;

	Context mContext;
	MqttClientService MCS;
	SensorManager mSensorManager;

	boolean isFirst = true;

	//sleep time
	int sleepTime = 50;

	//time
	long currentTimeMillis, currentTimeMillis1;

	//alpha of high-pass filter
	float alpha;
	//alpha of low-pass filter
	float alpha_LPF;

	//acceleration (加速度)
	int accelType = 0; //0:(accel-G-bias)*HPF, 1:accel-G, 2:linearAccel*HPF, 3:linearAccel*LPF, 4:linearAccel
	double accelThreshold = 0.1; //threshold to decide whether device is moving or not 端末が静止しているかどうか判定するための，加速度の変化量のしきい値
	float[] acceleration_with_gravity = new float[3];
	float[] acceleration = new float[3];
	float[] acceleration_temp = new float[3];
	float[] acceleration_temp2 = new float[3];
	float[] a1 = {0.0f, 0.0f, 0.0f}; //acceleration of time t-1  t-1の加速度
	float[] a2 = {0.0f, 0.0f, 0.0f}; //acceleration of time t-2  t-2の加速度
	float[] a3 = {0.0f, 0.0f, 0.0f}; //acceleration of time t-3  t-3の加速度
	float[] a4 = {0.0f, 0.0f, 0.0f}; //acceleration of time t-4  t-4の加速度
	float[] a5 = {0.0f, 0.0f, 0.0f}; //acceleration of time t-5  t-5の加速度
	float[] acceleration_gravity = new float[3]; //gravity in acceleration  加速度の低周波成分を保存する変数

	//gravity (重力)
	float[] gravity = new float[3];
	//Orientation (傾き)
	float[] orientation = new float[3];

	//Gyroscope （ジャイロスコープ）
	float[] gyro = new float[3];
	float[] gyroFixed = new float[3];
	float[] gyroFixed1 = new float[3];
	float[] gyro_diff = new float[3];
    ArrayList<Float> valueX     = new ArrayList<Float>();
    ArrayList<Float> valueY    = new ArrayList<Float>();
    ArrayList<Float> valueZ     = new ArrayList<Float>();
    int sampleCount = 9; //Total times of sampling in median filter  メディアンフィルタのサンプリング数
    int medianNum = 4; //median number of sampling data  サンプリングした値の使用値のインデックス（メディアン）

	//Magnetic field （地磁気）
	float[] magnet = new float[3];


	/*
	 * Constructor
	 * Register sensors here.
	 */
	public PublishSensorData(Context context){
		halt_ = false;

		//Context
		this.mContext = context;

		//SensorManager
		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		//Register Accelerometer (加速度センサ)
		List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if(sensors.size() > 0) {
			Log.d("SLAM","Accelerometer detected.");
			Sensor s = sensors.get(0);
			mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_FASTEST);
		}
		//Register Gravity (重力センサ)
		sensors = mSensorManager.getSensorList(Sensor.TYPE_GRAVITY);
		if(sensors.size() > 0) {
			Log.d("SLAM","Gravity detected.");
			Sensor s = sensors.get(0);
			mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_FASTEST);
		}
		//Register Accelerometer without Gravity (加速度センサ without 重力)
		sensors = mSensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
		if(sensors.size() > 0) {
			Log.d("SLAM","Linear Acceleration detected.");
			Sensor s = sensors.get(0);
			mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_FASTEST);
		}
		//Register Gyroscope (ジャイロスコープ)
		sensors = mSensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
		if(sensors.size() > 0) {
			Log.d("SLAM","Gyroscope detected.");
			Sensor s = sensors.get(0);
			mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_FASTEST);
		}
		//Register Magnetic Field (地磁気センサ)
		sensors = mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
		if(sensors.size() > 0) {
			Log.d("SLAM","Magnetic Field detected.");
			Sensor s = sensors.get(0);
			mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_FASTEST);
		}

		Log.d("SLAM","PublishSensorData constructor OK");
	}

	/*
	 * Set MCS instance (Mqtt Client Service)
	 */
	public void setMCS(MqttClientService MCS){
		this.MCS = MCS;
	}

	/*
	 * Set sampling rate
	 */
	public void setRate(int rate){
		this.sleepTime = (int) 1000 / rate;
	}

	/*
	 * Set accel type
	 */
	public void setAccelType(int type){
		this.accelType = type;
	}

	/*
	 * Set accel Threshold
	 */
	public void setAccelThreshold(float threshold_){
		this.accelThreshold = threshold_;
	}

	/*
	 * Set alpha of High-pass filter
	 */
	public void setAlpha(float alpha){
		this.alpha = alpha;
	}

	/*
	 * Set alpha of Low-pass filter
	 */
	public void setAlphaLPF(float alpha_LPF){
		this.alpha_LPF = alpha_LPF;
	}

	/*
	 * Main part of this thread
	 * Publish sensor data via MQTT.
	 * (非 Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run(){

		Log.d("SLAM","PublishSensorData run() start");
		try { Thread.sleep(4000); } catch (InterruptedException e) { e.printStackTrace(); }

		while(!halt_){
			// Fix gyro offset
			subtractGyroOffset();
			// Current time
			currentTimeMillis = System.currentTimeMillis();
			// Publish
			StringBuilder sb = new StringBuilder();
			sb.append(currentTimeMillis);
			sb.append("&");
			sb.append(acceleration[0]);
			sb.append("&");
			sb.append(acceleration[1]);
			sb.append("&");
			sb.append(acceleration[2]);
			sb.append("&");
			sb.append(orientation[0]);
			sb.append("&");
			sb.append(orientation[1]);
			sb.append("&");
			sb.append(orientation[2]);
			sb.append("&");
			sb.append(magnet[0]);
			sb.append("&");
			sb.append(magnet[1]);
			sb.append("&");
			sb.append(magnet[2]);
			sb.append("&");
			sb.append(gyroFixed[0]);
			sb.append("&");
			sb.append(gyroFixed[1]);
			sb.append("&");
			sb.append(gyroFixed[2]);
			MCS.publish("SLAM/input/all", new String(sb));
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * subtractGyroOffset
	 */
	private void subtractGyroOffset(){
		gyroFixed[0] = gyro[0] + 0.017453283f;
		gyroFixed[1] = gyro[1];
		gyroFixed[2] = gyro[2] - 0.017453283f;
//		gyroFixed[0] = gyro[0];
//		gyroFixed[1] = gyro[1];
//		gyroFixed[2] = gyro[2];
	}

	/*
	 * Stop this thread.
	 */
	public void halt(){
		if(!halt_){
			halt_ = true;
	    	Log.d("SLAM", "halt PublishSensorData");
			try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
			MCS.publish("SLAM/input/stop", "true");
			if (mSensorManager != null) {
				mSensorManager.unregisterListener(this);
				Log.d("SLAM","SensorManager unregister");
	        }
			interrupt();
		}
	}

	/*
	 * This method is called when sensor value changes.
	 * (非 Javadoc)
	 * @see android.hardware.SensorEventListener#onSensorChanged(android.hardware.SensorEvent)
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()){
        case Sensor.TYPE_ACCELEROMETER:
        	if(accelType == 0){
        		/////////////////////////////
        		/// accelType == 0
        		///
        		/// Data: ACCELEROMETER raw data = accel with gravity
        		/// Process:
        		/// 1. calc global accel by rotation matrix, and remove gravity
        		/// 2. remove bias
        		/// 3. high-pass filter
        		///
        		/// Data: 重力を含む生の加速度データ
        		/// Process:
        		/// 1. 回転行列かけてから重力加速度を引く
        		/// 2. さらに系統誤差を除去する
        		/// 3. さらにハイパスフィルタかける
        		/////////////////////////////
//        		acceleration = event.values.clone();
        		Utils.lowPassFilter(acceleration_temp2,event.values,alpha_LPF);
        		// 1. calc global accel by rotation matrix, and remove gravity  回転行列かけてグローバル座標系の加速度にする さらに重力加速度を引く
        		acceleration_temp = Utils.calcGlobalAccelWithoutGravity(acceleration_temp2,orientation);
        		// 2. remove bias  系統誤差の除去
        		Utils.calcAccelWithoutBias(acceleration_temp,orientation);
        		// 3. high-pass filter
            	// Apply HPF to X,Y,Z separately  XYZ軸で別々にハイパスフィルタ適用
            	// --- Apply HPF to X axis  X軸にハイパスフィルタ適用 ---
            	if(isDeviceStop(acceleration_temp[0],0)){ //Judge if the device is moving or not  静止しているかどうか判定
            		// If the device stop  静止している場合
            		// Apply HPF  低周波成分計算して引く（ハイパスフィルタの適用）
            		float[] data = Utils.highPassFilterSingle(acceleration_temp[0], acceleration_gravity[0], alpha);
            		acceleration[0] = data[0];
            		acceleration_gravity[0] = data[1];
            	}else{
            		// If the device is moving  動いている場合
            		// Only subtract low‐frequency‐component  低周波成分引くだけ
            		acceleration[0] = acceleration_temp[0] - acceleration_gravity[0];
            	}
            	// --- Apply HPF to Y axis  Y軸にハイパスフィルタ適用 ---
            	if(isDeviceStop(acceleration_temp[1],1)){ //Judge if the device is moving or not  静止しているかどうか判定
            		// If the device stop  静止している場合
            		// Apply HPF  低周波成分計算して引く（ハイパスフィルタの適用）
            		float[] data = Utils.highPassFilterSingle(acceleration_temp[1], acceleration_gravity[1], alpha);
            		acceleration[1] = data[0];
            		acceleration_gravity[1] = data[1];
            	}else{
            		// If the device is moving  動いている場合
            		// Only subtract low‐frequency‐component  低周波成分引くだけ
            		acceleration[1] = acceleration_temp[1] - acceleration_gravity[1];
            	}
            	// --- Apply HPF to Z axis  Z軸にハイパスフィルタ適用 ---
            	if(isDeviceStop(acceleration_temp[2],2)){ //Judge if the device is moving or not  静止しているかどうか判定
            		// If the device stop  静止している場合
            		// Apply HPF  低周波成分計算して引く（ハイパスフィルタの適用）
            		float[] data = Utils.highPassFilterSingle(acceleration_temp[2], acceleration_gravity[2], alpha);
            		acceleration[2] = data[0];
            		acceleration_gravity[2] = data[1];
            	}else{
            		// If the device is moving  動いている場合
            		// Only subtract low‐frequency‐component  低周波成分引くだけ
            		acceleration[2] = acceleration_temp[2] - acceleration_gravity[2];
            	}
        	}else if(accelType == 1){
        		/////////////////////////////
        		/// accelType == 1
        		///
        		/// Data: ACCELEROMETER raw data = accel with gravity
        		/// Process: calc global accel by rotation matrix, and remove gravity
        		///
        		/// Data: 重力を含む生の加速度データ
        		/// Process: 回転行列かけてから重力加速度を引く
        		/////////////////////////////
        		Utils.lowPassFilter(acceleration_temp2,event.values,alpha_LPF);
        		// calc global accel by rotation matrix, and remove gravity  回転行列かけてグローバル座標系の加速度にする さらに重力加速度を引く
        		acceleration = Utils.calcGlobalAccelWithoutGravity(acceleration_temp2,orientation);
        	}
            break;
        case Sensor.TYPE_GRAVITY:
//        	gravity = event.values.clone();
        	Utils.lowPassFilter(gravity,event.values,alpha_LPF);
        	/// Calc orientation  傾きの計算
        	Utils.calcOrientationFromGravity(gravity, magnet, orientation);
            break;
        case Sensor.TYPE_LINEAR_ACCELERATION:
        	if(accelType == 3){
        		/// LPF  ローパス
        		Utils.lowPassFilter(acceleration_temp2,event.values,alpha_LPF);
        		// calc global accel by rotation matrix  回転行列かけてグローバル座標系の加速度にする
        		acceleration = Utils.calcGlobalAccel(acceleration_temp2,orientation);
        	}else if(accelType == 4){
        		// Use row data  生データ使う
        		// calc global accel by rotation matrix  回転行列かけてグローバル座標系の加速度にする
        		acceleration = Utils.calcGlobalAccel(event.values,orientation);
        	}else if(accelType == 2){
        		/////////////////////////////
        		/// accelType == 2
        		///
        		/// Data: LINEAR_ACCELERATION = accel without gravity
        		/// Process: high-pass filter
        		///
        		/// Data: 生の加速度データから重力加速度を引いたデータ
        		/// Process: 系統誤差が残ってしまっているのでそれを除去する
        		/////////////////////////////
        		/// Row data  生データ
//            	acceleration = event.values.clone();
        		/// LPF  ローパス
//            	Utils.lowPassFilter(acceleration,event.values,alpha_LPF);
        		/// HPF  ハイパス
//            	Utils.highPassFilter(event.values, acceleration_gravity, acceleration, alpha);
            	/// HPF+LPF  ハイパス＋ローパス
//            	Utils.highPassFilter(event.values, acceleration_gravity, acceleration_temp, alpha);
//            	Utils.lowPassFilter(acceleration, acceleration_temp, alpha_LPF);
            	/// LPF+HPF  ローパス＋ハイパス
//            	Utils.lowPassFilter(acceleration_temp, event.values, alpha_LPF);
//        		Utils.highPassFilter(acceleration_temp, acceleration_gravity, acceleration, alpha);

            	/// LPF -> HPF  ローパス → ハイパス（条件付き）
            	Utils.lowPassFilter(acceleration_temp, event.values, alpha_LPF);
            	// Apply HPF to X,Y,Z separately  XYZ軸で別々にハイパスフィルタ適用
            	// --- Apply HPF to X axis  X軸にハイパスフィルタ適用 ---
            	if(isDeviceStop(acceleration_temp[0],0)){ //Judge if the device is moving or not  静止しているかどうか判定
            		// If the device stop  静止している場合
            		// Apply HPF  低周波成分計算して引く（ハイパスフィルタの適用）
            		float[] data = Utils.highPassFilterSingle(acceleration_temp[0], acceleration_gravity[0], alpha);
            		acceleration[0] = data[0];
            		acceleration_gravity[0] = data[1];
            	}else{
            		// If the device is moving  動いている場合
            		// Only subtract low‐frequency‐component  低周波成分引くだけ
            		acceleration[0] = acceleration_temp[0] - acceleration_gravity[0];
            	}
            	// --- Apply HPF to Y axis  Y軸にハイパスフィルタ適用 ---
            	if(isDeviceStop(acceleration_temp[1],1)){ //Judge if the device is moving or not  静止しているかどうか判定
            		// If the device stop  静止している場合
            		// Apply HPF  低周波成分計算して引く（ハイパスフィルタの適用）
            		float[] data = Utils.highPassFilterSingle(acceleration_temp[1], acceleration_gravity[1], alpha);
            		acceleration[1] = data[0];
            		acceleration_gravity[1] = data[1];
            	}else{
            		// If the device is moving  動いている場合
            		// Only subtract low‐frequency‐component  低周波成分引くだけ
            		acceleration[1] = acceleration_temp[1] - acceleration_gravity[1];
            	}
            	// --- Apply HPF to Z axis  Z軸にハイパスフィルタ適用 ---
            	if(isDeviceStop(acceleration_temp[2],2)){ //Judge if the device is moving or not  静止しているかどうか判定
            		// If the device stop  静止している場合
            		// Apply HPF  低周波成分計算して引く（ハイパスフィルタの適用）
            		float[] data = Utils.highPassFilterSingle(acceleration_temp[2], acceleration_gravity[2], alpha);
            		acceleration[2] = data[0];
            		acceleration_gravity[2] = data[1];
            	}else{
            		// If the device is moving  動いている場合
            		// Only subtract low‐frequency‐component  低周波成分引くだけ
            		acceleration[2] = acceleration_temp[2] - acceleration_gravity[2];
            	}

        	}
            break;
        case Sensor.TYPE_GYROSCOPE:
//        	gyro = event.values.clone();
        	valueX.add(event.values[0]);
        	valueY.add(event.values[1]);
        	valueZ.add(event.values[2]);
        	// When you get enough times of sampling data  必要なサンプリング数に達したら
        	if(valueX.size() == sampleCount){
        		Utils.medianFilter(gyro, valueX, valueY, valueZ, medianNum);
        		//Utils.medianLPFilter(gyro, valueX, valueY, valueZ, medianNum, alpha);
        		valueX.remove(0);
        		valueY.remove(0);
        		valueZ.remove(0);
        	}
        	break;
        case Sensor.TYPE_MAGNETIC_FIELD:
//        	magnet = event.values.clone();
        	Utils.lowPassFilter(magnet,event.values,alpha_LPF);
        	break;
        }
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	// Judge if the device is moving or not  端末が静止しているかどうか判定
	private boolean isDeviceStop(float a, int axis){
		/// Substitution of acceleration  加速度の代入
		a5[axis] = a4[axis];
		a4[axis] = a3[axis];
		a3[axis] = a2[axis];
		a2[axis] = a1[axis];
		a1[axis] = a;
		/// If a4 is 0, the device is stop  a4が0ならまだ静止しているとみなす
		if(a4[axis] == 0.0f){
			return true;
		}
		/// If acceleration is above threshold two times continuity, the device is moving  2回連続でしきい値以上なら，動いているとみなす
		//if(Math.abs(a1[axis]-a2[axis]) > accelThreshold &&
		//		Math.abs(a2[axis]-a3[axis]) > accelThreshold &&
		//		Math.abs(a3[axis]-a4[axis]) > accelThreshold){
		///  If al-a5 is above threshold, the device is moving  5期前と比較した差がしきい値以上なら，動いているとみなす
		if(Math.abs(a1[axis]-a5[axis]) > accelThreshold){
			return false;
		}else{
			return true;
		}
	}
}