package dolphin.apps.TaiwanTVGuide.provider;

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

    public static void startImdbActivity(Context context, String title) {
        Intent i = new Intent();
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //check if IMDB apk is supported
        i.setData(Uri.parse(String.format("imdb:///find?q=%s", title.replace(" ", "%20"))));
        if (isCallable(context, i)) {//[38]++ 2013-05-31
            Bundle extras = new Bundle();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                extras.putBinder(Utils.EXTRA_CUSTOM_TABS_SESSION, null);
            }
            extras.putInt(Utils.EXTRA_CUSTOM_TABS_TOOLBAR_COLOR,
                    context.getResources().getColor(android.R.color.holo_orange_dark));
            i.putExtras(extras);
            context.startActivity(i);
        } else {
            Toast.makeText(context, "not support IMDB", Toast.LENGTH_SHORT).show();
        }
    }

    public static int getPreferenceGroupIndex(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return (!preferences.contains("dTVGuide_DefaultGroup")) ? 5
                : Integer.parseInt(preferences.getString("dTVGuide_DefaultGroup", "5"));
    }
}
