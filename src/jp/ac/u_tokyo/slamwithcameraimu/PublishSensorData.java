package jp.ac.u_tokyo.slamwithcameraimu;

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

	//sleep time
	int sleepTime = 50;

	//acceleration (加速度)
	int accelType = 0;
	float[] acceleration_with_gravity = new float[3];
	float[] acceleration = new float[3];
	float[] acceleration_gravity = new float[3]; //計算用の一時変数
	float[] linear_acceleration = new float[3];
	float[] linear_acceleration_gravity = new float[3]; //計算用の一時変数
	
	//gravity (重力)
	float[] gravity = new float[3];

	//Gyroscope （ジャイロスコープ）
	float[] gyro = new float[3];

	//Magnetic field （地磁気）
	float[] magnet = new float[3];

	//Orientation (傾き)
	float[] ori = new float[3];
	float[] orientation = new float[3];
	//Rotation matrix
	private static final int MATRIX_SIZE = 16;
	float[] inR = new float[MATRIX_SIZE];
	float[] outR = new float[MATRIX_SIZE];
	float[] I = new float[MATRIX_SIZE];

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
	 * Main part of this thread
	 * Publish sensor data via MQTT.
	 * (非 Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run(){

		try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }

		while(!halt_){
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//Get time in millisecond
			long currentTimeMillis = System.currentTimeMillis();
			String time = String.valueOf(currentTimeMillis);
			String data = null;
			data = time + "&" +
					String.valueOf(acceleration[0]) + "&" +
					String.valueOf(acceleration[1]) + "&" +
					String.valueOf(acceleration[2]) + "&" +
					String.valueOf(gravity[0]) + "&" +
					String.valueOf(gravity[1]) + "&" +
					String.valueOf(gravity[2]) + "&" +
					String.valueOf(magnet[0]) + "&" +
					String.valueOf(magnet[1]) + "&" +
					String.valueOf(magnet[2]) + "&" +
					String.valueOf(gyro[0]) + "&" +
					String.valueOf(gyro[1]) + "&" +
					String.valueOf(gyro[2]);
			MCS.publish("SLAM/input/all", data);
		}
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
        		Utils.lowPassFilter(acceleration,event.values);
        	}else if(accelType == 1){
            	Utils.extractGravity(event.values, acceleration_gravity, acceleration);
        	}
            break;
        case Sensor.TYPE_GRAVITY:
//        	gravity = event.values.clone();
        	Utils.lowPassFilter(gravity,event.values);
            break;
        case Sensor.TYPE_LINEAR_ACCELERATION:
        	if(accelType == 2){
        		Utils.extractGravity(event.values, acceleration_gravity, acceleration);
        	}
            break;
        case Sensor.TYPE_GYROSCOPE:
//        	gyro = event.values.clone();
        	Utils.lowPassFilter(gyro,event.values);
        	break;
        case Sensor.TYPE_MAGNETIC_FIELD:
//        	magnet = event.values.clone();
        	Utils.lowPassFilter(magnet,event.values);
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