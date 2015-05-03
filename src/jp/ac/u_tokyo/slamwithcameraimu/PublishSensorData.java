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

	//acceleration (加速度)
	float[] acceleration_with_gravity = new float[3];
	float[] acceleration = new float[3];
	float[] acceleration_gravity = new float[3]; //計算用の一時変数

	//Gyroscope （ジャイロスコープ）
	float[] gyro = new float[3];

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
			Sensor s = sensors.get(0);
			mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_GAME);
		}
		//Register Gyroscope (ジャイロスコープ)
		sensors = mSensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
		if(sensors.size() > 0) {
			Sensor s = sensors.get(0);
			mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_GAME);
		}
		//Register Magnetic Field (地磁気センサ)
		sensors = mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
		if(sensors.size() > 0) {
			Sensor s = sensors.get(0);
			mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_GAME);
		}
	}

	/*
	 * Set MCS instance (Mqtt Client Service)
	 */
	public void setMCS(MqttClientService MCS){
		this.MCS = MCS;
	}

	/*
	 * Main part of this thread
	 * Publish sensor data via MQTT.
	 * (非 Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run(){

		while(!halt_){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//acceleration with gravity
			MCS.publish("SLAM/input/acceleration_with_gravity/x", String.valueOf(acceleration_with_gravity[0]));
			MCS.publish("SLAM/input/acceleration_with_gravity/y", String.valueOf(acceleration_with_gravity[1]));
			MCS.publish("SLAM/input/acceleration_with_gravity/z", String.valueOf(acceleration_with_gravity[2]));
			//acceleration
			MCS.publish("SLAM/input/acceleration/x", String.valueOf(acceleration[0]));
			MCS.publish("SLAM/input/acceleration/y", String.valueOf(acceleration[1]));
			MCS.publish("SLAM/input/acceleration/z", String.valueOf(acceleration[2]));
			//Gyro
			MCS.publish("SLAM/input/gyro/x", String.valueOf(gyro[0]));
			MCS.publish("SLAM/input/gyro/y", String.valueOf(gyro[1]));
			MCS.publish("SLAM/input/gyro/z", String.valueOf(gyro[2]));
			//Magnet
			MCS.publish("SLAM/input/magnet/x", String.valueOf(magnet[0]));
			MCS.publish("SLAM/input/magnet/y", String.valueOf(magnet[1]));
			MCS.publish("SLAM/input/magnet/z", String.valueOf(magnet[2]));
		}
	}

	/*
	 * Stop this thread.
	 */
	public void halt(){
		if(!halt_){
	    	Log.d("SLAM", "halt PublishSensorData");
			halt_ = true;
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
        	//acceleration raw data (with gravity)
        	acceleration_with_gravity[0] = event.values[0];
        	acceleration_with_gravity[1] = event.values[1];
        	acceleration_with_gravity[2] = event.values[2];
        	//Calc acceleration without gravity.
        	Utils.extractGravity(event.values, acceleration_gravity, acceleration);
            break;
        case Sensor.TYPE_GYROSCOPE:
        	//Gyroscope
        	gyro[0] = event.values[0];
        	gyro[1] = event.values[1];
        	gyro[2] = event.values[2];
        	break;
        case Sensor.TYPE_MAGNETIC_FIELD:
        	//Magnetic field
        	magnet[0] = event.values[0];
        	magnet[1] = event.values[1];
        	magnet[2] = event.values[2];
        	break;
        }
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}
