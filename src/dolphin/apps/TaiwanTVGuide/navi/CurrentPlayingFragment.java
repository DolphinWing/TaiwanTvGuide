package dolphin.apps.TaiwanTVGuide.navi;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import dolphin.apps.TaiwanTVGuide.R;
import dolphin.apps.TaiwanTVGuide.abs.TVGuideProgramABF;
import dolphin.apps.TaiwanTVGuide.provider.AtMoviesTVHttpHelper;
import dolphin.apps.TaiwanTVGuide.provider.ChannelItem;
import dolphin.apps.TaiwanTVGuide.provider.GuideExpandableListAdapter;
import dolphin.apps.TaiwanTVGuide.provider.ProgramItem;

/**
 * Created by dolphin on 2014/2/22.
 */
public class CurrentPlayingFragment extends Fragment {
    private final static String TAG = "CurrentPlayingFragment";
    public final static String ARG_CHANNEL_GROUP = "_group";
    public final static String ARG_LIST_TYPE = "_type";
    public final static String ARG_SHOW_ALL = "_show_all";
    public final static String ARG_EXPAND_ALL = "_expand_all";
    public final static String ARG_IS_EXPAND = "_expand_now";
    public final static String ARG_PREVIEW_DATE = "_preview_date";

    private ExpandableListView mListView;
    private View mLoadingView;
    private View mEmptyView;
    private String mGroup, mGroupId = null;
    private int mListType = 0;//0 as currently playing; 1 as today's show
    private AtMoviesTVHttpHelper mHelper;
    private ArrayList<ChannelItem> mChannelList = null;
    private boolean mIsLoading = true;
    private Calendar mPreviewDate;
    private boolean mShowTodayAll = true;
    private boolean mExpandAll = false;
    private ArrayList<Integer> mChannelProgramStartIndex;
    boolean bIsExpand = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expandable_list, null);

        mListView = (ExpandableListView) view.findViewById(android.R.id.list);
        mEmptyView = view.findViewById(android.R.id.text1);
        if (mEmptyView != null)
            mListView.setEmptyView(mEmptyView);
        mListView.setOnChildClickListener(OnChildClick);
        mListView.setOnItemLongClickListener(OnChildLongClick);

        mLoadingView = view.findViewById(android.R.id.progress);
        mLoadingView.setVisibility(mIsLoading ? View.VISIBLE : View.GONE);
        mEmptyView.setVisibility(mIsLoading ? View.GONE : View.VISIBLE);
        //Log.d(TAG, "onCreateView done");

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d(TAG, "onCreate");

        if (mPreviewDate == null)
            mPreviewDate = AtMoviesTVHttpHelper.getNowTime();

        SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (settings != null) {
            mShowTodayAll = settings.getBoolean("dTVGuide_ShowTodayAll", mShowTodayAll);
            mExpandAll = settings.getBoolean("dTVGuide_ExpendAll", mExpandAll);
        }

        mHelper = new AtMoviesTVHttpHelper(getActivity());
        if (mGroupId != null) {
            Log.d(TAG, "onCreate: start getting data");
            downloadData();
        }
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        //.d(TAG, "setArguments");

        if (args != null) {
            mGroup = args.getString(ARG_CHANNEL_GROUP);
            mGroupId = mGroup.split(" ")[1];
            //Log.d(TAG, "  groupId = " + mGroupId);
            mListType = args.getInt(ARG_LIST_TYPE, 0);
            //Log.d(TAG, "  listType = " + mListType);
            if (args.containsKey(ARG_SHOW_ALL))
                mShowTodayAll = args.getBoolean(ARG_SHOW_ALL);
            if (args.containsKey(ARG_EXPAND_ALL))
                mExpandAll = args.getBoolean(ARG_EXPAND_ALL);
            if (args.containsKey(ARG_IS_EXPAND))
                bIsExpand = args.getBoolean(ARG_IS_EXPAND);
            if (args.containsKey(ARG_PREVIEW_DATE)) {
                if (mPreviewDate == null)
                    mPreviewDate = AtMoviesTVHttpHelper.getNowTime();
                mPreviewDate.setTimeInMillis(args.getLong(ARG_PREVIEW_DATE));
            }

            if (mGroupId != null && mHelper != null) {
                Log.d(TAG, "setArguments: start getting data");
                downloadData();
            }
        }
    }

    private void downloadData() {
        mChannelProgramStartIndex = new ArrayList<Integer>();
        final EasyTracker easyTracker = EasyTracker.getInstance(getActivity());
        final long startTime = System.currentTimeMillis();

        new Thread(new Runnable() {
            @Override
            public void run() {
                //Log.d(TAG, "start getting data");
                switch (mListType) {
                    case 0:
                        mChannelList = mHelper.get_showtime_list(mGroupId);
                        mExpandAll = true;//force override
                        break;
                    case 1:
                        mChannelList = mHelper.get_group_guide_list(mPreviewDate, mGroupId);
                        break;
                }
                mIsLoading = false;
                long totalTime = System.currentTimeMillis() - startTime;
                Log.v(TAG, String.format("done getting data: %dms", totalTime));
                if (easyTracker != null) {
                    easyTracker.send(MapBuilder.createEvent("Network",//category (required)
                                    "downloadData",//action (required)
                                    String.format("type=%d", mListType),//label
                                    /*totalTime*/null)//Event value
                                    .build()
                    );
                }

                if (getActivity() != null)
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Log.d(TAG, "update UI");
                            updateProgramList();

                            if (mChannelList != null && mChannelList.size() > 0)
                                getActivity().setTitle(String.format("%s %d/%d",
                                        mGroup.split(" ")[0],
                                        mPreviewDate.get(Calendar.MONTH) + 1,
                                        mPreviewDate.get(Calendar.DAY_OF_MONTH)));
                            else if (mEmptyView != null)
                                mEmptyView.setVisibility(View.VISIBLE);
                            if (mLoadingView != null)
                                mLoadingView.setVisibility(View.GONE);
                        }
                    });
            }
        }).start();
    }

    private void updateProgramList() {
        if (mChannelList.size() > 0) {
            Calendar now = AtMoviesTVHttpHelper.getNowTime();
            Log.v(TAG, String.format("NOW: %02d/%02d %02d:%02d",
                    now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH),
                    now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE)));
            List<String> group = new ArrayList<String>();
            List<List<String>> child = new ArrayList<List<String>>();
            for (int i = 0; i < mChannelList.size(); i++) {
                ChannelItem chan = mChannelList.get(i);
                group.add(chan.Name);

                List<String> item = new ArrayList<String>();
                if (!mShowTodayAll && mListType != 0
                        && DateUtils.isToday(mPreviewDate.getTimeInMillis())) {
                    boolean bAfterProgram = false;
                    for (int j = 1; j < chan.Programs.size(); j++) {
                        ProgramItem program = chan.Programs.get(j);
                        if (!bAfterProgram) {
                            if (program.Date.before(now)) {// check if program ends
                                continue;
                            }
                            // after now, get current playing program
                            mChannelProgramStartIndex.add(Integer.valueOf(j - 1));
                            add_channel_item(item, chan.Programs.get(j - 1));
                            bAfterProgram = true;// set later all insert to list
                        }

                        add_channel_item(item, program);
                    }
                } else {// not today, don't check current time
                    for (int j = 0; j < chan.Programs.size(); j++) {
                        ProgramItem program = chan.Programs.get(j);
                        add_channel_item(item, program);
                    }
                }
                child.add(item);
            }

            if (mListView != null) {
                mListView.setAdapter(new GuideExpandableListAdapter(
                        getActivity(), group, child));
                expand_all(mExpandAll);// [1.0.0.6]dolphin++
            }
        } else {
            Log.w(TAG, "no data");
        }
    }

    protected void add_channel_item(List<String> item, ProgramItem prog) {
        item.add(String.format("%02d:%02d  %s",
                prog.Date.get(Calendar.HOUR_OF_DAY),
                prog.Date.get(Calendar.MINUTE), prog.Name));
    }

    protected void expand_all(boolean bExpand) {
        bIsExpand = bExpand;// [1.0.0.6]dolphin++
        ExpandableListAdapter adapter = mListView.getExpandableListAdapter();
        if (adapter != null)
            for (int i = 0; i < adapter.getGroupCount(); i++) {
                if (bIsExpand) {
                    mListView.expandGroup(i);
                } else {
                    mListView.collapseGroup(i);
                }
            }
    }

    private ExpandableListView.OnChildClickListener OnChildClick =
            new ExpandableListView.OnChildClickListener() {

                @Override
                public boolean onChildClick(ExpandableListView parent, View v,
                                            int groupPosition, int childPosition,
                                            long id) {
                    if (groupPosition >= mChannelList.size()) {//[49]++
                        Toast.makeText(getActivity(), R.string.no_data,
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    ChannelItem channel = mChannelList.get(groupPosition);
                    if (channel != null) {

                        int child = childPosition;
                        if (!mShowTodayAll && mListType != 0
                                && DateUtils.isToday(mPreviewDate.getTimeInMillis())) {
                            child += mChannelProgramStartIndex.get(groupPosition).intValue();
                        }// [1.0.0.7]dolphin++ if only show partial program
                        // Log.d(TAG, String.format("%d %d", groupPosition,child));

                        ProgramItem program = channel.Programs.get(child);
                        startProgramActivity(getActivity(), mPreviewDate, channel, program);
                    }
                    return true;// True if the click was handled
                }
            };

    public static void startProgramActivity(Context context, Calendar previewDate,
                                            ChannelItem channel, ProgramItem program) {
        Intent intent = new Intent();
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(context, TVGuideProgramABF.class);

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

    //[50]++
    private ExpandableListView.OnItemLongClickListener OnChildLongClick =
            new ExpandableListView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view,
                                               int position, long id) {
                    //Log.d(TAG, String.format("onItemLongClick %d", position));
                    //http://stackoverflow.com/a/8320128/2673859
                    if (ExpandableListView.getPackedPositionType(id)
                            == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                        int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                        int childPosition = ExpandableListView.getPackedPositionChild(id);

                        // You now have everything that you would as if this was an
                        // OnChildClickListener()
                        // Add your logic here.
                        Log.d(TAG, String.format(" group: %d", groupPosition));
                        Log.d(TAG, String.format(" child: %d", childPosition));
                        ChannelItem channel = mChannelList.get(groupPosition);
                        if (channel != null) {

                            int child = childPosition;
                            if (!mShowTodayAll
                                    && DateUtils.isToday(mPreviewDate.getTimeInMillis())) {
                                child += mChannelProgramStartIndex.get(groupPosition).intValue();
                            }// [1.0.0.7]dolphin++ if only show partial program
                            // Log.d(TAG, String.format("%d %d", groupPosition,child));

                            ProgramItem program = channel.Programs.get(child);
                            startAddingCalendar(getActivity(), channel, program);
                        }

                        // Return true as we are handling the event.
                        return true;
                    }

                    Log.w(TAG, "long click on group head");
                    return true;//still don't expand/collapse
                }
            };

    //Android Essentials: Adding Events to the User’s Calendar
    //http://goo.gl/jyT75l
    public static void startAddingCalendar(Context context,
                                           ChannelItem channel, ProgramItem program) {
        Intent calIntent = new Intent(Intent.ACTION_INSERT);
        calIntent.setData(CalendarContract.Events.CONTENT_URI);

        calIntent.setType("vnd.android.cursor.item/event");
        calIntent.putExtra(CalendarContract.Events.TITLE, String.format("%s - %s",
                channel.Name, program.Name));
        //calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, null);
        calIntent.putExtra(CalendarContract.Events.DESCRIPTION, program.ClipLength);

        calIntent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false);
        calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                program.Date.getTimeInMillis());
        calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                program.Date.getTimeInMillis());

        calIntent.putExtra(CalendarContract.Events.ACCESS_LEVEL,
                CalendarContract.Events.ACCESS_PRIVATE);
        calIntent.putExtra(CalendarContract.Events.AVAILABILITY,
                CalendarContract.Events.AVAILABILITY_FREE);

        context.startActivity(calIntent);
    }
}