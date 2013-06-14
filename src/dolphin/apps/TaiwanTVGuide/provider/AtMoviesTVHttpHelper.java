package dolphin.apps.TaiwanTVGuide.provider;

import android.content.Context;
import android.util.Log;

import com.quanta.pobu.net.QHttpHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dolphin.apps.TaiwanTVGuide.R;

public class AtMoviesTVHttpHelper extends QHttpHelper {
    private static final String TAG = "AtMoviesTVHttpHelper";

    public static final String ATMOVIES_TV_URL = "http://tv.atmovies.com.tw/tv";

    public static final String KEY_CHANNEL_TIME = "CHANNEL_TIME";
    public static final String KEY_TVDATA = "TVDATA";
    public static final String KEY_GROUP = "GROUP";
    public static final String KEY_CHANNEL = "CHANNEL";
    public static final String KEY_CHANNEL_ID = "CHANNEL_ID";
    public static final String KEY_PROGRAM_NAME = "PROGRAM_NAME";
    public static final String KEY_DATE = "DATE";

    protected Context mContext;

    public AtMoviesTVHttpHelper(Context context) {
        mContext = context;
    }

    public boolean _init(Context context) {
        mContext = context;
        // Log.d(TAG, String.format("_init %s",
        // mContext.getString(R.string.app_name)));

        if (!checkNeworkAvailable(mContext)) {
            return false;
        }

        return true;
    }

    public ArrayList<ChannelItem> get_group_guide_list(Calendar cal,
                                                       String group_id) {
        String url = String.format("%s/%s", ATMOVIES_TV_URL,
                mContext.getString(R.string.url_group_guide));
        // Log.d(TAG, url);
        String date = String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        // Log.d(TAG, date);
        url = url.replace("@date", date).replace("@group", group_id);
        url = url.replace("#@channel", "");//[0.4.0.18] @ 2011-06-01
        Log.d(TAG, url);
        ArrayList<ChannelItem> list = null;

        try {
            String response = super.getUrlContent(url);// , ENCODE_BIG5);
            if (response == null || response == "") {
                throw new Exception("no response");
            }
            // Log.d(TAG, String.format("response %d, %s", response.length(),
            // response.substring(46, 246)));
            list = new ArrayList<ChannelItem>();

			/*
             * <a name="CH56"></a> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <a
			 * class=at15b target="_self" href=
			 * "/tv/attv.cfm?action=channeltime&channel_id=CH56&tday=2011-04-11"
			 * >HBO電影台</a>
			 */
            String pattern = "<a name=\"([^\"]*)\"></[^<]*[^>]*>([^<]*)</";
            Matcher mTitle = Pattern.compile(pattern).matcher(response);
            String srcHtml = response;
            int startIndex = response.indexOf("<table border=\"1");
            while (mTitle.find()) {
                // Log.d(TAG, String.format("%s: %s", mTitle.group(1), mTitle
                // .group(2)));
                ChannelItem channel = new ChannelItem(mTitle.group(1),
                        mTitle.group(2), group_id);
                // get program
                srcHtml = srcHtml.substring(startIndex);
                startIndex = srcHtml.indexOf("</table>") + 1;
                String programHtml = srcHtml.substring(0, startIndex - 1);
                /*
                 * <td align="center" class=at9>09:50</td> <td class=at9
				 * width=80%><font color=#ffffff>　 <a target="_self"href=
				 * "/tv/attv.cfm?action=tvdata&tvtimeid=MCH56201104110950&tday=2011-04-11&channel_id=CH56"
				 * > <font class=at11>空前絕後滿天飛2 </font></a><font color=#606060>
				 * </font></font> </td>
				 */
                String pat = "<font class=at11>([^<]*)";
                Matcher mProgram = Pattern.compile(pat).matcher(programHtml);
                while (mProgram.find()) {
                    // Log.d(TAG, String.format("=== %s", mProgram.group(1)));
                    ProgramItem item = new ProgramItem(mProgram.group(1));
                    channel.Programs.add(item);
                }

                int i = 0;
                try {// try to parse time
                    pat = "<td align=\"center\" class=at9>([^<]*)";
                    mProgram = Pattern.compile(pat).matcher(programHtml);
                    while (mProgram.find()) {
                        // Log.d(TAG, String.format("=== %s",
                        // mProgram.group(1)));
                        ProgramItem item = channel.Programs.get(i);
                        if (item != null) {
                            // item.Date = cal;
                            item.Date = Calendar.getInstance();
                            item.Date.set(cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH));
                            String time = mProgram.group(1);
                            // Log.d(TAG, String.format("=== %s", time));
                            int hour =
                                    Integer.parseInt(time.substring(0, time.indexOf(":")));
                            int minute =
                                    Integer.parseInt(time.substring(time.indexOf(":") + 1));
                            // Log.d(TAG, String.format("=== %d %02d:%02d", i,
                            // hour, minute));
                            if (i == 0 && hour > 12) {
                                item.Date.add(Calendar.HOUR_OF_DAY, -24);
                            }
                            item.Date.set(Calendar.HOUR_OF_DAY, hour);
                            item.Date.set(Calendar.MINUTE, minute);
                            // Log.d(TAG, String.format("=== %d/%d %02d:%02d",
                            // item.Date.get(Calendar.MONTH) + 1, item.Date
                            // .get(Calendar.DAY_OF_MONTH), item.Date
                            // .get(Calendar.HOUR_OF_DAY), item.Date
                            // .get(Calendar.MINUTE)));

                            // item.Description = mProgram.group(1);
                        }
                        i++;
                    }
                } catch (Exception e1) {
                    Log.e(TAG, String.format(
                            "get_group_guide_list date: %s, %s",
                            this.getClass().getName(), e1.getMessage()));
                }
                try {// try to parse URL
                    pat = "attv.cfm\\?action=tvdata[^\"]*";
                    mProgram = Pattern.compile(pat).matcher(programHtml);
                    i = 0;
                    while (mProgram.find()) {
                        ProgramItem item = channel.Programs.get(i);
                        if (item != null) {
                            item.Url = mProgram.group(0);
                            // Log.d(TAG, String.format("%s", item.Url));
                        }
                        i++;
                    }
                } catch (Exception e1) {
                    Log.e(TAG, String.format(
                            "get_group_guide_list url: %s, %s",
                            this.getClass().getName(), e1.getMessage()));
                }

                list.add(channel);
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("get_group_guide_list: %s, %s",
                    this.getClass().getName(), e.getMessage()));
        }

