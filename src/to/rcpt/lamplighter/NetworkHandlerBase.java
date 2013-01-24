package to.rcpt.lamplighter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class NetworkHandlerBase {
	private final String urlBase;

	protected NetworkHandlerBase(AttributeSet attrs) {
		// TODO(dichro): do attribute lookups properly
		StringBuilder sb = new StringBuilder("http://192.168.1.11:10443/")
				.append(attrs.getAttributeValue("http://lamplighter.rcpt.to/",
						"target")).append("/Living%20Room/");
		String arg = attrs.getAttributeValue("http://lamplighter.rcpt.to/",
				"arg");
		if (arg != null) {
			sb.append(arg);
		}
		urlBase = sb.toString();
	}

	protected void go(View view, float arg) {
		go(view, urlBase + arg);
	}

	protected void go(View view) {
		go(view, urlBase);
	}

	public static void go(View view, String urlstr) {
		new Thread(new Executor(view, urlstr)).start();
	}

	private static class Executor implements Runnable {
		private final String urlstr;
		private final View view;

		Executor(View view, String urlstr) {
			this.view = view;
			this.urlstr = urlstr;
		}

		public void run() {
			try {
				URL url;
				url = new URL(urlstr);
				HttpURLConnection urlConnection;
				urlConnection = (HttpURLConnection) url.openConnection();
				InputStream inputStream = urlConnection.getInputStream();
				// read 100 bytes, Toast if not ok
				urlConnection.disconnect();
				// InputStream in = new
				// BufferedInputStream(urlConnection.getInputStream());
				// readStream(in);
				update(true, null);
			} catch (MalformedURLException e) {
				update(true, "...bad URL?");
			} catch (IOException e) {
				Log.i(getClass().getName(), e.getMessage());
				e.printStackTrace();
				update(true, "...IO exception?");
			} catch (Exception e) {
				Log.i(getClass().getName(), e.getMessage());
				e.printStackTrace();
				update(true, "...unknown exception?");
			}
		}

		private void update(final boolean enabled, final String text) {
			view.post(new Runnable() {
				@Override
				public void run() {
					view.setEnabled(enabled);
					if (text != null) {
						Toast.makeText(view.getContext(), text,
								Toast.LENGTH_SHORT).show();
					}
				}
			});
		}
	}
}