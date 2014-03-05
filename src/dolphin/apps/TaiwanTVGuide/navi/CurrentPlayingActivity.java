package dolphin.apps.TaiwanTVGuide.navi;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Calendar;
import java.util.Locale;

import dolphin.apps.TaiwanTVGuide.R;
import dolphin.apps.TaiwanTVGuide.TVGuidePreference;

/**
 * Created by dolphin on 2014/2/22.
 */
public class CurrentPlayingActivity extends Activity
        implements ActionBar.OnNavigationListener {
    private String[] mPlanetTitles;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private int mGroupIndex = 0;
    private SharedPreferences mPrefs;
    private boolean mShowTodayAll = true;
    private boolean mExpandAll = false;
    private Calendar mPreviewDate;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //since API level=9
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            // http://goo.gl/cmG1V , solve android.os.NetworkOnMainThreadException
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork() // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
        }

        //set default locale
        //http://stackoverflow.com/a/4239680
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.locale = Locale.TAIWAN;
        Locale.setDefault(config.locale);
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        //Log.d(TAG, config.locale.getDisplayCountry(Locale.US));

        setContentView(R.layout.activity_navigation_drawer);

        mPlanetTitles = getResources().getStringArray(R.array.channel_group);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mPlanetTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                mGroupIndex = position;
                selectItem(mGroupIndex, mPreviewDate.getTimeInMillis());
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.app_name, R.string.app_name) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        //mDrawerLayout.openDrawer(mDrawerList);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getActionBar().setListNavigationCallbacks(
                ArrayAdapter.createFromResource(getBaseContext(),
                        R.array.list_type,
                        android.R.layout.simple_spinner_dropdown_item),
                this);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mShowTodayAll = mPrefs.getBoolean("dTVGuide_ShowTodayAll", true);
        mExpandAll = mPrefs.getBoolean("dTVGuide_ExpendAll", false);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mGroupIndex = (!mPrefs.contains("dTVGuide_DefaultGroup")) ? 5
                        : Integer.parseInt(mPrefs.getString("dTVGuide_DefaultGroup", "5"));
                selectItem(mGroupIndex, System.currentTimeMillis());
            }
        }

        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mShowTodayAll = mPrefs.getBoolean("dTVGuide_ShowTodayAll", true);
        mExpandAll = mPrefs.getBoolean("dTVGuide_ExpendAll", false);
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position, long milliSeconds) {
        String group = mPlanetTitles[position];
        // Create a new fragment and specify the planet to show based on position
        Fragment fragment = new CurrentPlayingFragment();
        Bundle args = new Bundle();
        args.putString(CurrentPlayingFragment.ARG_CHANNEL_GROUP, group);
        args.putInt(CurrentPlayingFragment.ARG_LIST_TYPE,
                getActionBar().getSelectedNavigationIndex());
        args.putBoolean(CurrentPlayingFragment.ARG_EXPAND_ALL, mExpandAll);
        args.putBoolean(CurrentPlayingFragment.ARG_SHOW_ALL, mShowTodayAll);
        args.putLong(CurrentPlayingFragment.ARG_PREVIEW_DATE, milliSeconds);
        fragment.setArguments(args);

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(group);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...
        switch (item.getItemId()) {
            case R.id.program_option_refresh:
                selectItem(mGroupIndex, System.currentTimeMillis());
                return true;
            case R.id.preview_prev_day:
                selectItem(mGroupIndex, addPreviewDate(-1));
                return true;
            case R.id.preview_next_day:
                selectItem(mGroupIndex, addPreviewDate(1));
                return true;
            case R.id.preview_option_expand:
                break;
            case R.id.preference: {
                Intent intent2 = new Intent();
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent2.setClass(CurrentPlayingActivity.this,
                        TVGuidePreference.class);
                startActivityForResult(intent2, 0);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public long addPreviewDate(int day) {
        mPreviewDate.add(Calendar.HOUR_OF_DAY, day * 24);
        return mPreviewDate.getTimeInMillis();
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.program_option_refresh).setVisible(!drawerOpen);
        boolean bNow = getActionBar().getSelectedNavigationIndex() == 0;
        menu.setGroupVisible(R.id.preview_option_group, !drawerOpen & !bNow);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onNavigationItemSelected(int i, long l) {
        mPreviewDate = Calendar.getInstance();
        selectItem(mGroupIndex, mPreviewDate.getTimeInMillis());
        invalidateOptionsMenu();
        return true;
    }
}