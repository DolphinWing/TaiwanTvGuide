package dolphin.apps.TaiwanTVGuide.provider;

import java.util.ArrayList;

public class ChannelItem
{
	public String Name;
	public String ID;
	public String Group;
	public ArrayList<ProgramItem> Programs;

	public ChannelItem()
	{
		init(null, null, null);
	}

	public ChannelItem(String id)
	{
		init(id, null, null);
	}

	public ChannelItem(String id, String name)
	{
		init(id, name, null);
	}

	public ChannelItem(String id, String name, String group)
	{
		init(id, name, group);
	}

	private void init(String id, String name, String group)
	{
		Name = name;
		ID = id;
		Group = group;
		Programs = new ArrayList<ProgramItem>();
	}
}
