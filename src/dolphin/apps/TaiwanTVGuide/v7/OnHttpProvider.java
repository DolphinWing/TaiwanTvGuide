package dolphin.apps.TaiwanTVGuide.v7;

/**
 * Created by dolphin on 2015/03/14.
 */
public interface OnHttpProvider {
    void registerOnHttpListener(OnHttpListener listener);
    void unregisterOnHttpListener(OnHttpListener listener);
    void refresh();
}
