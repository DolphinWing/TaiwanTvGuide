package dolphin.apps.TaiwanTVGuide.v7;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import dolphin.apps.TaiwanTVGuide.MyApplication;
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

    private Tracker mTracker;
    private FirebaseAnalytics mFirebaseAnalytics;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity().getIntent() != null) {
            Bundle bundle = getActivity().getIntent().getExtras();
            if (bundle != null) {
                mProgramName = bundle.getString(AtMoviesTVHttpHelper.KEY_PROGRAM_NAME);
                mUrl = bundle.getString(AtMoviesTVHttpHelper.KEY_TVDATA);
            }
        }

        mTracker = ((MyApplication)getActivity().getApplication()).getDefaultTracker();//[76]++
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_program_info, container, false);
        mChtTitle = (TextView) rootView.findViewById(android.R.id.title);
        if (mChtTitle != null) {
            mChtTitle.setText(mProgramName);
        }
        mEngTitle = (TextView) rootView.findViewById(android.R.id.text1);
        if (mEngTitle != null) {
            mEngTitle.setText("");
        }
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
            if (mChtTitle != null) {
                mChtTitle.setText(mProgramName);
            }
            if (mEngTitle != null) {
                if (programItem.Name != null && programItem.Name.length() > mProgramName.length()) {
                    final String title = programItem.Name.substring(mProgramName.length()).trim();
                    mEngTitle.setText(title);
                    mEngTitle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Action");
                            mFirebaseAnalytics.logEvent("show_imdb", bundle);
                            Utils.startImdbActivity(getActivity(), title);
                        }
                    });
                } else {
                    mEngTitle.setVisibility(View.GONE);

                }
            }

            if (mDescription != null) {
                if (programItem.Description != null && !programItem.Description.isEmpty()) {
                    //Log.d(TAG, "desc: " + programItem.Description);
                    mDescription.setText(Html.fromHtml(programItem.Description));
                } else {
                    mDescription.setText(R.string.no_data);
                }
            }

            if (mReplays != null) {
                if (programItem.Replays != null && programItem.Replays.size() > 0) {
                    Log.v(TAG, "replay: " + programItem.Replays.size());
                    mReplays.setVisibility(View.VISIBLE);
                    mReplays.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Bundle bundle = new Bundle();
                            //bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "");
                            //bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "show replay dialog");
                            bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Action");
                            //mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
                            mFirebaseAnalytics.logEvent("show_replay_dialog", bundle);

                            showReplayDialog(programItem);
                        }
                    });
                } else {
                    Log.w(TAG, "no replay");
                    mReplays.setVisibility(View.GONE);
                }
            }

            if (mGoToUrl != null) {
                if (mUrl != null && !mUrl.isEmpty()) {
                    mGoToUrl.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Utils.startBrowserActivity(getActivity(),
                                    AtMoviesTVHttpHelper.ATMOVIES_TV_URL + "/" + mUrl);
                        }
                    });
                    mGoToUrl.setVisibility(View.GONE);//[69]++ hide this since we can't open the url
                } else {
                    mGoToUrl.setVisibility(View.GONE);
                }
            }
        } else {
            Log.e(TAG, "no data");//TODO: show no data
            Toast.makeText(getActivity(), "no data...", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }

    @Override
    public void onHttpTimeout() {

    }

    private void showReplayDialog(ProgramItem programItem) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < programItem.Replays.size(); i++) {
            Calendar cal = programItem.Replays.get(i);
            String str = String.format(Locale.TAIWAN,"%02d/%02d  %02d:%02d",
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
            list.add(str);
        }

        //[76]++
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("replay list")
                .build());

        final ProgramItem item = programItem;
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.recent_replays)
                .setAdapter(new ArrayAdapter<>(getActivity(),
                                android.R.layout.simple_list_item_1, list),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //click recent replay and add to calendar
                                item.Date = item.Replays.get(i);
                                Utils.startAddingCalendar(getActivity(), null, item);

                                //[76]++
                                mTracker.send(new HitBuilders.EventBuilder()
                                        .setCategory("Action")
                                        .setAction("add to calendar")
                                        .build());

                                Bundle bundle = new Bundle();
                                //bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "");
                                //bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "add to calendar");
                                bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Action");
                                //mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
                                mFirebaseAnalytics.logEvent("add_to_calendar", bundle);
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
