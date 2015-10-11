package dolphin.apps.TaiwanTVGuide.v7;

/**
 * Created by dolphin on 2015/03/14.
 *
 * HTTP download status listener
 */
public interface OnHttpListener {
    void onHttpStart();
    void onHttpUpdated(Object data);
    void onHttpTimeout();
}
