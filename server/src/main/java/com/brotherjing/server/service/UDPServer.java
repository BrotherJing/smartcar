package com.brotherjing.server.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import com.brotherjing.server.CONSTANT;
import com.brotherjing.server.GlobalEnv;
import com.brotherjing.utils.Logger;
import com.brotherjing.utils.Protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer extends Service {

    Thread thread;
    DatagramSocket socket;
    InetAddress clientAddr;

    HandlerThread networkThread;
    Handler networkHandler;

    boolean isOpen;

    public UDPServer() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        thread = new Thread(runnable);
        thread.start();
        networkThread = new HandlerThread("network");
        networkThread.start();
        networkHandler = new Handler(networkThread.getLooper());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isOpen = false;
        thread.interrupt();
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                socket = new  DatagramSocket (Protocol.UDP_SERVER_PORT);
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

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder {
        public void send(byte[] bytes){
            final byte[] data = bytes;
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
    }
}
