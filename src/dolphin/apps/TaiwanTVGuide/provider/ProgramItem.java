package dolphin.apps.TaiwanTVGuide.provider;

import java.util.ArrayList;
import java.util.Calendar;

public class ProgramItem
{
	public String Name;
	public Calendar Date;
	public String Url;
	public String Description;
	public String Channel;
	public ArrayList<Calendar> Replays;//[0.5.0.19] @ 2011-06-01 change the Replays from <String> to <Calendar>
	public String ClipLength;

	public ProgramItem()
	{
		init(null, null, null);
	}

	public ProgramItem(String name)
	{
		init(name, null, null);
	}

	public ProgramItem(String name, Calendar cal)
	{
		init(name, cal, null);
	}

	public ProgramItem(String name, Calendar cal, String desc)
	{
		init(name, cal, desc);
	}

	private void init(String name, Calendar cal, String desc)
	{
		Name = name;
		Date = cal;//Date.setTime(cal.getTime());
		Description = desc;

		Replays = new ArrayList<Calendar>();
		//[0.5.0.19] @ 2011-06-01 change the Replays from <String> to <Calendar>

		Channel = null;
		ClipLength = null;
	}

	@Override
	public String toString()
	{
		String str = "";
		try {
			str += "[ " + Name + " " + Date.toString();
            for (Calendar Replay : Replays) {
                str += " " + Replay.toString();
            }
			str += " ]";
		} catch (Exception e) {
			return super.toString();
		}
		return str;
	}
}
