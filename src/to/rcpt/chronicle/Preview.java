package to.rcpt.chronicle;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.jwetherell.motion_detection.detection.IMotionDetection;
import com.jwetherell.motion_detection.detection.RgbMotionDetection;
import com.jwetherell.motion_detection.image.ImageProcessing;

/**
 * A simple wrapper around a Camera and a SurfaceView that renders a centered
 * preview of the Camera to the surface. We need to center the SurfaceView
 * because not all devices have cameras that support preview sizes at the same
 * aspect ratio as the device's display.
 */
class Preview extends FrameLayout implements SurfaceHolder.Callback,
		Camera.PreviewCallback {
	ViewAspectRatioMeasurer varm = new ViewAspectRatioMeasurer(1.0);

	public class ViewAspectRatioMeasurer {

		private double aspectRatio;

		/**
		 * Create a ViewAspectRatioMeasurer instance.<br/>
		 * <br/>
		 * Note: Don't construct a new instance everytime your
		 * <tt>View.onMeasure()</tt> method is called.<br />
		 * Instead, create one instance when your <tt>View</tt> is constructed,
		 * and use this instance's <tt>measure()</tt> methods in the
		 * <tt>onMeasure()</tt> method.
		 * 
		 * @param aspectRatio
		 */
		public ViewAspectRatioMeasurer(double aspectRatio) {
			this.aspectRatio = aspectRatio;
		}

		/**
		 * Measure with the aspect ratio given at construction.<br />
		 * <br />
		 * After measuring, get the width and height with the
		 * {@link #getMeasuredWidth()} and {@link #getMeasuredHeight()} methods,
		 * respectively.
		 * 
		 * @param widthMeasureSpec
		 *            The width <tt>MeasureSpec</tt> passed in your
		 *            <tt>View.onMeasure()</tt> method
		 * @param heightMeasureSpec
		 *            The height <tt>MeasureSpec</tt> passed in your
		 *            <tt>View.onMeasure()</tt> method
		 */
		public void measure(int widthMeasureSpec, int heightMeasureSpec) {
			measure(widthMeasureSpec, heightMeasureSpec, this.aspectRatio);
		}

		/**
		 * Measure with a specific aspect ratio<br />
		 * <br />
		 * After measuring, get the width and height with the
		 * {@link #getMeasuredWidth()} and {@link #getMeasuredHeight()} methods,
		 * respectively.
		 * 
		 * @param widthMeasureSpec
		 *            The width <tt>MeasureSpec</tt> passed in your
		 *            <tt>View.onMeasure()</tt> method
		 * @param heightMeasureSpec
		 *            The height <tt>MeasureSpec</tt> passed in your
		 *            <tt>View.onMeasure()</tt> method
		 * @param aspectRatio
		 *            The aspect ratio to calculate measurements in respect to
		 */
		public void measure(int widthMeasureSpec, int heightMeasureSpec,
				double aspectRatio) {
			int widthMode = MeasureSpec.getMode(widthMeasureSpec);
			int widthSize = widthMode == MeasureSpec.UNSPECIFIED ? Integer.MAX_VALUE
					: MeasureSpec.getSize(widthMeasureSpec);
			int heightMode = MeasureSpec.getMode(heightMeasureSpec);
			int heightSize = heightMode == MeasureSpec.UNSPECIFIED ? Integer.MAX_VALUE
					: MeasureSpec.getSize(heightMeasureSpec);

			if (heightMode == MeasureSpec.EXACTLY
					&& widthMode == MeasureSpec.EXACTLY) {
				/*
				 * Possibility 1: Both width and height fixed
				 */
				measuredWidth = widthSize;
				measuredHeight = heightSize;

			} else if (heightMode == MeasureSpec.EXACTLY) {
				/*
				 * Possibility 2: Width dynamic, height fixed
				 */
				measuredWidth = (int) Math.min(widthSize, heightSize
						* aspectRatio);
				measuredHeight = (int) (measuredWidth / aspectRatio);

			} else if (widthMode == MeasureSpec.EXACTLY) {
				/*
				 * Possibility 3: Width fixed, height dynamic
				 */
				measuredHeight = (int) Math.min(heightSize, widthSize
						/ aspectRatio);
				measuredWidth = (int) (measuredHeight * aspectRatio);

			} else {
				/*
				 * Possibility 4: Both width and height dynamic
				 */
				if (widthSize > heightSize * aspectRatio) {
					measuredHeight = heightSize;
					measuredWidth = (int) (measuredHeight * aspectRatio);
				} else {
					measuredWidth = widthSize;
					measuredHeight = (int) (measuredWidth / aspectRatio);
				}

			}
		}

		private Integer measuredWidth = null;

		/**
		 * Get the width measured in the latest call to <tt>measure()</tt>.
		 */
		public int getMeasuredWidth() {
			if (measuredWidth == null) {
				throw new IllegalStateException(
						"You need to run measure() before trying to get measured dimensions");
			}
			return measuredWidth;
		}

		private Integer measuredHeight = null;

		/**
		 * Get the height measured in the latest call to <tt>measure()</tt>.
		 */
		public int getMeasuredHeight() {
			if (measuredHeight == null) {
				throw new IllegalStateException(
						"You need to run measure() before trying to get measured dimensions");
			}
			return measuredHeight;
		}

	}

	private final String TAG = "Chronicle";

	private SurfaceView mSurfaceView;
	private SurfaceHolder mHolder;
	private Size mPreviewSize;
	private List<Size> mSupportedPreviewSizes;
	private Camera mCamera;

	Preview(Context context) {
		super(context);
		mSurfaceView = new SurfaceView(context);
		addView(mSurfaceView);
		// LayoutParams params = (LayoutParams) mSurfaceView.getLayoutParams();
		// params.height = LayoutParams.FILL_PARENT;
		// params.width = LayoutParams.FILL_PARENT;
		// mSurfaceView.setLayoutParams(params);
		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = mSurfaceView.getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void setCamera(Camera camera) {
		mCamera = camera;
		if (mCamera != null) {
			mSupportedPreviewSizes = mCamera.getParameters()
					.getSupportedPreviewSizes();
			requestLayout();
		}
	}

	public void switchCamera(Camera camera) {
		setCamera(camera);
		try {
			camera.setPreviewDisplay(mHolder);
		} catch (IOException exception) {
			Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
		}
		if (mSupportedPreviewSizes != null) {
			mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes,
					getMeasuredWidth(), getMeasuredHeight());
		}
		Camera.Parameters parameters = camera.getParameters();
		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		requestLayout();
		camera.setParameters(parameters);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int desiredW = MeasureSpec.makeMeasureSpec(400, MeasureSpec.EXACTLY);
		int desiredH = MeasureSpec.makeMeasureSpec(400, MeasureSpec.EXACTLY);
		// // We purposely disregard child measurements because act as a
		// // wrapper to a SurfaceView that centers the camera preview instead
		// // of stretching it.
		// final int width = resolveSize(getSuggestedMinimumWidth(),
		// widthMeasureSpec);
		// final int height = resolveSize(getSuggestedMinimumHeight(),
		// heightMeasureSpec);
		// Log.i(TAG, "size " + width + "x" + height);
		// setMeasuredDimension(width, height);
		// if (mSupportedPreviewSizes != null) {
		// mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width,
		// height);
		// }
		// int h = getSuggestedMinimumHeight();
		// int w = getSuggestedMinimumWidth();
		int w = resolveSize(desiredW, widthMeasureSpec);
		int h = resolveSize(desiredH, heightMeasureSpec);
		Log.i(TAG, "spec " + MeasureSpec.toString(widthMeasureSpec) + " "
				+ " x " + MeasureSpec.toString(heightMeasureSpec));
		Log.i(TAG,
				"resolved " + MeasureSpec.toString(w) + " x "
						+ MeasureSpec.toString(h));
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		Log.i(TAG, "did " + getMeasuredWidth() + "x" + getMeasuredHeight());
		int mw = getMeasuredWidth(), mh = getMeasuredHeight();
		boolean ok = true;
		if (mw < 400) {
			mw = 400;
			ok = false;
		}
		if (mh < 400) {
			mh = 400;
			ok = false;
		}
		if (!ok) {
			// Log.i(TAG, "overriding " + mw + "x" + mh);
			// setMeasuredDimension(mw, mh);
		}
		// setMeasuredDimension(w, h);
		// varm.measure(widthMeasureSpec, heightMeasureSpec);
		// Log.i(TAG,
		// "trying " + varm.getMeasuredWidth() + "x"
		// + varm.getMeasuredHeight());
		// setMeasuredDimension(varm.getMeasuredWidth(),
		// varm.getMeasuredHeight());
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.i(TAG, "onLayout(" + changed + ", " + l + ", " + t + ", " + r
				+ ", " + b + ")");
		super.onLayout(changed, l, t, r, b);
		// if (changed)
		// for (int i = 0; i < getChildCount(); i++) {
		// final View child = getChildAt(i);
		//
		// final int width = r - l;
		// final int height = b - t;
		//
		// int previewWidth = width;
		// int previewHeight = height;
		// if (mPreviewSize != null) {
		// previewWidth = mPreviewSize.width;
		// previewHeight = mPreviewSize.height;
		// }
		//
		// // Center the child SurfaceView within the parent.
		// // if (width * previewHeight > height * previewWidth) {
		// // final int scaledChildWidth = previewWidth * height
		// // / previewHeight;
		// // child.layout((width - scaledChildWidth) / 2, 0,
		// // (width + scaledChildWidth) / 2, height);
		// // } else {
		// // final int scaledChildHeight = previewHeight * width
		// // / previewWidth;
		// // child.layout(0, (height - scaledChildHeight) / 2, width,
		// // (height + scaledChildHeight) / 2);
		// // }
		// child.layout(0, 0, 240, 240);
		// }
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		try {
			if (mCamera != null) {
				Log.i(TAG, "size " + mCamera.getParameters().getPreviewSize());
				mCamera.setPreviewDisplay(holder);
			}
		} catch (IOException exception) {
			Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		if (mCamera != null) {
			mCamera.stopPreview();
		}
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		Log.i(TAG, "getOptimalPreviewSize " + w + "x" + h);
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		Log.i(TAG, "out " + optimalSize.width + "x" + optimalSize.height);
		return optimalSize;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		if (mSupportedPreviewSizes != null) {
			mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes,
					getMeasuredWidth(), getMeasuredHeight());
			// 280, 280);
		}
		// interesting. If you launch with the screen off, NPE occurs here.
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		requestLayout();
		mCamera.setParameters(parameters);
		mCamera.setPreviewCallbackWithBuffer(this);
		// TODO(dichro): calculate the correct size
		mCamera.addCallbackBuffer(new byte[1200000]);
		mCamera.startPreview();
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
		mCamera.addCallbackBuffer(data);
	}
}