package dolphin.apps.TaiwanTVGuide;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import dolphin.apps.TaiwanTVGuide.provider.AtMoviesTVHttpHelper;
import dolphin.apps.TaiwanTVGuide.provider.ChannelItem;
import dolphin.apps.TaiwanTVGuide.provider.GuideExpandableListAdapter;
import dolphin.apps.TaiwanTVGuide.provider.ProgramItem;

public class TVGuidePreview extends Activity
{
	private static final String TAG = "TVGuidePreview";
	protected AtMoviesTVHttpHelper mHelper;
	private Calendar mPreviewDate;
	protected boolean mShowTodayAll = false;
	protected boolean mExpandAll = false;
	protected boolean bIsExpand = false;

	protected ArrayList<ChannelItem> mChannelList;
	protected ArrayList<Integer> mChannelProgramStartIndex;

	protected Spinner mSpinnerGroup;
	protected Thread threadUpdateContent;

	private ExpandableListView mListView;
	private LinearLayout mLoadingLayout;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mListView =
			(ExpandableListView) findViewById(R.id.ExpandableListViewGuideList);
		mListView.setOnChildClickListener(OnChildClick);
		mLoadingLayout =
			(LinearLayout) findViewById(R.id.fullscreen_loading_indicator);
		// show_loading(true);
		mHelper = new AtMoviesTVHttpHelper(this);
		//mHelper._init(this);
		mPreviewDate = Calendar.getInstance();

		mSpinnerGroup = (Spinner) findViewById(R.id.SpinnerGroup);
		mSpinnerGroup.setOnItemSelectedListener(OnItemSelected);
		int group_index = 5;// [1.1.0.8]dolphin++ add default group
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (settings != null) {
			group_index = Integer.parseInt(settings.getString(
				"dTVGuide_DefaultGroup", "5"));
		}
		try {// [1.2.0.12]dolphin++ use current playing page group
			group_index = getIntent().getExtras().getInt(
				AtMoviesTVHttpHelper.KEY_GROUP, group_index);
		}
		catch (Exception e1) {
		}
		mSpinnerGroup.setSelection(group_index);

