package jp.ac.u_tokyo.slamwithcameraimu;

import org.eclipse.paho.android.service.MqttTraceHandler;

import android.util.Log;

public class MqttTraceCallback implements MqttTraceHandler {

	public void traceDebug(java.lang.String arg0, java.lang.String arg1) {
		Log.i(arg0, arg1);
	};

	public void traceError(java.lang.String arg0, java.lang.String arg1) {
		Log.e(arg0, arg1);
	};

	public void traceException(java.lang.String arg0, java.lang.String arg1,
			java.lang.Exception arg2) {
		Log.e(arg0, arg1, arg2);
	};

}
