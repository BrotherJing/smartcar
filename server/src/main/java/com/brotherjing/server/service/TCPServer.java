package com.brotherjing.server.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.brotherjing.server.CONSTANT;
import com.brotherjing.server.GlobalEnv;
import com.brotherjing.utils.Logger;
import com.brotherjing.utils.NetworkUtil;
import com.brotherjing.utils.Protocol;
import com.brotherjing.utils.bean.Message;
import com.brotherjing.utils.bean.TextMessage;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TCPServer extends Service {

    private ServerSocket serverSocket;

    private List<ClientThread> clients;

    private String IP;
    private boolean isConnected;

    private Thread serverThread;

    private List<Socket> clientSockets;

    public TCPServer() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IP = null;
        isConnected = false;

        clients = new ArrayList<>();
        clientSockets = new ArrayList<>();

        IP = NetworkUtil.getLocalIP(this);
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
        Message message = new Gson().fromJson(msg,Message.class);
        notifyUI(msg);
        if(message.getMsgType()== Protocol.MSG_TYPE_TEXT) {
            message = new Gson().fromJson(msg,TextMessage.class);
            if (((TextMessage)message).getText().equals(Protocol.REQ_VIDEO)) {
                //client.sendImage();
                GlobalEnv.put(CONSTANT.GLOBAL_AUDIENCE_ADDR, client.getIp());//only one audience for real time video

                Intent intent = new Intent(CONSTANT.ACTION_NEW_REQ);
                intent.putExtra(CONSTANT.KEY_REQ_TYPE, CONSTANT.REQ_TYPE_VIDEO);
                sendBroadcast(intent);

            }else if(((TextMessage)message).getText().equals(Protocol.REQ_AUDIO)){
                //TODO: start audio recognition

                Intent intent = new Intent(CONSTANT.ACTION_NEW_REQ);
                intent.putExtra(CONSTANT.KEY_REQ_TYPE, CONSTANT.REQ_TYPE_AUDIO);
                sendBroadcast(intent);

            }else if(((TextMessage)message).getText().equals(Protocol.REQ_END_VIDEO)){

                Intent intent = new Intent(CONSTANT.ACTION_NEW_REQ);
                intent.putExtra(CONSTANT.KEY_REQ_TYPE, CONSTANT.REQ_TYPE_END_VIDEO);
                sendBroadcast(intent);

            } else {
                sendToAll(message);
            }
        }
    }

    //send a broadcast to activities
    private void notifyUI(String msg){
        Intent intent1 = new Intent(CONSTANT.ACTION_NEW_MSG);
        intent1.putExtra(CONSTANT.KEY_MSG_DATA, msg);
        sendBroadcast(intent1);
    }

    //send message to all clients
    public void sendToAll(Message msg){
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
        clientSockets.clear();
        try {
            serverSocket.close();
            isConnected = false;
            Logger.i("server closed");
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public void broadcastNewClient(String id){
        Intent intent = new Intent(CONSTANT.ACTION_NEW_CLIENT);
        intent.putExtra(CONSTANT.KEY_CLIENT_NAME, id);
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
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
                    clientSockets.add(socket);
                    String id = System.currentTimeMillis()+"";

                    //notify new client
                    Logger.i("new client! " + id);

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

        public List<Socket> getClientSockets(){return clientSockets;}

        public List<ClientThread> getClients(){return clients;}
    }

}
