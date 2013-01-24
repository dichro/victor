package to.rcpt.lamplighter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

class NetworkHandlerBase {
	private final View view;
	private final String urlBase;

	NetworkHandlerBase(View view, String urlBase) {
		this.view = view;
		this.urlBase = urlBase;
	}

	protected void go(float arg) {
		new Thread(new Executor(urlBase + arg)).start();
	}

	private void update(final boolean enabled, final String text) {
		view.post(new Runnable() {
			@Override
			public void run() {
				view.setEnabled(enabled);
				Toast.makeText(view.getContext(), text, Toast.LENGTH_SHORT)
						.show();
			}
		});
	}

	private class Executor implements Runnable {
		private final String urlstr;

		Executor(String url) {
			this.urlstr = url;
		}

		public void run() {
			try {
				update(false, "Sending request...");
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
				update(true, "...OK.");
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
	}
}