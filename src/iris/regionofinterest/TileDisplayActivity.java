package iris.regionofinterest;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;

public class TileDisplayActivity extends Activity {
	private static final String TAG = "iris";
	static final String IrisPATH = "/DCIM/Iris/";
	
	// For WebView
	//static WebView mWebView;
	WebView mWebView;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
		setContentView(R.layout.tiledisplay);
		
		mWebView = (WebView)findViewById(R.id.webview);	
		
		Bundle b = getIntent().getExtras();
		final long fileName = b.getLong("fileName");
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.loadUrl("file:///sdcard/DCIM/Iris/"+fileName+".png");
		Log.d(TAG,"after mWebView");
	}
	@Override
	protected void onDestroy() {
		// Clean up
		super.onDestroy();
		mWebView.destroy();
		Log.d(TAG, "onDestroy in tileDisplayActivity");
		//Toast.makeText(this,"onDestroy in tileDisplayActivity", Toast.LENGTH_SHORT).show();
	}
}