package dolphin.apps.TaiwanTVGuide.navi;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TabHost;

import dolphin.apps.TaiwanTVGuide.R;
import dolphin.apps.TaiwanTVGuide.abs.MyTabsAdapter;
import dolphin.apps.TaiwanTVGuide.provider.ProgramItem;

@Deprecated
public class TVGuideProgramABF extends FragmentActivity implements
        TVGuideProgramFragment.OnProgramDataListenter {
    private final static String TAG = "TVGuideProgramABF";

    private TabHost mTabHost;
    private ViewPager mViewPager;
    private MyTabsAdapter mTabsAdapter;
    private long mStartTime = 0;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
//        setTheme(R.style.Theme_Holo_orange_dark);//[40]++
        super.onCreate(savedInstanceState);

        //This has to be called before setContentView and you must use the
        //class in com.actionbarsherlock.view and NOT android.view
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        //		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        //		//The following two options trigger the collapsing of the main action bar view.
        //		//See the parent activity for the rest of the implementation
        //		getSupportActionBar().setDisplayShowHomeEnabled(false);
        //		getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(false);
        }

        setContentView(R.layout.fragment_tabs_pager);
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        if (mTabHost != null) {
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
                            //.setIndicator(createTabIndicator(R.string.guide_intro)),
                            .setIndicator(getString(R.string.guide_intro)),
                    TVGuideProgramFragment.class, null);

//		mTabsAdapter.addTab(
//			mTabHost.newTabSpec("recent_replays").setIndicator(
//				getString(R.string.recent_replays)),
//			TVGuideProgramRecentReplaysFragment.class, null);
            mTabsAdapter.addTab(mTabHost.newTabSpec("recent_replays")
                            //.setIndicator(createTabIndicator(R.string.recent_replays)),
                            .setIndicator(getString(R.string.recent_replays)),
                    TVGuideProgramRecentReplaysFragment.class, null);
            //mTabsAdapter.addTab(
            //	mTabHost.newTabSpec("imdb").setIndicator("IMDB"),
            //	TVGuideWebViewFragment.class, null);

            if (savedInstanceState != null) {
                mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
            }
        }
    }

//    private View createTabIndicator(int titleResId) {
//        return createTabIndicator(titleResId, 0);
//    }
//
//    private View createTabIndicator(int titleResId, int iconResId) {
//        View tabInd = LayoutInflater.from(this).inflate(R.layout.tab_indicator_holo, null);
//        //tabInd.setBackgroundResource(R.drawable.tab_indicator_ab_holo_orange_dark);
//        tabInd.setBackgroundResource(R.drawable.tab_indicator_holo);
//        if (titleResId > 0) {
//            ((TextView) tabInd.findViewById(android.R.id.title)).setText(titleResId);
//        }
//        if (iconResId > 0) {
//            ((ImageView) tabInd.findViewById(android.R.id.icon)).setImageResource(iconResId);
//        }
//        // http://stackoverflow.com/a/8296074
//        tabInd.setMinimumHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
//                56.0f, getResources().getDisplayMetrics()));
//        return tabInd;
//    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (outState != null) {//create a new object for save state
            super.onSaveInstanceState(outState);//save current tab

            if (mTabHost != null) {
                outState.putString("tab", mTabHost.getCurrentTabTag());
            }
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
        long totalTime = System.currentTimeMillis() - mStartTime;
        Log.v(TAG, String.format("done getting data: %dms", totalTime));

        //Log.d(TAG, "mTabsAdapter " + mTabsAdapter.getCount());
        //Log.d(TAG, "  " + mTabsAdapter.getItem(1).toString());
        //get recent_replay tab and set replay data
        if (progItem != null) {
            Log.d(TAG, "onDataReceived " + progItem.Replays.size());
            //http://goo.gl/T0AXV
            FragmentManager fmgr = getSupportFragmentManager();
            if (fmgr != null) {
                FragmentTransaction trans = fmgr.beginTransaction();
                TVGuideFragment frag;

                if (mTabHost != null) {
                    frag = (TVGuideFragment) fmgr.findFragmentByTag(getFragmentTag(1));
                } else {
                    frag = (TVGuideFragment) fmgr.findFragmentById(R.id.fragment1);
                }
                frag.updateView(progItem);
                trans.commit();
            }
        }
    }

}
