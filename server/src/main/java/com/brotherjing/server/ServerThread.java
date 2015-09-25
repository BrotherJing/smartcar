package com.brotherjing.server;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.*;
import android.os.Process;
import android.util.Log;

import com.brotherjing.utils.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Brotherjing on 2015/9/20.
 */
public class ServerThread extends HandlerThread{

    private Context context;
    private ServerSocket serverSocket;
    private ServerHandler handler;
    private MainActivity.MainThreadHandler mainThreadHandler;

    private List<ClientThread> clients;

    private String IP;

    public ServerThread(String name,Context context) {
        super(name, Process.THREAD_PRIORITY_BACKGROUND);
        this.context = context;
        clients = new ArrayList<>();
    }

    public void initAndStart(){
        start();
    }

    @Override
    public void run() {
        IP = getLocalIP();
        if(IP==null){
            return;
        }
        Logger.i(IP);
        try{
            serverSocket = new ServerSocket(CONSTANT.PORT);
            if(mainThreadHandler!=null){
                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putInt(CONSTANT.KEY_MSG_TYPE,CONSTANT.MSG_IP_ADDR);
                bundle.putString(CONSTANT.KEY_IP_ADDR, IP);
                msg.setData(bundle);
                mainThreadHandler.sendMessage(msg);
                Logger.i(IP+" in msg");
            }
            Logger.i(Thread.currentThread().getName()+" in server thread");
            while (true){
                Socket socket = serverSocket.accept();
                String name = System.currentTimeMillis()+"";
                Logger.i("new client! "+name);
                ClientThread clientThread = new ClientThread(name,socket,this);
                clients.add(clientThread);
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public ServerHandler getHandler(){
        return handler;
    }

    public void setMainThreadHandler(MainActivity.MainThreadHandler handler){
        this.mainThreadHandler = handler;
    }

    public class ServerHandler extends Handler{
        public ServerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.getData().getInt(CONSTANT.KEY_MSG_TYPE)){
                case CONSTANT.MSG_TEST:
                    Logger.i("test");break;
                default:break;
            }
        }
    }

    private String getLocalIP(){
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        if(ipAddress==0)return null;
        return ((ipAddress & 0xff)+"."+(ipAddress>>8 & 0xff)+"."
                +(ipAddress>>16 & 0xff)+"."+(ipAddress>>24 & 0xff));
    }

    public void sendToAll(String str){
        for(ClientThread thread : clients){
            thread.send(str);
        }
    }

    public void quitAll(){
        Logger.i(Thread.currentThread().getName() + " in server thread");
        for(ClientThread thread : clients){
            thread.quit();
            thread = null;
        }
        clients.clear();
        clients = null;
        try {
            serverSocket.close();
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }
}
