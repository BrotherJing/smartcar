package com.brotherjing.server;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.brotherjing.utils.Logger;

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
    //ServerThread.ServerHandler handler;

    boolean isConnected;
    String name;

    public ClientThread(String name,Socket socket) {
        super(name);
        this.name = name;
        this.mSocket = socket;
        //this.handler = handler;
        try{
            dis = new DataInputStream(mSocket.getInputStream());
            dos = new DataOutputStream(mSocket.getOutputStream());
            isConnected = true;
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public void send(String str){
        try{
            dos.writeUTF(str);
            dos.flush();
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        try{
            while(isConnected){
                String input = dis.readUTF();
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String date = format.format(new Date());
                Logger.i(input + "[" + date + "]");
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }
}
