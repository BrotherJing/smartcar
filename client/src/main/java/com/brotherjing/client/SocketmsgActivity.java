package com.brotherjing.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.Toast;

import com.brotherjing.utils.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.MessageDigest;

public class SocketmsgActivity extends Activity {


    private static final String TAG = SocketmsgActivity.class.getSimpleName();

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
        btn_submit.setClickable(false);

        ip = "192.168.1.105";
        port = "12345";

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        connect();
                    }
                }).start();
                Logger.i(ip +" "+ port);
                connect();

            }
        });

        if (isConnect){
            btn_submit.setClickable(true);
            btn_submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    str = edt_input.getText().toString();
                    try {
                        Logger.i(str);
                        dos.writeUTF(str);
                    } catch (UnknownHostException e) {
                        Logger.i("connect failure");
                        Toast.makeText(SocketmsgActivity.this, "connect failure", Toast.LENGTH_LONG).show();
                        disConnect();
                        connect();
//            Intent intent = new Intent(SocketmsgActivity.this, InitActivity.class);
//            startActivity(intent);
//            SocketmsgActivity.this.finish();
                        e.printStackTrace();
                    } catch (SocketTimeoutException e) {
                        Logger.i("connect timeout");
                        Toast.makeText(SocketmsgActivity.this, "connect timeout", Toast.LENGTH_LONG).show();
                        disConnect();
                        connect();
//            Intent intent = new Intent(SocketmsgActivity.this, InitActivity.class);
//            startActivity(intent);
//            SocketmsgActivity.this.finish();
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disConnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_socketmsg, menu);
        return true;

//        if (ip == null || port == null) {
//            Intent intent = new Intent(SocketmsgActivity.this, InitActivity.class);
//            startActivity(intent);
//            SocketmsgActivity.this.finish();
//        } else {
//            connect();
//        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void ReceiveMsg() {
        if (isConnect) {
            try {
                while ((reMsg = dis.readUTF()) != null){
                    Logger.i(reMsg);
                    try {
                        Message msg = new Message();
                        msg.what = 1;
                        mHandler.sendMessage(msg);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void connect() {
        try {
            Logger.i("connect is here1");
            if (socket == null){
                Logger.i("connect is here2");
                socket = new Socket(ip, Integer.parseInt(port));
            }
//            isa = new InetSocketAddress(ip, Integer.parseInt(port));
//            socket.connect(isa, 5000);
            Logger.i("connect is here3");

            if (socket.isConnected()) {
                Toast.makeText(SocketmsgActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                dos = new DataOutputStream(socket.getOutputStream());
                dis = new DataInputStream(socket.getInputStream());
                dos.writeUTF("online");

                /**
                 *
                 */
                thread = new Thread(null, new Runnable() {
                    @Override
                    public void run() {
                        Logger.i("running");
                        ReceiveMsg();

                    }
                }, "Message");
                thread.start();
                Logger.i("connect");
                isConnect = true;

            }
        } catch (UnknownHostException e) {
            Logger.i("connect failure");
            Toast.makeText(SocketmsgActivity.this, "connect failure", Toast.LENGTH_LONG).show();
//            disConnect();
//            connect();
//            Intent intent = new Intent(SocketmsgActivity.this, InitActivity.class);
//            startActivity(intent);
//            SocketmsgActivity.this.finish();
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            Logger.i("connect timeout");
            Toast.makeText(SocketmsgActivity.this, "connect timeout", Toast.LENGTH_LONG).show();
//            disConnect();
//            connect();
//            Intent intent = new Intent(SocketmsgActivity.this, InitActivity.class);
//            startActivity(intent);
//            SocketmsgActivity.this.finish();
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    break;
            }
        }
    };

    public void disConnect(){
        if (dos != null){
            try {
                dos.writeUTF("offline");
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
