package dolphin.apps.TaiwanTVGuide.abs;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import dolphin.apps.TaiwanTVGuide.R;
import dolphin.apps.TaiwanTVGuide.provider.AtMoviesTVHttpHelper;
import dolphin.apps.TaiwanTVGuide.provider.ProgramItem;

public class TVGuideProgramFragment extends TVGuideFragment {
    public final static String TAG = "TVGuideProgramFragment";

    private AtMoviesTVHttpHelper mHelper;
    private ProgramItem progItem;

    private LinearLayout mLoadingLayout;
    //TextView tvChannel;
    //TextView tvTitle;
    TextView tvDesc;
    //TextView tvTitleEng;

    private String mUrl;
    private String mName;
    private String mGroup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHelper = new AtMoviesTVHttpHelper(getActivity());

        try {// get URL
            Bundle extras = getActivity().getIntent().getExtras();
            mUrl = extras.getString(AtMoviesTVHttpHelper.KEY_TVDATA);
            if (mUrl == null || mUrl == "") {
                throw new Exception("no url");
            }

            mName = extras.getString(AtMoviesTVHttpHelper.KEY_PROGRAM_NAME);
            if (mName == null || mName == "") {
                throw new Exception("no name");
            }
            getActivity().setTitle(mName);

            mGroup = extras.getString(AtMoviesTVHttpHelper.KEY_GROUP);
            Log.d(TAG, String.format("group = %s", mGroup));
        } catch (Exception e2) {
            Log.e(TAG, "" + e2.getMessage());
            // this.finish();
            //send_message(EVENT_MSG_ASK_ABORT, 0, 0, 100);
        }

        send_message(EVENT_MSG_UPDATE_DESCRIPTION, 0, 0, 10);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.program, container, false);
        if (view != null) {
            TextView tvUrl = (TextView) view.findViewById(R.id.TextViewUrl);
            SpannableString content = new SpannableString(
                    getString(R.string.atmovies_tv));
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            tvUrl.setText(content);
            tvUrl.setVisibility(View.GONE);

            mLoadingLayout = (LinearLayout) view.findViewById(R.id.fullscreen_loading_indicator);
            TextView tvChannel = (TextView) view.findViewById(R.id.TextViewChannel);
            tvChannel.setVisibility(View.GONE);//[0.8.0.26]
            TextView tvTitle = (TextView) view.findViewById(R.id.TextViewTitle);
            tvTitle.setVisibility(View.GONE);//[0.8.0.26]
            tvDesc = (TextView) view.findViewById(R.id.TextViewDescription);
            TextView tvTitleEng = (TextView) view.findViewById(R.id.TextViewTitleEng);
            tvTitleEng.setVisibility(View.GONE);//[0.8.0.26]
        }

        //return super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    // http://code.google.com/p/android/issues/detail?id=19917
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null && outState.isEmpty()) {
            outState.putBoolean("bug:fix", true);
        }
    }

    private void send_message(int what, int arg1, int arg2, int delayMillis) {
        Message msg = mHandler.obtainMessage(what, arg1, arg2);
        if (delayMillis > 0 && msg != null) {
            mHandler.sendMessageDelayed(msg, delayMillis);
        } else {
            mHandler.sendMessage(msg);
        }
    }

    private final static int EVENT_MSG_SHOW_LOADING = 10001;
    private final static int EVENT_MSG_LOAD_DESCRIPTION = 10004;
    //private final static int EVENT_MSG_ASK_ABORT = 10013;
    private final static int EVENT_MSG_UPDATE_DESCRIPTION = 10104;
    //private final static int EVENT_MSG_SHOW_REPLAY = 10105;
    private final static int EVENT_MSG_UPDATE_FRAGMENT = 10201;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //Log.v(TAG, msg.toString());
            switch (msg.what) {
                case EVENT_MSG_SHOW_LOADING:
                    show_loading((msg.arg1 == 1));
                    break;
                case EVENT_MSG_LOAD_DESCRIPTION:
                    show_loading(true);
                    load_detail();
                    break;

                case EVENT_MSG_UPDATE_DESCRIPTION:
                    show_loading(true);
                    // [1.2.0.10]dolphin++ use thread to download detail
                    Thread thread = new Thread(myRunnable);
                    thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
                    thread.start();
                    break;

                case EVENT_MSG_UPDATE_FRAGMENT:
                    if (mDataListenter != null)//[45]++ avoid NullPointerException
                        mDataListenter.onDataReceived(progItem);
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    Runnable myRunnable = new Runnable() {
        public void run() {
            progItem = mHelper.get_program_guide(mUrl);
            send_message(EVENT_MSG_UPDATE_FRAGMENT, 0, 0, 200);
            send_message(EVENT_MSG_LOAD_DESCRIPTION, 0, 0, 100);
        }
    };
    Thread.UncaughtExceptionHandler uncaughtExceptionHandler =
            new Thread.UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread arg0, Throwable arg1) {
                    Log.e(TAG, "thread exception! " + arg1.getMessage());
                    progItem = null;
                    send_message(EVENT_MSG_UPDATE_FRAGMENT, 0, 0, 200);
                    send_message(EVENT_MSG_LOAD_DESCRIPTION, 0, 0, 100);
                }
            };

    private void show_loading(boolean bShown) {
        // Android Market Loading effect
        // http://blog.lytsing.org/archives/46.html
        if (mLoadingLayout != null)//[45]++ avoid NullPointerException
            mLoadingLayout.setVisibility(bShown ? View.VISIBLE : View.GONE);

        final SherlockFragmentActivity activity = getSherlockActivity();
        if (activity != null)//[45]++ avoid NullPointerException
            activity.setSupportProgressBarIndeterminateVisibility(bShown);
    }

    private void load_detail() {
        //tvTitleEng.setText("");// clear the content

        // ProgramItem progItem = mHelper.get_program_guide(url);
        try {
            //String name = getActivity().getIntent().getExtras().getString(
            //	AtMoviesTVHttpHelper.KEY_PROGRAM_NAME);
            //this.setTitle(String.format("%s - %s",
            //	this.getString(R.string.app_name), name));

            // @android:style/Theme.NoTitleBar
            if (progItem != null) {
                if (progItem.Channel != null) {
                    //tvChannel.setText(progItem.Channel);
                    //[0.8.0.26] use ActionBar, hide element
                    getSActionBar().setTitle(progItem.Name);
                    getSActionBar().setSubtitle(progItem.Channel);
                    //tvTitle.setText(name);

                    if (progItem.Description != null
                            && progItem.Description != "")
                        tvDesc.setText(progItem.Description);
                    else
                        tvDesc.setText(R.string.no_data);
                } else {// no channel
                    throw new Exception("no channel");
                }
            } else {// no data
                throw new Exception("no data");
            }
        } catch (Exception e1) {
            Log.e(TAG, "" + e1.getMessage());
            //tvChannel.setText(R.string.no_data);
            //tvTitle.setText("");
            //tvTitleEng.setText("");
            if (tvDesc != null)//[45]++ avoid NullPointerException
                tvDesc.setText(R.string.no_data);
            progItem = null;//clear the item
        }

        show_loading(false);
        // send_message(EVENT_MSG_SHOW_LOADING, 0, 0, 100);
    }

    public interface OnProgramDataListenter {
        public void onDataReceived(ProgramItem progItem);
    }

    private OnProgramDataListenter mDataListenter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mDataListenter = (OnProgramDataListenter) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnProgramDataListenter");
        }
    }

}
