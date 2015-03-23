package dolphin.apps.TaiwanTVGuide.v7;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import dolphin.apps.TaiwanTVGuide.R;
import dolphin.apps.TaiwanTVGuide.provider.AtMoviesTVHttpHelper;
import dolphin.apps.TaiwanTVGuide.provider.ChannelItem;
import dolphin.apps.TaiwanTVGuide.provider.ProgramItem;
import dolphin.apps.TaiwanTVGuide.provider.Utils;


public class ProgramListActivity extends ActionBarActivity implements OnHttpProvider, OnHttpListener {
    private final static String TAG = "ProgramListActivity";
    private final static boolean AUTO_SELECT = true;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Switch mSwitch;
    private View mLeftPane;
    private View mLoadingPane;
    private View mEmptyView;

    private String[] mChannelGroups;
    private int mGroupIndex = 0;
    private String mGroupId;
    private Calendar mPreviewDate;
    private int mListType = AtMoviesTVHttpHelper.TYPE_NOW_PLAYING;
    private String mUrl = AtMoviesTVHttpHelper.ATMOVIES_TV_URL;

    private AtMoviesTVHttpHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // http://goo.gl/cmG1V , solve android.os.NetworkOnMainThreadException
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .permitNetwork()
                .build());

        //set default locale
        //http://stackoverflow.com/a/4239680
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.locale = Locale.TAIWAN;
        Locale.setDefault(config.locale);

        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_program_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_action_list);

        mChannelGroups = getResources().getStringArray(R.array.channel_group);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
        mDrawerList = (ListView) findViewById(R.id.category_list);
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.listview_category, android.R.id.text1, mChannelGroups));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                selectItem(position);
                mDrawerLayout.closeDrawer(mLeftPane);
            }
        });
        mSwitch = (Switch) findViewById(R.id.switch1);
        mSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean checked) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                mListType = checked ? AtMoviesTVHttpHelper.TYPE_ALL_DAY :
                        AtMoviesTVHttpHelper.TYPE_NOW_PLAYING;
                selectItem(mGroupIndex);
            }
        });
        mLeftPane = findViewById(R.id.left_drawer);
        mLoadingPane = findViewById(R.id.fullscreen_loading_indicator);
        mLoadingPane.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;//do nothing
            }
        });
        //mLoadingPane.setVisibility(View.GONE);
        mEmptyView = findViewById(android.R.id.empty);
        View retryView = findViewById(R.id.action_retry);
        if (retryView != null) {
            retryView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectItem(mGroupIndex);
                }
            });
        }
        mEmptyView.setVisibility(View.GONE);

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, new ProgramListFragment())
                .commit();

        mHelper = new AtMoviesTVHttpHelper(this);
        mPreviewDate = AtMoviesTVHttpHelper.getNowTime();

        if (AUTO_SELECT) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            selectItem(Utils.getPreferenceGroupIndex(this));
            mDrawerLayout.closeDrawer(mLeftPane);
        } else {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mDrawerLayout.openDrawer(mLeftPane);
                }
            });
        }
        registerOnHttpListener(this);
    }

    @Override
    protected void onDestroy() {
        unregisterOnHttpListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_program_list, menu);
        return true;
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        //boolean drawerOpen = mDrawerLayout.isDrawerOpen(mLeftPane);
        menu.setGroupVisible(R.id.preview_option_group, /*!drawerOpen & */mSwitch.isChecked());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
            case R.id.action_search_menu://[62]++
                mDrawerLayout.openDrawer(mLeftPane);
                return true;
            case R.id.program_option_refresh:
                selectItem(mGroupIndex);
                return true;
            case R.id.preview_prev_day:
                mPreviewDate.add(Calendar.HOUR_OF_DAY, -24);
                selectItem(mGroupIndex);
                //mTitle = new SimpleDateFormat("MM/dd", Locale.TAIWAN).format(mPreviewDate.getTime());
                return true;
            case R.id.preview_next_day:
                mPreviewDate.add(Calendar.HOUR_OF_DAY, 24);
                selectItem(mGroupIndex);
                //Title = new SimpleDateFormat("MM/dd", Locale.TAIWAN).format(mPreviewDate.getTime());
                return true;
            case R.id.preference: {
                Intent intent2 = new Intent();
                //intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent2.setClass(ProgramListActivity.this, SettingsActivity.class);
                startActivityForResult(intent2, 0);
            }
            return true;
            case R.id.action_browser: {
                Intent intent3 = new Intent(Intent.ACTION_VIEW);
                intent3.setData(Uri.parse(mUrl));
                startActivityForResult(intent3, 0);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {
        mGroupIndex = position;
        mGroupId = mChannelGroups[position].split(" ")[1];
        for (OnHttpListener listener : mOnHttpListener) {
            listener.onHttpStart();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<ChannelItem> channelItems = null;
                switch (mListType) {
                    case AtMoviesTVHttpHelper.TYPE_NOW_PLAYING:
                        channelItems = mHelper.get_showtime_list(mGroupId);
                        break;
                    case AtMoviesTVHttpHelper.TYPE_ALL_DAY:
                        channelItems = mHelper.get_group_guide_list(mPreviewDate, mGroupId);
                        break;
                }

                final ArrayList<ChannelItem> items = channelItems;
                ProgramListActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (OnHttpListener listener : mOnHttpListener) {
                            listener.onHttpUpdated(items);
                        }
                    }
                });
            }
        }).start();

        mUrl = mUrl.replace("@group", mChannelGroups[position].split(" ")[1]);
    }

    private List<OnHttpListener> mOnHttpListener = new ArrayList<>();

    @Override
    public void registerOnHttpListener(OnHttpListener listener) {
        mOnHttpListener.add(listener);
    }

    @Override
    public void unregisterOnHttpListener(OnHttpListener listener) {
        mOnHttpListener.remove(listener);
    }

    @Override
    public void refresh() {
        selectItem(mGroupIndex);
    }

    private void setLoading(boolean loading) {
        setSupportProgressBarIndeterminateVisibility(loading);
        if (mLoadingPane != null) {
            mLoadingPane.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onHttpStart() {
        //Log.d(TAG, "onHttpStart");
        setLoading(true);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mEmptyView.setVisibility(View.GONE);
    }

    @Override
    public void onHttpUpdated(Object data) {
        if (data == null || ((ArrayList) data).size() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
        }
        //Log.d(TAG, "onHttpUpdated " + data);
        String group = mChannelGroups[mGroupIndex];
        String title = mSwitch.isChecked()
                ? String.format("%s  %02d/%02d", group.substring(0, 2),
                mPreviewDate.get(Calendar.MONTH) + 1, mPreviewDate.get(Calendar.DAY_OF_MONTH))
                : String.format("%s  %s", group.substring(0, 2), mSwitch.getTextOff());
        getSupportActionBar().setTitle(title);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        setLoading(false);
    }

    @Override
    public void onHttpTimeout() {
        //Log.d(TAG, "onHttpTimeout");
        mEmptyView.setVisibility(View.VISIBLE);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        setLoading(false);
    }

    public static void startProgramActivity(Context context, Calendar previewDate,
                                            ChannelItem channel, ProgramItem program) {
        Intent intent = new Intent();
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(context, ProgramInfoActivity.class);

        intent.putExtra(
                AtMoviesTVHttpHelper.KEY_DATE,
                String.format("%04d-%02d-%02d",
                        previewDate.get(Calendar.YEAR),
                        previewDate.get(Calendar.MONTH) + 1,
                        previewDate.get(Calendar.DAY_OF_MONTH))
        );

        if (channel != null) {
            intent.putExtra(AtMoviesTVHttpHelper.KEY_GROUP, channel.Group);
            intent.putExtra(AtMoviesTVHttpHelper.KEY_CHANNEL_ID, channel.ID);

            if (program != null) {
                intent.putExtra(AtMoviesTVHttpHelper.KEY_TVDATA, program.Url);
                // Log.d(TAG, program.Url);
                intent.putExtra(AtMoviesTVHttpHelper.KEY_PROGRAM_NAME, program.Name);
                intent.putExtra(AtMoviesTVHttpHelper.KEY_CHANNEL, program.Channel);

                context.startActivity(intent);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mLeftPane)) {
            mDrawerLayout.closeDrawer(mLeftPane);
        } else {
            super.onBackPressed();
        }
    }
}
