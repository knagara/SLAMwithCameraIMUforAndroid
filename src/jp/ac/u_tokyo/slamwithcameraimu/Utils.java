package jp.ac.u_tokyo.slamwithcameraimu;

import java.util.ArrayList;
import java.util.Collections;

public class Utils {

	/**
	 * High-pass filter
	 * @param newValues 新しい値
	 * @param lowValue 前回の低周波領域値が渡され、今回の低周波領域値が格納される配列
	 * @param value ハイパスフィルタ適用後の値が格納される配列
	 */
	static void highPassFilter(float[] newValues, float[] lowValue, float[] value, float alpha) {
	    lowValue[0] = alpha * lowValue[0] + (1 - alpha) * newValues[0];
	    lowValue[1] = alpha * lowValue[1] + (1 - alpha) * newValues[1];
	    lowValue[2] = alpha * lowValue[2] + (1 - alpha) * newValues[2];
	    value[0] = newValues[0] - lowValue[0];
	    value[1] = newValues[1] - lowValue[1];
	    value[2] = newValues[2] - lowValue[2];
	}

	/*
	 * Low-pass filter
	 */
	static float lowPassFilterSingle(float values, float newValues, float alpha){
		return (alpha * values + (1 - alpha) * newValues);
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

	/*
	 * Median filter
	 */
	static void medianFilter(float[] values, ArrayList<Float> valueX, ArrayList<Float> valueY, ArrayList<Float> valueZ, int medianNum){
		//X
		ArrayList<Float> lst = (ArrayList<Float>) valueX.clone();
		Collections.sort(lst);
		values[0] = lst.get(medianNum);
		//Y
		lst = (ArrayList<Float>) valueY.clone();
		Collections.sort(lst);
		values[1] = lst.get(medianNum);
		//Z
		lst = (ArrayList<Float>) valueZ.clone();
		Collections.sort(lst);
		values[2] = lst.get(medianNum);
	}

	/*
	 * Median filter + Low-pass filter
	 */
	static void medianLPFilter(float[] values, ArrayList<Float> valueX, ArrayList<Float> valueY, ArrayList<Float> valueZ, int medianNum, float alpha){
		//X
		ArrayList<Float> lst = (ArrayList<Float>) valueX.clone();
		Collections.sort(lst);
		values[0] = (values[0]*alpha) + lst.get(medianNum)*(1 - alpha);
		//Y
		lst = (ArrayList<Float>) valueY.clone();
		Collections.sort(lst);
		values[1] = (values[1]*alpha) + lst.get(medianNum)*(1 - alpha);
		//Z
		lst = (ArrayList<Float>) valueZ.clone();
		Collections.sort(lst);
		values[2] = (values[2]*alpha) + lst.get(medianNum)*(1 - alpha);
	}
}
