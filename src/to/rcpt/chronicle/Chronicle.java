package to.rcpt.chronicle;

import to.rcpt.lamplighter.R;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;

public class Chronicle extends Activity {
	public static final String REFERENCE_IMAGE = "referenceImage";
	private static final String DEFAULT_CAMERA = "defaultCamera";
	private final String TAG = getClass().getName();
	private Preview mPreview;
	Camera mCamera;
	int numberOfCameras;
	private int cameraCurrentlyLocked = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	private void setupCamera() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mPreview = new Preview(this);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		GridLayout ll = (GridLayout) findViewById(R.id.top);
		ll.addView(mPreview, 0);

		numberOfCameras = Camera.getNumberOfCameras();
		cameraCurrentlyLocked = prefs.getInt(DEFAULT_CAMERA, -1);
		Log.i(TAG, "read def camera " + cameraCurrentlyLocked + " of "
				+ numberOfCameras);

		if ((cameraCurrentlyLocked < 0)
				|| (cameraCurrentlyLocked >= numberOfCameras)) {
			CameraInfo cameraInfo = new CameraInfo();
			for (int i = 0; i < numberOfCameras; i++) {
				Camera.getCameraInfo(i, cameraInfo);
				if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
					cameraCurrentlyLocked = i;
				}
			}
		}
		if (cameraCurrentlyLocked < 0) {
			cameraCurrentlyLocked = numberOfCameras - 1;
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

		if (cameraCurrentlyLocked < 0) {
			return;
		}
		Log.i(TAG, "Opening camera " + cameraCurrentlyLocked);
		mCamera = Camera.open(cameraCurrentlyLocked);
		Log.i(TAG, "Got camera " + mCamera);
		mPreview.setCamera(mCamera);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mCamera != null) {
			mPreview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.camera_menu, menu);
		if (numberOfCameras <= 1) {
			MenuItem mi = menu.findItem(R.id.switch_camera);
			mi.setEnabled(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.switch_camera:
			if (mCamera != null) {
				mCamera.stopPreview();
				mPreview.setCamera(null);
				mCamera.release();
				mCamera = null;
			}
			mCamera = Camera
					.open((cameraCurrentlyLocked + 1) % numberOfCameras);
			cameraCurrentlyLocked = (cameraCurrentlyLocked + 1)
					% numberOfCameras;
			mPreview.switchCamera(mCamera);
			mCamera.startPreview();
			Editor prefs = getPreferences(MODE_PRIVATE).edit();
			prefs.putInt(DEFAULT_CAMERA, cameraCurrentlyLocked);
			Log.i(TAG, "Def camera " + cameraCurrentlyLocked);
			prefs.commit();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}

// ----------------------------------------------------------------------
