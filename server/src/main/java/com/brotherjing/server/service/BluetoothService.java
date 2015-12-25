package com.brotherjing.server.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.brotherjing.server.CONSTANT;
import com.brotherjing.utils.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class BluetoothService extends Service {

    Thread clientThread=null;
    HandlerThread handlerThread=null;
    Handler clientHandler,uiHandler;
    DataInputStream dis;
    DataOutputStream dos;

    BluetoothAdapter adapter;
    BluetoothDevice device;

    public BluetoothService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        adapter = BluetoothAdapter.getDefaultAdapter();

        uiHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Intent intent;
                if(msg.what==CONSTANT.RECEIVE_MSG){
                    intent = new Intent(CONSTANT.ACTION_NEW_MSG_BT);
                    intent.putExtra(CONSTANT.KEY_MSG_DATA, msg.getData().getString(CONSTANT.KEY_MSG_DATA));
                    sendBroadcast(intent);
                }else if(msg.what==CONSTANT.CONNECTED){
                    intent = new Intent(CONSTANT.ACTION_DEVICE_CONNECTED);
                    sendBroadcast(intent);
                }
            }
        };
        Logger.i("bluetooth service create");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logger.i("bluetooth service bind");
        device = intent.getParcelableExtra(CONSTANT.KEY_DEVICE);
        startThread(device);
        return new MyBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(clientHandler!=null)clientHandler.removeMessages(CONSTANT.SEND_MSG);
        if(handlerThread!=null)handlerThread.interrupt();
        if(clientThread!=null)clientThread.interrupt();
        clientThread = null;
    }

    private void startThread(BluetoothDevice device){
        if(clientThread!=null)return;//already started
        clientThread = new ClientThread(device);
        clientThread.start();
        handlerThread = new HandlerThread(device.getName());
        handlerThread.start();
        clientHandler = new Handler(handlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what== CONSTANT.SEND_MSG){
                    try {
                        //dos.writeUTF(msg.getData().getString(Constant.KEY_MSG_CONTENT));
                        dos.writeBytes(new String(msg.getData().getString(CONSTANT.KEY_MSG_CONTENT).getBytes("utf-8"),"iso-8859-1"));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private class ClientThread extends Thread{

        BluetoothDevice device;

        public ClientThread(BluetoothDevice device) {
            super();
            this.device = device;
        }

        @Override
        public void run() {
            try {
                Log.i("yj", device.getName() + " " + device.getAddress());
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString(CONSTANT.MY_UUID));
                adapter.cancelDiscovery();
                socket.connect();
                Logger.i("bluetooth socket connected!!");
                uiHandler.sendEmptyMessage(CONSTANT.CONNECTED);
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
                while (true){
                    byte[] bytes = new byte[128];
                    int len = dis.read(bytes);
                    //dis.read(bytes);
                    //String ascii = new String(str.getBytes("utf-8"),"iso-8859-1");
                    String ascii = new String(bytes,0,len,"iso-8859-1");
                    String str = new String(ascii.getBytes("iso-8859-1"),"utf-8");
                    Message message = uiHandler.obtainMessage(CONSTANT.RECEIVE_MSG);
                    Bundle bundle = new Bundle();
                    bundle.putString(CONSTANT.KEY_MSG_CONTENT,str);
                    message.setData(bundle);
                    message.sendToTarget();
                }
            }catch (IOException e){
                e.printStackTrace();
                BluetoothService.this.stopSelf();
            }
        }
    }

    public class MyBinder extends Binder {
        public void send(String input){
            if(clientHandler==null)return;
            Message message = clientHandler.obtainMessage(CONSTANT.SEND_MSG);
            Bundle bundle = new Bundle();
            bundle.putString(CONSTANT.KEY_MSG_CONTENT,input);
            message.setData(bundle);
            message.sendToTarget();
        }
    }

}
