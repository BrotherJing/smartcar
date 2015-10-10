package com.brotherjing.server.service;

import android.os.HandlerThread;

import com.brotherjing.server.service.TCPServer;
import com.brotherjing.utils.DateUtil;
import com.brotherjing.utils.Logger;
import com.brotherjing.utils.bean.TCPMessage;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Brotherjing on 2015/9/20.
 */
public class ClientThread extends HandlerThread {

    Socket mSocket;
    DataInputStream dis;
    DataOutputStream dos;

    //the service context
    TCPServer server;

    boolean isConnected;
    String name;

    public ClientThread(String name,Socket socket,TCPServer server){
        super(name);
        this.name = name;
        this.mSocket = socket;
        this.server = server;
    }

    public void send(TCPMessage msg){
        try{
            dos.writeUTF(new Gson().toJson(msg));
            dos.flush();
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        try{
            dis = new DataInputStream(mSocket.getInputStream());
            dos = new DataOutputStream(mSocket.getOutputStream());
            isConnected = true;
            Logger.i("client connected");
            while(!isInterrupted()){
                String input = dis.readUTF();
                TCPMessage msg = new Gson().fromJson(input,TCPMessage.class);
                //String timestamp = System.currentTimeMillis()+"";
                //Logger.i(input + "[" + timestamp + "]");
                if(server!=null) {
                    server.sendToAll(msg);
                }
            }
        }catch (IOException ex){
            ex.printStackTrace();
            Logger.i("client not connected");
            quitSelf();
        }
    }

    public void quitSelf(){
        isConnected = false;
        try {
            dis.close();
            dos.close();
            mSocket.close();
        }catch (IOException ex){
            ex.printStackTrace();
        }
        server = null;
        interrupt();
    }
}
