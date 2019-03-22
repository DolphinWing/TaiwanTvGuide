package dolphin.apps.TaiwanTVGuide;

import android.app.Application;
import android.text.format.DateUtils;

import com.google.android.gms.analytics.Tracker;

import java.util.Calendar;

/**
 * Created by jimmyhu on 2016/5/10.
 * <p/>
 * For Analytics
 */
public class MyApplication extends Application {
    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();

        AnalyticsTrackers.initialize(this);
    }

    //https://developers.google.com/analytics/devguides/collection/android/v4/#application
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            mTracker = AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP);
        }
        return mTracker;
    }

    private boolean mShowAll = false;

    public boolean isShowAllPrograms() {
        return mShowAll;
    }

    public void setShowAllPrograms(boolean visible) {
        mShowAll = visible;
    }

    private Calendar mPreviewDate;

    public void setPreviewDate(Calendar cal) {
        mPreviewDate = cal;
    }

    public Calendar getPreviewDate() {
        return mPreviewDate;
    }

    public boolean isPreviewDateToday() {
        return DateUtils.isToday(getPreviewDate().getTimeInMillis());
    }
}
