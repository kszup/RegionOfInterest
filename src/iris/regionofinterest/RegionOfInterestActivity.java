package iris.regionofinterest;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class RegionOfInterestActivity extends Activity implements SurfaceHolder.Callback {
    /** Called when the activity is first created. */
	private static final String TAG = "iris";
	
	private DrawOnTop mDrawOnTop;
	
	Camera mCamera;
	boolean mPreviewRunning = false;
	private SurfaceHolder mSurfaceHolder;
	private SurfaceView mSurfaceView;
	
	long fileName;
	Bitmap mBitmap;
	Bitmap newbm;
	
	int mWidth;
	int mHeight;
	int mStartX;
	int mStartY;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        mDrawOnTop = new DrawOnTop(this);
        
        setContentView(R.layout.cam);
        mSurfaceView = (SurfaceView)findViewById(R.id.surface);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // Surface doesn't own buffer (speed improvement)
        
        addContentView(mDrawOnTop, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));  
    }
	@Override
    public boolean onTouchEvent(MotionEvent event) {
    	int action = event.getAction();
    	switch(action) {
    	case (MotionEvent.ACTION_DOWN): {
    		DrawOnTop.xpos = (int)event.getX();
    		DrawOnTop.ypos = (int)event.getY();
    		Log.d(TAG, "ACTION_DOWN: "+DrawOnTop.xpos+","+DrawOnTop.ypos);
    		break;
    		}
    	case (MotionEvent.ACTION_UP): {
    		DrawOnTop.xpos = (int)event.getX();
    		DrawOnTop.ypos = (int)event.getY();
    		Log.d(TAG, "ACTION_UP: "+DrawOnTop.xpos+","+DrawOnTop.ypos);
    		
    		Toast.makeText(this, "Cropping and Saving. Please wait... "+DrawOnTop.xpos+","+DrawOnTop.ypos, Toast.LENGTH_SHORT).show();
    		captureImage();
    		break;
    		}
    	case (MotionEvent.ACTION_MOVE):{
    		DrawOnTop.xpos = (int)event.getX();
    		DrawOnTop.ypos = (int)event.getY();
    		//Log.d(TAG, "ACTION_MOVE: "+DrawOnTop.xpos+","+DrawOnTop.ypos);
    		break;
    		}
    	}
		return super.onTouchEvent(event);
    }
    public void captureImage() {
    	mCamera.takePicture(mShutterCallback, mPictureCallback, mPNG);
    	Log.d(TAG, "mCamera.takePicture");   	
    }
    ShutterCallback mShutterCallback = new ShutterCallback() { // Do something when picture is taken (noise)
    	//@Override
    	public void onShutter() {}
    };
    PictureCallback mPictureCallback = new PictureCallback() { // For raw picture data if hw can handle...
    	public void onPictureTaken(byte[] data, Camera c){ 
    		Log.d(TAG, "onPictureTaken - raw"); 
    	}
    };
    PictureCallback mPNG = new PictureCallback() {
    	public void onPictureTaken(byte[] data, Camera c) { // For compressed picture data
    		if(data !=null) {
    			imgCapture(data);
    			tileDisplay();
    			data = null;
    		} else {
    			Log.e(TAG, "mPNG: no data!");
    		}
    		Log.d(TAG, "onPictureTaken - mPNG");     		
    	}
    };
    void tileDisplay() { 
    // Start tileDisplayActivity
    	Intent tileDisplay = new Intent(RegionOfInterestActivity.this, TileDisplayActivity.class);
    	Bundle b = new Bundle();
    	b.putLong("fileName", fileName);
        tileDisplay.putExtras(b);
		startActivity(tileDisplay);
    }
    void imgCapture(byte[] data) {
    // Saving only cropped image to sdcard
    	FileOutputStream outStream = null;
    	fileName = System.currentTimeMillis();
    	try {
    		outStream = new FileOutputStream(String.format("/sdcard/DCIM/Iris/%d.png", fileName));
    	} catch (FileNotFoundException e) {
    		Log.e(TAG,"caught FileNotFoundException");
    		e.printStackTrace();
    	}
    	mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
    	mWidth = mBitmap.getWidth()/mDrawOnTop.xDiv;
    	mHeight = mBitmap.getHeight()/mDrawOnTop.yDiv;
    	mStartX = mWidth*mDrawOnTop.startX;
    	mStartY = mHeight*mDrawOnTop.startY;
    	newbm = Bitmap.createBitmap(mBitmap,mStartX,mStartY,mWidth,mHeight,null,false);
    	newbm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
    	mBitmap.recycle();
    	data = null;
    }
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated");
		try {		
			mCamera = Camera.open();
			mCamera.setPreviewDisplay(holder);
			mCamera.setPreviewCallback(new PreviewCallback() {
				public void onPreviewFrame(byte[] data, Camera camera) {
					mDrawOnTop.invalidate();
				}
			});
		} catch (IOException e) {
			mCamera.release();
			mCamera = null;
			Log.e(TAG,"caught IOException");
			Log.e(TAG,e.toString());
		}
	}
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed");
		if (mPreviewRunning) {
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mPreviewRunning = false;
			mCamera.release();
			mCamera = null;
		}
		Toast.makeText(this, "Camera Released", Toast.LENGTH_SHORT).show();
	}
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Log.d(TAG, "surfaceChanged");
		try {
			if (mPreviewRunning) {
				mCamera.stopPreview();
				mPreviewRunning = false;
			}
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			parameters.setAntibanding(Camera.Parameters.ANTIBANDING_OFF);
			parameters.setColorEffect(Camera.Parameters.EFFECT_MONO);
			mCamera.setParameters(parameters);
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
			mPreviewRunning = true;
		} catch(Exception e) {
			Log.e(TAG,"caught stopPreview exception");
			Log.e(TAG,e.toString());
		}
	}
	public void onRestart() {
		super.onRestart();
		Log.d(TAG,"mBitmap: "+mBitmap); // Should give null
		Toast.makeText(this, "onRestart", Toast.LENGTH_SHORT).show();
	}
	public void onPause() {
		super.onPause();
		if(mBitmap != null) {
			mBitmap.recycle();
			mBitmap = null;
		}
		Toast.makeText(this,"onPause in RegionOfInterest", Toast.LENGTH_SHORT).show();
	}
}

