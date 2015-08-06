package jp.ac.u_tokyo.slamwithcameraimu;

import android.content.Context;
import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.Toast;

public class QuickToastTask extends AsyncTask<String, Integer, Integer> {
	private Toast toast;
	private final Context context;
//	private final int msgResId;
	private int dispTime;
	private static final int SHORT = 800;
	private final String text;

	public QuickToastTask(final Context context, final String text, int time) {
		this.text = text;
		this.context = context;
		this.dispTime = time;
	}

	@Override
	protected void onPreExecute() {
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		this.toast = toast;
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	@Override
	protected Integer doInBackground(final String... params) {
		try {
                        //ここでトースト表示時間分Sleepさせる。
			Thread.sleep(dispTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	protected void onPostExecute(final Integer i) {
                //キャンセルするとトーストが消えます。
		this.toast.cancel();
	}
}
