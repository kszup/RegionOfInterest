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
	int x;
	int y;

	Camera mCamera;
	byte[] tempdata;
	boolean mPreviewRunning = false;
	private SurfaceHolder mSurfaceHolder;
	private SurfaceView mSurfaceView;
	
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
    	}
    		break;
    	case (MotionEvent.ACTION_UP): {
    		DrawOnTop.xpos = (int)event.getX();
    		DrawOnTop.ypos = (int)event.getY();
    		Log.d(TAG, "ACTION_UP: "+DrawOnTop.xpos+","+DrawOnTop.ypos);
    		Toast.makeText(this, "ACTION_UP: "+DrawOnTop.xpos+","+DrawOnTop.ypos, Toast.LENGTH_SHORT).show();
    		
    		mCamera.takePicture(mShutterCallback, mPictureCallback, mJpeg);
    	}
    		break;
    	case (MotionEvent.ACTION_MOVE):{
    		DrawOnTop.xpos = (int)event.getX();
    		DrawOnTop.ypos = (int)event.getY();
    		//Log.d(TAG, "ACTION_MOVE: "+DrawOnTop.xpos+","+DrawOnTop.ypos);
    	}
    		break;
    	}
		return super.onTouchEvent(event);
    }
    public void captureImage(Camera mCamera) {
    	mCamera.takePicture(mShutterCallback, mPictureCallback, mJpeg);
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
    PictureCallback mJpeg = new PictureCallback() {
    	public void onPictureTaken(byte[] data, Camera c) { // For compressed picture data
    		if(data !=null) {
    			tempdata = data;
    			imgCapture();
    			Intent TileDisplay = new Intent(RegionOfInterestActivity.this, TileDisplayActivity.class);
    			startActivity(TileDisplay);
    		}
    	}
    };
    void imgCapture() {
    	FileOutputStream outStream = null;
    	try {
    		outStream = new FileOutputStream(String.format("/sdcard/DCIM/Iris/%d.jpg", System.currentTimeMillis()));
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	}
    	Bitmap mBitmap = BitmapFactory.decodeByteArray(tempdata, 0, tempdata.length);
    	mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
    	mBitmap.recycle();
    }
	public void surfaceCreated(SurfaceHolder holder) {
		Log.e(TAG, "surfaceCreated");
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
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
			parameters.setAntibanding(Camera.Parameters.ANTIBANDING_OFF);
			parameters.setColorEffect(Camera.Parameters.EFFECT_MONO);
			mCamera.setParameters(parameters);
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
			mPreviewRunning = true;
		} catch(Exception e) {
			Log.e(TAG,e.toString());
		}
	}
}

class DrawOnTop extends View {
	Bitmap mBitmap;
	static Paint mPaintRed;
	Paint mPaintYellow;
	static int xpos;
	static int ypos;
	int xWidth;
	int yHeight;
	
	public DrawOnTop(Context context) {
		super(context);
		mPaintYellow = new Paint();
		mPaintYellow.setStyle(Paint.Style.FILL);
		mPaintYellow.setColor(Color.YELLOW);

		mPaintRed = new Paint();
		mPaintRed.setStyle(Paint.Style.FILL);
		mPaintRed.setColor(Color.RED);
		mPaintRed.setTextSize(25);
		
		mBitmap = null;
	}
	@Override
	protected void onDraw(Canvas canvas) {
		int canvasWidth = canvas.getWidth();
		int canvasHeight = canvas.getHeight();
		
		xWidth = canvasWidth/4;
		yHeight = canvasHeight/4;
		for (int i = 0; i < 4; i++) {
			canvas.drawLine(xWidth+(xWidth*i), 0, xWidth+(xWidth*i), canvasHeight, mPaintYellow);
			canvas.drawLine(0,yHeight+(yHeight*i),canvasWidth,yHeight+(yHeight*i),mPaintYellow);
		}
		String position = "(x,y) position of touch: ("+ xpos + "," + ypos +")";
		canvas.drawText(position, 25, 25, mPaintRed);
		
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				if (xpos > (xWidth*i) && xpos < (xWidth+(xWidth*i))) {
					if (ypos > (yHeight*j) && ypos < (yHeight+(yHeight*j))) {
						canvas.drawLine(xWidth*i, yHeight*j, (xWidth*i)+xWidth, (yHeight*j), mPaintRed);
						canvas.drawLine(xWidth*i, yHeight*j, xWidth*i, (yHeight*j)+yHeight, mPaintRed);
						canvas.drawLine((xWidth*i)+xWidth, (yHeight*j)+yHeight, xWidth*i, (yHeight*j)+yHeight, mPaintRed);
						canvas.drawLine((xWidth*i)+xWidth, (yHeight*j)+yHeight, (xWidth*i)+xWidth, yHeight*j, mPaintRed);
					}
				}
			}
		}
	}
}
/*
class Preview extends SurfaceView implements SurfaceHolder.Callback {
	SurfaceHolder mHolder;
	Camera mCamera;
	DrawOnTop mDrawOnTop;
	boolean mFinished;
	
	Preview(Context context, DrawOnTop drawOnTop) {
		super(context);
		
		mDrawOnTop = drawOnTop;
		mFinished = false;
		mHolder = getHolder(); // WHAT IS THIS?
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.setPreviewCallback(new PreviewCallback() {
				public void onPreviewFrame(byte[] data, Camera camera) {
					if(mFinished)
						return;
					mDrawOnTop.invalidate();
				}
			});
		} catch (IOException exception) {
			mCamera.release();
			mCamera = null;
		}
	}
	public void surfaceDestroyed(SurfaceHolder holder) {
		mFinished = true;
		mCamera.setPreviewCallback(null);
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
		Toast.makeText(getContext(), "Camera Released", Toast.LENGTH_SHORT).show();
	}
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Camera.Parameters parameters = mCamera.getParameters();
		
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
		
		parameters.setAntibanding(Camera.Parameters.ANTIBANDING_OFF);
		
		parameters.setColorEffect(Camera.Parameters.EFFECT_MONO);
		
		mCamera.setParameters(parameters);
		mCamera.startPreview();		
	}
}*/