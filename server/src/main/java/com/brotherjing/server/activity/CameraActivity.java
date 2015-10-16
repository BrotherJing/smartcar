package com.brotherjing.server.activity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.brotherjing.server.Camera.CameraPreview;
import com.brotherjing.server.Camera.CameraUtils;
import com.brotherjing.server.R;
import com.brotherjing.utils.Logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class CameraActivity extends AppCompatActivity {

    @SuppressWarnings("deprecation")
    private Camera mCamera;
//    private MediaRecorder mMediaRecorder;
    private CameraPreview mCameraPreview;

    /* Widget */
    private Button mCaptureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mCamera = CameraUtils.getCameraInstance();
        mCameraPreview = new CameraPreview(this, mCamera);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.camera_preview);
        frameLayout.addView(mCameraPreview);

        mCaptureButton = (Button) findViewById(R.id.button_capture);
        mCaptureButton.setEnabled(hasCamera());
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            @SuppressWarnings("deprecation")
            public void onClick(View view) {
                /* Callback interface used to supply image data from a photo capture.  */
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes, Camera camera) {
                        String filename = UUID.randomUUID().toString() + ".jpg";
                        FileOutputStream fos = null;
                        boolean success = true;

                        try {
                            fos = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
                            fos.write(bytes);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            success = false;
                        } catch (IOException e) {
                            e.printStackTrace();
                            success = false;
                        } finally {
                            if (fos != null){
                                try {
                                    fos.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Logger.i("Error closing file");
                                }
                            }
                        }

                        if (success){
                            Logger.i("JPED save at " + filename);
                        }

                        finish();
                    }
                });
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private boolean hasCamera(){
        PackageManager packageManager = getApplicationContext().getPackageManager();
        boolean hasACamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
                || packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD
                || Camera.getNumberOfCameras() > 0;

        if (!hasACamera){
            return false;
        } else {
            return true;
        }

    }


}
