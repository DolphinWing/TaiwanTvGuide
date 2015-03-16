package dolphin.apps.TaiwanTVGuide.v7;

/**
 * Created by dolphin on 2015/03/14.
 */
public interface OnHttpProvider {
    public void registerOnHttpListener(OnHttpListener listener);
    public void unregisterOnHttpListener(OnHttpListener listener);
    public void refresh();
}
