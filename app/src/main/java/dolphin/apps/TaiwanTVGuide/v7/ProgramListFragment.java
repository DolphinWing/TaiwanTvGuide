package dolphin.apps.TaiwanTVGuide.v7;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tonicartos.superslim.LayoutManager;

import java.util.ArrayList;

import dolphin.apps.TaiwanTVGuide.MyApplication;
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

    private OnHttpProvider mProvider;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnHttpProvider) {
            mProvider = (OnHttpProvider) context;
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
        if (mRecyclerView != null) {
            mRecyclerView.setLayoutManager(new LayoutManager(getActivity()));
            mRecyclerView.setAdapter(new ProgramListAdapter(getActivity(),
                    new ArrayList<ChannelItem>()));
        }
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
        //mEmptyView.setVisibility(View.GONE);
        Log.d(TAG, "onHttpStart");
    }

    @Override
    public void onHttpUpdated(Object data) {
        //Log.d(TAG, "onHttpUpdated: " + data);
        //mRecyclerView.setVisibility(View.VISIBLE);
        if (data != null && getActivity() != null) {
            ArrayList<ChannelItem> channelItems = (ArrayList<ChannelItem>) data;
            MyApplication application = (MyApplication) getActivity().getApplication();
            ProgramListAdapter adapter = new ProgramListAdapter(getActivity(), channelItems,
                    !application.isPreviewDateToday() || application.isShowAllPrograms());
            //mAdapter.setHeaderDisplay(LayoutManager.LayoutParams.HEADER_STICKY);
            if (mRecyclerView != null) {
                mRecyclerView.setAdapter(adapter);
            }
            //mEmptyView.setVisibility(mAdapter != null && mAdapter.getItemCount() > 0
            //        ? View.VISIBLE : View.GONE);
        } else {
            //mEmptyView.setVisibility(View.VISIBLE);
            Log.w(TAG, "no data!");
        }
    }

    @Override
    public void onHttpTimeout() {
        //mRecyclerView.setVisibility(View.INVISIBLE);
        //mEmptyView.setVisibility(View.VISIBLE);
        Log.w(TAG, "timeout!");
    }
}
