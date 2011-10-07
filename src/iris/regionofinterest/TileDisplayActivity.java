package iris.regionofinterest;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

public class TileDisplayActivity extends Activity {
	private static final String TAG = "iris";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
		setContentView(R.layout.tiledisplay);
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			Log.d(TAG, "Back Button Pressed");
			onBackPressed();
		}
		return super.onKeyDown(keyCode, event);
	}
	public void onBackPressed() {
		Intent ROI = new Intent(TileDisplayActivity.this, RegionOfInterestActivity.class);
		startActivity(ROI);
		
	}
}