		mShowTodayAll = false;
		mExpandAll = false;
	}

	private void send_message(int what, int arg1, int arg2, int delayMillis)
	{
		Message msg = mHandler.obtainMessage(what, arg1, arg2);
		if (delayMillis > 0 && msg != null) {
			mHandler.sendMessageDelayed(msg, delayMillis);
		}
		else {
			mHandler.sendMessage(msg);
		}
	}

	private final static int EVENT_MSG_SHOW_LOADING = 10001;
	private final static int EVENT_MSG_UPDATE_CHANNEL_LIST = 10002;
	private final static int EVENT_MSG_SHOW_TOAST_TEXT = 10003;
	private final static int EVENT_MSG_ASK_SET_WIFI = 10011;
	private final static int EVENT_MSG_ASK_ABORT = 10013;
	private final static int EVENT_MSG_UPDATE_DATA = 10022;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what) {
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
					startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
					close_program();
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

	protected void show_loading(boolean bShown)
	{
		// Android Market Loading effect
		// http://blog.lytsing.org/archives/46.html
		mLoadingLayout.setVisibility(bShown ? View.VISIBLE : View.GONE);
		mListView.setVisibility(bShown ? View.GONE : View.VISIBLE);
	}

	protected void close_program()
	{
		this.finish();
	}

	protected void show_toast_program(int groupPosition, int childPosition)
	{
		ChannelItem chan = mChannelList.get(groupPosition);
		ProgramItem prog = chan.Programs.get(childPosition);
		Toast.makeText(this, prog.Url, Toast.LENGTH_SHORT).show();
	}

	protected void add_channel_item(List<String> item, ProgramItem prog)
	{
		item.add(String.format("%02d:%02d  %s",
			prog.Date.get(Calendar.HOUR_OF_DAY),
			prog.Date.get(Calendar.MINUTE), prog.Name));
	}

	protected void update_data(int position)
	{
		String group_name = getResources()
				.getStringArray(R.array.channel_group)[position];
		String group_id = group_name.split(" ")[1];
		mChannelList = mHelper.get_group_guide_list(mPreviewDate, group_id);
		mChannelProgramStartIndex = new ArrayList<Integer>();
	}

	protected void get_channel_list(int position, boolean bUseThread)
	{
		if (bUseThread) {// [1.2.0.9]dolphin++ use thread
			try {
				if (threadUpdateContent != null
					&& threadUpdateContent.isAlive()) {
					// threadUpdateContent.stop();
					threadUpdateContent.interrupt();
					threadUpdateContent = null;
				}
			}
			catch (Exception e) {
				Log.e(TAG, "thread! " + e.getMessage());
			}
			threadUpdateContent = new Thread(new Runnable() {
				public void run()
				{
					int pos = mSpinnerGroup.getSelectedItemPosition();
					update_data(pos);
					send_message(EVENT_MSG_UPDATE_CHANNEL_LIST, pos, 0, 100);
				}
			});
			threadUpdateContent
					.setUncaughtExceptionHandler(onUncaughtExceptionHandler);
			threadUpdateContent.start();
		}
		else {
			update_data(position);
			send_message(EVENT_MSG_UPDATE_CHANNEL_LIST, position, 0, 0);
		}
	}

	protected void update_channel_list(int position, boolean bUpdate)
	{
		String group_name = getResources()
				.getStringArray(R.array.channel_group)[position];
		// String group_id = group_name.split(" ")[1];
		// Log.d(TAG, group_id);
		this.setTitle(String.format("%s - %s %04d/%02d/%02d",
			this.getString(R.string.app_name), group_name.split(" ")[0],
			mPreviewDate.get(Calendar.YEAR),
			mPreviewDate.get(Calendar.MONTH) + 1,
			mPreviewDate.get(Calendar.DAY_OF_MONTH)));

		if (bUpdate) {// if download new data from web server
			// mChannelList =
			// mHelper.get_group_guide_list(mPreviewDate, group_id);
			get_channel_list(position, false);
		}

		if (mChannelList.size() > 0) {
			Calendar now = Calendar.getInstance();
			Log.d(TAG,
				String.format("NOW: %02d:%02d",
					now.get(Calendar.HOUR_OF_DAY),
					now.get(Calendar.MINUTE)));
			List<String> group = new ArrayList<String>();
			List<List<String>> child = new ArrayList<List<String>>();
			for (int i = 0; i < mChannelList.size(); i++) {
				ChannelItem chan = mChannelList.get(i);
				group.add(chan.Name);

				List<String> item = new ArrayList<String>();
				if (!mShowTodayAll
					&& now.get(Calendar.YEAR) == mPreviewDate
							.get(Calendar.YEAR)
					&& mPreviewDate.get(Calendar.MONTH) == now
							.get(Calendar.MONTH)
					&& now.get(Calendar.DAY_OF_MONTH) == mPreviewDate
							.get(Calendar.DAY_OF_MONTH)) {
					boolean bAfterProgram = false;
					for (int j = 1; j < chan.Programs.size(); j++) {
						ProgramItem prog = chan.Programs.get(j);
						if (!bAfterProgram) {
							if (prog.Date.before(now)) {// check if program ends
								// Log.d(TAG, String.format("%d ==> %02d:%02d",
								// i,
								// prog.Date.get(Calendar.HOUR_OF_DAY),
								// prog.Date
								// .get(Calendar.MINUTE)));
								continue;
							}
							// after now, get current playing program
							mChannelProgramStartIndex.add(new Integer(j - 1));// [1.0.0.7]dolphin++
							add_channel_item(item, chan.Programs.get(j - 1));
							bAfterProgram = true;// set later all insert to list
						}

						add_channel_item(item, prog);
					}
				}
				else {// not today, don't check current time
					for (int j = 0; j < chan.Programs.size(); j++) {
						ProgramItem prog = chan.Programs.get(j);
						add_channel_item(item, prog);
					}
				}
				child.add(item);
			}

			mListView.setAdapter(new GuideExpandableListAdapter(
					TVGuidePreview.this, group, child));
			expand_all(mExpandAll);// [1.0.0.6]dolphin++
		}
		else {
			// show no data
			// ask go to Wi-Fi setup, retry, or leave?
			show_no_data();
		}

		show_loading(false);
	}

	protected void expand_all(boolean bExpand)
	{
		bIsExpand = bExpand;// [1.0.0.6]dolphin++
		for (int i = 0; i < mListView.getExpandableListAdapter()
				.getGroupCount(); i++) {
			if (bIsExpand) {
				mListView.expandGroup(i);
			}
			else {
				mListView.collapseGroup(i);
			}
		}

		//[0.5.0.20] @ 2011-06-01
		//		MenuItem item = (MenuItem) findViewById(R.id.preview_option_expand);
		//		item.setIcon(bIsExpand
		//			? android.R.drawable.button_onoff_indicator_on
		//			: android.R.drawable.button_onoff_indicator_off);
	}

	Spinner.OnItemSelectedListener OnItemSelected =
		new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id)
			{
				// TODO Auto-generated method stub
				show_loading(true);
				send_message(EVENT_MSG_UPDATE_DATA, position, 1, 100);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
				// TODO Auto-generated method stub

			}
		};

	ExpandableListView.OnChildClickListener OnChildClick =
		new ExpandableListView.OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id)
			{
				// TODO Auto-generated method stub
				// Log
				// .d(TAG, String.format("%d %d", groupPosition,
				// childPosition));
				// send_message(EVENT_MSG_SHOW_TOAST_TEXT, groupPosition,
				// childPosition, 0);

				Intent intent = new Intent();
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setClass(TVGuidePreview.this, TVGuideProgram.class);

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
						&& Calendar.getInstance().get(Calendar.YEAR) == mPreviewDate
								.get(Calendar.YEAR)
						&& mPreviewDate.get(Calendar.MONTH) == Calendar
								.getInstance().get(Calendar.MONTH)) {
						child += mChannelProgramStartIndex.get(groupPosition)
								.intValue();
					}// [1.0.0.7]dolphin++ if only show partial program
					// Log
					// .d(TAG, String.format("%d %d", groupPosition,
					// child));
					intent.putExtra(AtMoviesTVHttpHelper.KEY_CHANNEL_ID,
						chan.ID);

					ProgramItem prog = chan.Programs.get(child);
					if (prog != null) {
						intent.putExtra(AtMoviesTVHttpHelper.KEY_TVDATA,
							prog.Url);
						// Log.d(TAG, prog.Url);
						intent.putExtra(AtMoviesTVHttpHelper.KEY_PROGRAM_NAME,
							prog.Name);
						intent.putExtra(AtMoviesTVHttpHelper.KEY_CHANNEL,
							prog.Channel);

						startActivity(intent);
					}
				}
				return true;// True if the click was handled
			}
		};

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// TODO Auto-generated method stub
		// return super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.preview_option, menu);

		//[0.5.0.20] @ 2011-06-01
		//		MenuItem expItem = menu.findItem(R.id.preview_option_expand);
		//		if (expItem != null) {
		//			Log.d(TAG, String.format("bIsExpand = %s", bIsExpand ? "expand"
		//				: "collapse"));
		//			expItem.setIcon(bIsExpand
		//				? android.R.drawable.button_onoff_indicator_on
		//				: android.R.drawable.button_onoff_indicator_off);
		//		}
		return true;
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu)
	{
		//[0.5.0.20] @ 2011-06-01
		MenuItem expItem = menu.findItem(R.id.preview_option_expand);
		if (expItem != null) {
			Log.d(TAG, String.format("bIsExpand = %s", bIsExpand ? "expand"
				: "collapse"));
			expItem.setIcon(bIsExpand
				? android.R.drawable.button_onoff_indicator_on
				: android.R.drawable.button_onoff_indicator_off);
		}

		return super.onMenuOpened(featureId, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Spinner spinner = (Spinner) findViewById(R.id.SpinnerGroup);

		// Handle item selection
		switch (item.getItemId()) {
			case R.id.preview_option_group:
				// spinner.requestFocusFromTouch();
				mSpinnerGroup.performClick();
				return true;
			case R.id.preview_option_expand:
				// Log.d(TAG, String.format("%d", listView
				// .getExpandableListAdapter().getGroupCount()));
				expand_all(!bIsExpand);
				//item.setChecked(bIsExpand);
				//item.setIcon(bIsExpand
				//	? android.R.drawable.button_onoff_indicator_on
				//	: android.R.drawable.button_onoff_indicator_off);
				return true;
			case R.id.preview_prev_day:
				show_loading(true);
				mPreviewDate.add(Calendar.HOUR_OF_DAY, -24);
				send_message(EVENT_MSG_UPDATE_DATA,
					mSpinnerGroup.getSelectedItemPosition(), 1, 100);
				break;
			case R.id.preview_next_day:
				show_loading(true);
				mPreviewDate.add(Calendar.HOUR_OF_DAY, 24);
				send_message(EVENT_MSG_UPDATE_DATA,
					mSpinnerGroup.getSelectedItemPosition(), 1, 100);
				break;
			case R.id.preference:
				Intent intent = new Intent();
				// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setClass(TVGuidePreview.this, TVGuidePreference.class);
				startActivityForResult(intent, 0);
				return true;
				// default:
				// return super.onOptionsItemSelected(item);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();

		// Log.d(TAG, String.format("onResume()"));

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (settings != null) {
			mShowTodayAll = settings.getBoolean("dTVGuide_ShowTodayAll", true);
			// Log.d(TAG, String.format("onResume() %d", mShowTodayAll ? 1 :
			// 0));
			// Editor editor = settings.edit();//start edit
			// editor.putString("dTVGuide_ShowTodayAll", city_id);
			// editor.commit();
			mExpandAll = settings.getBoolean("dTVGuide_ExpendAll", false);

			// int group_index =
			// Integer.parseInt(settings.getString("dTVGuide_DefaultGroup",
			// "5"));
			// Log.d(TAG, String.format("group_index=%d", group_index));
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, String.format("onActivityResult() %d %d", requestCode,
			resultCode));
		switch (requestCode) {
			case 0:
				if (resultCode == Activity.RESULT_OK) {
					show_loading(true);
					send_message(EVENT_MSG_UPDATE_CHANNEL_LIST,
						mSpinnerGroup.getSelectedItemPosition(), 0, 100);
				}
				break;
		}
	}

	protected void show_no_data()
	{
		AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(this);
		MyAlertDialog.setTitle(R.string.app_name);
		MyAlertDialog.setMessage(R.string.no_network);
		MyAlertDialog.setPositiveButton(R.string.open_wifi_setup,
			new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1)
				{
					// TODO Auto-generated method stub
					send_message(EVENT_MSG_ASK_SET_WIFI, 0, 0, 100);
				}

			});
		MyAlertDialog.setNeutralButton(R.string.retry,
			new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1)
				{
					// TODO Auto-generated method stub
					send_message(EVENT_MSG_UPDATE_CHANNEL_LIST,
						mSpinnerGroup.getSelectedItemPosition(), 1, 500);
				}

			});
		MyAlertDialog.setNegativeButton(R.string.abort,
			new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1)
				{
					// TODO Auto-generated method stub
					send_message(EVENT_MSG_ASK_ABORT, 0, 0, 100);
				}

			});
		MyAlertDialog.show();
	}

	// protected Runnable onUpdateContent = new Runnable() {
	// public void run()
	// {
	// send_message(EVENT_MSG_UPDATE_CHANNEL_LIST, mSpinnerGroup
	// .getSelectedItemPosition(), 1, 100);
	// }
	// };
	private Thread.UncaughtExceptionHandler onUncaughtExceptionHandler =
		new Thread.UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread arg0, Throwable arg1)
			{
				// TODO Auto-generated method stub
				Log.e(TAG, "thread exception!" + arg1.getMessage());
				show_no_data();// [1.2.0.10]dolphin++
			}
		};
}
