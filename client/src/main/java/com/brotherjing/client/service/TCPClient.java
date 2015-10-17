package com.brotherjing.client.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.widget.ImageView;

import com.brotherjing.client.CONSTANT;
import com.brotherjing.utils.ImageCache;
import com.brotherjing.utils.Logger;
import com.brotherjing.utils.Protocol;
import com.brotherjing.utils.bean.TCPMessage;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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
    private int msgType;
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
                    case CONSTANT.MSG_SEND_MSG:
                        send(msg.getData().getString(CONSTANT.KEY_MSG_DATA));
                        break;
                    case CONSTANT.MSG_NEW_MSG:
                        Intent intent = new Intent(CONSTANT.ACTION_NEW_MSG);
                        intent.putExtra(CONSTANT.KEY_MSG_DATA, msg.getData().getString(CONSTANT.KEY_MSG_DATA));
                        sendBroadcast(intent);
                        break;
                    case CONSTANT.MSG_NEW_IMG:
                        Intent intent2 = new Intent(CONSTANT.ACTION_NEW_IMG);
                        intent2.putExtra(CONSTANT.KEY_MSG_DATA, msg.getData().getString(CONSTANT.KEY_MSG_DATA));
                        sendBroadcast(intent2);
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
        while (isConnect) {
            try {
                msgType = dis.readInt();
                Message msg = networkHandler.obtainMessage();
                Bundle bundle = new Bundle();
                switch (msgType){
                    case Protocol.TYPE_JSON:
                        reMsg = dis.readUTF();
                        //System.out.println(reMsg);
                        msg.what = CONSTANT.MSG_NEW_MSG;
                        bundle.putString(CONSTANT.KEY_MSG_DATA,reMsg);
                        msg.setData(bundle);
                        msg.sendToTarget();
                        break;
                    case Protocol.TYPE_IMAGE:
                        int size = dis.readInt();
                        byte[] data = new byte[size];
                        int len = 0;
                        while (len < size) {
                            len += dis.read(data, len, size - len);
                        }
                        ByteArrayOutputStream outPut = new ByteArrayOutputStream();
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, outPut);
                        String name = System.currentTimeMillis()+"";
                        ImageCache.addBitmap(name,bmp);

                        msg.what = CONSTANT.MSG_NEW_IMG;
                        bundle.putString(CONSTANT.KEY_MSG_DATA,name);
                        msg.setData(bundle);
                        msg.sendToTarget();
                        break;
                    case Protocol.TYPE_VIDEO:
                        int frame_size = 1024;
                        int number = 0,num;
                        byte[] buffer = new byte[1024 * 64];
                        while (true) {
                            try {
                                num = dis.read(buffer, number, frame_size);
                                number += num;
                                if (num < frame_size) {
                                    break;
                                }
                            } catch (IOException e) {
                                break;
                            }
                        }

                        number = 0;
                        // �������������Ի�ȡ��Ƶ��

                        //��ȡ��ǰ���32���Լ��Ŀ�ͷ
                        try {
                            dis.read(buffer,0,32);
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                        RandomAccessFile raf = null;
                        try {
                            File file = new File("/sdcard/stream.h264");
                            if (file.exists())
                                file.delete();
                            raf = new RandomAccessFile(file, "rw");
                        } catch (Exception ex) {
                            Logger.i(ex.toString());
                        }

                        //��Щ����Ҫ��Ӧ�����ڵ���Ƶ���ã������仯�Ļ���Ҫȥ����ȷ����
                        //��Ȼ��֪���ǲ��ǲ�ͬ�Ļ����ǲ���һ����������ֻ��һ��HTC G7�����ԡ�
                        byte[] h264sps={0x67,0x42,0x00,0x0C,(byte) 0x96,0x54,0x0B,0x04,(byte) 0xA2};
                        byte[] h264pps={0x68,(byte) 0xCE,0x38,(byte) 0x80};
                        byte[] h264head={0,0,0,1};
                        try {
                            raf.write(h264head);
                            raf.write(h264sps);
                            raf.write(h264head);
                            raf.write(h264pps);
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                        while (true)
                        {
                            try {
                                //��ȡÿ���ĳ���
                                int h264length=dis.readInt();
                                number =0;
                                raf.write(h264head);
                                while(number<h264length)
                                {
                                    int lost=h264length-number;
                                    num = dis.read(buffer,0,frame_size<lost?frame_size:lost);
                                    Logger.i(String.format("H264 %d,%d,%d", h264length,number,num));
                                    number+=num;
                                    raf.write(buffer, 0, num);
                                }
                            } catch (IOException e) {
                                break;
                            }
                        }

                        raf.close();
                        /*while (true) {
                            try {
                                int h264length = dis.readInt();
                                int number = 0,num;
                                //raf.write(h264head);
                                while (number < h264length) {
                                    int lost = h264length - number;
                                    num = dis.read(buffer, 0, frame_size < lost ? frame_size : lost);
                                    Logger.i(String.format("H264 %d,%d,%d", h264length, number, num));
                                    number += num;
                                    //raf.write(buffer, 0, num);
                                }
                            }catch (Exception ex){
                                ex.printStackTrace();
                                break;
                            }
                        }*/
                }
            } catch (SocketException e) {
                System.out.println("exit!");
                isConnect = false;
            } catch (IOException e) {
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
                send("connect");
                System.out.println("connect");
                isConnect=true;
            }
        }catch (UnknownHostException e) {
            System.out.println("unknown host");
            e.printStackTrace();
        }catch (SocketTimeoutException e) {
            System.out.println("timeout");
            e.printStackTrace();
        }catch (IOException e) {
            System.out.println("io error");
            e.printStackTrace();
        }
    }

    public void send(String text){
        if(isConnect){
            try {
                TCPMessage message = new TCPMessage(name,System.currentTimeMillis()+"",text);
                dos.writeInt(Protocol.TYPE_JSON);
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
                dis.close();
                dos.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disConnect();
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
            Message msg = networkHandler.obtainMessage(CONSTANT.MSG_SEND_MSG);
            Bundle bundle = new Bundle();
            bundle.putString(CONSTANT.KEY_MSG_DATA,text);
            msg.setData(bundle);
            msg.sendToTarget();
        }
    }

}
