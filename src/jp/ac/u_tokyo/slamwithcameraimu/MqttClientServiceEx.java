package jp.ac.u_tokyo.slamwithcameraimu;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import android.content.Context;

public class MqttClientServiceEx extends MqttClientService implements MqttCallback {


	public MqttClientServiceEx(Context context, String host, int port, String user, String pass, String clientId){
		super(context, host, port, user, pass, clientId);
	}

	/*
	 * This method is called when connect() success.
	 */
	public void connected(){

	}

	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {

	}
}
