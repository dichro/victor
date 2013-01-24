package to.rcpt.lamplighter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioButton;

public class PostingRadioButton extends RadioButton {
	public PostingRadioButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnClickListener(new Listener(attrs));
	}

	private static class Listener extends NetworkHandlerBase implements
			View.OnClickListener {
		Listener(AttributeSet attrs) {
			super(attrs);
		}

		@Override
		public void onClick(View v) {
			go(v);
		}
	}
}
