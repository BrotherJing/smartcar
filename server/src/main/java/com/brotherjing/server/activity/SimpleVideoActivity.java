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
import com.brotherjing.server.service.UDPServer;
import com.brotherjing.utils.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class SimpleVideoActivity extends ActionBarActivity {

    final private int VIDEO_QUALITY = 30;
    final private int SKIP_FRAME = 4;

    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private Camera.Size bestSize;
    private int videoFormatIndex;

    private Button takeVideoButton;

    private boolean isRecording = false;
    private List<ClientThread> clientList;
    private TCPServer.MyBinder binder;

    private UDPServer.MyBinder udpBinder;

    private int frame_skipped = 0;

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
                    takeVideoButton.setText("Video");
                    isRecording = false;
                } else {
                    takeVideoButton.setText("Stop");
                    isRecording = true;
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
                Logger.i("best size of camera is " + bestSize.height + " " + bestSize.width);
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
        //bindService(new Intent(this, TCPServer.class), mServiceConnection, BIND_AUTO_CREATE);
        bindService(new Intent(this,UDPServer.class),mServiceConnection,BIND_AUTO_CREATE);
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
        releaseCamera();
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, TCPServer.class));
        super.onDestroy();
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
    }

    private Camera.Size getBestSupportedSized(List<Camera.Size> sizes, int width, int height) {
        Camera.Size bestSize = sizes.get(0);
        int bestArea = bestSize.width * bestSize.height,originArea = width*height;
        //float rate = Math.abs(bestArea-originArea)/originArea;
        int diff = Math.abs(bestArea-originArea);
        for (Camera.Size size : sizes) {
            int temp = size.width * size.height;
            //Logger.i("size is "+size.width+" "+size.height);
            //float newRate = Math.abs(temp-originArea)/originArea;
            int newDiff = Math.abs(temp-originArea);
            if (newDiff<diff) {
                bestSize = size;
                diff = newDiff;
            }
        }
        return bestSize;
    }

    private Camera.PreviewCallback callback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] bytes, Camera camera) {
            if(!isRecording)return;

            ++frame_skipped;
            if(frame_skipped==SKIP_FRAME)frame_skipped=0;
            else return;

            try{
                if(bytes!=null){
                    YuvImage img = new YuvImage(bytes,videoFormatIndex,
                            bestSize.width,bestSize.height,null);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    img.compressToJpeg(new Rect(0,0,bestSize.width,bestSize.height),
                            VIDEO_QUALITY,outputStream);
                    sendImage(outputStream.toByteArray());
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    private void sendImage(byte[] data){
        /*clientList.get(0).prepareForVideo();
        clientList.get(0).send(data);*/
        udpBinder.send(data);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            /*binder = (TCPServer.MyBinder) iBinder;
            //clientSockets = binder.getClientSockets();
            clientList = binder.getClients();*/
            udpBinder = (UDPServer.MyBinder)iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
}
