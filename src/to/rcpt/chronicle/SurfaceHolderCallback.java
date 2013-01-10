package to.rcpt.chronicle;

import java.io.IOException;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

class SurfaceHolderCallback implements SurfaceHolder.Callback {
	/**
	 * 
	 */
	private final Preview preview;

	/**
	 * @param preview
	 */
	SurfaceHolderCallback(Preview preview) {
		this.preview = preview;
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it
		// where
		// to draw.
		try {
			if (this.preview.mCamera != null) {
				Log.i(this.preview.TAG, "size "
						+ this.preview.mCamera.getParameters().getPreviewSize());
				this.preview.mCamera.setPreviewDisplay(holder);
			}
		} catch (IOException exception) {
			Log.e(this.preview.TAG,
					"IOException caused by setPreviewDisplay()", exception);
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		if (this.preview.mCamera != null) {
			this.preview.mCamera.stopPreview();
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		if (this.preview.mSupportedPreviewSizes != null) {
			this.preview.mPreviewSize = this.preview.getOptimalPreviewSize(
					this.preview.mSupportedPreviewSizes,
					this.preview.getMeasuredWidth(),
					this.preview.getMeasuredHeight());
			// 280, 280);
		}
		// interesting. If you launch with the screen off, NPE occurs here.
		Camera.Parameters parameters = this.preview.mCamera.getParameters();
		parameters.setPreviewSize(this.preview.mPreviewSize.width,
				this.preview.mPreviewSize.height);
		this.preview.requestLayout();
		this.preview.mCamera.setParameters(parameters);
		this.preview.mCamera.setPreviewCallbackWithBuffer(new MotionDetector(
				preview));
		// TODO(dichro): calculate the correct size
		this.preview.mCamera.addCallbackBuffer(new byte[1200000]);
		this.preview.mCamera.startPreview();
	}
}