package com.brotherjing.server.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.brotherjing.server.R;
import com.brotherjing.server.service.ClientThread;
import com.brotherjing.server.service.TCPServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class SimpleVideoActivity extends ActionBarActivity {

    final private int VIDEO_QUALITY =60;

    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private MediaRecorder mMediaRecorder;
    private Camera.Size bestSize;
    private int videoFormatIndex;

    private Button takeVideoButton;

    private boolean isRecording = false;
    private List<ClientThread> clientList;
    private TCPServer.MyBinder binder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_video);
        takeVideoButton = (Button) findViewById(R.id.btn_video);
        takeVideoButton.setText("Video");
        takeVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecording) {
                    /*mMediaRecorder.stop();
                    releaseMediaRecorder();
                    mCamera.lock();*/
                    takeVideoButton.setText("Video");
                    isRecording = false;
                } else {
                    takeVideoButton.setText("Stop");
                    isRecording = true;
                    /*if (prepareVideoRecorder()) {
                        mMediaRecorder.start();
                        takeVideoButton.setText("Stop");
                        isRecording = true;
                    } else {
                        releaseMediaRecorder();
                        Logger.i("not prepared");
                    }*/
                }
            }
        });

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        final SurfaceHolder holder = mSurfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                //Tell the camera to use this surface as its preview area
                try {
                    if (mCamera != null) {
                        //该方法用来连接camera和surface
                        mCamera.setPreviewDisplay(holder);
                        mCamera.setPreviewCallback(callback);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            /**
             * @param surfaceHolder
             * @param i
             * @param i1 the new width of the surface
             * @param i2 the new height of the surface
             */
            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                if (mCamera == null) {
                    return;
                }

                Camera.Parameters parameters = mCamera.getParameters();
                //相机的preview 大小不能随意设置 如果设置了不可接受的值 应用将会抛出异常
                Camera.Size size = getBestSupportedSized(parameters.getSupportedPreviewSizes(), i1, i2); //to be reset in the next section
                parameters.setPreviewSize(size.width, size.height);
                mCamera.setParameters(parameters);
                bestSize = size;
                videoFormatIndex = mCamera.getParameters().getPreviewFormat();
                try {
                    // Call startPreview() to start updating the preview surface
                    // Preview must be started before you can take a picture.

                    // surface上绘制帧
                    mCamera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                    mCamera.release();
                    mCamera = null;
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                /*if (mCamera != null) {
                    mCamera.stopPreview();
                }*/
                if (null != mCamera) {
                    mCamera.setPreviewCallback(null); // ！！这个必须在前，不然退出出错
                    mCamera.stopPreview();
                    mCamera = null;
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, TCPServer.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mServiceConnection);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            mCamera = Camera.open(0);
        } else {
            mCamera = Camera.open();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //releaseMediaRecorder();
        releaseCamera();
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, TCPServer.class));
        super.onDestroy();
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

    private void releaseCamera() {
        try{
            if (mCamera != null) {
                mCamera.setPreviewCallback(null); // ！！这个必须在前，不然退出出错
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onPause();
        /*if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }*/
    }

    private Camera.Size getBestSupportedSized(List<Camera.Size> sizes, int width, int height) {
        Camera.Size bestSize = sizes.get(0);
        int largestArea = bestSize.width * bestSize.height;
        for (Camera.Size size : sizes) {
            int temp = size.width * size.height;
            if (largestArea < temp) {
                bestSize = size;
                largestArea = temp;
            }
        }
        return bestSize;
    }

    private Camera.PreviewCallback callback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] bytes, Camera camera) {
            if(!isRecording)return;
            try{
                if(bytes!=null){
                    YuvImage img = new YuvImage(bytes,videoFormatIndex,
                            bestSize.width,bestSize.height,null);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    img.compressToJpeg(new Rect(0,0,bestSize.width/4,bestSize.height/4),
                            VIDEO_QUALITY,outputStream);
                    sendImage(outputStream.toByteArray());
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    private void sendImage(byte[] data){
        clientList.get(0).prepareForVideo();
        clientList.get(0).send(data);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (TCPServer.MyBinder) iBinder;
            //clientSockets = binder.getClientSockets();
            clientList = binder.getClients();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
}
