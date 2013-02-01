package to.rcpt.lamplighter;

import net.margaritov.preference.colorpicker.ColorPickerView;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;

public class PostingColourPicker extends ColorPickerView {
	public PostingColourPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnColorChangedListener(new Listener(attrs));
	}

	private class Listener extends NetworkHandlerBase implements
			ColorPickerView.OnColorChangedListener {
		protected Listener(AttributeSet attrs) {
			super(attrs);
		}

		private float[] hsv = new float[3];

		@Override
		public void onColorChanged(int color) {
			Log.i(getClass().getName(), "Dispatching " + color);
			Color.colorToHSV(color, hsv);
			go(PostingColourPicker.this, (float) (hsv[0] / 360.0), hsv[1]);
		}
	}
}
