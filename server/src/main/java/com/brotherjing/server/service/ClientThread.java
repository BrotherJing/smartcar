package com.brotherjing.server.service;

import android.content.res.AssetManager;
import android.os.HandlerThread;

import com.brotherjing.server.service.TCPServer;
import com.brotherjing.utils.DateUtil;
import com.brotherjing.utils.Logger;
import com.brotherjing.utils.Protocol;
import com.brotherjing.utils.bean.TCPMessage;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public Socket getMySocket(){
        return mSocket;
    }

    public void prepareForVideo(){
        try{
            dos.writeInt(Protocol.TYPE_VIDEO);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public void send(String msg){
        try{
            dos.writeInt(Protocol.TYPE_JSON);
            dos.writeUTF(msg);
            dos.flush();
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public void sendImage(){
        try {
            AssetManager assetManager = server.getAssets();
            InputStream is = assetManager.open("test.jpg");
            int size = is.available();
            byte[] data = new byte[size];
            is.read(data);

            dos.writeInt(Protocol.TYPE_IMAGE);
            dos.writeInt(size);
            dos.write(data);

            dos.flush();
            is.close();
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
                int msg_type = dis.readInt();
                switch (msg_type){
                    case Protocol.TYPE_JSON:
                        String input = dis.readUTF();
                        //TCPMessage msg = new Gson().fromJson(input,TCPMessage.class);
                        //String timestamp = System.currentTimeMillis()+"";
                        Logger.i(input);
                        if(server!=null) {
                            server.receiveJSON(this, input);
                        }
                        break;
                    case Protocol.TYPE_IMAGE:
                        break;
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
