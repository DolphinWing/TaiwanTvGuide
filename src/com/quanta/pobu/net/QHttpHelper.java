package com.quanta.pobu.net;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;
import org.w3c.dom.Document;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class QHttpHelper
{
	private static final String TAG = "QHttpHelper";

	private final static int DEFAULT_NETWORK_TIMEOUT = 20000;

	public final static String ENCODE_UTF8 = "utf-8";
	public final static String ENCODE_BIG5 = "big5";

	/**
	 * {@link StatusLine} HTTP status code when no server error has occurred.
	 */
	private static final int HTTP_STATUS_OK = 200;

	/**
	 * Shared buffer used by {@link #getUrlContent(String)} when reading results
	 * from an API request.
	 */
	private static byte[] sBuffer = new byte[512];

	/**
	 * User-agent string to use when making requests. Should be filled using
	 * {@link #prepareUserAgent(Context)} before making any other calls.
	 */
	private static String sUserAgent = null;

	/**
	 * Prepare the internal User-Agent string for use. This requires a
	 * {@link Context} to pull the package name and version number for this
	 * application.
	 */
	public static void prepareUserAgent(Context context)
	{
		try {
			// Read package name and version number from manifest
			PackageManager manager = context.getPackageManager();
			PackageInfo info =
				manager.getPackageInfo(context.getPackageName(), 0);
			sUserAgent =
				String.format("%s/%s (Linux; Android)", info.packageName,
					info.versionName);

		}
		catch (NameNotFoundException e) {
			Log
					.e(TAG,
						"Couldn't find package information in PackageManager",
						e);
		}
	}

	/**
	 * Pull the raw text content of the given URL. This call blocks until the
	 * operation has completed, and is synchronized because it uses a shared
	 * buffer {@link #sBuffer}.
	 * 
	 * @param url
	 *            The exact URL to request.
	 * @return The raw content returned by the server.
	 * @throws ApiException
	 *             If any connection or server error occurs.
	 */
	public static synchronized String getUrlContent(String url, int timeout,
			String encode) throws Exception
	{
		// if (sUserAgent == null) {
		// throw new Exception("User-Agent string must be prepared");
		// }

		try {
			//How to set HttpResponse timeout for Android in Java
			//http://stackoverflow.com/questions/693997/how-to-set-httpresponse-timeout-for-android-in-java
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeout);
			// Set the default socket timeout (SO_TIMEOUT) 
			// in milliseconds which is the timeout for waiting for data.
			HttpConnectionParams.setSoTimeout(httpParameters, timeout);

			// Create client and set our specific user-agent string
			HttpClient client = new DefaultHttpClient(httpParameters);
			HttpGet request = new HttpGet(url);// Uri.encode(url)
			request.setHeader("User-Agent", sUserAgent);
			//request.setHeader("Content-Type", "text/xml; charset=" + encode);
			//request.setHeader("HTTP_REFERRER", url);
			request.setHeader("REFERER", url);//put referer(referrer) to header
			//request.setHeader("REFERRER", url);
			// return sUserAgent;

			HttpResponse response = client.execute(request);

			// Check if server response is valid
			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() != HTTP_STATUS_OK) {
				throw new Exception(
						"Invalid response from server: " + status.toString());
			}

			// Pull content stream from response
			HttpEntity entity = response.getEntity();
			InputStream inputStream = entity.getContent();

			//if (ENCODE_UTF8 == encode) {
			ByteArrayOutputStream content = new ByteArrayOutputStream();

			// Read response into a buffered stream
			int readBytes = 0;
			while ((readBytes = inputStream.read(sBuffer)) != -1) {
				content.write(sBuffer, 0, readBytes);
			}

			// Return result from buffered stream
			return new String(content.toByteArray(), encode);
			//}

			//StringBuilder sb = new StringBuilder("");
			//// Read response into a buffered stream
			//String utf8 = new String(content.toByteArray(), encode);
			//sb.append(utf8);
			//return sb.toString();
		}
		catch (IOException e) {
			// throw new Exception("Problem communicating with API", e);
			return "IOException: " + e.getMessage();
		}
	}

	/**
	 * get URL contents
	 * @param url
	 * @param encode
	 * @return
	 * @throws Exception
	 */
	public static synchronized String getUrlContent(String url, String encode)
			throws Exception
	{
		return getUrlContent(url, DEFAULT_NETWORK_TIMEOUT, encode);
	}

	public static synchronized String getUrlContent(String url, int timeout)
			throws Exception
	{
		return getUrlContent(url, DEFAULT_NETWORK_TIMEOUT, ENCODE_UTF8);
	}

	public static synchronized String getUrlContent(String url)
			throws Exception
	{
		return getUrlContent(url, DEFAULT_NETWORK_TIMEOUT);
	}

	/**
	 * remove some ISO-8859 encoded HTML
	 * @param content
	 * @return
	 */
	public static String removeISO8859HTML(String content)
	{
		String res_content = content;
		// HTML ISO-8859-1 Reference
		// http://www.w3schools.com/tags/ref_entities.asp
		res_content = res_content.replace("&lt;", "<").replace("&#60;", "<");// less-than
		res_content = res_content.replace("&gt;", ">").replace("&#62;", ">");// greater-than
		res_content =
			res_content.replace("&quot;", "\"").replace("&#34;", "\"");// quotation
		// mark
		res_content = res_content.replace("&apos;", "'").replace("&#39;", "'");// apostrophe
		res_content = res_content.replace("&amp;", "&").replace("&#38;", "&");// ampersand
		res_content = res_content.replace("&nbsp;", " ").replace("&#160;", " ");// non-breaking
		// space
		res_content =
			res_content.replace("&deg;", "�X").replace("&#176;", "�X");// degree
		return res_content;
	}

	public static String removeHTML(String content)
	{
		String tmp =
			Pattern.compile("<!--[^-]*-->").matcher(content).replaceAll("")
					.trim();
		return Pattern.compile("<[^>]*>").matcher(tmp).replaceAll("").trim();
	}

	// Loading remote images
	// http://stackoverflow.com/questions/3075637/loading-remote-images
	public static synchronized Bitmap getRemoteImage(final URL aURL, int timeout)
	{
		try {
			URLConnection conn = aURL.openConnection();
			conn.setReadTimeout(timeout);
			conn.connect();
			BufferedInputStream bis =
				new BufferedInputStream(conn.getInputStream());
			Bitmap bm = BitmapFactory.decodeStream(bis);
			bis.close();
			return bm;
		}
		catch (Exception e) {
		}
		return null;
	}

	public static synchronized Bitmap getRemoteImage(final URL aURL)
	{
		return getRemoteImage(aURL, DEFAULT_NETWORK_TIMEOUT);
	}

	/**
	 * get remote image
	 * @param url
	 * @param timeout
	 * @return
	 */
	public static synchronized Bitmap getRemoteImage(String url, int timeout)
	{
		try {
			return getRemoteImage(new URL(url), timeout);
		}
		catch (MalformedURLException e) {
			//throw e;
		}
		return null;
	}

	public static synchronized Bitmap getRemoteImage(String url)
	{
		return getRemoteImage(url, DEFAULT_NETWORK_TIMEOUT);
	}

	public static synchronized boolean getRemoteImage(Context context,
			String url, String fileName)
	{
		return getRemoteImage(context, url, fileName, DEFAULT_NETWORK_TIMEOUT);
	}

	public static synchronized boolean getRemoteImage(Context context,
			String url, String fileName, int timeout)
	{
		try {
			return getRemoteImage(context, new URL(url), fileName, timeout);
		}
		catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * get remote image and save to local storage
	 * @param context
	 * @param aURL
	 * @param fileName
	 * @param timeout
	 * @return
	 */
	public static synchronized boolean getRemoteImage(Context context,
			URL aURL, String fileName, int timeout)
	{
		boolean result = false;
		try {
			URLConnection conn = aURL.openConnection();
			conn.setReadTimeout(timeout);
			conn.connect();
			BufferedInputStream bis =
				new BufferedInputStream(conn.getInputStream());
			Bitmap bm = BitmapFactory.decodeStream(bis);
			OutputStream outstream =
				context.openFileOutput(fileName, Context.MODE_WORLD_WRITEABLE);
			if (outstream == null)
				throw new IOException("null stream");
			result = bm.compress(Bitmap.CompressFormat.PNG, 100, outstream);
			bis.close();
			outstream.close();
			outstream = null;
			bm.recycle();
			bm = null;
		}
		catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Exception: " + e.getMessage());
			result = false;
		}
		return result;
	}

	/**
	 * get XML Document from URL
	 * @param url
	 * @return
	 */
	public static synchronized Document getXMLDocument(String url)
	{
		Document doc = null;

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(url);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, String.format("parseXML: %s", e.getMessage()), e);
		}

		return doc;
	}

	/**
	 * get JSON object from URL
	 * @param url
	 * @return
	 */
	public static synchronized JSONObject getJSONObject(String url)
	{
		JSONObject response = null;

		try {
			response = new JSONObject(getUrlContent(url));
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, String.format("getJSONObject: %s", e.getMessage()), e);
		}

		return response;
	}

	//<updated>2010-10-29T05:03:49.000Z</updated>
	private static final String PATTERN_TIME_STR =
		"([\\d]+)[\\D]+([\\d]+)[\\D]+([\\d]+)[\\D]+([\\d]+)[\\D]+([\\d]+)[\\D]+([\\d]+)";

	public static Calendar parseRssDateStr(String date_str)
	{
		return parseRssDateStr(TimeZone.getDefault(), date_str);
	}

	/**
	 * convert the date string to Calendar structure
	 * @param tz time zone
	 * @param date_str date string
	 * @return
	 */
	public static Calendar parseRssDateStr(TimeZone tz, String date_str)
	{
		Calendar cal = Calendar.getInstance(tz);
		Matcher mTime = Pattern.compile(PATTERN_TIME_STR).matcher(date_str);
		if (mTime.find()) {//get updated time
			cal.set(Calendar.YEAR, Integer.parseInt(mTime.group(1)));
			cal.set(Calendar.MONTH, Integer.parseInt(mTime.group(2)) - 1);//0-11
			cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(mTime.group(3)));
			cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(mTime.group(4)));
			cal.set(Calendar.MINUTE, Integer.parseInt(mTime.group(5)));
			cal.set(Calendar.SECOND, Integer.parseInt(mTime.group(6)));
			return cal;
		}

		return null;//return cal.getTime();
	}

	/**
	 * check if network is available
	 * @param context
	 * @return
	 */
	public static boolean checkNeworkAvailable(Context context)
	{
		ConnectivityManager connMgr =
			(ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo wifi =
			connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifi.isAvailable()) {
			//Toast.makeText(this, "Wifi" , Toast.LENGTH_LONG).show();
			Log.v(TAG, "wifi.isAvailable()");
			return true;
		}

		NetworkInfo mobile =
			connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mobile.isAvailable()) {
			//Toast.makeText(this, "Mobile 3G " , Toast.LENGTH_LONG).show();
			Log.v(TAG, "mobile.isAvailable()");
			return true;
		}
		//else {
		//{Toast.makeText(this, "No Network " , Toast.LENGTH_LONG).show();}
		Log.v(TAG, "No Network");
		//}
		return false;
	}

	private final static String UPLOAD_LINE_END = "\r\n";
	private final static String UPLOAD_TWO_HYPHENS = "--";
	private final static String UPLOAD_BOUNDARY = "*****";

	/**
	 * Push file to PHP server
	 * @param urlString PHP url to handle the upload file
	 * @param existingFileName local file
	 * @return
	 */
	public static String doPhpFileUpload(String urlString,
			String existingFileName)
	{
		//Android: How to upload .mp3 file to http server?
		//http://stackoverflow.com/a/5176670

		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		DataInputStream inStream = null;
		//String existingFileName =
		//	Environment.getExternalStorageDirectory().getAbsolutePath()
		//		+ "/mypic.png";

		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		String responseFromServer = "";
		//String urlString = "http://192.168.1.51/android/upload.php";
		try {
			//------------------ CLIENT REQUEST
			FileInputStream fileInputStream =
				new FileInputStream(new File(existingFileName));
			// open a URL connection to the Servlet
			URL url = new URL(urlString);
			// Open a HTTP connection to the URL
			conn = (HttpURLConnection) url.openConnection();
			// Allow Inputs
			conn.setDoInput(true);
			// Allow Outputs
			conn.setDoOutput(true);
			// Don't use a cached copy.
			conn.setUseCaches(false);
			// Use a post method.
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type",
				"multipart/form-data;boundary=" + UPLOAD_BOUNDARY);
			dos = new DataOutputStream(conn.getOutputStream());
			dos.writeBytes(UPLOAD_TWO_HYPHENS + UPLOAD_BOUNDARY
				+ UPLOAD_LINE_END);
			dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";"
				+ "filename=\"" + existingFileName + "\"" + UPLOAD_LINE_END);
			dos.writeBytes(UPLOAD_LINE_END);
			// create a buffer of maximum size
			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];
			// read file and write it into form...
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}
			// send multipart form data necesssary after file data...
			dos.writeBytes(UPLOAD_LINE_END);
			dos.writeBytes(UPLOAD_TWO_HYPHENS + UPLOAD_BOUNDARY
				+ UPLOAD_TWO_HYPHENS + UPLOAD_LINE_END);
			// close streams
			Log.d(TAG, "File is written");
			fileInputStream.close();
			dos.flush();
			dos.close();
		}
		catch (MalformedURLException ex) {
			Log.e(TAG, "error: " + ex.getMessage(), ex);
		}
		catch (IOException ioe) {
			Log.e(TAG, "error: " + ioe.getMessage(), ioe);
		}

		//------------------ read the SERVER RESPONSE
		try {
			inStream = new DataInputStream(conn.getInputStream());
			String str;

			while ((str = inStream.readLine()) != null) {
				//Log.e(TAG, "Server Response " + str);
				responseFromServer += str;
			}
			inStream.close();
			Log.d(TAG, responseFromServer);
		}
		catch (IOException ioex) {
			Log.e(TAG, "error: " + ioex.getMessage(), ioex);
		}

		return responseFromServer;
	}
}
