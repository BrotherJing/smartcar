package com.brotherjing.server.service;

import android.os.*;
import android.os.Process;

import com.brotherjing.server.CONSTANT;
import com.brotherjing.server.activity.MainActivity;
import com.brotherjing.utils.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brotherjing on 2015/9/20.
 */
public class ServerThread extends HandlerThread{

    private ServerSocket serverSocket;
    private MainActivity.MainThreadHandler mainThreadHandler;

    private List<ClientThread> clients;

    private String IP;

    public ServerThread(String name,String ip) {
        super(name, Process.THREAD_PRIORITY_BACKGROUND);
        IP = ip;
        clients = new ArrayList<>();
    }

    public void initAndStart(){
        start();
    }

    @Override
    public void run() {
        if(IP==null){
            return;
        }
        Logger.i(IP);
        try{
            serverSocket = new ServerSocket(CONSTANT.PORT);
            if(mainThreadHandler!=null){
                /*Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putInt(CONSTANT.KEY_MSG_TYPE,CONSTANT.MSG_IP_ADDR);
                bundle.putString(CONSTANT.KEY_IP_ADDR, IP);
                msg.setData(bundle);
                mainThreadHandler.sendMessage(msg);*/

                Logger.i(IP+" in msg");
            }
            Logger.i(Thread.currentThread().getName()+" in server thread");
            while (true){
                Socket socket = serverSocket.accept();
                String name = System.currentTimeMillis()+"";
                Logger.i("new client! "+name);
                /*ClientThread clientThread = new ClientThread(name,socket,this);
                clientThread.start();
                clients.add(clientThread);*/
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }
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
