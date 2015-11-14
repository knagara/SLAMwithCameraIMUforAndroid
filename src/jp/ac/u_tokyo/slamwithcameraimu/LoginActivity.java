package jp.ac.u_tokyo.slamwithcameraimu;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {

	SharedPreferences sp;
	EditText server, port, user, pass, clientId, rate, accel_g, alpha, alpha_LPF, detector, threshold, accelThreshold;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		//Preferences
        sp = PreferenceManager.getDefaultSharedPreferences(this);

		//Button
		View buttonLogin = findViewById(R.id.button_login);
		buttonLogin.setOnClickListener(this);

		//EditText
		server = (EditText) findViewById(R.id.editText1);
		port = (EditText) findViewById(R.id.editText2);
		user = (EditText) findViewById(R.id.editText3);
		pass = (EditText) findViewById(R.id.editText4);
		clientId = (EditText) findViewById(R.id.editText5);
		rate = (EditText) findViewById(R.id.editText6);
		accel_g = (EditText) findViewById(R.id.editText7);
		alpha = (EditText) findViewById(R.id.editText9);
		alpha_LPF = (EditText) findViewById(R.id.editText10);
		detector = (EditText) findViewById(R.id.editText11);
		threshold = (EditText) findViewById(R.id.editText12);
		accelThreshold = (EditText) findViewById(R.id.editText13);
	}

	@Override
	protected void onResume(){
		super.onResume();

		//Preferences data
        String serverStr = sp.getString("server", "");
        String portStr = sp.getString("port", "");
        String userStr = sp.getString("user", "");
        String passStr = sp.getString("pass", "");
        String clientIdStr = sp.getString("clientId", "");
        String rateStr = sp.getString("rate", "");
        String accel_gStr = sp.getString("accel_g", "");
        String alphaStr = sp.getString("alpha", "");
        String alpha_LPFStr = sp.getString("alpha_LPF", "");
        String detectorStr = sp.getString("detector", "");
        String thresholdStr = sp.getString("threshold", "");
        String accelThresholdStr = sp.getString("accelThreshold", "");

        //Set data to the form
        server.setText(serverStr);
        port.setText(portStr);
        user.setText(userStr);
        pass.setText(passStr);
        clientId.setText(clientIdStr);
        rate.setText(rateStr);
        accel_g.setText(accel_gStr);
        alpha.setText(alphaStr);
        alpha_LPF.setText(alpha_LPFStr);
        detector.setText(detectorStr);
        threshold.setText(thresholdStr);
        accelThreshold.setText(accelThresholdStr);
	}

	@Override
	public void onClick(View v) {
		Intent intent;
	    switch(v.getId()){
	    case R.id.button_login:
	    	Log.d("SLAM", "MainActivity");
	    	//Check data and save data to preferences
			boolean isOK = saveButtonClick();
			if (isOK) {
				//Go back to MainActivity
				Toast.makeText(this, getString(R.string.success),
						Toast.LENGTH_SHORT).show();
				intent = new Intent(LoginActivity.this, MainActivity.class);
				startActivity(intent);
			}
	    }
	}

	private boolean saveButtonClick() {
		//Check data
		if (server.getText().toString().equals("")
				|| port.getText().toString().equals("")
				|| user.getText().toString().equals("")
				|| pass.getText().toString().equals("")
				|| clientId.getText().toString().equals("")
				|| rate.getText().toString().equals("")
				|| accel_g.getText().toString().equals("")
				|| alpha.getText().toString().equals("")
				|| alpha_LPF.getText().toString().equals("")
				|| detector.getText().toString().equals("")
				|| threshold.getText().toString().equals("")
				|| accelThreshold.getText().toString().equals("")) {

			Toast.makeText(this, getString(R.string.please_fill),
					Toast.LENGTH_SHORT).show();
			return false;
		} else {
			//Save data to preferences
			Editor edit = sp.edit();
			edit.putString("server", server.getText().toString());
			edit.putString("port", port.getText().toString());
			edit.putString("user", user.getText().toString());
			edit.putString("pass", pass.getText().toString());
			edit.putString("clientId", clientId.getText().toString());
			edit.putString("rate", rate.getText().toString());
			edit.putString("accel_g", accel_g.getText().toString());
			edit.putString("alpha", alpha.getText().toString());
			edit.putString("alpha_LPF", alpha_LPF.getText().toString());
			edit.putString("detector", detector.getText().toString());
			edit.putString("threshold", threshold.getText().toString());
			edit.putString("accelThreshold", accelThreshold.getText().toString());
			edit.commit();
			return true;
		}
	}
}
