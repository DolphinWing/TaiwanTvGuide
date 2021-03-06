package dolphin.apps.TaiwanTVGuide;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

//import com.google.analytics.tracking.android.EasyTracker;
@Deprecated
public class TVGuidePreference extends PreferenceActivity {
    private static final String TAG = "TVGuidePreference";
    private boolean mValueUpdated = false;

    private ListPreference mChannelCategory;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Android PreferenceActivity
        // http://www.cnblogs.com/wservices/archive/2010/07/08/1773449.html
        addPreferencesFromResource(R.xml.preference);
        this.setTitle(String.format("%s - %s", getString(R.string.app_name),
                getString(R.string.preference)));//[1.0.0.6]dolphin++

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (settings != null) {
            settings.registerOnSharedPreferenceChangeListener(onPreferenceChanged);
        }

        //[65]dolphin++ set ListPreference summary
        mChannelCategory = (ListPreference) findPreference("dTVGuide_DefaultGroup");
        updateSummary(mChannelCategory);

        //[65]dolphin++ remove no use preferences
        getPreferenceScreen().removePreference(findPreference("list_group"));

        this.findPreference("dTVGuide_VersionInfo").setSummary(
                getVersionName(getBaseContext(), TVGuidePreference.class));
        mValueUpdated = false;
    }

    private void updateSummary(Preference preference) {
        if (preference == mChannelCategory) {
            preference.setSummary(mChannelCategory.getEntry());
        }
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener onPreferenceChanged =
            new SharedPreferences.OnSharedPreferenceChangeListener() {

                @Override
                public void onSharedPreferenceChanged(
                        SharedPreferences sharedPreferences, String key) {
                    Log.d(TAG, String.format("KEY=%s", key));
                    mValueUpdated = true;
                    updateSummary(findPreference(key));//[65]dolphin++ update summary
                }

            };

    @Override
    public void onBackPressed() {
        Log.d(TAG, String.format("onBackPressed()"));
        //super.onBackPressed();
        Intent intent = new Intent();
        setResult(mValueUpdated ? Activity.RESULT_OK : Activity.RESULT_CANCELED, intent);
        this.finish();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, String.format("onDestroy()"));

        try {//try to un-register the listener
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            if (settings != null) {
                settings.unregisterOnSharedPreferenceChangeListener(onPreferenceChanged);
            }
        } catch (Exception e1) {
            Log.e(TAG, e1.getMessage());
        }

        super.onDestroy();
    }

    /**
     * get version name
     *
     * @param context
     * @param cls
     * @return
     */
    public static String getVersionName(Context context, Class<?> cls) {
        try {
            PackageInfo info = getPackageInfo(context, cls);
            //return String.format("%s.%d", info.versionName, info.versionCode);
            return String.format("%s r%d", info.versionName, info.versionCode);
        } catch (Exception e) {
            Log.e(TAG, "getVersionName: " + e.getMessage());
        }
        return null;
    }

    /**
     * get package info
     *
     * @param context
     * @param cls
     * @return
     */
    public static PackageInfo getPackageInfo(Context context, Class<?> cls) {
        try {
            return context.getPackageManager().getPackageInfo(
                    new ComponentName(context, cls).getPackageName(), 0);
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            Log.e(TAG, "getPackageInfo: " + e.getMessage());
        }
        return null;
    }

}
