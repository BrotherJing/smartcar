package com.brotherjing.client.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;

import com.brotherjing.client.CONSTANT;
import com.brotherjing.utils.bean.TCPMessage;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class TCPClient extends Service {


    Thread clientThread;
    String IP;
    private String name = null;
    private String ip = null;
    private String port = null;

    Socket socket;
    private InetSocketAddress isa = null;

    DataInputStream dis = null;
    DataOutputStream dos = null;
    private String reMsg = null;
    private boolean isConnect;

    HandlerThread networkThread;
    Handler networkHandler;

    public TCPClient() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isConnect = false;
        networkThread = new HandlerThread("network");
        networkThread.start();
        networkHandler = new Handler(networkThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case CONSTANT.MSG_NEW_MSG:
                        send(msg.getData().getString(CONSTANT.KEY_MSG_DATA));
                        break;
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    private String getLocalIP(){
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        if(ipAddress==0)return null;
        return ((ipAddress & 0xff)+"."+(ipAddress>>8 & 0xff)+"."
                +(ipAddress>>16 & 0xff)+"."+(ipAddress>>24 & 0xff));
    }

    //runnable for server thread
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try{
                connect();
                ReceiveMsg();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    };

    private void ReceiveMsg() {
        if(isConnect) {
            try {
                while ((reMsg = dis.readUTF()) != null) {
                    System.out.println(reMsg);
                }
            } catch (SocketException e) {
                // TODO: handle exception
                System.out.println("exit!");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void connect() {
        try {
            socket = new Socket();
            isa = new InetSocketAddress(ip,Integer.parseInt(port));
            socket.connect(isa,5000);

            if(socket.isConnected()){
                dos = new DataOutputStream(socket.getOutputStream());
                dis = new DataInputStream(socket.getInputStream());
                TCPMessage message = new TCPMessage(name,System.currentTimeMillis()+"","connect");
                dos.writeUTF(new Gson().toJson(message));
                dos.flush();
                System.out.println("connect");
                isConnect=true;
            }
        }catch (UnknownHostException e) {
            System.out.println("連接失敗");
            e.printStackTrace();
        }catch (SocketTimeoutException e) {
            System.out.println("連接超時，服務器未開啟或IP錯誤");
            e.printStackTrace();
        }catch (IOException e) {
            System.out.println("連接失敗");
            e.printStackTrace();
        }
    }

    public void send(String text){
        if(isConnect){
            try {
                TCPMessage message = new TCPMessage(name,System.currentTimeMillis()+"",text);
                dos.writeUTF(new Gson().toJson(message));
                dos.flush();
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }

    public void disConnect() {
        if(dos!=null){
            try {
                dos.writeUTF("offline:"+name);

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class MyBinder extends Binder{
        public String getIP(){
            return IP;
        }

        public void connectServer(String s_ip,String s_port,String c_name){
            //start TCP client
            ip = s_ip;
            port = s_port;
            name = c_name;
            clientThread = new Thread(runnable);
            clientThread.start();
        }

        public void send(String text){
            Message msg = networkHandler.obtainMessage(CONSTANT.MSG_NEW_MSG);
            Bundle bundle = new Bundle();
            bundle.putString(CONSTANT.KEY_MSG_DATA,text);
            msg.setData(bundle);
            msg.sendToTarget();
        }
    }

}
