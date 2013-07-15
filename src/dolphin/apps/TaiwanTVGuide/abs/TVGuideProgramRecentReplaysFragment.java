package dolphin.apps.TaiwanTVGuide.abs;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import dolphin.apps.TaiwanTVGuide.R;
import dolphin.apps.TaiwanTVGuide.TVGuideWebView;
import dolphin.apps.TaiwanTVGuide.provider.AtMoviesTVHttpHelper;
import dolphin.apps.TaiwanTVGuide.provider.ProgramItem;

public class TVGuideProgramRecentReplaysFragment extends TVGuideFragment {
    public final static String TAG = "TVGuideProgramRecentReplaysFragment";
    private static final String IMDB_URL = "http://www.imdb.com/find?s=all&q=";

    //private TextView mRecentReplays = null;
    private ListView mReplayList = null;
    private TextView mEngTitle = null;
    private ProgramItem progItem = null;
    private View imdbView = null;//[44]++

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.replays, container, false);
        //mRecentReplays = (TextView) view.findViewById(R.id.TextViewData);
        //mRecentReplays.setText(R.string.no_data);
        TextView tvUrl = (TextView) view.findViewById(R.id.TextViewUrl);
        SpannableString content = new SpannableString(
                getString(R.string.atmovies_tv));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        tvUrl.setText(content);
        tvUrl.setOnClickListener(onSourceUrlClicked);

        mReplayList = (ListView) view.findViewById(R.id.listViewTimeList);
        mReplayList.setEmptyView(view.findViewById(R.id.TextViewData));
        mEngTitle = (TextView) view.findViewById(R.id.TextViewTitleEng);
        mEngTitle.setOnClickListener(onEnglishTitleClicked);
        imdbView = view.findViewById(R.id.imdbLink);//[44]++
        //updateTime(null);
        return view;
    }

    // http://code.google.com/p/android/issues/detail?id=19917
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState.isEmpty()) {
            outState.putBoolean("bug:fix", true);
        }
    }

    @Override
    public void updateView(ProgramItem pItem) {
        //Log.d(TAG, "updateTime");
        progItem = pItem;

        List<String> replays = new ArrayList<String>();
        String str = "";
        if (progItem != null && progItem.Replays.size() > 0) {
            Log.d(TAG, "  " + progItem.Channel);
            Log.d(TAG, "  replays: " + progItem.Replays.size());

            for (int i = 0; i < progItem.Replays.size(); i++) {
                Calendar cal = progItem.Replays.get(i);
                str = String.format("%02d/%02d  %02d:%02d",
                        cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.DAY_OF_MONTH),
                        cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
                //str += "\n";
                replays.add(str);
            }
            //Log.d(TAG, str);
            mReplayList.setAdapter(new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_1, replays));
        } else {
            str = getString(R.string.no_data);
            mReplayList.setAdapter(null);
        }

        Bundle bundle = getActivity().getIntent().getExtras();
        String name = bundle.getString(AtMoviesTVHttpHelper.KEY_PROGRAM_NAME);
        if (progItem.Name.length() > name.length()) {
            String engTitle = progItem.Name.substring(name.length()).trim();
            Log.d(TAG, "English Title: " + engTitle);
            // Can I underline text in an android layout?
            // http://stackoverflow.com/a/2394939
            SpannableString content = new SpannableString(engTitle);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);

            if (mEngTitle != null) {//[45]++ avoid NullPointerException
                mEngTitle.setText(content);
                mEngTitle.setTag(engTitle);
                mEngTitle.setVisibility(View.VISIBLE);
            }
        } else if (imdbView != null) {
            imdbView.setVisibility(View.GONE);//[44]++
        }

        //		if (mRecentReplays != null)
        //			mRecentReplays.setText(str);
    }

    private Button.OnClickListener onSourceUrlClicked =
            new Button.OnClickListener() {

                @Override
                public void onClick(View view) {
                    String url = getActivity().getIntent().getExtras()
                            .getString(AtMoviesTVHttpHelper.KEY_TVDATA);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.setData(Uri.parse(String.format("%s/%s",
                            AtMoviesTVHttpHelper.ATMOVIES_TV_URL, url)));
                    //startActivity(i);
                }
            };

    private Button.OnClickListener onEnglishTitleClicked =
            new Button.OnClickListener() {

                @Override
                public void onClick(View view) {
                    String name = view.getTag().toString();

                    Intent i = new Intent();
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //TODO check if IMDB apk is supported
                    i.setData(Uri.parse(String.format("imdb:///find?q=%s",
                            name.replace(" ", "%20"))));
                    if (isCallable(getActivity(), i)) {//[38]++ 2013-05-31
                        startActivity(i);
                        return;//stop here since we already launch the app
                    }

                    String url = IMDB_URL + name.replace(" ", "+");
                    i = new Intent();
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //use our internal activity to handle data
                    i.setClass(getActivity(), TVGuideWebView.class);
                    i.putExtra(AtMoviesTVHttpHelper.KEY_TVDATA, url);
                    i.putExtra(AtMoviesTVHttpHelper.KEY_CHANNEL, progItem.Channel);

                    Bundle bundle = getActivity().getIntent().getExtras();
                    if (bundle != null) {//2013-05-31++ copy from old page
                        i.putExtra(AtMoviesTVHttpHelper.KEY_CHANNEL_ID,
                                bundle.getString(AtMoviesTVHttpHelper.KEY_CHANNEL_ID));
                        i.putExtra(AtMoviesTVHttpHelper.KEY_DATE,
                                bundle.getString(AtMoviesTVHttpHelper.KEY_DATE));
                        i.putExtra(AtMoviesTVHttpHelper.KEY_GROUP,
                                bundle.getString(AtMoviesTVHttpHelper.KEY_GROUP));
                    }

                    if (isCallable(getActivity(), i)) {
                        startActivity(i);
                    }
                }

            };

    /**
     * check if any activity can handle this intent
     *
     * @param context
     * @param intent
     * @return
     */
    public static boolean isCallable(Context context, Intent intent) {
        List<ResolveInfo> list = context.getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
}
