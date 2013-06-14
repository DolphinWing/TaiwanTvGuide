package dolphin.apps.TaiwanTVGuide.abs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import dolphin.apps.TaiwanTVGuide.R;
import dolphin.apps.TaiwanTVGuide.provider.AtMoviesTVHttpHelper;
import dolphin.apps.TaiwanTVGuide.provider.ProgramItem;

public class TVGuideWebViewFragment extends TVGuideFragment
{
	WebView webview;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.webview, container, false);
		webview = (WebView) view.findViewById(R.id.webView1);
		//webview.getSettings().setJavaScriptEnabled(true);
		webview.setWebViewClient(new WebViewClient() {
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl)
			{
				Toast.makeText(getActivity(), "Oh no! " + description,
					Toast.LENGTH_SHORT).show();
			}
		});
		return view;
	}

	@Override
	public void updateView(ProgramItem pItem)
	{
		String pName = getActivity().getIntent().getExtras().getString(
			AtMoviesTVHttpHelper.KEY_PROGRAM_NAME);
		if (pItem.Name != null
			&& pItem.Name.length() > pName.length()) {
			String engTitle = pItem.Name.substring(pName.length()).trim();
			String url =
				"http://www.imdb.com/find?s=all&q="
					+ engTitle.replace(" ", "+");
			webview.loadUrl(url);
		} else {
			webview.clearView();
		}
		super.updateView(pItem);
	}
}
