package dolphin.apps.TaiwanTVGuide;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import dolphin.apps.TaiwanTVGuide.provider.AtMoviesTVHttpHelper;

@Deprecated
public class TVGuideWebView extends Activity
{
	private String myUrl = "";
	private WebView webview;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// TODO Auto-generated method stub
		setContentView(R.layout.webview);
		webview = (WebView) findViewById(R.id.webView1);
		// WebView webview = new WebView(this);
		// setContentView(webview);
		String url = getIntent().getExtras().getString(
			AtMoviesTVHttpHelper.KEY_TVDATA);
		// Toast.makeText(this, url, Toast.LENGTH_SHORT).show();
		// String channel = getIntent().getExtras().getString(
		// AtMoviesTVHttpHelper.KEY_CHANNEL);
		String date = getIntent().getExtras().getString(
			AtMoviesTVHttpHelper.KEY_DATE);
		String chan_id = getIntent().getExtras().getString(
			AtMoviesTVHttpHelper.KEY_CHANNEL_ID);
		//		tv_url = AtMoviesTVHttpHelper.ATMOVIES_TV_URL
		//			+ "/attv.cfm?action=channeltime&channel_id=" + chan_id
		//			+ "&tday=" + date;
		//		myUrl = String.format("%s/%s", AtMoviesTVHttpHelper.ATMOVIES_TV_URL,
		//			getString(R.string.url_channel_time));
		//		myUrl = myUrl.replace("@date", date).replace("@channel", chan_id);
		String group_id = getIntent().getExtras().getString(
			AtMoviesTVHttpHelper.KEY_GROUP);
		myUrl = String.format("%s/%s", AtMoviesTVHttpHelper.ATMOVIES_TV_URL,
			getString(R.string.url_group_guide));
		myUrl = myUrl.replace("@date", date).replace("@group", group_id);
		myUrl = myUrl.replace("@channel", chan_id);//[0.4.0.18] @ 2011-06-01
		//Toast.makeText(this, myUrl, Toast.LENGTH_LONG).show();

		final Activity activity = this;
		// getWindow().requestFeature(Window.FEATURE_PROGRESS);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setWebViewClient(new WebViewClient() {
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl)
			{
				Toast.makeText(activity, "Oh no! " + description,
					Toast.LENGTH_SHORT).show();
			}
		});

		if (url.indexOf("http") != 0) {
			webview.loadUrl(myUrl);
			// webview.loadUrl(AtMoviesTVHttpHelper.ATMOVIES_TV_URL
			// + "/attv.cfm?action=todaytime");
			// at_url = String.format("%s/%s",
			// AtMoviesTVHttpHelper.ATMOVIES_TV_URL, url);
			// webview.loadUrl(at_url);
			// myHandler.sendMessageDelayed(myHandler.obtainMessage(1), 3000);
		}
		else {
			webview.loadUrl(url);
			// webview.loadUrl("http://www.google.com");
		}

	}

	// private Handler myHandler = new Handler() {
	//
	// @Override
	// public void handleMessage(Message msg) {
	// // TODO Auto-generated method stub
	// super.handleMessage(msg);
	// webview.loadUrl(tv_url);
	// }
	// };
}
