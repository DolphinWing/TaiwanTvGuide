package dolphin.apps.TaiwanTVGuide;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import dolphin.apps.TaiwanTVGuide.provider.AtMoviesTVHttpHelper;

public class TVGuidePlaying extends TVGuidePreview
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mShowTodayAll = true;
		mExpandAll = true;
	}

	@Override
	protected void onResume()
	{
		// TODO Auto-generated method stub
		super.onResume();

		mShowTodayAll = true;
		mExpandAll = true;
	}

	@Override
	protected void update_data(int position)
	{
		// TODO Auto-generated method stub
		//super.update_data(position);
		String group_name =
			getResources().getStringArray(R.array.channel_group)[position];
		String group_id = group_name.split(" ")[1];
		mChannelList = mHelper.get_showtime_list(group_id);
		mChannelProgramStartIndex = new ArrayList<Integer>();
	}

	@Override
	protected void update_channel_list(int position, boolean bUpdate)
	{
		// TODO Auto-generated method stub
		//super.update_channel_list(position, bUpdate);
		String group_name =
			getResources().getStringArray(R.array.channel_group)[position];
		//		String group_id = group_name.split(" ")[1];
		//		//Log.d(TAG, group_id);
		//		mChannelList = mHelper.get_showtime_list(group_id);

		super.update_channel_list(position, false);

		this.setTitle(String.format("%s - %s (%s)", this
				.getString(R.string.app_name), group_name.split(" ")[0], this
				.getString(R.string.now_playing)));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// TODO Auto-generated method stub
		// return super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.now_playing_option, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		//Spinner spinner = (Spinner) findViewById(R.id.SpinnerGroup);

		// Handle item selection
		switch (item.getItemId()) {
			case R.id.preview_option_group:
				// spinner.requestFocusFromTouch();
				mSpinnerGroup.performClick();
				return true;

			case R.id.playing_option_view_list:
				Intent intent1 = new Intent();
				//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent1.setClass(TVGuidePlaying.this, TVGuidePreview.class);
				//[1.2.0.12]dolphin++ use current playing page group
				intent1.putExtra(AtMoviesTVHttpHelper.KEY_GROUP, mSpinnerGroup
						.getSelectedItemPosition());
				startActivity(intent1);
				return true;

			case R.id.preference:
				Intent intent2 = new Intent();
				//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent2.setClass(TVGuidePlaying.this, TVGuidePreference.class);
				startActivityForResult(intent2, 0);
				return true;
				//default:
				//	return super.onOptionsItemSelected(item);
		}

		return super.onOptionsItemSelected(item);
	}

}
