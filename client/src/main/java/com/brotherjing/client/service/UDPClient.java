package com.brotherjing.client.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.Message;

import com.brotherjing.client.CONSTANT;
import com.brotherjing.utils.ImageCache;
import com.brotherjing.utils.Logger;
import com.brotherjing.utils.Protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPClient extends Service {

    Thread thread;
    DatagramSocket socket;
    byte data[] = new byte[1024*64];

    boolean isOpen;

    public UDPClient() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        thread = new Thread(runnable);
        thread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isOpen = false;
        socket.close();
        thread.interrupt();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void receive(){
        while(isOpen){
            try {
                DatagramPacket packet = new DatagramPacket(data, data.length);
                socket.receive(packet);
                try {
                    ByteArrayOutputStream outPut1 = new ByteArrayOutputStream();
                    Bitmap bmp1 = BitmapFactory.decodeByteArray(packet.getData(), 0, packet.getLength());
                    bmp1.compress(Bitmap.CompressFormat.JPEG, 40, outPut1);
                    String name1 = System.currentTimeMillis() + "";
                    ImageCache.addBitmap(name1, bmp1);

                    Intent intent2 = new Intent(CONSTANT.ACTION_NEW_IMG);
                    intent2.putExtra(CONSTANT.KEY_MSG_DATA, name1);
                    sendBroadcast(intent2);
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
