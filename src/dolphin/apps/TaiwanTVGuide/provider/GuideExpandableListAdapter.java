package dolphin.apps.TaiwanTVGuide.provider;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;

import dolphin.apps.TaiwanTVGuide.R;

public class GuideExpandableListAdapter extends BaseExpandableListAdapter {
    //http://www.iteye.com/topic/620297
    //http://www.cnblogs.com/salam/archive/2010/10/05/1844392.html

    private Context mContext;
    private List<String> mGroup;
    private List<List<String>> mChildren;

    public GuideExpandableListAdapter(Context context, List<String> group,
                                      List<List<String>> children) {
        mContext = context;
        mGroup = group;
        mChildren = children;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mChildren.get(groupPosition).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        String string = mChildren.get(groupPosition).get(childPosition);
        //return getGenericView(string, 64);
        return getGenericView(string, R.layout.listview_program);
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mChildren.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroup.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return mGroup.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String string = mGroup.get(groupPosition);
        //return getGenericView(string, 80);
        return getGenericView(string, R.layout.listview_channel);
    }

    //View stub to create Group/Children 's View
    //public TextView getGenericView(String s, int h)
    public View getGenericView(String s, int layout) {
//		// Layout parameters for the ExpandableListView
//		AbsListView.LayoutParams lp =
//			new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, h);
//		//	new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
//		//			ViewGroup.LayoutParams.WRAP_CONTENT);
        View view = LayoutInflater.from(mContext).inflate(layout, null, false);
        TextView text = (TextView) view.findViewById(android.R.id.text1);
//		TextView text = new TextView(mContext);
//		text.setLayoutParams(lp);
//		// Center the text vertically
//		text.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
//		// Set the text starting position
//		text.setPadding(h - 20, 0, 0, 0);
//		// Set the text size
//		text.setTextSize((float) Math.sqrt(h) * 3.0f);
//		text.setLines(1);
//        text.setTextAppearance(mContext, android.R.attr.textAppearanceMedium);
//        text.setTextAppearance(mContext, attr);
        text.setText(s);
//		return text;

        return view;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