        return list;
    }

    public ProgramItem get_program_guide(String tvdataUrl) {
        String url = String.format("%s/%s", ATMOVIES_TV_URL, tvdataUrl);
        Log.d(TAG, url);

        ProgramItem progItem = null;
        try {
            String response = super.getUrlContent(url);// , ENCODE_BIG5);
            if (response == null || response == "") {
                throw new Exception("no response");
            }
            // Log.d(TAG, String.format("response %d, %s", response.length(),
            // response.substring(46, 246)));
            // Log.d(TAG, response);

			/*
             * <table class=at11 width="96%" align=center border="0"
			 * cellspacing="4" cellpadding="2"> <tr><td width="80%" valign=top>
			 * <a class=at11
			 * href="attv.cfm?action=channeltime&channel_id=CH56&tday=2011-04-10"
			 * > <b>【HBO電影台】</b></a> <BR>&nbsp;<font class=at12
			 * color="303033"><b> 絕命終結站4 THE FINAL DESTINATION </b>
			 */
            if (!response.contains("<table class=at11")) {
                throw new Exception("no content");
            }

            progItem = new ProgramItem();

            String programHtml = response.substring(response.indexOf("<table class=at11"));
            String pattern = "<b>([^<]*)</";
            Matcher mProgram = Pattern.compile(pattern).matcher(programHtml);
            if (mProgram.find()) {// channel name
                progItem.Channel = mProgram.group(1).trim();
            }
            if (mProgram.find()) {// Chinese title and English title
                progItem.Name = mProgram.group(1).trim();
                // [1.2.0.12]dolphin++ replace duplicate space
                progItem.Name = Pattern.compile("[ ]+").matcher(progItem.Name).replaceAll(" ");
            }

			/*
             * <table class=at9 border="0" cellspacing="0" cellpadding="1">
			 * <tr><td valign=top rowspan=2>播映時間：</td> <td valign=top><a
			 * href="attv.cfm?action=todaytime&tday=2011-04-10&group_id=M#CH56"
			 * >2011/04/10</a></td> <tr><td valign=top>04:10 ～ 05:30</td>
			 * <tr><td valign=top>節目長度：</td> <td valign=top>1小時20分</td> </table>
			 * <HR color=f98764> <B><font class=at12>節目介紹</font></B><BR> <font
			 * class=at11>
			 */

            try {
                String descHtml = programHtml.substring(programHtml.indexOf("<font class=at11"));
                if (descHtml != null) {
                    descHtml = descHtml.substring(0, descHtml.indexOf("<img"));
                    descHtml = descHtml.replace("<BR><BR><P>", "\n");
                    // descHtml = descHtml.replace("<[^>]*>", "");
                    descHtml = QHttpHelper.removeHTML(descHtml);
                    progItem.Description = descHtml;
                } else {
                    progItem.Description = null;
                }
            } catch (Exception eDesc) {
                progItem.Description = null;
            }

			/*
             * <table bgcolor=F2FFD3 width=120 class=at9 border=1
			 * bordercolor=7AAB81 cellpadding=2 cellspacing=2> <tr><td
			 * bgcolor=CDDCAA align=center><font color=303099>最近重播時間</font></td>
			 * <tr><tdalign=center><a href=
			 * "attv.cfm?action=channeltime&channel_id=CH56&tday=2011-04-09"
			 * >04/09</a> 23:00</td> <tr><td align=center><a
			 */
            try {
                String repHtml = programHtml.substring(programHtml
                        .indexOf("<font color=303099"));
                repHtml = repHtml.substring(0, repHtml.indexOf("<form"));
                if (repHtml != null) {
                    String repPattern = "<a[^>]*>([^<]*)<[^>]*>([^<]*)";
                    Matcher mRep = Pattern.compile(repPattern).matcher(repHtml);
                    while (mRep.find()) {
                        //[0.5.0.19] @ 2011-06-01 change the Replays from <String> to <Calendar>
                        String replay_date = mRep.group(1).trim();
                        String replay_time = mRep.group(2).trim();
                        Log.v(TAG, "  " + replay_date + " " + replay_time);
                        Calendar cal = Calendar.getInstance();
                        try {
                            int month = Integer.parseInt(replay_date.split("/")[0]);
                            int day_of_month = Integer.parseInt(replay_date.split("/")[1]);
                            cal.set(Calendar.MONTH, month - 1);
                            cal.set(Calendar.DAY_OF_MONTH, day_of_month);
                            int hour = Integer.parseInt(replay_time.split(":")[0]);
                            int mins = Integer.parseInt(replay_time.split(":")[1]);
                            cal.set(Calendar.HOUR_OF_DAY, hour);
                            cal.set(Calendar.MINUTE, mins);
                            cal.set(Calendar.SECOND, 0);

                            if (cal.before(Calendar.getInstance())) {
                                continue;//time has passed
                            }
                        } catch (Exception eSpl) {
                            Log.e(TAG, "eSpl: " + eSpl.getMessage());
                            continue;
                        }
                        progItem.Replays.add(cal);
                    }
                }
            } catch (Exception eRep) {
                Log.e(TAG, eRep.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("get_program_guide: %s, %s",
                    this.getClass().getName(), e.getMessage()));
        }

        return progItem;
    }

    public ArrayList<ChannelItem> get_showtime_list(String group_id) {
        String url = String.format("%s/%s", ATMOVIES_TV_URL,
                mContext.getString(R.string.url_showtime));
        // Log.d(TAG, date);
        url = url.replace("@group", group_id);
        Log.d(TAG, url);
        ArrayList<ChannelItem> list = null;

        try {
            String response = super.getUrlContent(url);// , ENCODE_BIG5);
            if (response == null || response == "") {
                throw new Exception("no response");
            }
            // Log.d(TAG, String.format("response %d, %s", response.length(),
            // response.substring(46, 246)));
            list = new ArrayList<ChannelItem>();

			/*
             * <tr bgcolor=#ffffff> <td nowrap class=at11 align=right
			 * bgcolor=#ffffff>&nbsp;<a target="_self"href=
			 * "attv.cfm?action=channeltime&tday=2011-04-14&channel_id=CH50"
			 * >緯來日本台</a>&nbsp;&nbsp;</td>
			 */
            String pattern =
                    "<td nowrap[^<]*<a tar[^ ]* href=\"([^\"]*)\">([^<]*)</";
            Matcher mTitle = Pattern.compile(pattern).matcher(response);
            String srcHtml = response;
            int startIndex = response.indexOf("<tr bgcolor=#ffffff>");
            while (mTitle.find()) {
                String t1 = mTitle.group(1);
                ChannelItem channel = new ChannelItem(t1.substring(t1.lastIndexOf("=") + 1),
                        mTitle.group(2), group_id);
                // Log.d(TAG, channel.ID);
                /*
				 * <td class=at11 width=40%>&nbsp;<font class=at9
				 * color=#acacac>14:00</font>&nbsp; <font
				 * color=#2A5C8A>【熱門9點檔】</font> <a target="_self"href=
				 * "attv.cfm?action=tvdata&tvtimeid=JCH50201104141400&channelid=CH50&html=n"
				 * >不毛地帶 </a>　<font color=#606060></font> </td> <td class=at11
				 * width=40%>&nbsp;<font class=at9
				 * color=#acacac>15:00</font>&nbsp; <a target="_self"href=
				 * "attv.cfm?action=tvdata&tvtimeid=JCH50201104141500&channelid=CH50&html=n"
				 * >超省時生活 </a>　<font color=#606060></font> </td>
				 */
                srcHtml = srcHtml.substring(startIndex + 1);
                String programHtml = srcHtml;
                try {// for last one
                    startIndex = programHtml.indexOf("<tr bgcolor=#ffffff>");
                    programHtml = programHtml.substring(0, startIndex - 1);
                    programHtml = Pattern.compile("<font color[^<]*[^>]*>")
                            .matcher(programHtml).replaceAll("");// [0.3.0.15]dolphin++
                } catch (Exception e2) {
                }
                programHtml = programHtml.replace("<font color=#ff0000>☆</font>", "");

                String pat =
                        "<font class=at9[^>]*>([^<]*)<[^<]*<a tar[^ ]* href=\"([^\"]*)\">([^<]*)</";
                Matcher mProgram = Pattern.compile(pat).matcher(programHtml);
                int i = 0;
                while (mProgram.find()) {
                    // Log.d(TAG, String.format("=== %s", mProgram.group(1)));
                    ProgramItem item = new ProgramItem(mProgram.group(3));

                    item.Date = Calendar.getInstance();
                    String time = mProgram.group(1);
                    // Log.d(TAG, String.format("=== %s", time));
                    int hour = Integer.parseInt(time.substring(0, time.indexOf(":")));
                    int minute = Integer.parseInt(time.substring(time.indexOf(":") + 1));
                    // Log.d(TAG, String.format("=== %d %02d:%02d", i,
                    // hour, minute));
                    if (i == 0 && hour > 12) {
                        item.Date.add(Calendar.HOUR_OF_DAY, -24);
                    }
                    item.Date.set(Calendar.HOUR_OF_DAY, hour);
                    item.Date.set(Calendar.MINUTE, minute);

                    item.Url = mProgram.group(2);
                    // Log.d(TAG, item.Url);

                    i++;
                    channel.Programs.add(item);
                }

                list.add(channel);
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("get_group_guide_list: %s, %s",
                    this.getClass().getName(), e.getMessage()));
        }

        return list;
    }
}
