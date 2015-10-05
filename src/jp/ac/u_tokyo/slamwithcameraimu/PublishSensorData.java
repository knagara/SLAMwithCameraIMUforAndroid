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
	int accelType = 0;
	float[] acceleration_with_gravity = new float[3];
	float[] acceleration = new float[3];
	float[] acceleration_temp = new float[3];
	float[] acceleration_gravity = new float[3]; //計算用の一時変数

	//gravity (重力)
	float[] gravity = new float[3];

	//Gyroscope （ジャイロスコープ）
	float[] gyro = new float[3];
	float[] gyroFixed = new float[3];
	float[] gyroFixed1 = new float[3];
	float[] gyro_diff = new float[3];
    ArrayList<Float> valueX     = new ArrayList<Float>();
    ArrayList<Float> valueY    = new ArrayList<Float>();
    ArrayList<Float> valueZ     = new ArrayList<Float>();
    int sampleCount = 9; //サンプリング数
    int medianNum = 4; //サンプリングした値の使用値のインデックス

	//Magnetic field （地磁気）
	float[] magnet = new float[3];

	//Orientation (傾き)
	float[] ori = new float[3];
	float[] orientation = new float[3];

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
		//Register Orientaion (方位センサ)
//		sensors = mSensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
//		if(sensors.size() > 0) {
//			Log.d("SLAM","Orientation detected.");
//			Sensor s = sensors.get(0);
//			mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_FASTEST);
//		}

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
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//Get time in millisecond
//			if(!isFirst){
//				currentTimeMillis1 = currentTimeMillis;
//			}
			currentTimeMillis = System.currentTimeMillis();
			//Gyro offset
			subtractGyroOffset();
//			if(!isFirst){
//				//Gyro diff
//				calcGyroDiff((float)(currentTimeMillis-currentTimeMillis1)/1000.0f);
//			}
			StringBuilder sb = new StringBuilder();
			sb.append(currentTimeMillis);
			sb.append("&");
			sb.append(acceleration[0]);
			sb.append("&");
			sb.append(acceleration[1]);
			sb.append("&");
			sb.append(acceleration[2]);
			sb.append("&");
			sb.append(gravity[0]);
			sb.append("&");
			sb.append(gravity[1]);
			sb.append("&");
			sb.append(gravity[2]);
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

//			setPreviousGyro();
//			if(isFirst){
//				isFirst = false;
//			}
		}
	}

	/**
	 * setPreviousGyro
	 */
	private void setPreviousGyro(){
		gyroFixed1[0] = gyroFixed[0];
		gyroFixed1[1] = gyroFixed[1];
		gyroFixed1[2] = gyroFixed[2];
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

	/**
	 * calcGyroDiff
	 */
	private void calcGyroDiff(float t){
		gyro_diff[0] = Utils.lowPassFilterSingle(gyro_diff[0], (gyroFixed[0]-gyroFixed1[0])/t, alpha_LPF);
		gyro_diff[1] = Utils.lowPassFilterSingle(gyro_diff[1], (gyroFixed[1]-gyroFixed1[1])/t, alpha_LPF);
		gyro_diff[2] = Utils.lowPassFilterSingle(gyro_diff[2], (gyroFixed[2]-gyroFixed1[2])/t, alpha_LPF);
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
//        		acceleration = event.values.clone();
        		Utils.lowPassFilter(acceleration,event.values,alpha_LPF);
        	}else if(accelType == 1){
            	Utils.highPassFilter(event.values, acceleration_gravity, acceleration_temp, alpha);
            	Utils.lowPassFilter(acceleration, acceleration_temp, alpha_LPF);
        	}
            break;
        case Sensor.TYPE_GRAVITY:
//        	gravity = event.values.clone();
        	Utils.lowPassFilter(gravity,event.values,alpha_LPF);
            break;
        case Sensor.TYPE_LINEAR_ACCELERATION:
        	if(accelType == 2){
        		/// 生データ
//            	acceleration = event.values.clone();
        		/// ローパス
            	Utils.lowPassFilter(acceleration,event.values,alpha_LPF);
        		/// ハイパス
//            	Utils.highPassFilter(event.values, acceleration_gravity, acceleration, alpha);
            	/// ハイパス＋ローパス
//            	Utils.highPassFilter(event.values, acceleration_gravity, acceleration_temp, alpha);
//            	Utils.lowPassFilter(acceleration, acceleration_temp, alpha_LPF);
            	/// ローパス＋ハイパス
//            	if(true){ //ハイパスフィルタをかける条件に合うかどうか
//                	Utils.lowPassFilter(acceleration_temp, event.values, alpha_LPF);
//            		Utils.highPassFilter(acceleration_temp, acceleration_gravity, acceleration, alpha);
//            	}else{
//            		Utils.lowPassFilter(acceleration, event.values, alpha_LPF);
//            	}
        	}
            break;
        case Sensor.TYPE_GYROSCOPE:
//        	gyro = event.values.clone();
        	valueX.add(event.values[0]);
        	valueY.add(event.values[1]);
        	valueZ.add(event.values[2]);
        	//必要なサンプリング数に達したら
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
//        case Sensor.TYPE_ORIENTATION:
//        	ori = event.values.clone();
//            break;
        }
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}