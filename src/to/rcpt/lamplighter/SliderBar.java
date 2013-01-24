package to.rcpt.lamplighter;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.SeekBar;

public class SliderBar extends SeekBar {

	private class Handler extends NetworkHandlerBase implements OnSeekBarChangeListener {
		Handler() {
			super(SliderBar.this, "http://192.168.1.11:10443/" + target
					+ "/Living%20Room/");
		}

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
			go((float) getProgress() / (float) getMax());
		}
	}

	private final String target;

	public SliderBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO(dichro): do this properly
		target = attrs.getAttributeValue("http://lamplighter.rcpt.to/",
				"target");
		setOnSeekBarChangeListener(new Handler());
	}
}
