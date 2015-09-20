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
import java.util.HashMap;

/**
 * Created by Brotherjing on 2015/9/20.
 */
public class ServerThread extends HandlerThread{

    private Context context;
    private ServerSocket serverSocket;
    private OutputStream os;
    private ServerHandler handler;
    private MainActivity.MainThreadHandler mainThreadHandler;

    private HashMap<String,ClientThread> clients;

    private String IP;

    public ServerThread(String name,Context context) {
        super(name, Process.THREAD_PRIORITY_BACKGROUND);
        this.context = context;
        clients = new HashMap<>();
    }

    public void initAndStart(){
        start();
        //handler = new ServerHandler(getLooper());
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
            while (true){
                Socket socket = serverSocket.accept();
                String name = System.currentTimeMillis()+"";
                ClientThread clientThread = new ClientThread(name,socket);
                clients.put(name,clientThread);
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
}
