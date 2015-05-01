package jp.ac.u_tokyo.slamwithcameraimu;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

public class ProcessingActivity extends Activity implements OnClickListener {

	SharedPreferences sp;

	String server, user, pass, clientId;
	int port;
	MqttClientService MCS;

	PublishSensorData publishSensorData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_processing);

		//Preferences
		sp = PreferenceManager.getDefaultSharedPreferences(this);

		//Button
		View buttonStop = findViewById(R.id.button_stop);
		buttonStop.setOnClickListener(this);

		//PublishSensorData (This is a thread.)
		publishSensorData = new PublishSensorData();
	}

	@Override
	protected void onResume(){
		super.onResume();

		//Mqtt login data
		server = sp.getString("server", "");
		port = Integer.parseInt(sp.getString("port", ""));
		user = sp.getString("user", "");
		pass = sp.getString("pass", "");
		clientId = sp.getString("clientId", "");

		//MqttClientService
		MCS = new MqttClientService(this,server,port,user,pass,clientId);

		if(!publishSensorData.isAlive()){
			publishSensorData.start();
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if(keyCode == KeyEvent.KEYCODE_BACK) {
	        //Back Key
	    	publishSensorData.halt();
	       return super.onKeyDown(keyCode, event);
	    } else {
	        return super.onKeyDown(keyCode, event);
	    }
	}

	@Override
	protected void onPause(){
		super.onPause();
		Log.d("SLAM", "OnPause");
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
	public void onClick(View v) {
		Intent intent;
	    switch(v.getId()){
	    case R.id.button_stop:
			publishSensorData.halt();
	    	intent = new Intent(ProcessingActivity.this, MainActivity.class);
	    	startActivity(intent);
	    	break;
	    }
	}

}
