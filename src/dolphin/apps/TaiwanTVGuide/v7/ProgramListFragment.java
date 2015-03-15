package dolphin.apps.TaiwanTVGuide.v7;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tonicartos.superslim.LayoutManager;

import java.util.ArrayList;
import dolphin.apps.TaiwanTVGuide.R;
import dolphin.apps.TaiwanTVGuide.provider.ChannelItem;

/**
 * Created by dolphin on 2015/03/14.
 * <p/>
 * use RecyclerView try
 */
public class ProgramListFragment extends Fragment implements OnHttpListener {
    private final static String TAG = "ProgramListFragment";
    private RecyclerView mRecyclerView;
    private ProgramListAdapter mAdapter;

    private OnHttpProvider mProvider;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof OnHttpProvider) {
            mProvider = (OnHttpProvider) activity;
            mProvider.registerOnHttpListener(this);
        }
    }

    @Override
    public void onDetach() {
        if (mProvider != null) {
            mProvider.unregisterOnHttpListener(this);
        }
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_program_list, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LayoutManager(getActivity()));
        return rootView;
    }

//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
////        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
////        mRecyclerView.setLayoutManager(new LayoutManager(getActivity()));
////        ItemDecorator decor = new ItemDecorator.Builder(getActivity())
////                .setDrawableBelow(R.drawable.divider_horizontal, ItemDecorator.INTERNAL)
////                .decorateSlm(LinearSLM.ID)
////                .decorateSlm(GridSLM.ID)
////                .build();
////        mRecyclerView.addItemDecoration(decor);
//    }

    @Override
    public void onHttpStart() {

    }

    @Override
    public void onHttpUpdated(Object data) {
        if (data != null) {
            ArrayList<ChannelItem> channelItems = (ArrayList<ChannelItem>) data;
            mAdapter = new ProgramListAdapter(getActivity(), channelItems);
            //mAdapter.setHeaderDisplay(LayoutManager.LayoutParams.HEADER_STICKY);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            //TODO: show no data
        }
    }

    @Override
    public void onHttpTimeout() {

    }
}
