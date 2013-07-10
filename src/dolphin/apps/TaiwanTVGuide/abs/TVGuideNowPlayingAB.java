package dolphin.apps.TaiwanTVGuide.abs;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import dolphin.apps.TaiwanTVGuide.R;
import dolphin.apps.TaiwanTVGuide.provider.AtMoviesTVHttpHelper;

public class TVGuideNowPlayingAB extends TVGuideListActionBarThemedActivity
{
    public final static String TAG = "TVGuideNowPlayingAB";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //this.setTitle("");
        getSActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(Menu.NONE, R.id.playing_option_view_list, 0, R.string.guide_list)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.add(Menu.NONE, R.id.program_option_refresh, 0, R.string.refresh)
                .setIcon(R.drawable.ic_action_refresh)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        //[42]++ add refresh show text if possible
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        final int index = getSActionBar().getSelectedNavigationIndex();
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.playing_option_view_list:
            //Log.d(TAG, "HOME!");
            {
                Intent intent1 = new Intent();
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent1.setClass(TVGuideNowPlayingAB.this,
                    TVGuidePreviewAB.class);
                //[1.2.0.12]dolphin++ use current playing page group
                intent1.putExtra(AtMoviesTVHttpHelper.KEY_GROUP, index);
                startActivity(intent1);
                //close_program();
                this.finish();//[34]++ no need NowPlaying
            }
                return true;
            case R.id.program_option_refresh:
                show_loading(true);
                send_message(EVENT_MSG_UPDATE_DATA, index, 1, 100);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        mShowTodayAll = true;
        mExpandAll = true;
    }

    // @Override
    protected void update_data(int position)
    {
        //super.update_data(position);
        String group_id = getChannelGroup(position).split(" ")[1];
        mChannelList = mHelper.get_showtime_list(group_id);
        mChannelProgramStartIndex = new ArrayList<Integer>();
    }

    @Override
    protected void update_channel_list(int position, boolean bUpdate)
    {
        //super.update_channel_list(position, bUpdate);
        //String group_name = getChannelGroup(position);

        super.update_channel_list(position, false);

        //this.setTitle(String.format("%s - %s (%s)", this
        //		.getString(R.string.app_name), group_name.split(" ")[0], this
        //		.getString(R.string.now_playing)));
    }
}
