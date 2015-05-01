package jp.ac.u_tokyo.slamwithcameraimu;

import android.util.Log;

public class PublishSensorData extends Thread {

	private boolean halt_ = false;
	MqttClientService MCS;

	public PublishSensorData(){
		halt_ = false;
	}

	public void setMCS(MqttClientService MCS){
		this.MCS = MCS;
	}

	public void run(){

		while(!halt_){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			MCS.publish("SLAM/input/sensor", "1");
		}

	}

	public void halt(){
		if(!halt_){
	    	Log.d("SLAM", "halt PublishSensorData");
			halt_ = true;
			interrupt();
		}
	}
}
