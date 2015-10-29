package jp.ac.u_tokyo.slamwithcameraimu;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

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
    int qos = 0;
    boolean retain = false;

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
			// Construct the MqttClient instance
		    client = new MqttAndroidClient(context, uri, clientId);
			// Set this wrapper as the callback handler
			client.setCallback(this);
		    client.setTraceCallback(new MqttTraceCallback());
	    } catch (NullPointerException e) {
	        e.getCause();
	    } catch (Exception e) {
	        e.getCause();
	    }
	}

	public void setConf(int qos, boolean retain){
		this.qos = qos;
		this.retain = retain;
	}

	public void connect(){
		try {
			//set connect options
			conOpt = new MqttConnectOptions();
			conOpt.setCleanSession(cleanSession);
			conOpt.setUserName(user);
			conOpt.setPassword(pass.toCharArray());
			//connect
//			client.connect(conOpt);
            client.connect(conOpt, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken arg0) {
                    Log.i("MQTT", "Connection Successful.");
                }
                @Override
                public void onFailure(IMqttToken arg0, Throwable arg1) {
                    Log.i("MQTT", "Connection Failed.");
                    Log.i("MQTT", ""+arg1);
                }
            });
		} catch (MqttSecurityException e) {
			e.printStackTrace();
			Log.d("MQTT","MQTT Unable to Connect by Security");
		} catch (MqttException e) {
			e.printStackTrace();
			Log.d("MQTT","MQTT Unable to Connect");
		}
	}

	public void close(){
		try {
			client.disconnect();
			Log.d("MQTT","MQTT Disconnected");
		} catch (MqttException e) {
			e.printStackTrace();
			Log.d("MQTT","Unable to disconnect: "+e.toString());
		}
	}

	/**
	 * Performs a single publish
	 * @param topic the topic to publish to/
	 * @param message the message to publish
	 * @param qos the qos to publish at
	 * @throws MqttException
	 */
	public void publish(String topic, String message) {
		//publish by default qos and retain
		this.publish(topic,message,this.qos,this.retain);
	}
	public void publish(String topic, String message, int qos, boolean retained) {
		if (!client.isConnected()){
			Log.d("MQTT","MQTT not connected.");
			return;
		}else{
			MqttMessage mqttmessage = new MqttMessage(message.getBytes());
			try {
				//publish
				client.publish(topic, mqttmessage);
			} catch (MqttPersistenceException e) {
				e.printStackTrace();
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}
	}
	public void publishBinary(String topic, byte[] message) {
		//publish by default qos and retain
		this.publishBinary(topic,message,this.qos,this.retain);
	}
	public void publishBinary(String topic, byte[] message, int qos, boolean retained) {
		if (!client.isConnected()){
			Log.d("MQTT","MQTT not connected.");
			return;
		}else{
			MqttMessage mqttmessage = new MqttMessage(message);
			try {
				//publish
				client.publish(topic, mqttmessage);
			} catch (MqttPersistenceException e) {
				e.printStackTrace();
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Subscribes to a topic and blocks until Enter is pressed
	 * @param topicName the topic to subscribe to
	 * @param qos the qos to subscibe at
	 * @throws MqttException
	 */
	public void subscribe(String topicName) {
		//subscribe by default qos
		this.subscribe(topicName, this.qos);
	}
	public void subscribe(String topicName, int qos) {
		if (!client.isConnected()){
			Log.d("MQTT","MQTT not connected.");
			return;
		}
		Log.d("MQTT","Subscribing to topic \""+topicName+"\" qos "+qos);
		try {
			//subscribe
			client.subscribe(topicName, qos);
		} catch (MqttSecurityException e) {
			e.printStackTrace();
			Log.d("MQTT","Unable to set up subscribe message(MqttSecurityException): "+e.toString());
		} catch (MqttException e) {
			e.printStackTrace();
			Log.d("MQTT","Unable to set up subscribe message(MqttException): "+e.toString());
		}
	}

	public void unsubscribe(String topicName) {
		if (!client.isConnected()){
			Log.d("MQTT","MQTT not connected.");
			return;
		}
		Log.d("MQTT","Unsubscribing to topic \""+topicName);
		try {
			client.unsubscribe(topicName);
		} catch (MqttException e) {
			e.printStackTrace();
			Log.d("MQTT","Unable to unsubscribe(MqttException): "+e.toString());
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
		Log.d("MQTT","Connection to " + uri + " lost!");
		reconnect(100);
	}

    /// <summary>
    /// Reconnect to MQTT Broker when client connection lost
    /// </summary>
    /// <param name="waitTime">wait time interval</param>
	private void reconnect(int waitTime){
		Log.d("MQTT","Client reconnecting... waitTime="+waitTime);
		try {Thread.sleep(waitTime);} catch (InterruptedException e) {e.printStackTrace();}
		try{
			if(!client.isConnected()){
				client.connect();
			}
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}finally{
			try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		}
		if(client.isConnected()){
			Log.d("MQTT","Client connected");
			connected();
			return;
		}else{
			waitTime = waitTime * 2;
			if(waitTime < 10*60*1000){
				reconnect(waitTime);
				return;
			}else{
				Log.d("MQTT","Error: Cannot connect to MQTT broker. (in reconnect method)");
				return;
			}
		}
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
