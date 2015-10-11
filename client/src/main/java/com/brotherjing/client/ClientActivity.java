package com.brotherjing.client;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.brotherjing.client.service.TCPClient;
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

    private String reMsg = null;
    private boolean isConnect = false;

    private String name = "FUCKER";
    private String ip = null;
    private String port = null;

    private TCPClient.MyBinder binder;
    private TCPClientConnection conn;

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

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ip = edt_ip.getText().toString();
                port = edt_port.getText().toString();
                //networkHandler.sendEmptyMessage(1);//connect
                binder.connectServer(ip, port, name);
            }
        });
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //networkHandler.sendEmptyMessage(2);//send
                binder.send(edt_input.getText().toString());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(ClientActivity.this, TCPClient.class),conn=new TCPClientConnection(),BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(conn);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(ClientActivity.this,TCPClient.class));
    }

    private class TCPClientConnection implements ServiceConnection{
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (TCPClient.MyBinder)iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }

}
