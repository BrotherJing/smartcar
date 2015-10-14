package com.brotherjing.client;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.speech.SpeechRecognizer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.voicerecognition.android.ui.BaiduASRDigitalDialog;
import com.brotherjing.client.service.TCPClient;
import com.brotherjing.utils.ImageCache;
import com.brotherjing.utils.bean.TCPMessage;
import com.dxjia.library.BaiduVoiceHelper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;


public class ClientActivity extends ActionBarActivity {

    private final int REQUEST_UI = 1;

    private String name = "FUCKER";
    private String ip = null;
    private String port = null;

    private TCPClient.MyBinder binder;
    private TCPClientConnection conn;
    private MainThreadReceiver receiver;

    //test parameter//
    private EditText edt_ip, edt_port;
    private Button btn_connect;
    private EditText edt_input;
    private Button btn_submit,btn_asr;
    private LinearLayout ll_chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socketmsg);

        //register broadcast listening to server event
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CONSTANT.ACTION_NEW_MSG);
        intentFilter.addAction(CONSTANT.ACTION_NEW_IMG);
        receiver = new MainThreadReceiver();
        registerReceiver(receiver, intentFilter);

        edt_ip = (EditText) findViewById(R.id.edt_ip);
        edt_port = (EditText) findViewById(R.id.edt_port);
        btn_connect = (Button) findViewById(R.id.btn_connect);
        edt_input = (EditText) findViewById(R.id.edt_input);
        btn_submit = (Button) findViewById(R.id.btn_submit);
        btn_asr = (Button) findViewById(R.id.btn_asr);
        ll_chat = (LinearLayout) findViewById(R.id.ll_chat);

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
        btn_asr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent(ClientActivity.this,BaiduASRDigitalDialog.class);
                //intent.setAction("com.baidu.action.RECOGNIZE_SPEECH");
                intent.putExtra("appid", CONSTANT.APP_ID);
                intent.putExtra("key", CONSTANT.API_KEY);
                intent.putExtra("secret", CONSTANT.SECRET_KEY);
                intent.putExtra("sound_start", R.raw.bdspeech_recognition_start);
                intent.putExtra("sound_end", R.raw.bdspeech_speech_end);
                intent.putExtra("sound_success", R.raw.bdspeech_recognition_success);
                intent.putExtra("sound_error", R.raw.bdspeech_recognition_error);
                intent.putExtra("sound_cancel", R.raw.bdspeech_recognition_cancel);
                intent.putExtra("sample", 16000);

                intent.putExtra("language", "cmn-Hans-CN");
                //Log.i("yj", Environment.getExternalStorageDirectory().getAbsolutePath()+"/temp_license_2015-10-14");
                intent.putExtra("license-file-path", "/sdcard/temp_license_2015-10-14");
                startActivityForResult(intent, REQUEST_UI);*/
                BaiduVoiceHelper.startBaiduVoiceDialogForResult(ClientActivity.this,
                        CONSTANT.API_KEY, CONSTANT.SECRET_KEY, REQUEST_UI);
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
        unregisterReceiver(receiver);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            ArrayList<String> results = data.getStringArrayListExtra(SpeechRecognizer.RESULTS_RECOGNITION);
            String res = "";
            for(String i : results){
                res+=i+"\n";
            }
            Toast.makeText(ClientActivity.this,res,Toast.LENGTH_SHORT).show();
        }
    }

    //broadcast receiver listening to server events
    private class MainThreadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //if server is up and run, it will send ip address back
            if(intent.getAction().equals(CONSTANT.ACTION_NEW_IMG)){
                ImageView iv = new ImageView(ClientActivity.this);
                iv.setImageBitmap(ImageCache.getBitmapFromMemoryCache(intent.getStringExtra(CONSTANT.KEY_MSG_DATA)));
                ll_chat.addView(iv);
            }
            else if(intent.getAction().equals(CONSTANT.ACTION_NEW_MSG)){
                TextView tv = new TextView(ClientActivity.this);
                tv.setText(intent.getStringExtra(CONSTANT.KEY_MSG_DATA));
                ll_chat.addView(tv);
            }
        }
    }
}
