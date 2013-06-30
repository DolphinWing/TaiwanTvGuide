package dolphin.apps.TaiwanTVGuide.abs;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Window;

import dolphin.apps.TaiwanTVGuide.R;

public class ActionBarThemedActivity extends SherlockActivity
{
	public final static String TAG = "ActionBarThemedActivity";

	private ActionBar mActionBar = null;

	public ActionBar getSActionBar()
	{
		return mActionBar;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		//[40]-- setTheme(R.style.Theme_Sherlock);
        setTheme(R.style.Theme_Holo_orange_dark);
		super.onCreate(savedInstanceState);

		//This has to be called before setContentView and you must use the
		//class in com.actionbarsherlock.view and NOT android.view
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		//use ActionBarSherlock library
		mActionBar = (ActionBar) getSupportActionBar();
	}

	public void show_loading(boolean bShown)
	{
		setSupportProgressBarIndeterminateVisibility(bShown);
	}

	public void close_program()
	{
		this.finish();
	}

	public void show_no_data()
	{
		Log.w(TAG, "no data!");
		Toast.makeText(getBaseContext(), R.string.no_data,
			Toast.LENGTH_SHORT).show();
	}

	public Thread.UncaughtExceptionHandler onUncaughtExceptionHandler =
		new Thread.UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread arg0, Throwable arg1)
			{
				Log.e(TAG, "thread exception!" + arg1.getMessage());
				show_no_data();// [1.2.0.10]dolphin++
			}
		};
}
