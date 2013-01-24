package to.rcpt.lamplighter;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

public class PostingRadioButton extends RadioButton {
	public PostingRadioButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(getClass().getName(), "onClick");
			}
		});
	}

	private class Listener extends NetworkHandlerBase implements
			View.OnClickListener {
		Listener(View view, AttributeSet attrs) {
			super(view, attrs);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

		}

	}
}
