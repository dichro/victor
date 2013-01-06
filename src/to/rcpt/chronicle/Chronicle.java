package to.rcpt.chronicle;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

public class Chronicle extends Activity {
	public class JpegCallback implements PictureCallback {
		public String referenceImage = null;

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.i(TAG, "onPictureTaken");
//			Intent i = new Intent(Chronicle.this, Review.class);
////			i.putExtra(IMAGE_DATA, data);
//			i.putExtra(IMAGE_ROTATION, getWindowManager().getDefaultDisplay().getRotation());
//			if(referenceImage != null)
//				i.putExtra(REFERENCE_IMAGE, referenceImage);
//			startActivity(i);
		}
	}

//	public static final String IMAGE_DATA = "imageData";
//	public static final String IMAGE_ROTATION = "imageRotation";
    public static final String REFERENCE_IMAGE = "referenceImage";
	private static final String DEFAULT_CAMERA = "defaultCamera";
	private static final String TAG = "Chronicle";
	private Preview mPreview;
    Camera mCamera;
    int numberOfCameras;
    int cameraCurrentlyLocked;
	public JpegCallback pictureCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Create a RelativeLayout container that will hold a SurfaceView,
        // and set it as the content of our activity.
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		String backgroundImagePath = prefs.getString(REFERENCE_IMAGE, null);
        Drawable d;
        if(backgroundImagePath == null)
        	d = null;
        else
        	d = getBackgroundDrawable(backgroundImagePath);
		pictureCallback = new JpegCallback();
		pictureCallback.referenceImage = backgroundImagePath;
        mPreview = new Preview(this, d, pictureCallback);
        setContentView(mPreview);

        numberOfCameras = Camera.getNumberOfCameras();
        cameraCurrentlyLocked = prefs.getInt(DEFAULT_CAMERA, -1);
        Log.i(TAG, "read def camera " + cameraCurrentlyLocked + " of " + numberOfCameras);

        if((cameraCurrentlyLocked < 0) || (cameraCurrentlyLocked >= numberOfCameras)) {
        	CameraInfo cameraInfo = new CameraInfo();
        	for (int i = 0; i < numberOfCameras; i++) {
        		Camera.getCameraInfo(i, cameraInfo);
        		if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
        			cameraCurrentlyLocked = i;
        		}
        	}
        }
       
    }

    @Override
    protected void onResume() {
        super.onResume();

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
        if(numberOfCameras <= 1) {
        	MenuItem mi = menu.findItem(R.id.switch_camera);
        	mi.setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.pick_picture:
        	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        	intent.setType("image/*");
        	startActivityForResult(intent, 0);
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
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 

        switch(requestCode) { 
        case 0:
            if(resultCode == RESULT_OK){  
                Uri selectedImage = imageReturnedIntent.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();
                
        		mPreview.setBackgroundDrawable(getBackgroundDrawable(filePath));

                Editor prefs = getPreferences(MODE_PRIVATE).edit();
                prefs.putString(REFERENCE_IMAGE, filePath);
                prefs.commit();
                pictureCallback.referenceImage = filePath;
            }
        }
    }

	private Drawable getBackgroundDrawable(String filePath) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 8;
		Bitmap b = BitmapFactory.decodeFile(filePath, options);
		BitmapDrawable d = new BitmapDrawable(getResources(), b);
		d.setAlpha(128);
		return d;
	}
}

// ----------------------------------------------------------------------

/**
 * A simple wrapper around a Camera and a SurfaceView that renders a centered preview of the Camera
 * to the surface. We need to center the SurfaceView because not all devices have cameras that
 * support preview sizes at the same aspect ratio as the device's display.
 */
class Preview extends FrameLayout implements SurfaceHolder.Callback {
    private final String TAG = "Chronicle";

    SurfaceView mSurfaceView;
    SurfaceHolder mHolder;
    Size mPreviewSize;
    List<Size> mSupportedPreviewSizes;
    Camera mCamera;
    PictureCallback pictureCallback;
	private TextView text;

    Preview(Context context, Drawable d, PictureCallback cb) {
        super(context);
        pictureCallback = cb;
        mSurfaceView = new SurfaceView(context);
        addView(mSurfaceView);
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//		mSurfaceView.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				mCamera.takePicture(null, null, pictureCallback);
//			}
//		});
    }

    public void setBackgroundDrawable(Drawable d) {
    	text.setBackgroundDrawable(d);
    }
    
    public void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
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
           mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, getMeasuredWidth(), getMeasuredHeight());
       }
       Camera.Parameters parameters = camera.getParameters();
       parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
       requestLayout();
       camera.setParameters(parameters);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed)
          for(int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if (mPreviewSize != null) {
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
            }

            // Center the child SurfaceView within the parent.
            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0,
                        (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2,
                        width, (height + scaledChildHeight) / 2);
            }
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        try {
            if (mCamera != null) {
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
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
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
        return optimalSize;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, getMeasuredWidth(), getMeasuredHeight());
        }
        // interesting. If you launch with the screen off, NPE occurs here.
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        requestLayout();
        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }
}