package to.rcpt.chronicle;


import android.app.Activity;
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
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

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
