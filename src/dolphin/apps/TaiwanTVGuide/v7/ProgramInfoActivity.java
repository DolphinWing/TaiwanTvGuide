package dolphin.apps.TaiwanTVGuide.v7;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

import dolphin.apps.TaiwanTVGuide.MyApplication;
import dolphin.apps.TaiwanTVGuide.R;
import dolphin.apps.TaiwanTVGuide.provider.AtMoviesTVHttpHelper;
import dolphin.apps.TaiwanTVGuide.provider.ProgramItem;

public class ProgramInfoActivity extends AppCompatActivity implements OnHttpProvider, OnHttpListener {
    private final static String TAG = "ProgramInfoActivity";

    private View mLoadingPane;

    private AtMoviesTVHttpHelper mHelper;
    private String mUrl, mName, mGroup, mChannelId;

    private Tracker mTracker;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_program_info);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ProgramInfoFragment())
                    .commit();
        }

        mLoadingPane = findViewById(R.id.fullscreen_loading_indicator);
        if (mLoadingPane != null) {
            mLoadingPane.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return true;//do nothing
                }
            });
        }

        mHelper = new AtMoviesTVHttpHelper(this);

        registerOnHttpListener(this);

        if (getIntent() != null) {
            final Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mUrl = bundle.getString(AtMoviesTVHttpHelper.KEY_TVDATA);
                        mName = bundle.getString(AtMoviesTVHttpHelper.KEY_PROGRAM_NAME);
                        mGroup = bundle.getString(AtMoviesTVHttpHelper.KEY_GROUP);
                        mChannelId = bundle.getString(AtMoviesTVHttpHelper.KEY_CHANNEL_ID);
                        download(mUrl, mName, mGroup, mChannelId);

                        if (mTracker != null) {
                            HitBuilders.ScreenViewBuilder builder = new HitBuilders.ScreenViewBuilder()
                                    .setCustomDimension(1, mGroup)
                                    .setCustomDimension(2, mChannelId);
                            mTracker.send(builder.build());
                            Log.d(TAG, "send custom dimension with screen view");
                            Log.d(TAG, "  group: " + mGroup);
                            Log.d(TAG, "  channel: " + mChannelId);
                            mTracker.setScreenName(null);//clear the screen view
                        }

                        Bundle bundle = new Bundle();
                        //bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "");
                        //bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Program Info");
                        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Action");
                        bundle.putString("group", mGroup);
                        bundle.putString("channel", mChannelId);
                        if (mFirebaseAnalytics != null) {
                            //mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
                            mFirebaseAnalytics.logEvent("program_info", bundle);
                        }
                    }
                });
            }
        }

        MyApplication application = (MyApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("Program Info");
        //mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void onDestroy() {
        unregisterOnHttpListener(this);
        super.onDestroy();
    }

    private void download(String url, String name, String group, String channelId) {
        for (OnHttpListener listener : mOnHttpListener) {
            listener.onHttpStart();
        }

        final String dataUrl = url;
        new Thread(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                final ProgramItem programItem = mHelper.get_program_guide(dataUrl);
                long cost = System.currentTimeMillis() - start;
                Log.d(TAG, String.format("download info cost %d ms", cost));
                HitBuilders.TimingBuilder builder = new HitBuilders.TimingBuilder()
                        .setCategory("Network")
                        .setVariable("Download")
                        .setLabel("detail")
                        .setValue(cost);
                mTracker.send(builder.build());//[76]++

                Bundle bundle = new Bundle();
                //bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "Program Info");
                bundle.putString("group", mGroup);
                bundle.putString("channel", mChannelId);
                //mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM_LIST, bundle);
                //mFirebaseAnalytics.logEvent("Action/Program Info", bundle);

                ProgramInfoActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (OnHttpListener listener : mOnHttpListener) {
                            listener.onHttpUpdated(programItem);
                        }
                    }
                });
            }
        }).start();
    }

    private void setLoading(boolean loading) {
        //setSupportProgressBarIndeterminateVisibility(loading);
        if (mLoadingPane != null) {
            mLoadingPane.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onHttpStart() {
        setLoading(true);
    }

    @Override
    public void onHttpUpdated(Object data) {
        setLoading(false);
    }

    @Override
    public void onHttpTimeout() {
        setLoading(false);
    }

    private List<OnHttpListener> mOnHttpListener = new ArrayList<>();

    @Override
    public void registerOnHttpListener(OnHttpListener listener) {
        mOnHttpListener.add(listener);
    }

    @Override
    public void unregisterOnHttpListener(OnHttpListener listener) {
        mOnHttpListener.remove(listener);
    }

    @Override
    public void refresh() {
        download(mUrl, mName, mGroup, mChannelId);
    }
}
