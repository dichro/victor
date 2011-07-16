package to.rcpt.chronicle;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;

public class Review extends Activity {
    private static final String TAG = "Review";
	private byte[] imageData;
	private int imageRotation;

	// sigh. Do you suppose Scala would work in Dalvik?
    interface BitmapFactoryQuery {
    	Bitmap factoryDecode(BitmapFactory.Options options);
    	int getRotation();
    };
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review);
        Intent i = getIntent();
        imageData = i.getByteArrayExtra(Chronicle.IMAGE_DATA);
        imageRotation = i.getIntExtra(Chronicle.IMAGE_ROTATION, 0);
        final String referenceFile = i.getStringExtra(Chronicle.REFERENCE_IMAGE);
        ImageView reference = (ImageView)findViewById(R.id.reference_image);
        ImageView prospect = (ImageView)findViewById(R.id.prospective_image);
        BitmapFactoryQuery referenceBitmap = new BitmapFactoryQuery() {
			@Override
			public Bitmap factoryDecode(Options options) {
				return BitmapFactory.decodeFile(referenceFile, options);
			}
			@Override
			public int getRotation() {
				return 0;
			}
		};
        BitmapFactoryQuery prospectBitmap = new BitmapFactoryQuery() {
			@Override
			public Bitmap factoryDecode(Options options) {
				return BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);
			}
			public int getRotation() {
				switch(imageRotation) {
//				case Surface.ROTATION_0:
//					return 0;
//				case Surface.ROTATION_90:
//					return 270;
//				case Surface.ROTATION_180:
//					return 180;
//				case Surface.ROTATION_270:
//					return 90;
				default:
					return 0;
				}
			}
		};
        
		projectBitmap(reference, referenceBitmap);
		projectBitmap(prospect, prospectBitmap);
    }

	private void projectBitmap(ImageView view, BitmapFactoryQuery bitmapQuery) {
		// TODO(dichro): there should be a way of having the layout declare the proportions
		// of the two imageviews and the button panel, and then just pull out the laid-out
		// dimensions here and use them as the target dimensions for the image. But I can't
		// work out how to do it, so we just aim to make each picture a fixed fraction of the 
		// governing dimension.
		Display display = getWindowManager().getDefaultDisplay();
		float perImageScreenFraction = 0.4f;
        float width = display.getWidth();
        float height = display.getHeight();
        int orientation = getResources().getConfiguration().orientation;
        if(orientation != Configuration.ORIENTATION_LANDSCAPE)
        	height *= perImageScreenFraction;
        if(orientation != Configuration.ORIENTATION_PORTRAIT)
        	width *= perImageScreenFraction;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		bitmapQuery.factoryDecode(options);
		
		int rotation = bitmapQuery.getRotation();
		float hScale;
		float vScale;
		if((rotation % 180) == 0) {
			hScale = (float)options.outHeight / height;
			vScale = (float)options.outWidth / width;
		} else {
			hScale = (float)options.outHeight / width;
			vScale = (float)options.outWidth / height;
		}		
		float maxScale = hScale > vScale ? hScale : vScale;
		options.inSampleSize = (int)maxScale;
		if (options.inSampleSize < 1)
			options.inSampleSize = 1;
		options.inJustDecodeBounds = false;
		
		int scaledHeight = (int)(options.outHeight / maxScale);
		int scaledWidth = (int)(options.outWidth / maxScale);
		
		Log.i(TAG, "Projecting " + options.outWidth + "x" + options.outHeight + " into " +
				width + "x" + height + " by downsampling " + options.inSampleSize + "x and scaling " +
						" to " +
				scaledWidth + "x" + scaledHeight);
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(
				bitmapQuery.factoryDecode(options),
				scaledWidth, scaledHeight, true);
		if(rotation != 0) {
			Matrix m = new Matrix();
			m.postRotate(rotation);
			scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, 
					scaledBitmap.getWidth(), scaledBitmap.getHeight(), m, true);
		}
		view.setImageBitmap(scaledBitmap);
	}
	
	public void acceptImage(View v) {
		
	}
}
