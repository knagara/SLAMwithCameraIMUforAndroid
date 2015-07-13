package jp.ac.u_tokyo.slamwithcameraimu;

public class Utils {
	/**
	 * 加速度から重力加速度を取り除く。
	 *
	 * @param values
	 *            センサーの加速度
	 * @param gravity
	 *            前回の重力加速度が渡され、今回の重力加速度が格納される配列
	 * @param linearAccelatation
	 *            加速度から重力加速度が取り除かれた値が格納される配列
	 */
	static void extractGravity(float[] values, float[] gravity,
			float[] linearAccelatation, float alpha) {
		// 加速度から重力の影響を取り除く。以下参照。
		// http://developer.android.com/intl/ja/reference/android/hardware/SensorEvent.html#values
		//float alpha = 0.85f;

		gravity[0] = alpha * gravity[0] + (1 - alpha) * values[0];
		gravity[1] = alpha * gravity[1] + (1 - alpha) * values[1];
		gravity[2] = alpha * gravity[2] + (1 - alpha) * values[2];

		linearAccelatation[0] = values[0] - gravity[0];
		linearAccelatation[1] = values[1] - gravity[1];
		linearAccelatation[2] = values[2] - gravity[2];
	}

	/*
	 * Low-pass filter
	 */
	static void lowPassFilter(float[] values, float[] newValues, float alpha){
		//final float alpha = 0.8f;
		values[0] = alpha * values[0] + (1 - alpha) * newValues[0];
		values[1] = alpha * values[1] + (1 - alpha) * newValues[1];
		values[2] = alpha * values[2] + (1 - alpha) * newValues[2];
	}
}
