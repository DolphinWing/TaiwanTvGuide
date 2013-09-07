package dolphin.apps.TaiwanTVGuide.abs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import dolphin.apps.TaiwanTVGuide.R;
import dolphin.apps.TaiwanTVGuide.TVGuidePreference;
import dolphin.apps.TaiwanTVGuide.provider.AtMoviesTVHttpHelper;
import dolphin.apps.TaiwanTVGuide.provider.ChannelItem;
import dolphin.apps.TaiwanTVGuide.provider.GuideExpandableListAdapter;
import dolphin.apps.TaiwanTVGuide.provider.ProgramItem;

public abstract class TVGuideListActionBarThemedActivity
        extends ActionBarThemedActivity
        implements ActionBar.OnNavigationListener {
    public final static String TAG = "ActionBarThemedActivity";
    private String[] mGroups;

    public String[] getChannelGroups() {
        return mGroups;
    }

    public String getChannelGroup(int pos) {
        String[] groups = getChannelGroups();
        return groups[pos];
    }

    private Calendar mPreviewDate;

    public Calendar getPreviewDate() {
        return mPreviewDate;
    }

    public String getPreviewDateString() {
        return String.format("%4d/%02d/%02d",
                getPreviewDate().get(Calendar.YEAR),
                getPreviewDate().get(Calendar.MONTH) + 1,
                getPreviewDate().get(Calendar.DAY_OF_MONTH));
    }

    public String getPreviewDateTimeSpanString() {
        Calendar now = Calendar.getInstance();
        Calendar cal = getPreviewDate();
        if (DateUtils.isToday(cal.getTimeInMillis())) {//assuem the same date
            return getString(R.string.guide_list);
            //DateUtils.formatSameDayTime(cal.getTimeInMillis(), now.getTimeInMillis(),
            //    DateFormat.FULL, DateFormat.SHORT).toString();
        }

        //Locale.setDefault(Locale.TAIWAN);
        //[46]++ add DAY_OF_WEEK
        return new SimpleDateFormat("EEEE", Locale.TAIWAN).format(cal.getTime());
        //[46]--
        //+ "  " + DateUtils.getRelativeTimeSpanString(cal.getTimeInMillis(),
        //        now.getTimeInMillis(), DateUtils.HOUR_IN_MILLIS).toString();
    }

    public void addPreviewDate(int day) {
        mPreviewDate.add(Calendar.HOUR_OF_DAY, day * 24);
    }

    protected boolean mShowTodayAll = false;
    protected boolean mExpandAll = false;
    protected boolean bIsExpand = false;

    protected AtMoviesTVHttpHelper mHelper;

    protected ArrayList<ChannelItem> mChannelList;
    protected ArrayList<Integer> mChannelProgramStartIndex;
    private ExpandableListView mListView;
    private LinearLayout mLoadingLayout;

    protected Thread threadUpdateContent;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        send_message(EVENT_MSG_INIT, 0, 0, 200);

        //set default locale
        //http://stackoverflow.com/a/4239680
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.locale = Locale.TAIWAN;
        Locale.setDefault(config.locale);
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        Log.d(TAG, config.locale.getDisplayCountry(Locale.US));
    }

    private void _init() {
        setContentView(R.layout.main);

        mHelper = new AtMoviesTVHttpHelper(this);
        //mHelper._init(this);
        mPreviewDate = Calendar.getInstance();

        mListView = (ExpandableListView) findViewById(R.id.ExpandableListViewGuideList);
        mListView.setOnChildClickListener(OnChildClick);
        mLoadingLayout = (LinearLayout) findViewById(R.id.fullscreen_loading_indicator);

        mGroups = getResources().getStringArray(R.array.channel_group);

        Context context = getSActionBar().getThemedContext();
        //[32]-- getSActionBar().setDisplayHomeAsUpEnabled(true);
        //actionBar.setHomeButtonEnabled(true);

        getSActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ArrayAdapter<CharSequence> list =
                ArrayAdapter.createFromResource(context, R.array.channel_group,
                        R.layout.sherlock_spinner_item);
        list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
        getSActionBar().setListNavigationCallbacks(list, this);

        int group_index = 5;// [1.1.0.8]dolphin++ add default group
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (settings != null) {
            group_index = Integer.parseInt(settings.getString("dTVGuide_DefaultGroup", "5"));
        }
        try {// [1.2.0.12]dolphin++ use current playing page group
            group_index =
                    getIntent().getExtras().getInt(AtMoviesTVHttpHelper.KEY_GROUP, group_index);
        } catch (Exception e) {
            Log.e(TAG, "group_index: " + e.getMessage());
        }

        getSActionBar().setSelectedNavigationItem(group_index);

        //mListView.setEmptyView(findViewById(R.id.TextViewData));
    }

    @Override
    public boolean onNavigationItemSelected(int pos, long id) {
        //Log.d(TAG, String.format("%d: %s", pos, mGroups[pos]));
        show_loading(true);
        send_message(EVENT_MSG_UPDATE_DATA, pos, 1, 100);
        return false;
    }

    private final static int EVENT_MSG_SHOW_LOADING = 10001;
    private final static int EVENT_MSG_SHOW_TOAST_TEXT = 10003;
    private final static int EVENT_MSG_UPDATE_CHANNEL_LIST = 10012;
    private final static int EVENT_MSG_ASK_SET_WIFI = 10101;
    private final static int EVENT_MSG_ASK_ABORT = 10103;
    public final static int EVENT_MSG_UPDATE_DATA = 10022;
    private final static int EVENT_MSG_INIT = 20001;

    public void send_message(int what, int arg1, int arg2, int delayMillis) {
        Message msg = mHandler.obtainMessage(what, arg1, arg2);
        if (delayMillis > 0 && msg != null) {
            mHandler.sendMessageDelayed(msg, delayMillis);
        } else {
            mHandler.sendMessage(msg);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_MSG_INIT:
                    _init();
                    break;
                case EVENT_MSG_SHOW_LOADING:
                    show_loading((msg.arg1 == 1));
                    break;
                case EVENT_MSG_UPDATE_DATA:
                    get_channel_list(msg.arg1, (msg.arg2 == 1));
                    break;
                case EVENT_MSG_UPDATE_CHANNEL_LIST:
                    update_channel_list(msg.arg1, (msg.arg2 == 1));
                    break;
                case EVENT_MSG_SHOW_TOAST_TEXT:
                    show_toast_program(msg.arg1, msg.arg2);
                    break;
                case EVENT_MSG_ASK_SET_WIFI:
                    //	startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    //	close_program();
                    break;
                case EVENT_MSG_ASK_ABORT:
                    close_program();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    protected void show_toast_program(int groupPosition, int childPosition) {
        ChannelItem chan = mChannelList.get(groupPosition);
        ProgramItem prog = chan.Programs.get(childPosition);
        Toast.makeText(this, prog.Url, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void show_loading(boolean bShown) {
        // Android Market Loading effect
        // http://blog.lytsing.org/archives/46.html
        mLoadingLayout.setVisibility(bShown ? View.VISIBLE : View.GONE);
        mListView.setVisibility(bShown ? View.GONE : View.VISIBLE);
        mListView.setEnabled(!bShown);

        if (mOptionMenu != null) {//[31]++
            MenuItem r = mOptionMenu.findItem(R.id.program_option_refresh);
            if (r != null) {
                r.setEnabled(!bShown);
                if (getResources().getBoolean(R.bool.i_am_tablet))
                    r.setVisible(!bShown);
            }
        }

        super.show_loading(bShown);
    }

    protected void get_channel_list(int position, boolean bUseThread) {
        if (bUseThread) {// [1.2.0.9]dolphin++ use thread
            try {
                if (threadUpdateContent != null
                        && threadUpdateContent.isAlive()) {
                    // threadUpdateContent.stop();
                    threadUpdateContent.interrupt();
                    threadUpdateContent = null;
                }
            } catch (Exception e) {
                Log.e(TAG, "thread! " + e.getMessage());
            }
            threadUpdateContent = new Thread(new Runnable() {
                public void run() {
                    //int pos = mSpinnerGroup.getSelectedItemPosition();
                    ActionBar actionBar = (ActionBar) getSupportActionBar();
                    int pos = actionBar.getSelectedNavigationIndex();
                    update_data(pos);
                    send_message(EVENT_MSG_UPDATE_CHANNEL_LIST, pos, 0, 100);
                }
            });
            threadUpdateContent.setUncaughtExceptionHandler(onUncaughtExceptionHandler);
            threadUpdateContent.start();
        } else {
            update_data(position);
            send_message(EVENT_MSG_UPDATE_CHANNEL_LIST, position, 0, 0);
        }
    }

    abstract protected void update_data(int position);

    protected void add_channel_item(List<String> item, ProgramItem prog) {
        item.add(String.format("%02d:%02d  %s",
                prog.Date.get(Calendar.HOUR_OF_DAY),
                prog.Date.get(Calendar.MINUTE), prog.Name));
    }

    protected void update_channel_list(int position, boolean bUpdate) {
        //		String group_name = mGroups[position];
        // String group_id = group_name.split(" ")[1];
        Log.d(TAG, String.format("update_channel_list %d", position));
        //		this.setTitle(String.format("%s - %s %04d/%02d/%02d",
        //			this.getString(R.string.app_name), group_name.split(" ")[0],
        //			mPreviewDate.get(Calendar.YEAR),
        //			mPreviewDate.get(Calendar.MONTH) + 1,
        //			mPreviewDate.get(Calendar.DAY_OF_MONTH)));

        if (bUpdate) {// if download new data from web server
            // mChannelList =
            // mHelper.get_group_guide_list(mPreviewDate, group_id);
            get_channel_list(position, false);
        }

        if (mChannelList.size() > 0) {
            Calendar now = Calendar.getInstance();
            Log.d(TAG, String.format("NOW: %02d:%02d",
                    now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE)));
            List<String> group = new ArrayList<String>();
            List<List<String>> child = new ArrayList<List<String>>();
            for (int i = 0; i < mChannelList.size(); i++) {
                ChannelItem chan = mChannelList.get(i);
                group.add(chan.Name);

                List<String> item = new ArrayList<String>();
                if (!mShowTodayAll
                        && now.get(Calendar.YEAR) == mPreviewDate.get(Calendar.YEAR)
                        && mPreviewDate.get(Calendar.MONTH) == now.get(Calendar.MONTH)
                        && now.get(Calendar.DAY_OF_MONTH) == mPreviewDate.get(Calendar.DAY_OF_MONTH)) {
                    boolean bAfterProgram = false;
                    for (int j = 1; j < chan.Programs.size(); j++) {
                        ProgramItem prog = chan.Programs.get(j);
                        if (!bAfterProgram) {
                            if (prog.Date.before(now)) {// check if program ends
                                continue;
                            }
                            // after now, get current playing program
                            mChannelProgramStartIndex.add(Integer.valueOf(j - 1));
                            add_channel_item(item, chan.Programs.get(j - 1));
                            bAfterProgram = true;// set later all insert to list
                        }

                        add_channel_item(item, prog);
                    }
                } else {// not today, don't check current time
                    for (int j = 0; j < chan.Programs.size(); j++) {
                        ProgramItem prog = chan.Programs.get(j);
                        add_channel_item(item, prog);
                    }
                }
                child.add(item);
            }

            mListView.setAdapter(new GuideExpandableListAdapter(
                    TVGuideListActionBarThemedActivity.this, group, child));
            mListView.setEmptyView(findViewById(R.id.TextViewData));
            expand_all(mExpandAll);// [1.0.0.6]dolphin++
        } else {
            // show no data
            // ask go to Wi-Fi setup, retry, or leave?
            show_no_data();
        }

        onChannelListUpdated();
        show_loading(false);
    }

    protected void onChannelListUpdated() {
    }

    private ExpandableListView.OnChildClickListener OnChildClick =
            new ExpandableListView.OnChildClickListener() {

                @Override
                public boolean onChildClick(ExpandableListView parent, View v,
                                            int groupPosition, int childPosition, long id) {
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(TVGuideListActionBarThemedActivity.this,
                            TVGuideProgramABF.class);

                    intent.putExtra(
                            AtMoviesTVHttpHelper.KEY_DATE,
                            String.format("%04d-%02d-%02d",
                                    mPreviewDate.get(Calendar.YEAR),
                                    mPreviewDate.get(Calendar.MONTH) + 1,
                                    mPreviewDate.get(Calendar.DAY_OF_MONTH)));

                    ChannelItem chan = mChannelList.get(groupPosition);
                    if (chan != null) {
                        intent.putExtra(AtMoviesTVHttpHelper.KEY_GROUP, chan.Group);
                        int child = childPosition;
                        if (!mShowTodayAll
                                && Calendar.getInstance().get(Calendar.YEAR)
                                == mPreviewDate.get(Calendar.YEAR)
                                && mPreviewDate.get(Calendar.MONTH)
                                == Calendar.getInstance().get(Calendar.MONTH)) {
                            child += mChannelProgramStartIndex.get(groupPosition).intValue();
                        }// [1.0.0.7]dolphin++ if only show partial program
                        // Log.d(TAG, String.format("%d %d", groupPosition,child));
                        intent.putExtra(AtMoviesTVHttpHelper.KEY_CHANNEL_ID, chan.ID);

                        ProgramItem prog = chan.Programs.get(child);
                        if (prog != null) {
                            intent.putExtra(AtMoviesTVHttpHelper.KEY_TVDATA, prog.Url);
                            // Log.d(TAG, prog.Url);
                            intent.putExtra(AtMoviesTVHttpHelper.KEY_PROGRAM_NAME, prog.Name);
                            intent.putExtra(AtMoviesTVHttpHelper.KEY_CHANNEL, prog.Channel);

                            startActivity(intent);
                        }
                    }
                    return true;// True if the click was handled
                }
            };

    protected void expand_all(boolean bExpand) {
        bIsExpand = bExpand;// [1.0.0.6]dolphin++
        for (int i = 0; i < mListView.getExpandableListAdapter()
                .getGroupCount(); i++) {
            if (bIsExpand) {
                mListView.expandGroup(i);
            } else {
                mListView.collapseGroup(i);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Log.d(TAG, String.format("onResume()"));

        SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(this);
        if (settings != null) {
            mShowTodayAll = settings.getBoolean("dTVGuide_ShowTodayAll", true);
            // Log.d(TAG, String.format("onResume() %d", mShowTodayAll ? 1 :
            // 0));
            // Editor editor = settings.edit();//start edit
            // editor.putString("dTVGuide_ShowTodayAll", city_id);
            // editor.commit();
            mExpandAll = settings.getBoolean("dTVGuide_ExpendAll", false);

            // int group_index =
            // Integer.parseInt(settings.getString("dTVGuide_DefaultGroup", "5"));
            // Log.d(TAG, String.format("group_index=%d", group_index));
        }
    }

    protected Menu mOptionMenu = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mOptionMenu = menu;//store for loading use
        menu.add(Menu.NONE, R.id.preference, 100, R.string.preference)
                //[40]--.setIcon(android.R.drawable.ic_menu_preferences)
                .setIcon(R.drawable.ic_action_preference)
                .setShowAsAction(
                        MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        //[32] not always show preference icon for MS2
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Log.d(TAG, "HOME!");
            {
                close_program();
            }
            return true;
            case R.id.preference: {
                Intent intent2 = new Intent();
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent2.setClass(TVGuideListActionBarThemedActivity.this,
                        TVGuidePreference.class);
                startActivityForResult(intent2, 0);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, String.format("onActivityResult() %d %d", requestCode, resultCode));
        switch (requestCode) {
            case 0://return from preference
                if (resultCode == Activity.RESULT_OK) {
                    show_loading(true);
                    send_message(EVENT_MSG_UPDATE_CHANNEL_LIST,
                            getSActionBar().getSelectedNavigationIndex(),
                            0, 100);
                }
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        newConfig.locale = Locale.TAIWAN;
        super.onConfigurationChanged(newConfig);

        Locale.setDefault(Locale.TAIWAN);
        getBaseContext().getResources().updateConfiguration(newConfig,
                getBaseContext().getResources().getDisplayMetrics());
    }
}
