package dolphin.apps.TaiwanTVGuide.v7;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

import dolphin.apps.TaiwanTVGuide.R;
import dolphin.apps.TaiwanTVGuide.TVGuidePreference;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {

    private static final String VERSION_FILE = "Version.txt";
    private static final String VERSION_FILE_ENCODE = "UTF-8";
    private final static int TAPS_TO_BE_A_DEVELOPER = 5;

    //This API was added due to a newly discovered vulnerability.
    // Please see http://ibm.co/1bAA8kF or http://ibm.co/IDm2Es
    @TargetApi(Build.VERSION_CODES.KITKAT)
    //http://stackoverflow.com/a/20494759/2673859
    @Override
    protected boolean isValidFragment(String fragmentName) {
        return GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        LinearLayout root = (LinearLayout) findViewById(android.R.id.list)
                .getParent().getParent().getParent();
        if (root != null) {
            Toolbar bar = (Toolbar) LayoutInflater.from(this)
                    .inflate(R.layout.settings_toolbar, root, false);
            if (bar != null) {
                root.addView(bar, 0); // insert at top
                bar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
        }
        setupSimplePreferencesScreen();
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);
        findPreference("dTVGuide_VersionInfo")
                .setSummary(TVGuidePreference.getVersionName(getBaseContext(),
                        SettingsActivity.class));

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference("dTVGuide_DefaultGroup"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB || !isXLargeTablet(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener =
            new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    private int mDevHitCountdown = TAPS_TO_BE_A_DEVELOPER;
    private Toast mDevHitToast = null;

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals("dTVGuide_VersionInfo")) {
            if (--mDevHitCountdown <= 0) {
                showVersionTxtDialog(this);
            } else {
                if (mDevHitToast != null) {
                    mDevHitToast.cancel();
                }
                mDevHitToast = Toast.makeText(this, String.format(Locale.US,
                        "Still %d to show", mDevHitCountdown), Toast.LENGTH_LONG);
                mDevHitToast.show();
            }
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {

        private int mDevHitCountdown = TAPS_TO_BE_A_DEVELOPER;
        private Toast mDevHitToast = null;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("dTVGuide_DefaultGroup"));
            //bindPreferenceSummaryToValue(findPreference("example_list"));
            findPreference("dTVGuide_VersionInfo")
                    .setSummary(TVGuidePreference.getVersionName(getActivity(),
                            GeneralPreferenceFragment.class));
        }

        @Override
        public void onResume() {
            super.onResume();

            mDevHitCountdown = TAPS_TO_BE_A_DEVELOPER;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference.getKey().equals("dTVGuide_VersionInfo")) {
                if (--mDevHitCountdown <= 0) {
                    showVersionTxtDialog(getActivity());
                } else {
                    if (mDevHitToast != null) {
                        mDevHitToast.cancel();
                    }
                    mDevHitToast = Toast.makeText(getActivity(), String.format(Locale.US,
                            "Still %d to show", mDevHitCountdown), Toast.LENGTH_LONG);
                    mDevHitToast.show();
                }
                return true;
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }

    static AlertDialog mVersionDialog;

    static void showVersionTxtDialog(Context context) {
        if (mVersionDialog != null) {
            return;
        }
        mVersionDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.build_information)
                .setMessage(read_asset_text(context, VERSION_FILE, VERSION_FILE_ENCODE))
                .setPositiveButton(android.R.string.ok, null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        mVersionDialog = null;
                    }
                })
                .setCancelable(true)
                .create();
        mVersionDialog.setCanceledOnTouchOutside(true);
        mVersionDialog.show();
    }

    private static String read_asset_text(Context context, String asset_name, String encoding) {
        try {
            InputStreamReader sr =
                    new InputStreamReader(context.getAssets().open(asset_name),
                            (encoding != null) ? encoding : "UTF8");
            //Log.i(TAG, asset_name + " " + sr.getEncoding());

            int len = 0;
            StringBuilder sb = new StringBuilder();

            while (true) {//read from buffer
                char[] buffer = new char[1024];
                len = sr.read(buffer);//, size, 512);
                //Log.d(TAG, String.format("%d", len));
                if (len > 0) {
                    sb.append(buffer);
                } else {
                    break;
                }
            }
            //Log.i(TAG, String.format("  length = %d", sb.length()));

            sr.close();
            return sb.toString().trim();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
