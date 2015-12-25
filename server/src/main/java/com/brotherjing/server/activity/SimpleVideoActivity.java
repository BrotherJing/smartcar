package com.brotherjing.server.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.brotherjing.server.CONSTANT;
import com.brotherjing.server.GlobalEnv;
import com.brotherjing.server.R;
import com.brotherjing.server.controller.BluetoothCarController;
import com.brotherjing.server.service.BluetoothService;
import com.brotherjing.server.service.ClientThread;
import com.brotherjing.server.service.TCPServer;
import com.brotherjing.server.service.UDPServer;
import com.brotherjing.utils.Logger;
import com.brotherjing.utils.Protocol;
import com.brotherjing.utils.bean.CommandMessage;
import com.brotherjing.utils.bean.TextMessage;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

public class SimpleVideoActivity extends ActionBarActivity {

    final private int VIDEO_QUALITY = 30;
    final private int SKIP_FRAME = 4;

    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private Camera.Size bestSize;
    private int videoFormatIndex;

    private boolean isRecording = false;
    BluetoothService.MyBinder bluetoothBinder;
    BluetoothCarController carController = null;

    MainThreadReceiver receiver;

    private UDPServer.MyBinder udpBinder;

    private int frame_skipped = 0;
    private boolean isBluetoothConnected = false;
    private String clientIpAddr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_video);

        isRecording = true;

        isBluetoothConnected = GlobalEnv.getBoolean(CONSTANT.GLOBAL_IS_BLUETOOTH_CONNECTED,false);
        clientIpAddr = GlobalEnv.getString(CONSTANT.GLOBAL_AUDIENCE_ADDR);

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

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CONSTANT.ACTION_NEW_MSG);
        intentFilter.addAction(CONSTANT.ACTION_NEW_REQ);
        receiver = new MainThreadReceiver();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //bindService(new Intent(this, TCPServer.class), mServiceConnection, BIND_AUTO_CREATE);
        //bindService(new Intent(this, UDPServer.class), mServiceConnection, BIND_AUTO_CREATE);

        thread = new Thread(runnable);
        thread.start();
        networkThread = new HandlerThread("network");
        networkThread.start();
        networkHandler = new Handler(networkThread.getLooper());

        if(isBluetoothConnected)
            bindService(new Intent(this, BluetoothService.class), bluetoothConn, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //unbindService(mServiceConnection);
        if(isBluetoothConnected) {
            unbindService(bluetoothConn);
        }
        unregisterReceiver(receiver);

        isOpen = false;
        thread.interrupt();
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
                    //sendImage(outputStream.toByteArray());
                    send(outputStream.toByteArray());
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    private void sendImage(byte[] data){
        /*clientList.getString(0).prepareForVideo();
        clientList.getString(0).send(data);*/
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

    private ServiceConnection bluetoothConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bluetoothBinder = (BluetoothService.MyBinder)service;
            carController = new BluetoothCarController(bluetoothBinder);
            Toast.makeText(SimpleVideoActivity.this, "getString bluetooth binder", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    //broadcast receiver listening to server events
    private class MainThreadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(CONSTANT.ACTION_NEW_MSG)){
                com.brotherjing.utils.bean.Message message = new Gson().fromJson(intent.getStringExtra(CONSTANT.KEY_MSG_DATA), com.brotherjing.utils.bean.Message.class);
                if(message.getMsgType()== Protocol.MSG_TYPE_TEXT){
                    //TextMessage textMessage = new Gson().fromJson(intent.getStringExtra(CONSTANT.KEY_MSG_DATA),TextMessage.class);
                    Logger.i(intent.getStringExtra(CONSTANT.KEY_MSG_DATA));
                }else{
                    CommandMessage cmd = new Gson().fromJson(intent.getStringExtra(CONSTANT.KEY_MSG_DATA),CommandMessage.class);
                    if(carController!=null){
                        carController.processCommand(cmd.getCommand());
                    }
                }
            }else if(intent.getAction().equals(CONSTANT.ACTION_NEW_REQ)){
                int type = intent.getIntExtra(CONSTANT.KEY_REQ_TYPE,0);
                if(type==CONSTANT.REQ_TYPE_VIDEO){
                    if(clientIpAddr.equals(GlobalEnv.getString(CONSTANT.GLOBAL_AUDIENCE_ADDR))){
                        return;
                    }else{
                        clientIpAddr = GlobalEnv.getString(CONSTANT.GLOBAL_AUDIENCE_ADDR);
                        //udpBinder.setClientIpAddr(clientIpAddr);
                        setClientIpAddr(clientIpAddr);
                    }
                }else if(type==CONSTANT.REQ_TYPE_END_VIDEO){
                    SimpleVideoActivity.this.finish();
                }
            }
        }
    }

    Thread thread;
    DatagramSocket socket;
    InetAddress clientAddr;

    HandlerThread networkThread;
    Handler networkHandler;

    boolean isOpen;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                socket = new DatagramSocket(Protocol.UDP_SERVER_PORT);
                clientAddr = InetAddress.getByName(GlobalEnv.getString(CONSTANT.GLOBAL_AUDIENCE_ADDR));
                if(clientAddr==null){
                    Logger.i("no client found");
                    return;
                }
                isOpen = true;
            }catch (IOException e){
                e.printStackTrace();
            }

        }
    };

    public void send(final byte[] data){
        if(isOpen){
            networkHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        DatagramPacket packet = new DatagramPacket(data, data.length, clientAddr, Protocol.UDP_CLIENT_PORT);
                        socket.send(packet);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            });

        }
    }
    public void setClientIpAddr(String ipAddr){
        try {
            clientAddr = InetAddress.getByName(ipAddr);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }
}
