package com.brotherjing.client;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Camera;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.brotherjing.client.Class.CameraPreview;
import com.brotherjing.client.Class.CameraUtil;
import com.brotherjing.client.service.TCPClient;
import com.brotherjing.utils.bean.TextMessage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = CameraActivity.class.getSimpleName();

    @SuppressWarnings("deprecation")
    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private Button captureButton;
    private TCPClient.MyBinder binder;
    private TCPClientConnection conn;

    @Override
    @SuppressWarnings("deprecation")
//    @TargetApi(18)
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mCamera = CameraUtil.getCameraInstance();
        mCameraPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mCameraPreview);

        captureButton = (Button) findViewById(R.id.btn_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes, Camera camera) {

                        try {
                            binder.send(new TextMessage(bytes.toString()));
                        } catch (Exception e){
                            Log.d(TAG, e.getMessage());
                        }

                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(CameraActivity.this, TCPClient.class), conn = new TCPClientConnection(), BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(conn);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(CameraActivity.this, TCPClient.class));
    }

    private class TCPClientConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (TCPClient.MyBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }
}
