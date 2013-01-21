package to.rcpt.lamplighter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

public class SliderBar extends SeekBar {

	private class Handler implements OnSeekBarChangeListener, Runnable {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			Log.i(getClass().getName(), "onProgressChanged(..., " + progress
					+ ", " + fromUser + ")");
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			Log.i(getClass().getName(), "onStartTrackingTouch()");
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			Log.i(getClass().getName(), "onStopTrackingTouch()");
			setEnabled(false);
			new Thread(this).start();
		}

		public void run() {
			try {
				update(false, "Sending request");
				URL url;
				url = new URL(
						"http://192.168.1.2:10443/brightness/Living Room/"
								+ ((float) getProgress() / (float) getMax()));
				HttpURLConnection urlConnection;
				try {
					urlConnection = (HttpURLConnection) url.openConnection();
					InputStream inputStream = urlConnection.getInputStream();
					// read 100 bytes, Toast if not ok
					urlConnection.disconnect();
				} catch (IOException e) {
					Log.i(getClass().getName(), e.getMessage());
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// InputStream in = new
				// BufferedInputStream(urlConnection.getInputStream());
				// readStream(in);
			} catch (MalformedURLException e) {
				// oh no you didn't.
				return;
			} finally {
				update(true, "enabling");
			}
		}

		private void update(final boolean enabled, final String text) {
			post(new Runnable() {
				@Override
				public void run() {
					setEnabled(enabled);
					Toast.makeText(getContext(), text, Toast.LENGTH_SHORT)
							.show();
				}
			});
		}
	}

	public SliderBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnSeekBarChangeListener(new Handler());
	}
}
