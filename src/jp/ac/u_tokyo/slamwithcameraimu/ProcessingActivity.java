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
import android.widget.TextView;

public class ProcessingActivity extends Activity implements OnClickListener {

	SharedPreferences sp;

	String server, user, pass, clientId;
	int port;
	MqttClientService MCS;

	PublishSensorData publishSensorData;

	TextView text;
	String log = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_processing);

		Log.d("SLAM", "OnCreate");

		//Preferences
		sp = PreferenceManager.getDefaultSharedPreferences(this);

		//Button
		View buttonStop = findViewById(R.id.button_stop);
		buttonStop.setOnClickListener(this);

		//TextView
		text = (TextView) findViewById(R.id.textView1);

		//Init
		init();
	}

	private void init(){
		initMCS();
		initPublishSensorData();
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
		try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
	}

	private void initPublishSensorData(){
		//PublishSensorData (This is a thread.)
		publishSensorData = new PublishSensorData(getApplicationContext());
		publishSensorData.setMCS(MCS);
		publishSensorData.setRate(Integer.parseInt(sp.getString("rate", "20")));
		try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
		publishSensorData.start();
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

//		if(MCS.client.isConnected()){
//			log("Connected.");
//		}else{
//			log("Connection failed.");
//			Log.d("SLAM", "Mqtt is not connected. Please try again.");
//			Toast.makeText(this, getString(R.string.mqtt_not_connected),
//					Toast.LENGTH_SHORT).show();
//		}
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
