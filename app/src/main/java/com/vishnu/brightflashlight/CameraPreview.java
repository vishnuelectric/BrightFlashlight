package com.vishnu.brightflashlight;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

public class CameraPreview
        extends SurfaceView
        implements SurfaceHolder.Callback
{
    public Context context;
    public Camera mCamera;
    public SurfaceHolder mHolder;
    public Camera.Size mPreviewSize;
    public List<Camera.Size> mSupportedPreviewSizes;
    private int mCameraid;


    public CameraPreview(Context paramContext, int id) {
        super(paramContext);
        this.context = paramContext;
        this.mCameraid= id;
        mHolder= getHolder();
        mHolder.addCallback(this);


    }



    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return sizes.get(0);
    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        camera.setDisplayOrientation(result);
    }

    private void stopPreviewAndFreeCamera()
    {

        if (this.mCamera != null) {


        this.mCamera.stopPreview();
        this.mCamera.release();
        this.mCamera = null;
    }
    }
@Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }



    public void setCamera(Camera camera) {
        if (mCamera == camera) { return; }

        stopPreviewAndFreeCamera();

        mCamera = camera;

        if (mCamera != null) {
            List<Camera.Size> localSizes = mCamera.getParameters().getSupportedPreviewSizes();
            mSupportedPreviewSizes = localSizes;
           // requestLayout();



            // Important: Call startPreview() to start updating the preview
            // surface. Preview must be started before you can take a picture.

        }
    }

        @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
            if(mCamera == null)
                return;
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            if (this.mCameraid == 0) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                if(mCamera.getParameters().getSupportedFlashModes() != null)
                {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                }
            }
       // requestLayout();

        mCamera.setParameters(parameters);

        // Important: Call startPreview() to start updating the preview surface.
        // Preview must be started before you can take a picture.
        mCamera.startPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder paramSurfaceHolder)
    {

        try
        {
            if (this.mCamera != null) {
                this.mCamera.setPreviewDisplay(this.mHolder);
                setCameraDisplayOrientation((Activity) this.context, 0, this.mCamera);
            }

            return;
        }
        catch (IOException e)
        {

                e.printStackTrace();

        }
    }
          @Override
    public void surfaceDestroyed(SurfaceHolder paramSurfaceHolder)
    {

        if (this.mCamera == null) {
            return;
        }
        this.mCamera.stopPreview();
    }
}
