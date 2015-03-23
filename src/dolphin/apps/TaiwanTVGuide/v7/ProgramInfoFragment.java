package dolphin.apps.TaiwanTVGuide.v7;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import dolphin.apps.TaiwanTVGuide.R;
import dolphin.apps.TaiwanTVGuide.provider.AtMoviesTVHttpHelper;
import dolphin.apps.TaiwanTVGuide.provider.ProgramItem;
import dolphin.apps.TaiwanTVGuide.provider.Utils;

/**
 * Created by dolphin on 2015/03/15.
 */
public class ProgramInfoFragment extends Fragment implements OnHttpListener {
    private final static String TAG = "ProgramInfoFragment";

    private TextView mChtTitle;
    private TextView mEngTitle;
    private TextView mDescription;
    private View mReplays;
    private View mGoToUrl;

    private OnHttpProvider mProvider;
    private String mProgramName;
    private String mUrl;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity().getIntent() != null) {
            Bundle bundle = getActivity().getIntent().getExtras();
            if (bundle != null) {
                mProgramName = bundle.getString(AtMoviesTVHttpHelper.KEY_PROGRAM_NAME);
                mUrl = bundle.getString(AtMoviesTVHttpHelper.KEY_TVDATA);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_program_info, container, false);
        mChtTitle = (TextView) rootView.findViewById(android.R.id.title);
        mChtTitle.setText(mProgramName);
        mEngTitle = (TextView) rootView.findViewById(android.R.id.text1);
        mEngTitle.setText("");
        mDescription = (TextView) rootView.findViewById(android.R.id.message);
        mGoToUrl = rootView.findViewById(android.R.id.button1);
        mReplays = rootView.findViewById(android.R.id.button2);
        return rootView;
    }

    @Override
    public void onHttpStart() {

    }

    @Override
    public void onHttpUpdated(Object data) {
        if (data != null && data instanceof ProgramItem) {
            final ProgramItem programItem = (ProgramItem) data;
            mChtTitle.setText(mProgramName);

            if (programItem.Name.length() > mProgramName.length()) {
                final String title = programItem.Name.substring(mProgramName.length()).trim();
                mEngTitle.setText(title);
                mEngTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Utils.startImdbActivity(getActivity(), title);
                    }
                });
            } else {
                mEngTitle.setVisibility(View.GONE);
            }

            if (programItem.Description != null && !programItem.Description.isEmpty()) {
                mDescription.setText(programItem.Description);
            } else {
                mDescription.setText(R.string.no_data);
            }

            if (programItem.Replays != null && programItem.Replays.size() > 0) {
                mReplays.setVisibility(View.VISIBLE);
                mReplays.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showReplayDialog(programItem);
                    }
                });
            } else {
                mReplays.setVisibility(View.GONE);
            }

            if (mUrl != null && !mUrl.isEmpty()) {
                mGoToUrl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(AtMoviesTVHttpHelper.ATMOVIES_TV_URL + "/" +mUrl));
                        startActivity(intent);
                    }
                });
                mGoToUrl.setVisibility(View.GONE);//[69]++ hide this since we can't open the url
            } else {
                mGoToUrl.setVisibility(View.GONE);
            }
        } else {
            //TODO: show no data
        }
    }

    @Override
    public void onHttpTimeout() {

    }

    private void showReplayDialog(ProgramItem programItem) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < programItem.Replays.size(); i++) {
            Calendar cal = programItem.Replays.get(i);
            String str = String.format("%02d/%02d  %02d:%02d",
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
            list.add(str);
        }

        final ProgramItem item = programItem;
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.recent_replays)
                .setAdapter(new ArrayAdapter<String>(getActivity(),
                                android.R.layout.simple_list_item_1, list),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //click recent replay and add to calendar
                                item.Date = item.Replays.get(i);
                                Utils.startAddingCalendar(getActivity(), null, item);
                            }
                        })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing, just dismiss dialog
                    }
                })
                .show();
    }
}
