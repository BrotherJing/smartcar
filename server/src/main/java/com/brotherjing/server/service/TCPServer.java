package com.brotherjing.server.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;

import com.brotherjing.server.CONSTANT;
import com.brotherjing.utils.Logger;
import com.brotherjing.utils.bean.TCPMessage;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class TCPServer extends Service {

    private ServerSocket serverSocket;

    private List<ClientThread> clients;

    private String IP;
    private boolean isConnected;

    private Thread serverThread;

    public TCPServer() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IP = null;
        isConnected = false;
        clients = new ArrayList<>();

        IP = getLocalIP();
        if(IP==null){
            return;
        }
        Logger.i(IP);

        //start TCP server
        serverThread = new Thread(runnable);
        serverThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void receiveJSON(ClientThread client,String msg){
        TCPMessage message = new Gson().fromJson(msg,TCPMessage.class);
        notifyUI(msg);
        if(message.getText().equals("[req]")){
            client.sendImage();
        }else{
            sendToAll(msg);
        }
    }

    //send a broadcast to activities
    private void notifyUI(String msg){
        Intent intent1 = new Intent(CONSTANT.ACTION_NEW_MSG);
        intent1.putExtra(CONSTANT.KEY_MSG_DATA, msg);
        sendBroadcast(intent1);
    }

    //send message to all clients
    public void sendToAll(String msg){
        for(ClientThread thread : clients){
            thread.send(msg);
        }
    }

    public void quitAll(){
        //Logger.i(Thread.currentThread().getName() + " in server thread");
        for(ClientThread thread : clients){
            thread.quitSelf();
        }
        clients.clear();
        try {
            serverSocket.close();
            isConnected = false;
            Logger.i("server closed");
        }catch (IOException ex){
            ex.printStackTrace();
        }
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
                serverSocket = new ServerSocket(CONSTANT.PORT);
                Intent intent1 = new Intent(CONSTANT.ACTION_SERVER_UP);
                intent1.putExtra(CONSTANT.KEY_IP_ADDR, IP);
                sendBroadcast(intent1);
                Logger.i(IP + " in msg");
                Logger.i(Thread.currentThread().getName()+" in server thread");
                isConnected = true;
                while (isConnected){
                    Socket socket = serverSocket.accept();
                    String id = System.currentTimeMillis()+"";
                    Logger.i("new client! "+id);
                    ClientThread clientThread = new ClientThread(id,socket,TCPServer.this);
                    clientThread.start();
                    clients.add(clientThread);
                }
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        quitAll();
        serverThread.interrupt();
    }

    public class MyBinder extends Binder{
        public String getIP(){
            return IP;
        }
    }

}
