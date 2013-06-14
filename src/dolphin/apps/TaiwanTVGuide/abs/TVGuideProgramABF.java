package dolphin.apps.TaiwanTVGuide.abs;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.TabHost;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import dolphin.apps.TaiwanTVGuide.R;
import dolphin.apps.TaiwanTVGuide.provider.ProgramItem;

public class TVGuideProgramABF extends SherlockFragmentActivity implements
		TVGuideProgramFragment.OnProgramDataListenter
{
	public final static String TAG = "TVGuideProgramABF";

	TabHost mTabHost;
	ViewPager mViewPager;
	MyTabsAdapter mTabsAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		//This has to be called before setContentView and you must use the
		//class in com.actionbarsherlock.view and NOT android.view
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		//		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		//		//The following two options trigger the collapsing of the main action bar view.
		//		//See the parent activity for the rest of the implementation
		//		getSupportActionBar().setDisplayShowHomeEnabled(false);
		//		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);

		setContentView(R.layout.fragment_tabs_pager);
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mViewPager = (ViewPager) findViewById(R.id.pager);

		mTabsAdapter = new MyTabsAdapter(this, mTabHost, mViewPager);

		mTabsAdapter.addTab(
			mTabHost.newTabSpec("guide_intro").setIndicator(
				getString(R.string.guide_intro)),
			TVGuideProgramFragment.class, getIntent().getExtras());
		mTabsAdapter.addTab(
			mTabHost.newTabSpec("recent_replays").setIndicator(
				getString(R.string.recent_replays)),
			TVGuideProgramRecentReplaysFragment.class, null);
		//mTabsAdapter.addTab(
		//	mTabHost.newTabSpec("imdb").setIndicator("IMDB"),
		//	TVGuideWebViewFragment.class, null);

		if (savedInstanceState != null) {
			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		if (outState != null) {//create a new object for save state
			//	outState = new Bundle();
			//}
			//else {
			super.onSaveInstanceState(outState);//save current tab
			outState.putString("tab", mTabHost.getCurrentTabTag());
		}
	}

	protected void close_program()
	{
		this.finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private String getFragmentTag(int pos)
	{
		return "android:switcher:" + R.id.pager + ":" + pos;
	}

	@Override
	public void onDataReceived(ProgramItem progItem)
	{
		//Log.d(TAG, "mTabsAdapter " + mTabsAdapter.getCount());
		//Log.d(TAG, "  " + mTabsAdapter.getItem(1).toString());
		//get recent_replay tab and set replay data
		if (progItem != null) {
			Log.d(TAG, "onDataReceived " + progItem.Replays.size());
			//http://goo.gl/T0AXV
			FragmentManager fmgr = getSupportFragmentManager();
			if (fmgr != null) {
				FragmentTransaction trans = fmgr.beginTransaction();
				TVGuideFragment frag =
					(TVGuideFragment) fmgr.findFragmentByTag(getFragmentTag(1));
				frag.updateView(progItem);
				//frag =
				//	(TVGuideFragment) fmgr.findFragmentByTag(getFragmentTag(2));
				//if (frag != null)
				//	frag.updateView(progItem);
				trans.commit();
			}
		}
	}
}
