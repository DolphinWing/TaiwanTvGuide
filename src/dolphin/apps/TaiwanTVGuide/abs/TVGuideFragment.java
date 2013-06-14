package dolphin.apps.TaiwanTVGuide.abs;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;

import dolphin.apps.TaiwanTVGuide.provider.ProgramItem;

public class TVGuideFragment extends SherlockFragment
{
	public ActionBar getSActionBar()
	{
		return getSherlockActivity().getSupportActionBar();
	}

	public void updateView(ProgramItem pItem)
	{

	}
}
