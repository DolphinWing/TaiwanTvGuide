package dolphin.apps.TaiwanTVGuide;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import dolphin.apps.TaiwanTVGuide.provider.AtMoviesTVHttpHelper;
import dolphin.apps.TaiwanTVGuide.provider.ProgramItem;

public class TVGuideProgram extends Activity
{
	private static final String TAG = "TVGuideProgram";
	private AtMoviesTVHttpHelper mHelper;
	private LinearLayout mLoadingLayout;

	private ProgramItem progItem;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.program);

		// TODO Auto-generated method stub
		mLoadingLayout =
			(LinearLayout) findViewById(R.id.fullscreen_loading_indicator);
		mHelper = new AtMoviesTVHttpHelper(this);
		//mHelper._init(this);

		// String url =
		// getIntent().getExtras().getString(AtMoviesTVHttpHelper.KEY_TVDATA);
		// Toast.makeText(this, url, Toast.LENGTH_SHORT).show();
		send_message(EVENT_MSG_UPDATE_DESCRIPTION, 0, 0, 10);
		TextView tvUrl = (TextView) findViewById(R.id.TextViewUrl);
		SpannableString content = new SpannableString(
				getString(R.string.atmovies_tv));
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		tvUrl.setText(content);
		tvUrl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				String url = getIntent().getExtras().getString(
					AtMoviesTVHttpHelper.KEY_TVDATA);
				// Intent i = new Intent(Intent.ACTION_VIEW);
				// i.setData(Uri.parse(String.format("%s/%s",
				// AtMoviesTVHttpHelper.ATMOVIES_TV_URL, url)));
				Intent i = new Intent();
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.setClass(TVGuideProgram.this, TVGuideWebView.class);
				i.putExtra(AtMoviesTVHttpHelper.KEY_TVDATA, url);
				i.putExtra(AtMoviesTVHttpHelper.KEY_CHANNEL, progItem.Channel);
				i.putExtra(
					AtMoviesTVHttpHelper.KEY_CHANNEL_ID,
					getIntent().getExtras().getString(
						AtMoviesTVHttpHelper.KEY_CHANNEL_ID));
				i.putExtra(AtMoviesTVHttpHelper.KEY_DATE, getIntent()
						.getExtras().getString(AtMoviesTVHttpHelper.KEY_DATE));
				i.putExtra(AtMoviesTVHttpHelper.KEY_GROUP, getIntent()
						.getExtras().getString(AtMoviesTVHttpHelper.KEY_GROUP));
				startActivity(i);
				// ((TextView)v).setText(url);
			}
		});
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
	private final static int EVENT_MSG_LOAD_DESCRIPTION = 10004;
	private final static int EVENT_MSG_ASK_ABORT = 10013;
	private final static int EVENT_MSG_UPDATE_DESCRIPTION = 10104;
	private final static int EVENT_MSG_SHOW_REPLAY = 10105;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what) {
				case EVENT_MSG_SHOW_LOADING:
					show_loading((msg.arg1 == 1));
					break;
				case EVENT_MSG_LOAD_DESCRIPTION:
					show_loading(true);
					load_detail();
					break;

				case EVENT_MSG_ASK_ABORT:
					close_program();
					break;

				case EVENT_MSG_UPDATE_DESCRIPTION:
					show_loading(true);
					// [1.2.0.10]dolphin++ use thread to download detail
					Thread thread = new Thread(new Runnable() {
						public void run()
						{
							String url = null, name = null, group = null;
							try {// get URL
								url = getIntent().getExtras().getString(
									AtMoviesTVHttpHelper.KEY_TVDATA);
								if (url == null || url == "") {
									throw new Exception("no url");
								}

								name = getIntent().getExtras().getString(
									AtMoviesTVHttpHelper.KEY_PROGRAM_NAME);
								if (name == null || name == "") {
									throw new Exception("no name");
								}

								group = getIntent().getExtras().getString(
									AtMoviesTVHttpHelper.KEY_GROUP);
								Log.d(TAG, String.format("group = %s", group));
							}
							catch (Exception e2) {
								Log.e(TAG, e2.getMessage());
								// this.finish();
								send_message(EVENT_MSG_ASK_ABORT, 0, 0, 100);
							}
							//Log.d(TAG,
							//	group
							//		+ " "
							//		+ url.substring(url.lastIndexOf("/") + 1));

							progItem = mHelper.get_program_guide(url);
							send_message(EVENT_MSG_LOAD_DESCRIPTION, 0, 0, 100);
						}
					});
					thread
							.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

								@Override
								public void uncaughtException(Thread arg0,
										Throwable arg1)
								{
									// TODO Auto-generated method stub
									Log.e(TAG, "thread exception! "
										+ arg1.getMessage());
									progItem = null;
									send_message(EVENT_MSG_LOAD_DESCRIPTION, 0,
										0, 100);
								}
							});
					thread.start();
					break;
				case EVENT_MSG_SHOW_REPLAY://[0.5.0.19] @ 2011-06-01 add replay
					//Log.d(TAG, "EVENT_MSG_SHOW_REPLAY");
					show_replays();
					break;
				default:
					break;
			}
			super.handleMessage(msg);
		}
	};

	private void show_loading(boolean bShown)
	{
		// Android Market Loading effect
		// http://blog.lytsing.org/archives/46.html
		mLoadingLayout.setVisibility(bShown ? View.VISIBLE : View.GONE);
	}

	protected void close_program()
	{
		this.finish();
	}

	private void load_detail()
	{
		TextView tvChannel = (TextView) findViewById(R.id.TextViewChannel);
		TextView tvTitle = (TextView) findViewById(R.id.TextViewTitle);
		TextView tvDesc = (TextView) findViewById(R.id.TextViewDescription);
		TextView tvTitleEng = (TextView) findViewById(R.id.TextViewTitleEng);
		tvTitleEng.setText("");// clear the content

		// ProgramItem progItem = mHelper.get_program_guide(url);
		try {
			String name = getIntent().getExtras().getString(
				AtMoviesTVHttpHelper.KEY_PROGRAM_NAME);
			this.setTitle(String.format("%s - %s",
				this.getString(R.string.app_name), name));
			// @android:style/Theme.NoTitleBar
			if (progItem != null) {
				if (progItem.Channel != null) {
					tvChannel.setText(progItem.Channel);
					tvTitle.setText(name);
					if (progItem.Name != null) {
						if (progItem.Name.length() > name.length()) {
							String engTitle = progItem.Name.substring(
								name.length()).trim();
							Log.d(TAG, "English Title: " + engTitle);

							// Can I underline text in an android layout?
							// http://stackoverflow.com/questions/2394935/can-i-underline-text-in-an-android-layout
							SpannableString content = new SpannableString(
									engTitle);
							content.setSpan(new UnderlineSpan(), 0,
								content.length(), 0);

							tvTitleEng.setText(content);
							tvTitleEng
									.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View v)
										{
											// TODO Auto-generated method stub
											String url =
												"http://www.imdb.com/find?s=all&q="
													+ ((TextView) v).getText()
															.toString()
															.replace(" ", "+");
											// Intent i = new Intent(
											// Intent.ACTION_VIEW);
											// i.setData(Uri.parse(url));
											Intent i = new Intent();
											i
													.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
											i.setClass(TVGuideProgram.this,
												TVGuideWebView.class);
											i
													.putExtra(
														AtMoviesTVHttpHelper.KEY_TVDATA,
														url);
											startActivity(i);
										}
									});
							tvTitleEng.setVisibility(View.VISIBLE);

						}
					}

					if (progItem.Description != null
						&& progItem.Description != "")
						tvDesc.setText(progItem.Description);
					else
						tvDesc.setText(R.string.no_data);
				}
				else {// no channel
					throw new Exception("no channel");
				}
			}
			else {// no data
				throw new Exception("no data");
			}
		}
		catch (Exception e1) {
			Log.e(TAG, e1.getMessage());
			tvChannel.setText(R.string.no_data);
			tvTitle.setText("");
			tvTitleEng.setText("");
			tvDesc.setText("");
			progItem = null;//clear the item
		}

		show_loading(false);
		// send_message(EVENT_MSG_SHOW_LOADING, 0, 0, 100);
	}

	private void show_replays()
	{
		if (progItem != null) {
			Log.d(TAG, "EVENT_MSG_SHOW_REPLAY progItem != null");

			AlertDialog.Builder builder =
				new AlertDialog.Builder(this);
			builder.setTitle(R.string.recent_replays);
			Log.d(TAG, String.format("replay = %d", progItem.Replays.size()));

			if (progItem.Replays.size() > 0) {
				Log.d(TAG, "builder.setItems");

				String[] items = new String[progItem.Replays.size()];
				for (int i = 0; i < progItem.Replays.size(); i++) {
					Calendar cal = progItem.Replays.get(i);
					items[i] =
						String.format("%02d/%02d %02d:%02d",
							cal.get(Calendar.MONTH) + 1, cal
									.get(Calendar.DAY_OF_MONTH), cal
									.get(Calendar.HOUR_OF_DAY),
							cal.get(Calendar.MINUTE));
				}

				//Log.d(TAG, String.format("builder.setItems"));
				builder.setItems(items,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item)
				{
					Log.d(TAG, String.format("item %d", item));
				}
					});
				//AlertDialog alert = builder.create();
			}
			else {
				Log.d(TAG, String.format("builder.setMessage"));
				builder.setMessage(R.string.no_data);
			}
			builder.show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// TODO Auto-generated method stub
		// return super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.program_option, menu);
		return true;//[0.5.0.19] @ 2011-06-01 add option menu
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu)
	{
		//[0.5.0.21] @ 2011-06-01
		MenuItem rItem = menu.findItem(R.id.program_option_refresh);
		if (rItem != null) {
			Log.d(TAG, String.format("refresh enable = %s", (progItem == null)
				? "yes" : "no"));
			rItem.setEnabled((progItem == null));
		}

		return super.onMenuOpened(featureId, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.program_option_replay:
				send_message(EVENT_MSG_SHOW_REPLAY, 0, 0, 100);
				break;
			case R.id.program_option_refresh:
				send_message(EVENT_MSG_UPDATE_DESCRIPTION, 0, 0, 10);
				break;
		}

		return super.onOptionsItemSelected(item);
	}
}
