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
	float[] acceleration_gravity = new float[3];

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
			MCS.publish("SLAM/input/acceleration_withG/x", String.valueOf(acceleration_with_gravity[0]));
			MCS.publish("SLAM/input/acceleration_withG/y", String.valueOf(acceleration_with_gravity[1]));
			MCS.publish("SLAM/input/acceleration_withG/z", String.valueOf(acceleration_with_gravity[2]));
			//acceleration
			MCS.publish("SLAM/input/acceleration/x", String.valueOf(acceleration[0]));
			MCS.publish("SLAM/input/acceleration/y", String.valueOf(acceleration[1]));
			MCS.publish("SLAM/input/acceleration/z", String.valueOf(acceleration[2]));
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
        }
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}
