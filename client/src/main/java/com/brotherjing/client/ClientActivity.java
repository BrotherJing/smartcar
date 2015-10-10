package com.brotherjing.client;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.brotherjing.utils.bean.TCPMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;


public class ClientActivity extends ActionBarActivity {

    Thread thread = null;
    Socket socket = null;

    private InetSocketAddress isa = null;

    DataInputStream dis = null;
    DataOutputStream dos = null;
    private String reMsg = null;
    private boolean isConnect = false;

    private String name = null;
    private String ip = null;
    private String port = null;

    private HandlerThread networkThread;
    private Handler networkHandler;

    //test parameter//
    private EditText edt_ip, edt_port;
    private Button btn_connect;
    private EditText edt_input;
    private Button btn_submit;
    private String str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socketmsg);

        edt_ip = (EditText) findViewById(R.id.edt_ip);
        edt_port = (EditText) findViewById(R.id.edt_port);
        btn_connect = (Button) findViewById(R.id.btn_connect);
        edt_input = (EditText) findViewById(R.id.edt_input);
        btn_submit = (Button) findViewById(R.id.btn_submit);

        networkThread = new HandlerThread("network");
        networkThread.start();
        networkHandler = new Handler(networkThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 1:
                        connect();
                        break;
                    case 2:
                        send();
                        break;
                }
            }
        };

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ip = edt_ip.getText().toString();
                port = edt_port.getText().toString();
                networkHandler.sendEmptyMessage(1);//connect
            }
        });
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                networkHandler.sendEmptyMessage(2);//send
            }
        });
    }

    private void ReceiveMsg() {
        if (isConnect) {
            try {
                while ((reMsg = dis.readUTF()) != null) {
                    System.out.println(reMsg);
                    if (reMsg != null) {

                        try {
                            Message msgMessage = new Message();
                            msgMessage.what = 0x1981;
                            handler.sendMessage(msgMessage);
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
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

    public void send(){
        if(isConnect){
            try {
                //TCPMessage message = new TCPMessage(edt_input.getText().toString())
                //dos.writeUTF();
                dos.flush();
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }

    public void connect() {
        try {
            socket = new Socket();
            isa = new InetSocketAddress(ip,Integer.parseInt(port));
            socket.connect(isa,5000);

            if(socket.isConnected()){
                dos = new DataOutputStream (socket.getOutputStream());
                dis = new DataInputStream (socket.getInputStream());
                dos.writeUTF("online:" + name);
                dos.flush();
                /*thread = new Thread(null, doThread, "Message");
                thread.start();*/
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

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x1981:
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disConnect();
    }

}
