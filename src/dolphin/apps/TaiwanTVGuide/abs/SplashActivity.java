package dolphin.apps.TaiwanTVGuide.abs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends Activity
{
	//Hide application launcher icon in title bar when activity starts in android
	//http://stackoverflow.com/q/11430712

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_test_splash);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		Intent i = new Intent(getBaseContext(), TVGuideNowPlayingAB.class);
		i.putExtras(getIntent());
		startActivity(i);
		this.finish();
	}
}
