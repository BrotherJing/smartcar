package com.brotherjing.server.Camera;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.brotherjing.utils.Logger;

import java.io.IOException;
import java.util.List;

/**
 * Created by apple on 10/16/15.
 */
public class CameraPreview extends SurfaceView {

    @SuppressWarnings("depredcation")
    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;

    @SuppressWarnings("depredcation")
    public CameraPreview(Context context,Camera camera) {
        super(context);
        mCamera = camera;

        mSurfaceHolder = getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    mCamera.setPreviewDisplay(mSurfaceHolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                if (mCamera == null)
                    return;

                //the Surface has changed size, update the camera preview size
                Camera.Parameters parameters = mCamera.getParameters();
                Camera.Size size = getBestSupportedSize(parameters.getSupportedPreviewSizes(), i1, i2);
                parameters.setPreviewSize(size.width, size.height);
                mCamera.setParameters(parameters);

                try {
                    mCamera.startPreview();
                } catch (Exception e){
                    Logger.i("Could not start preview " + e.getMessage());
                    mCamera.release();
                    mCamera = null;
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                if (mCamera != null){
                    mCamera.release();
                    mCamera = null;
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    private Camera.Size getBestSupportedSize(List<Camera.Size>sizes,int width, int height){
        Camera.Size bestSize = sizes.get(0);
        int largestArea = bestSize.width * bestSize.height;
        for (Camera.Size size : sizes){
            int area = size.width * size.height;
            if (area > largestArea){
                largestArea = area;
                bestSize = size;
            }
        }
        return bestSize;
    }
}
