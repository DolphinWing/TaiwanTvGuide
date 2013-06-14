package dolphin.apps.TaiwanTVGuide.abs;

import java.util.ArrayList;

import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import dolphin.apps.TaiwanTVGuide.R;

public class TVGuidePreviewAB extends TVGuideListActionBarThemedActivity
{

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //this.setTitle(R.string.guide_list);
        getSActionBar().setDisplayHomeAsUpEnabled(false);
        getSActionBar().setHomeButtonEnabled(false);
    }

    protected void update_data(int position)
    {
        String group_name = getChannelGroup(position);
        //getResources().getStringArray(R.array.channel_group)[position];
        String group_id = group_name.split(" ")[1];
        Log.d(TAG, String.format("%d %s", position, group_id));
        mChannelList = mHelper.get_group_guide_list(getPreviewDate(), group_id);
        mChannelProgramStartIndex = new ArrayList<Integer>();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(Menu.NONE, R.id.preview_prev_day, 0, R.string.previous_day)
                .setIcon(android.R.drawable.ic_media_previous)//.setIcon(R.drawable.ic_menu_back)
                .setShowAsAction(
                    MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.add(Menu.NONE, R.id.program_option_refresh, 0, R.string.refresh)
                .setIcon(R.drawable.ic_menu_refresh)
                .setShowAsAction(
                    MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.add(Menu.NONE, R.id.preview_next_day, 0, R.string.next_day)
                .setIcon(android.R.drawable.ic_media_next)//.setIcon(R.drawable.ic_menu_forward)
                .setShowAsAction(
                    MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return super.onCreateOptionsMenu(menu);//true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.program_option_refresh:
                //send_message(EVENT_MSG_UPDATE_DATA,
                //	getSActionBar().getSelectedNavigationIndex(), 1, 100);
                break;
            case R.id.preview_prev_day:
                addPreviewDate(-1);
                break;
            case R.id.preview_next_day:
                addPreviewDate(1);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        show_loading(true);
        send_message(EVENT_MSG_UPDATE_DATA, getSActionBar().getSelectedNavigationIndex(), 1, 100);
        return true;
    }

    @Override
    protected void onChannelListUpdated()
    {
        this.setTitle(getPreviewDateString());
        this.getSActionBar().setSubtitle(getPreviewDateTimeSpanString());
    }
}
