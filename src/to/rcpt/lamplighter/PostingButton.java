package to.rcpt.lamplighter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class PostingButton extends Button {
	public PostingButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnClickListener(new ClickHandler());
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.i(getClass().getName(),
				"onMeasure(" + MeasureSpec.toString(widthMeasureSpec) + ", "
						+ MeasureSpec.toString(heightMeasureSpec) + ")");
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	private static class ClickHandler implements View.OnClickListener, Runnable {
		@Override
		public void onClick(View v) {
			new Thread(this).start();
		}

		public void run() {
			URL url;
			try {
				url = new URL("http://192.168.1.2:10443/allOff");
			} catch (MalformedURLException e) {
				// oh no you didn't.
				return;
			}
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
		}
	}
}
