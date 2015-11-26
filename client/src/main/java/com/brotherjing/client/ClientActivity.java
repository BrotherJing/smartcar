package com.brotherjing.client;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.speech.SpeechRecognizer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brotherjing.client.UI.DemoActivity;
import com.brotherjing.client.vuforia.ImageTargets.ImageTargets;
import com.brotherjing.client.QRcode.MipcaActivityCapture;
import com.brotherjing.client.activity.ViewCameraActivity;
import com.brotherjing.client.service.TCPClient;
import com.brotherjing.utils.ImageCache;
//import com.brotherjing.utils.bean.TCPMessage;
import com.brotherjing.utils.bean.TextMessage;
import com.dxjia.library.BaiduVoiceHelper;

import com.gc.materialdesign.views.*;
import java.util.ArrayList;


public class ClientActivity extends ActionBarActivity {

    private final int REQUEST_UI = 1;
    private final static int SCANNIN_GREQUEST_CODE = 1;


    private String name = "FUCKER";
    private String ip = null;
    private String port = null;

    private TCPClient.MyBinder binder;
    private TCPClientConnection conn;
    private MainThreadReceiver receiver;

    //test parameter//
    private EditText edt_ip, edt_port;
    private ButtonRectangle btn_connect;
    private EditText edt_input;
//    private Button btn_submit,btn_asr,btn_ar,btn_video,btn_qrcode;
    private ButtonRectangle btn_submit,btn_asr,btn_ar,btn_video,btn_qrcode;

    private Button btn_demo;
    private ImageView iv_video;
    private LinearLayout ll_chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socketmsg);

        //register broadcast listening to server event
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CONSTANT.ACTION_NEW_MSG);
        //intentFilter.addAction(CONSTANT.ACTION_NEW_IMG);
        receiver = new MainThreadReceiver();
        registerReceiver(receiver, intentFilter);

        edt_ip = (EditText) findViewById(R.id.edt_ip);
        edt_port = (EditText) findViewById(R.id.edt_port);
        btn_connect = (ButtonRectangle) findViewById(R.id.btn_connect);
        edt_input = (EditText) findViewById(R.id.edt_input);
//        btn_submit = (Button) findViewById(R.id.btn_submit);
//        btn_asr = (Button) findViewById(R.id.btn_asr);
//        btn_ar = (Button) findViewById(R.id.btn_ar);
//        btn_video = (Button) findViewById(R.id.btn_video);
//        btn_qrcode = (Button) findViewById(R.id.btn_qrcode);
        btn_submit = (ButtonRectangle) findViewById(R.id.btn_submit);
        btn_asr = (ButtonRectangle) findViewById(R.id.btn_asr);
        btn_ar = (ButtonRectangle) findViewById(R.id.btn_ar);
        btn_video = (ButtonRectangle) findViewById(R.id.btn_video);
        btn_qrcode = (ButtonRectangle) findViewById(R.id.btn_qrcode);
        iv_video = (ImageView)findViewById(R.id.iv_video);
        btn_demo = (Button) findViewById(R.id.btn_demo);

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
                binder.send(new TextMessage(edt_input.getText().toString()));
            }
        });
        btn_asr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaiduVoiceHelper.startBaiduVoiceDialogForResult(ClientActivity.this, CONSTANT.API_KEY, CONSTANT.SECRET_KEY, REQUEST_UI);
            }
        });
        btn_ar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ClientActivity.this, ImageTargets.class));
            }
        });
        btn_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binder.send(new TextMessage("[req]"));
                startActivity(new Intent(ClientActivity.this, ViewCameraActivity.class));
            }
        });
        btn_qrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ClientActivity.this, MipcaActivityCapture.class);
                startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
            }
        });
        btn_demo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ClientActivity.this, DemoActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(ClientActivity.this, TCPClient.class), conn = new TCPClientConnection(), BIND_AUTO_CREATE);
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
        //stopService(new Intent(ClientActivity.this,TCPClient.class));
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

        switch (requestCode) {
            case SCANNIN_GREQUEST_CODE:
                if (resultCode == RESULT_OK){
                    Bundle bundle = data.getExtras();
                    edt_ip.setText(bundle.getString("result"));
                    edt_port.setText("12345");
//                    mImageView.setImageBitmap((Bitmap) data.getParcelableExtra("bitmap"));
                }
                break;
            default:
                if (resultCode == RESULT_OK) {
                    ArrayList<String> results = data.getStringArrayListExtra(SpeechRecognizer.RESULTS_RECOGNITION);
                    String res = "";
                    for(String i : results){
                        res+=i+"\n";
                    }
                    Toast.makeText(ClientActivity.this,res,Toast.LENGTH_SHORT).show();
                }
                break;

        }

//        if (resultCode == RESULT_OK) {
//            ArrayList<String> results = data.getStringArrayListExtra(SpeechRecognizer.RESULTS_RECOGNITION);
//            String res = "";
//            for(String i : results){
//                res+=i+"\n";
//            }
//            Toast.makeText(ClientActivity.this,res,Toast.LENGTH_SHORT).show();
//        }
    }


    //broadcast receiver listening to server events
    private class MainThreadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //if server is up and run, it will send ip address back
            if(intent.getAction().equals(CONSTANT.ACTION_NEW_IMG)){
                /*ImageView iv = new ImageView(ClientActivity.this);
                iv.setImageBitmap(ImageCache.getBitmapFromMemoryCache(intent.getStringExtra(CONSTANT.KEY_MSG_DATA)));
                ll_chat.addView(iv);*/
                iv_video.setImageBitmap(ImageCache.getBitmapFromMemoryCache(intent.getStringExtra(CONSTANT.KEY_MSG_DATA)));
            }
            else if(intent.getAction().equals(CONSTANT.ACTION_NEW_MSG)){
                TextView tv = new TextView(ClientActivity.this);
                tv.setText(intent.getStringExtra(CONSTANT.KEY_MSG_DATA));
                ll_chat.addView(tv);
            }
        }
    }
}
