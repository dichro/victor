package to.rcpt.lamplighter;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.SeekBar;

public class SliderBar extends SeekBar {

	private class Handler extends NetworkHandlerBase implements
			OnSeekBarChangeListener {
		Handler(AttributeSet attrs) {
			super(SliderBar.this, attrs);
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

	public SliderBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO(dichro): do this properly
		setOnSeekBarChangeListener(new Handler(attrs));
	}
}
