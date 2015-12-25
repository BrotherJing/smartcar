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
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.brotherjing.client.CONSTANT;
import com.brotherjing.client.R;
import com.brotherjing.client.service.TCPClient;
import com.brotherjing.client.service.UDPClient;
import com.brotherjing.client.view.ImageSurfaceView;
import com.brotherjing.utils.ImageCache;
import com.brotherjing.utils.Logger;
import com.brotherjing.utils.Protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ViewCameraActivity extends Activity {

    ImageSurfaceView surfaceView;
    //UIReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_view_camera);

        surfaceView = (ImageSurfaceView)findViewById(R.id.surfaceview_camera);

        /*IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CONSTANT.ACTION_NEW_IMG);
        receiver = new UIReceiver();
        registerReceiver(receiver, intentFilter);*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        //startService(new Intent(ViewCameraActivity.this, UDPClient.class));

        thread = new Thread(runnable);
        thread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //stopService(new Intent(ViewCameraActivity.this, UDPClient.class));
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

    private void receive(){
        while(isOpen){
            try {
                DatagramPacket packet = new DatagramPacket(data, data.length);
                socket.receive(packet);
                try {
                    ByteArrayOutputStream outPut1 = new ByteArrayOutputStream();
                    final Bitmap bmp1 = BitmapFactory.decodeByteArray(packet.getData(), 0, packet.getLength());
                    bmp1.compress(Bitmap.CompressFormat.JPEG, 40, outPut1);
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
