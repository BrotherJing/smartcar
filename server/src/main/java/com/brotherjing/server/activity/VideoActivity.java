package com.brotherjing.server.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.brotherjing.server.R;
import com.brotherjing.server.service.ClientThread;
import com.brotherjing.server.service.TCPServer;
import com.brotherjing.utils.Protocol;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.List;

public class VideoActivity extends ActionBarActivity {


    private static final String TAG = VideoActivity.class.getSimpleName();
    @SuppressWarnings("deprecation")

    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private MediaRecorder mMediaRecorder;

    private Button takeVideoButton;

    private boolean isRecording = false;
    //private List<Socket> clientSockets;
    private List<ClientThread> clientList;
    private TCPServer.MyBinder binder;

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

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_video);

        takeVideoButton = (Button) findViewById(R.id.btn_video);
        takeVideoButton.setText("Video");
        takeVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecording) {
                    mMediaRecorder.stop();
                    releaseMediaRecorder();
                    mCamera.lock();

                } else {
                    if (prepareVideoRecorder()) {
                        mMediaRecorder.start();
                        takeVideoButton.setText("Stop");
                        isRecording = true;
                    } else {
                        releaseMediaRecorder();
                        Log.d(TAG, "not prepared");
                    }
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
                if (mCamera != null) {
                    mCamera.stopPreview();
                }
            }
        });
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
        releaseMediaRecorder();
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
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @SuppressWarnings("deprecation")
    private boolean prepareVideoRecorder() {
        /*if (clientSockets.isEmpty()) {
            return false;
        }*/
        if(clientList.isEmpty())return false;

        mMediaRecorder = new MediaRecorder();

        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        //final ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(clientSockets.get(0));
        clientList.get(0).prepareForVideo();
        final ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(clientList.get(0).getMySocket());
        mMediaRecorder.setOutputFile(pfd.getFileDescriptor());
        mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());

        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException " + e.getMessage());
            releaseMediaRecorder();

            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
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
