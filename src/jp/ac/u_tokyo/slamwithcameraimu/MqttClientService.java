package jp.ac.u_tokyo.slamwithcameraimu;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import android.content.Context;
import android.util.Log;

public class MqttClientService implements MqttCallback {

	Context context;
	String host;
	int port;
	String user;
	String pass;
	String clientId;
	String uri;
    boolean cleanSession = true;

	public MqttAndroidClient client;
	private MqttConnectOptions conOpt;

	public MqttClientService(Context context, String host, int port, String user, String pass, String clientId){
		this.context = context;
		this.host = host;
		this.port = port;
		this.user = user;
		this.pass = pass;
		this.clientId = clientId;

		try{
		    uri = "tcp://" + host + ":" + port;

			conOpt = new MqttConnectOptions();
			conOpt.setCleanSession(cleanSession);
			conOpt.setUserName(user);
			conOpt.setPassword(pass.toCharArray());

			// Construct the MqttClient instance
		    client = new MqttAndroidClient(context, uri, clientId);
			// Set this wrapper as the callback handler
			client.setCallback(this);

	    } catch (NullPointerException e) {
	        e.getCause();
	    } catch (Exception e) {
	        e.getCause();
	    }
	}



	/**
	 * Performs a single publish
	 * @param topic the topic to publish to
	 * @param message the message to publish
	 * @param qos the qos to publish at
	 * @throws MqttException
	 */
	public void publish(String topic, String message, int qos, boolean retained) {
		if (!client.isConnected()){
			Log.d("SLAM","MQTT not connected.");
			return;
		}
		try {
			// Get an instance of the topic
			MqttTopic mqtttopic = client.getTopic(topic);

			MqttMessage mqttmessage = new MqttMessage(message.getBytes());
			mqttmessage.setQos(qos);
			mqttmessage.setRetained(retained);

			// Publish the message
			MqttDeliveryToken token = mqtttopic.publish(mqttmessage);


			// Wait until the message has been delivered to the server
			token.waitForCompletion();

		} catch (MqttException e) {
			e.printStackTrace();
			Log.d("SLAM","Unable to set up publish message: "+e.toString());
		}
	}

	/**
	 * Subscribes to a topic and blocks until Enter is pressed
	 * @param topicName the topic to subscribe to
	 * @param qos the qos to subscibe at
	 * @throws MqttException
	 */
	public void subscribe(String topicName, int qos) {
		if (!client.isConnected()){
			log("MQTT not connected.");
			return;
		}
		log("Subscribing to topic \""+topicName+"\" qos "+qos);
		try {
			client.subscribe(topicName, qos);
		} catch (MqttSecurityException e) {
			e.printStackTrace();
			log("Unable to set up subscribe message(MqttSecurityException): "+e.toString());
		} catch (MqttException e) {
			e.printStackTrace();
			log("Unable to set up subscribe message(MqttException): "+e.toString());
		}
	}

	public void unsubscribe(String topicName) {
		if (!client.isConnected()){
			log("MQTT not connected.");
			return;
		}
		log("Unsubscribing to topic \""+topicName);
		try {
			client.unsubscribe(topicName);
		} catch (MqttException e) {
			e.printStackTrace();
			log("Unable to unsubscribe(MqttException): "+e.toString());
		}
	}

	public void connect(){
		try {
			client.connect(conOpt);
			log("MQTT Connected");
		} catch (MqttSecurityException e) {
			e.printStackTrace();
			log("MQTT Unable to Connect by Security");
		} catch (MqttException e) {
			e.printStackTrace();
			log("MQTT Unable to Connect");
		}
	}

	public void close(){
		try {
			client.disconnect();
			log("MQTT Disconnected");
		} catch (MqttException e) {
			e.printStackTrace();
			log("Unable to disconnect: "+e.toString());
		}
	}


	/****************************************************************/
	/* Methods to implement the MqttCallback interface			  */
	/****************************************************************/

	/**
	 * @see MqttCallback#connectionLost(Throwable)
	 */
	public void connectionLost(Throwable cause) {
		// Called when the connection to the server has been lost.
		// An application may choose to implement reconnection
		// logic at this point.
		// This sample simply exits.
		Log.d("SLAM","Connection to " + uri + " lost!");
	}

	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO 自動生成されたメソッド・スタブ

	}
}
