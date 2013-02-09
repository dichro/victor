package to.rcpt.lamplighter;

import android.app.Activity;
import android.os.Bundle;

public class SingleLightActivity extends Activity {
	private final int layout;

	public SingleLightActivity(int layout) {
		this.layout = layout;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layout);
	}
}
