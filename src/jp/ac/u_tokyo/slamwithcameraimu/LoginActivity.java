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
	EditText server, port, user, pass, clientId;

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

        //Set data to the form
        server.setText(serverStr);
        port.setText(portStr);
        user.setText(userStr);
        pass.setText(passStr);
        clientId.setText(clientIdStr);

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
						Toast.LENGTH_LONG).show();
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
				|| clientId.getText().toString().equals("")) {

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
			edit.commit();
			return true;
		}
	}
}
