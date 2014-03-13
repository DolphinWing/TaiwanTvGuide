package dolphin.apps.TaiwanTVGuide.abs;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import dolphin.apps.TaiwanTVGuide.R;
import dolphin.apps.TaiwanTVGuide.provider.ProgramItem;

public class TVGuideProgramABF extends SherlockFragmentActivity implements
        TVGuideProgramFragment.OnProgramDataListenter {
    public final static String TAG = "TVGuideProgramABF";

    TabHost mTabHost;
    ViewPager mViewPager;
    MyTabsAdapter mTabsAdapter;
    long mStartTime = 0;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Holo_orange_dark);//[40]++
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

        mStartTime = System.currentTimeMillis();
        //[Android] Custom TabHost Style
        //http://www.dotblogs.com.tw/alonstar/archive/2012/04/18/android_tabhost.aspx
        //How to change tab style in Android?
        //http://stackoverflow.com/a/3029300
//		mTabsAdapter.addTab(
//			mTabHost.newTabSpec("guide_intro").setIndicator(
//				getString(R.string.guide_intro)),
//			TVGuideProgramFragment.class, getIntent().getExtras());
        mTabsAdapter.addTab(mTabHost.newTabSpec("guide_intro")
                .setIndicator(createTabIndicator(R.string.guide_intro)),
                //getString(R.string.title_file_location_local)),
                TVGuideProgramFragment.class, null);

//		mTabsAdapter.addTab(
//			mTabHost.newTabSpec("recent_replays").setIndicator(
//				getString(R.string.recent_replays)),
//			TVGuideProgramRecentReplaysFragment.class, null);
        mTabsAdapter.addTab(mTabHost.newTabSpec("recent_replays")
                .setIndicator(createTabIndicator(R.string.recent_replays)),
                //getString(R.string.title_file_location_local)),
                TVGuideProgramRecentReplaysFragment.class, null);
        //mTabsAdapter.addTab(
        //	mTabHost.newTabSpec("imdb").setIndicator("IMDB"),
        //	TVGuideWebViewFragment.class, null);

        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }
    }

    private View createTabIndicator(int titleResId) {
        return createTabIndicator(titleResId, 0);
    }

    private View createTabIndicator(int titleResId, int iconResId) {
        View tabInd = LayoutInflater.from(this).inflate(R.layout.tab_indicator_holo, null);
        //tabInd.setBackgroundResource(R.drawable.tab_indicator_ab_holo_orange_dark);
        tabInd.setBackgroundResource(R.drawable.tab_indicator_holo);
        ((TextView) tabInd.findViewById(android.R.id.title)).setText(titleResId);
        // http://stackoverflow.com/a/8296074
        tabInd.setMinimumHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                56.0f, getResources().getDisplayMetrics()));
        return tabInd;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (outState != null) {//create a new object for save state
            //	outState = new Bundle();
            //}
            //else {
            super.onSaveInstanceState(outState);//save current tab
            outState.putString("tab", mTabHost.getCurrentTabTag());
        }
    }

    protected void close_program() {
        this.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getFragmentTag(int pos) {
        return "android:switcher:" + R.id.pager + ":" + pos;
    }

    @Override
    public void onDataReceived(ProgramItem progItem) {
        EasyTracker easyTracker = EasyTracker.getInstance(this);
        if (easyTracker != null) {
            easyTracker.set(Fields.SCREEN_NAME, TAG);
            // MapBuilder.createEvent().build() returns a Map of event fields and values
            // that are set and sent with the hit.
            easyTracker.send(MapBuilder.createEvent("UX",//Event category (required)
                            "network",//Event action (required)
                            "onDataReceived",//Event label
                            System.currentTimeMillis() - mStartTime)//Event value
                            .build()
            );
        }
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

    @Override
    public void onStart() {
        super.onStart();
        //... // The rest of your onStart() code.
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        //... // The rest of your onStop() code.
        EasyTracker.getInstance(this).activityStop(this);
    }
}
