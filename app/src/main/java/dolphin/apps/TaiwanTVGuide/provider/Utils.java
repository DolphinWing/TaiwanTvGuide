package dolphin.apps.TaiwanTVGuide.provider;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.widget.Toast;

import java.util.List;

/**
 * Created by dolphin on 2015/03/14.
 * <p/>
 * common utilities
 */
public class Utils {
    //https://developer.chrome.com/multidevice/android/customtabs
    //https://github.com/GoogleChrome/custom-tabs-client
    public static final String EXTRA_CUSTOM_TABS_SESSION = "android.support.customtabs.extra.SESSION";
    public static final String EXTRA_CUSTOM_TABS_TOOLBAR_COLOR = "android.support.customtabs.extra.TOOLBAR_COLOR";

    //Android Essentials: Adding Events to the User’s Calendar
    //http://goo.gl/jyT75l
    public static void startAddingCalendar(Context context, ChannelItem channel, ProgramItem program) {
        Intent calIntent = new Intent(Intent.ACTION_INSERT);
        calIntent.setData(CalendarContract.Events.CONTENT_URI);

        calIntent.setType("vnd.android.cursor.item/event");
        calIntent.putExtra(CalendarContract.Events.TITLE, String.format("%s - %s",
                channel != null ? channel.Name : program.Channel, program.Name));
        //calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, null);
        calIntent.putExtra(CalendarContract.Events.DESCRIPTION, program.ClipLength);

        calIntent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false);
        calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                program.Date.getTimeInMillis());
        calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                program.Date.getTimeInMillis());

        calIntent.putExtra(CalendarContract.Events.ACCESS_LEVEL,
                CalendarContract.Events.ACCESS_PRIVATE);
        calIntent.putExtra(CalendarContract.Events.AVAILABILITY,
                CalendarContract.Events.AVAILABILITY_FREE);

        if (isCallable(context, calIntent)) {
            context.startActivity(calIntent);
        } else {
            Toast.makeText(context, "not support Calendar", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * check if any activity can handle this intent
     *
     * @param context
     * @param intent
     * @return
     */
    private static boolean isCallable(Context context, Intent intent) {
        List<ResolveInfo> list = context.getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private static boolean isImdbApkInstalled(Context context) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("imdb:///find?q=Tom%20Hanks"));
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, 0);
        if (list != null && list.size() > 0) {
            for (ResolveInfo resolveInfo : list) {
                //Log.d("CpblCalendarHelper", resolveInfo.activityInfo.packageName);
                if (resolveInfo.activityInfo.packageName.startsWith("com.imdb.mobile")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if Google Chrome is installed.
     *
     * @param context Context
     * @return true if installed
     */
    public static boolean isGoogleChromeApkInstalled(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, 0);
        if (list != null && list.size() > 0) {
            for (ResolveInfo resolveInfo : list) {
                //Log.d("CpblCalendarHelper", resolveInfo.activityInfo.packageName);
                if (resolveInfo.activityInfo.packageName.startsWith("com.android.chrome")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void startImdbActivity(Context context, String title) {
        Intent i = new Intent();
        //check if IMDB apk is supported
        i.setData(Uri.parse(String.format("imdb:///find?q=%s", title.replace(" ", "%20"))));
        if (isCallable(context, i)) {//[38]++ 2013-05-31
            if (isImdbApkInstalled(context)) {
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } else {
                Bundle extras = new Bundle();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    extras.putBinder(Utils.EXTRA_CUSTOM_TABS_SESSION, null);
                }
                extras.putInt(Utils.EXTRA_CUSTOM_TABS_TOOLBAR_COLOR,
                        context.getResources().getColor(android.R.color.holo_orange_dark));
                i.putExtras(extras);
            }
            if (!isGoogleChromeApkInstalled(context)) {
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(i);
        } else {
            Toast.makeText(context, "not support IMDB", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * start a browser activity
     *
     * @param context Context
     * @param url     url
     */
    public static void startBrowserActivity(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //[169]dolphin++ add Chrome Custom Tabs
            Bundle extras = new Bundle();
            extras.putBinder(Utils.EXTRA_CUSTOM_TABS_SESSION, null);
            extras.putInt(Utils.EXTRA_CUSTOM_TABS_TOOLBAR_COLOR,
                    context.getResources().getColor(android.R.color.holo_orange_dark));
            intent.putExtras(extras);
        }
        if (!isGoogleChromeApkInstalled(context)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        try {//[97]dolphin++
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            //Toast.makeText(context, R.string.query_error, Toast.LENGTH_SHORT).show();
        }
    }

    public static int getPreferenceGroupIndex(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return (!preferences.contains("dTvGuideDefaultGroupIndex")) ? 3
                : Integer.parseInt(preferences.getString("dTvGuideDefaultGroupIndex", "3"));
    }
}
