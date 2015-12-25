package com.brotherjing.client.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.brotherjing.client.CONSTANT;
import com.brotherjing.client.Direction.DirectionActivity;
import com.brotherjing.client.R;
import com.brotherjing.client.controller.TCPSmartcarControllerImpl;
import com.brotherjing.client.service.TCPClient;
import com.brotherjing.client.service.UDPClient;
import com.brotherjing.client.view.ImageSurfaceView;
import com.brotherjing.utils.ImageCache;
import com.brotherjing.utils.Logger;
import com.brotherjing.utils.Protocol;
import com.brotherjing.utils.bean.TextMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ViewDirectionActivity extends Activity {

    private static final String TAG = ViewDirectionActivity.class.getSimpleName();
    ImageSurfaceView surfaceView;
    //UIReceiver receiver;

    private ImageView btn_forward, btn_left, btn_right, btn_back, btn_stop;
    private TCPSmartcarControllerImpl mTCPSmartcarController;
    private TCPClient.MyBinder binder;

    private TCPClientConnection conn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_view_direction);

        surfaceView = (ImageSurfaceView)findViewById(R.id.surfaceview_camera);

        /*IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CONSTANT.ACTION_NEW_IMG);
        receiver = new UIReceiver();
        registerReceiver(receiver, intentFilter);*/

//        mTCPSmartcarController = new TCPSmartcarControllerImpl(binder);

        /* Control Part */
        btn_forward = (ImageView) findViewById(R.id.forwarding);
        btn_forward.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(TAG, "forward");
                mTCPSmartcarController.forward();
                return false;
            }
        });
        btn_left = (ImageView) findViewById(R.id.left);
        btn_left.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(TAG, "left");
                mTCPSmartcarController.turnLeft(0);
                return false;
            }
        });
        btn_right = (ImageView) findViewById(R.id.right);
        btn_right.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(TAG, "right");
                mTCPSmartcarController.turnRight(0);

                return false;
            }
        });
        btn_back = (ImageView) findViewById(R.id.back);
        btn_back.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(TAG, "backword");
                mTCPSmartcarController.backward();
                return false;
            }
        });
        btn_stop = (ImageView) findViewById(R.id.stop);
        btn_stop.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(TAG, "stop");
                mTCPSmartcarController.stop();
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        thread = new Thread(runnable);
        thread.start();

        //startService(new Intent(ViewDirectionActivity.this, UDPClient.class));
        bindService(new Intent(ViewDirectionActivity.this, TCPClient.class), conn = new TCPClientConnection(), BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        binder.send(new TextMessage(Protocol.REQ_END_VIDEO));
        unbindService(conn);
        //stopService(new Intent(ViewDirectionActivity.this, UDPClient.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(receiver);

        isOpen = false;
        socket.close();
        thread.interrupt();
    }

    //broadcast receiver listening to server events
    /*private class UIReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //if server is up and run, it will send ip address back
            if(intent.getAction().equals(CONSTANT.ACTION_NEW_IMG)){
                surfaceView.drawBitmap(ImageCache.getBitmapFromMemoryCache(intent.getStringExtra(CONSTANT.KEY_MSG_DATA)));
            }
        }
    }*/

    private class TCPClientConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (TCPClient.MyBinder)iBinder;
            mTCPSmartcarController = new TCPSmartcarControllerImpl(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }

    private void receive(){
        while(isOpen){
            try {
                socket.receive(packet);
                try {
                    //ByteArrayOutputStream outPut1 = new ByteArrayOutputStream();
                    bmp1 = null;
                    bmp1 = BitmapFactory.decodeByteArray(packet.getData(), 0, packet.getLength());
                    //bmp1.compress(Bitmap.CompressFormat.JPEG, 40, outPut1);
                    /*String name1 = System.currentTimeMillis() + "";
                    ImageCache.addBitmap(name1, bmp1);

                    Intent intent2 = new Intent(CONSTANT.ACTION_NEW_IMG);
                    intent2.putExtra(CONSTANT.KEY_MSG_DATA, name1);
                    sendBroadcast(intent2);*/
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            surfaceView.drawBitmap(bmp1);
                        }
                    });
                } catch (Exception ex) {
                    Logger.i("EXCEPTION!!!");
                    ex.printStackTrace();
                    //break;
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    Thread thread;
    DatagramSocket socket;
    byte data[] = new byte[1024*64];
    boolean isOpen;
    Bitmap bmp1;
    DatagramPacket packet = new DatagramPacket(data, data.length);

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                socket = new DatagramSocket(Protocol.UDP_CLIENT_PORT);
                isOpen = true;

                receive();

            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
    };
}
