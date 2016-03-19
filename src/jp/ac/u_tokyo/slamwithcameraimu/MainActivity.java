package jp.ac.u_tokyo.slamwithcameraimu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//Button
		View buttonLogin = findViewById(R.id.button_login);
		buttonLogin.setOnClickListener(this);
		View buttonStart = findViewById(R.id.button_start);
		buttonStart.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent intent;
	    switch(v.getId()){
	    case R.id.button_login:
	    	Log.d("SLAM", "LoginActivity");
	    	intent = new Intent(MainActivity.this, LoginActivity.class);
	    	startActivity(intent);
	    	break;
	    case R.id.button_start:
	    	Log.d("SLAM", "ProcessingActivity");
	    	intent = new Intent(MainActivity.this, ProcessingActivity.class);
	    	startActivity(intent);
	    	break;
	    }
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if(keyCode == KeyEvent.KEYCODE_BACK) {
	        //Back Key 何もしない
	    	return true;
	    } else {
	        return super.onKeyDown(keyCode, event);
	    }
	}
}