class DrawOnTop extends View { 
// Drawing grid and marking selected tile
	static Paint mPaintRed;
	Paint mPaintYellow;
	static int xpos;
	static int ypos;
	int xWidth;
	int yHeight;
	int startX = 0;
	int startY = 0;

	//Grid is 6x5
	int xDiv = 6;
	int yDiv = 5;
	
	public DrawOnTop(Context context) {
		super(context);
		mPaintYellow = new Paint();
		mPaintYellow.setStyle(Paint.Style.FILL);
		mPaintYellow.setColor(Color.YELLOW);

		mPaintRed = new Paint();
		mPaintRed.setStyle(Paint.Style.FILL);
		mPaintRed.setColor(Color.RED);
		mPaintRed.setTextSize(25);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		int canvasWidth = canvas.getWidth();
		int canvasHeight = canvas.getHeight();
		xWidth = canvasWidth/xDiv;
		yHeight = canvasHeight/yDiv;
		for (int i = 0; i < xDiv-1; i++) {
			canvas.drawLine(xWidth+(xWidth*i), 0, xWidth+(xWidth*i), canvasHeight, mPaintYellow);
		}
		for (int i = 0; i < yDiv-1; i++) {
			canvas.drawLine(0,yHeight+(yHeight*i),canvasWidth,yHeight+(yHeight*i),mPaintYellow);
		}
		String position = "(x,y) position of touch: ("+ xpos + "," + ypos +")";
		canvas.drawText(position, 25, 25, mPaintRed);
		
		for (int i = 0; i < xDiv+1; i++) {
			for (int j = 0; j < yDiv+1; j++) {
				if (xpos > (xWidth*i) && xpos < (xWidth+(xWidth*i))) {
					if (ypos > (yHeight*j) && ypos < (yHeight+(yHeight*j))) {
						canvas.drawLine(xWidth*i, yHeight*j, (xWidth*i)+xWidth, (yHeight*j), mPaintRed);
						canvas.drawLine(xWidth*i, yHeight*j, xWidth*i, (yHeight*j)+yHeight, mPaintRed);
						canvas.drawLine((xWidth*i)+xWidth, (yHeight*j)+yHeight, xWidth*i, (yHeight*j)+yHeight, mPaintRed);
						canvas.drawLine((xWidth*i)+xWidth, (yHeight*j)+yHeight, (xWidth*i)+xWidth, yHeight*j, mPaintRed);
						startX = i;
						startY = j;
					}
				}
			}
		}
		
		String tile = "Tile Number (start point): "+startX+","+startY+" Tile size: "+xWidth+","+yHeight;
		canvas.drawText(tile, 25, 50, mPaintRed);
	}
}