package dolphin.apps.TaiwanTVGuide.v7;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tonicartos.superslim.GridSLM;
import com.tonicartos.superslim.LinearSLM;

import java.util.ArrayList;
import java.util.Calendar;

import dolphin.apps.TaiwanTVGuide.R;
import dolphin.apps.TaiwanTVGuide.provider.ChannelItem;
import dolphin.apps.TaiwanTVGuide.provider.ProgramItem;

/**
 * Created by dolphin on 2015/03/14.
 * Adapter for RecyclerView
 */
public class ProgramListAdapter extends RecyclerView.Adapter<ProgramViewHolder>
        implements ProgramViewHolder.OnItemClickListener {
    private final static String TAG = "ProgramListAdapter";

    private final Context mContext;
    private final ArrayList<MyItem> mItems;

//    private int mHeaderDisplay;

    private static final int VIEW_TYPE_HEADER = 0;

    private static final int VIEW_TYPE_CONTENT = 1;

    private static class MyItem {
        public boolean isHeader;
        //public Object object;
        public ChannelItem channel;
        public ProgramItem program;
        public int sectionFirstPosition;
        public int sectionManager;

        public MyItem(boolean header, ChannelItem c, ProgramItem p, int pos, int mgr) {
            isHeader = header;
            //object = obj;
            channel = c;
            program = p;
            sectionFirstPosition = pos;
            sectionManager = mgr;
        }
    }

    public ProgramListAdapter(Context context, ArrayList<ChannelItem> items) {
        mContext = context;
        mItems = new ArrayList<>();

        int sectionManager = -1;
        int headerCount = 0, itemCount = 0;
        int sectionFirstPosition = 0;
        for (ChannelItem item : items) {
            sectionManager = (sectionManager + 1) % 2;
            sectionFirstPosition = headerCount + itemCount;
            mItems.add(new MyItem(true, item, null, sectionFirstPosition, sectionManager));
            headerCount++;
            for (ProgramItem p : item.Programs) {
                itemCount++;
                mItems.add(new MyItem(false, item, p, sectionFirstPosition, sectionManager));
            }
        }
    }

    @Override
        public int getItemViewType(int position) {
        return mItems.get(position).isHeader ? VIEW_TYPE_HEADER : VIEW_TYPE_CONTENT;
    }

    @Override
    public ProgramViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_HEADER) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listview_channel, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listview_program, parent, false);
        }
        return new ProgramViewHolder(view);
    }

    private String getChannelText(ChannelItem item) {
        return item.Name;
    }

    private String getProgramText(ProgramItem item) {
        return String.format("%02d:%02d  %s",
                item.Date.get(Calendar.HOUR_OF_DAY),
                item.Date.get(Calendar.MINUTE), item.Name);
    }

    private String getItemText(MyItem item) {
        return item.isHeader ? getChannelText(item.channel) : getProgramText(item.program);
    }

    @Override
    public void onBindViewHolder(ProgramViewHolder holder, int position) {
        final MyItem item = mItems.get(position);
        final View itemView = holder.itemView;

        holder.bindItem(getItemText(item), item);
        if (item.isHeader) {
            holder.setOnItemClickListener(null);
        } else {
            itemView.setTag(item);
            holder.setOnItemClickListener(this);
        }

//        final LayoutManager.LayoutParams params = (LayoutManager.LayoutParams) itemView.getLayoutParams();
//        //params.section = item.section;
//        params.setFirstPosition(item.sectionFirstPosition);
//        params.setSlm(LinearSLM.ID);
//        itemView.setLayoutParams(params);
        final GridSLM.LayoutParams lp = new GridSLM.LayoutParams(itemView.getLayoutParams());
//        if (item.isHeader) {
//            lp.headerDisplay = mHeaderDisplay;
//        }
//        if (position == item.sectionFirstPosition) {
//            lp.setSlm(item.sectionManager == 0 ? LinearSLM.ID : GridSLM.ID);
//        }
        lp.setSlm(LinearSLM.ID);
        lp.setFirstPosition(item.sectionFirstPosition);
        itemView.setLayoutParams(lp);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

//    public void setHeaderDisplay(int headerDisplay) {
//        mHeaderDisplay = headerDisplay;
//        notifyHeaderChanges();
//    }
//
//    private void notifyHeaderChanges() {
//        for (int i = 0; i < mItems.size(); i++) {
//            if (mItems.get(i).isHeader) {
//                notifyItemChanged(i);
//            }
//        }
//    }

    @Override
    public void onItemClick(View view) {
        //Log.d(TAG, view.getTag().toString());
        MyItem item = (MyItem) view.getTag();
        if (item != null) {
            ProgramListActivity.startProgramActivity(mContext, item.program.Date,
                    item.channel, item.program);
        }
    }

}
