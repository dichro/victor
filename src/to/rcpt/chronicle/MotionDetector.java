package to.rcpt.chronicle;

import android.hardware.Camera;
import android.hardware.Camera.Size;

import com.jwetherell.motion_detection.detection.IMotionDetection;
import com.jwetherell.motion_detection.detection.RgbMotionDetection;
import com.jwetherell.motion_detection.image.ImageProcessing;

class MotionDetector implements Camera.PreviewCallback {
	/**
	 * 
	 */
	private final Preview preview;

	/**
	 * @param preview
	 */
	MotionDetector(Preview preview) {
		this.preview = preview;
	}

	private final IMotionDetection detector = new RgbMotionDetection();

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// Log.i(TAG, "got " + data.length + " from " + camera);
		Size previewSize = camera.getParameters().getPreviewSize();
		int[] rgb = ImageProcessing.decodeYUV420SPtoRGB(data,
				previewSize.width, previewSize.height);
		// Log.i(TAG,
		// "detect said "
		// + detector.detect(rgb, previewSize.width,
		// previewSize.height));
		this.preview.mCamera.addCallbackBuffer(data);
	}
}