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

/**
 * A simple wrapper around a Camera and a SurfaceView that renders a centered
 * preview of the Camera to the surface. We need to center the SurfaceView
 * because not all devices have cameras that support preview sizes at the same
 * aspect ratio as the device's display.
 */
class Preview extends FrameLayout {
	final String TAG = "Chronicle";

	private SurfaceView mSurfaceView;
	private SurfaceHolder mHolder;
	Size mPreviewSize;
	List<Size> mSupportedPreviewSizes;
	Camera mCamera;

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
		mHolder.addCallback(new SurfaceHolderCallback(this));
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		setBackgroundColor(-1);
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
		Log.i(TAG, "onMeasure(" + MeasureSpec.toString(widthMeasureSpec) + " "
				+ ", " + MeasureSpec.toString(heightMeasureSpec) + ")");
		setMeasuredDimension(300, 300);
		// int desiredW = MeasureSpec.makeMeasureSpec(400, MeasureSpec.EXACTLY);
		// int desiredH = MeasureSpec.makeMeasureSpec(400, MeasureSpec.EXACTLY);
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
		// int w = resolveSize(desiredW, widthMeasureSpec);
		// int h = resolveSize(desiredH, heightMeasureSpec);
		// Log.i(TAG,
		// "resolved " + MeasureSpec.toString(w) + " x "
		// + MeasureSpec.toString(h));
		// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// Log.i(TAG, "did " + getMeasuredWidth() + "x" + getMeasuredHeight());
		// int mw = getMeasuredWidth(), mh = getMeasuredHeight();
		// boolean ok = true;
		// if (mw < 400) {
		// mw = 400;
		// ok = false;
		// }
		// if (mh < 400) {
		// mh = 400;
		// ok = false;
		// }
		// if (!ok) {
		// Log.i(TAG, "overriding " + mw + "x" + mh);
		// setMeasuredDimension(mw, mh);
		// }
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
		int x = 5;
		if (changed) {
			mSurfaceView.layout(x, x, 300 - 2 * x, 300 - 2 * x);
		}
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

	Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
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

		// Cannot find the one match the aspect ratio, ignore the
		// requirement
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
}