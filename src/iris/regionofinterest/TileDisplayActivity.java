package iris.regionofinterest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class TileDisplayActivity extends Activity {
	private static final String TAG = "iris";
	static final String IrisPATH = "/DCIM/Iris/";
	
	ImageView tile;
	int mStartX;
	int mStartY;
	int mWidth;
	int mHeight;
	int mYdiv;
	int mXdiv;
	Bitmap bm;
	Bitmap newbm;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
		setContentView(R.layout.tiledisplay);
		
		tile = (ImageView)findViewById(R.id.image);
		String ImageDir = Environment.getExternalStorageDirectory().getAbsolutePath()+IrisPATH;
		
		Intent i = new Intent(this, ListFiles.class);
		i.putExtra("directory", ImageDir);
		startActivityForResult(i,0);		
		
		Bundle b = getIntent().getExtras();
		final int startX = b.getInt("startX");
		final int startY = b.getInt("startY");
		final int width = b.getInt("width");
		final int height = b.getInt("height");
		final int yDiv = b.getInt("yDiv");
		final int xDiv = b.getInt("xDiv");
		
		Log.d(TAG,"Bundled Values in onCreate: Tile Number: "+startX+","+startY+", width-"+width+", height-"+height+" Divs: "+xDiv+","+yDiv);
		mStartX = startX;
		mStartY = startY;
		mWidth = width;
		mHeight = height;
		mYdiv = yDiv;
		mXdiv = xDiv;		
		
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Get specific image Path and display	
		Log.d(TAG,"Bundled Values: mStartX-"+mStartX+", mStartY-"+mStartY+", width-"+mWidth+", height-"+mHeight);
		
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 0 && resultCode == RESULT_OK) {
			
			String tmp = data.getExtras().getString("selectedFile");
			BitmapFactory.Options options = new BitmapFactory.Options();
			//options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			//options.inSampleSize=2;
			bm = BitmapFactory.decodeFile(tmp,options);
			mWidth = bm.getWidth()/mXdiv;
			mHeight = bm.getHeight()/mYdiv;
			mStartX = mStartX*mWidth;
			mStartY = mStartY*mHeight;
			Log.d(TAG,"[m Values] mStartX: "+mStartX+" mStartY: "+mStartY+" mWidth: "+mWidth+" mHeight: "+mHeight);
			newbm = Bitmap.createBitmap(bm,mStartX,mStartY,mWidth,mHeight,null,false);
			tile.setImageBitmap(newbm);
			Log.d(TAG,"setImageBitmap: "+newbm.getWidth()+"x"+newbm.getHeight());
		}
	}
	@Override
	protected void onDestroy() {
		// Clean up
		super.onDestroy();
		bm.recycle();
		newbm.recycle();
	}
}
