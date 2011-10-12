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
		
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Get specific image Path and display
		Bundle b= getIntent().getExtras();
		final int startX = b.getInt("startX");
		final int startY = b.getInt("startY");
		final int width = b.getInt("width");
		final int height = b.getInt("height");
		Log.d(TAG,"Bundled Values: startX-"+startX+", startY-"+startY+", width-"+width+", height-"+height);
		
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 0 && resultCode == RESULT_OK) {
			
			String tmp = data.getExtras().getString("selectedFile");
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			options.inSampleSize=2;
			Bitmap bm = BitmapFactory.decodeFile(tmp,options);
			Bitmap newbm = Bitmap.createBitmap(bm,startX,startY,width,height,null,false);
			tile.setImageBitmap(newbm);
			Log.d(TAG,"setImageBitmap"+bm.getWidth()+"x"+bm.getHeight());
			//processImg(selectedImg);
		}
	}
	void processImg(Bitmap image) {

	}
}