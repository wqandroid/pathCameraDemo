package com.wq.letpapa;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImage.OnPictureSavedListener;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageLookupFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageToneCurveFilter;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.wq.letpapa.gpuimg.GPUImageFilterTools.FilterAdjuster;
import com.wq.letpapa.utils.CameraHelper;
import com.wq.letpapa.utils.CameraHelper.CameraInfo2;

public class MainActivity extends Activity {

	private GPUImage mGPUImage;
	private CameraHelper mCameraHelper;
	private CameraLoader mCamera;
	private GPUImageFilter mFilter;
	private FilterAdjuster mFilterAdjuster;
	GLSurfaceView glSurfaceView;

	RelativeLayout camera_emptyview;

	ImageView iv_changecamera,take_camera;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.fragment_main);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		glSurfaceView = (GLSurfaceView) findViewById(R.id.surfaceView);
		take_camera=(ImageView) findViewById(R.id.take_camera);
		camera_emptyview = (RelativeLayout) findViewById(R.id.camera_emptyview);
		iv_changecamera=(ImageView) findViewById(R.id.iv_changecamera);
		mGPUImage = new GPUImage(this);
		mGPUImage.setGLSurfaceView(glSurfaceView);
		mCameraHelper = new CameraHelper(this);
		mCamera = new CameraLoader();
		changePreviewOverlayHeight(camera_emptyview);

		iv_changecamera.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mCamera.switchCamera();				
			}
		});
		take_camera.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mCamera.mCameraInstance.getParameters().getFocusMode()
						.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
					takePicture();
				} else {
					mCamera.mCameraInstance
							.autoFocus(new Camera.AutoFocusCallback() {

								@Override
								public void onAutoFocus(final boolean success,
										final Camera camera) {
									takePicture();
								}
							});
				}
			}
		});
	}

	public void doclick(View v) {
		switchFilterTo(new GPUImageGrayscaleFilter());
		switch (v.getId()) {
		case R.id.filter_heibai:
			switchFilterTo(new GPUImageGrayscaleFilter());
			break;
		case R.id.filter_holga:
			GPUImageLookupFilter amatorka = new GPUImageLookupFilter();
			amatorka.setBitmap(BitmapFactory.decodeResource(getResources(),
					R.drawable.lookup_amatorka));
			switchFilterTo(amatorka);
			break;
		case R.id.filters_summer:
			GPUImageToneCurveFilter toneCurveFilter = new GPUImageToneCurveFilter();
			toneCurveFilter.setFromCurveFileInputStream(getResources()
					.openRawResource(R.raw.tone_cuver_sample));
			switchFilterTo(toneCurveFilter);
			break;
		case R.id.filter_pro:
			GPUImageToneCurveFilter prof = new GPUImageToneCurveFilter();
			prof.setFromCurveFileInputStream(getResources().openRawResource(
					R.raw.pro));
			switchFilterTo(prof);
			break;
		case R.id.filters_shoreline:
			GPUImageToneCurveFilter filters_shoreline = new GPUImageToneCurveFilter();
			filters_shoreline.setFromCurveFileInputStream(getResources()
					.openRawResource(R.raw.filters_shoreline));
			switchFilterTo(filters_shoreline);
			break;
		case R.id.filters_vivid:
			GPUImageToneCurveFilter filters_vivid = new GPUImageToneCurveFilter();
			filters_vivid.setFromCurveFileInputStream(getResources()
					.openRawResource(R.raw.filters_vivid));
			switchFilterTo(filters_vivid);
			break;
		case R.id.filters_sol:
			GPUImageToneCurveFilter filters_sol = new GPUImageToneCurveFilter();
			filters_sol.setFromCurveFileInputStream(getResources()
					.openRawResource(R.raw.filters_sol));
			switchFilterTo(filters_sol);
			break;
		case R.id.filters_instant:
			GPUImageToneCurveFilter filters_instant = new GPUImageToneCurveFilter();
			filters_instant.setFromCurveFileInputStream(getResources()
					.openRawResource(R.raw.filters_instant));
			switchFilterTo(filters_instant);
			break;
		}
	}

	private void switchFilterTo(final GPUImageFilter filter) {
		if (mFilter == null
				|| (filter != null && !mFilter.getClass().equals(
						filter.getClass()))) {
			mFilter = filter;
			mGPUImage.setFilter(mFilter);
			mFilterAdjuster = new FilterAdjuster(mFilter);
		}
	}

	private void changePreviewOverlayHeight(RelativeLayout frameLayout) {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;

		RelativeLayout.LayoutParams linearLayoutParams;
		linearLayoutParams = (RelativeLayout.LayoutParams) frameLayout
				.getLayoutParams();
		linearLayoutParams.height = width;
		frameLayout.setLayoutParams(linearLayoutParams);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mCamera.onResume();
	}

	@Override
	protected void onPause() {
		mCamera.onPause();
		super.onPause();
	}

	
	
	
	
	
	   
	
	
	
	Size camerasize;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private void takePicture() {
		// TODO get a size that is about the size of the screen
		Camera.Parameters params = mCamera.mCameraInstance.getParameters();
		List<Size> sizes = params.getSupportedPictureSizes();
		for (Size size : sizes) {
			System.out.println("输出图片大小[" + size.width + ":" + size.height
					+ "]\n");
			// 07-16 14:59:37.930: I/System.out(14550): 输出图片大小[480:320]
			// 07-16 14:59:37.930: I/System.out(14550): 输出图片大小[640:368]
			// 07-16 14:59:37.930: I/System.out(14550): 输出图片大小[640:480]
			// 07-16 14:59:37.930: I/System.out(14550): 输出图片大小[1280:720]
			// 07-16 14:59:37.930: I/System.out(14550): 输出图片大小[1280:960]
			// 07-16 14:59:37.930: I/System.out(14550): 输出图片大小[1920:1080]
			// 07-16 14:59:37.930: I/System.out(14550): 输出图片大小[2048:1536]
			// 07-16 14:59:37.930: I/System.out(14550): 输出图片大小[2592:1936]
			// 07-16 14:59:37.930: I/System.out(14550): 输出图片大小[2592:1952]
			// 07-16 14:59:37.930: I/System.out(14550): 输出图片大小[3264:2448]
			// 07-16 14:59:37.930: I/System.out(14550): 输出图片大小[2048:1152]
			// 07-16 14:59:37.930: I/System.out(14550): 输出图片大小[2592:1456]
			// 07-16 14:59:37.930: I/System.out(14550): 输出图片大小[3264:1836]
		}
		params.setPictureSize(1280, 960);
		params.setRotation(90);
		mCamera.mCameraInstance.setParameters(params);
		for (Camera.Size size2 : mCamera.mCameraInstance.getParameters()
				.getSupportedPictureSizes()) {
			Log.i("ASDF", "Supported: " + size2.width + "x" + size2.height);
		}
		mCamera.mCameraInstance.takePicture(null, null,
				new Camera.PictureCallback() {

					@Override
					public void onPictureTaken(byte[] data, final Camera camera) {

						final File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
						if (pictureFile == null) {
							Log.d("ASDF",
									"Error creating media file, check storage permissions");
							return;
						}

						try {
							FileOutputStream fos = new FileOutputStream(
									pictureFile);
							fos.write(data);
							fos.close();
						} catch (FileNotFoundException e) {
							Log.d("ASDF", "File not found: " + e.getMessage());
						} catch (IOException e) {
							Log.d("ASDF",
									"Error accessing file: " + e.getMessage());
						}

						data = null;
						Bitmap bitmap = BitmapFactory.decodeFile(pictureFile
								.getAbsolutePath());
						// mGPUImage.setImage(bitmap);
						final GLSurfaceView view = (GLSurfaceView) findViewById(R.id.surfaceView);
						view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
						mGPUImage.saveToPictures(bitmap, "GPUImage",
								System.currentTimeMillis() + ".jpg",
								new OnPictureSavedListener() {

									@Override
									public void onPictureSaved(final Uri uri) {
										pictureFile.delete();
										camera.startPreview();
										view.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
									}
								});
					}
				});
	}
	private static File getOutputMediaFile(final int type) {
		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"letpapa");
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("letpapa", "failed to create directory");
				return null;
			}
		}
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}
		return mediaFile;
	}
	
	private class CameraLoader {
		private int mCurrentCameraId = 0;
		private Camera mCameraInstance;

		public void onResume() {
			setUpCamera(mCurrentCameraId);
		}

		public void onPause() {
			releaseCamera();
		}

		public void switchCamera() {
			releaseCamera();
			mCurrentCameraId = (mCurrentCameraId + 1)
					% mCameraHelper.getNumberOfCameras();
			setUpCamera(mCurrentCameraId);
		}

		private void setUpCamera(final int id) {
			mCameraInstance = getCameraInstance(id);
			Parameters parameters = mCameraInstance.getParameters();
			// TODO adjust by getting supportedPreviewSizes and then choosing
			// the best one for screen size (best fill screen)
			// mCameraInstance.getParameters().getSupportedPreviewSizes();
			// parameters.setPreviewSize(720, 480);
			List<Size> sizes = parameters.getSupportedPreviewSizes();
			for (Size size : sizes) {
				System.out
						.println("[" + size.width + ":" + size.height + "]\n");
				if (size.width == 1280) {
					camerasize = size;
					break;
				}
			}
			if (parameters.getSupportedFocusModes().contains(
					Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
				parameters
						.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			}
			mCameraInstance.setParameters(parameters);

			int orientation = mCameraHelper.getCameraDisplayOrientation(
					MainActivity.this, mCurrentCameraId);
			CameraInfo2 cameraInfo = new CameraInfo2();
			mCameraHelper.getCameraInfo(mCurrentCameraId, cameraInfo);
			// resizePreview(camerasize.width, camerasize.height);
			boolean flipHorizontal = cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT ? true
					: false;
			mGPUImage.setUpCamera(mCameraInstance, orientation, flipHorizontal,
					false);

		}

		/** A safe way to get an instance of the Camera object. */
		private Camera getCameraInstance(final int id) {
			Camera c = null;
			try {
				c = mCameraHelper.openCamera(id);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return c;
		}

		private void releaseCamera() {
			mCameraInstance.setPreviewCallback(null);
			mCameraInstance.release();
			mCameraInstance = null;
		}
	}

}
