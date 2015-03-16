package dolphin.apps.TaiwanTVGuide.v7;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import java.util.ArrayList;
import java.util.List;

import dolphin.apps.TaiwanTVGuide.R;
import dolphin.apps.TaiwanTVGuide.provider.AtMoviesTVHttpHelper;
import dolphin.apps.TaiwanTVGuide.provider.ProgramItem;

public class ProgramInfoActivity extends ActionBarActivity implements OnHttpProvider, OnHttpListener {
    private final static String TAG = "ProgramInfoActivity";

    private View mLoadingPane;

    private AtMoviesTVHttpHelper mHelper;
    private String mUrl, mName, mGroup;


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
        mLoadingPane.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;//do nothing
            }
        });

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
                        download(mUrl, mName, mGroup);
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterOnHttpListener(this);
        super.onDestroy();
    }

    private void download(String url, String name, String group) {
        for (OnHttpListener listener : mOnHttpListener) {
            listener.onHttpStart();
        }

        final String dataUrl = url;
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ProgramItem programItem = mHelper.get_program_guide(dataUrl);
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
        setSupportProgressBarIndeterminateVisibility(loading);
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
        download(mUrl, mName, mGroup);
    }
}
