package jp.ac.u_tokyo.slamwithcameraimu;

import android.util.Log;

public class PublishSensorData extends Thread {

	private boolean halt_ = false;

	public PublishSensorData(){
		halt_ = false;
	}

	public void run(){

		while(!halt_){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Log.d("SLAM","a");
		}

	}

	public void halt(){
    	Log.d("SLAM", "halt PublishSensorData");
		halt_ = true;
		interrupt();
	}
}
