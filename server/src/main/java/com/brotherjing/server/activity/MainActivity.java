package com.brotherjing.server.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.SpeechRecognizer;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.brotherjing.server.CONSTANT;
import com.brotherjing.server.GlobalEnv;
import com.brotherjing.server.R;
import com.brotherjing.server.controller.BluetoothCarController;
import com.brotherjing.server.service.BluetoothService;
import com.brotherjing.server.service.TCPServer;
import com.brotherjing.utils.Logger;
import com.brotherjing.utils.Protocol;
import com.brotherjing.utils.bean.CommandMessage;
import com.brotherjing.utils.bean.TextMessage;
import com.dxjia.library.BaiduVoiceHelper;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;

import info.hoang8f.widget.FButton;


public class MainActivity extends ActionBarActivity {

    final static int REQ_BLUETOOTH = 1;
    final static int REQ_ASR = 2;

    boolean isBluetoothServiceBinded = false;

    TextView tv_addr,tv_content;
//    Button mButton, qrCodeButton,btnBluetooth;
    FButton mButton, qrCodeButton,btnBluetooth;

    MainThreadHandler handler;

    IntentFilter intentFilter;
    MainThreadReceiver receiver;

    TCPServer.MyBinder binder;
    BluetoothService.MyBinder bluetoothBinder;
    BluetoothCarController carController = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
//        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //register broadcast listening to server event
        intentFilter = new IntentFilter();
        intentFilter.addAction(CONSTANT.ACTION_SERVER_UP);
        intentFilter.addAction(CONSTANT.ACTION_NEW_MSG);
        intentFilter.addAction(CONSTANT.ACTION_NEW_CLIENT);
        intentFilter.addAction(CONSTANT.ACTION_NEW_REQ);
        receiver = new MainThreadReceiver();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        //initFragments();
        tv_addr = (TextView)findViewById(R.id.tv_ipaddr);
        tv_content = (TextView)findViewById(R.id.tv_content);
//        mButton = (Button) findViewById(R.id.button_capture_image);
//        qrCodeButton = (Button) findViewById(R.id.btn_generate_qrcode);
//        btnBluetooth = (Button) findViewById(R.id.btn_bluetooth);
        mButton = (FButton) findViewById(R.id.button_capture_image);
        qrCodeButton = (FButton) findViewById(R.id.btn_generate_qrcode);
        btnBluetooth = (FButton) findViewById(R.id.btn_bluetooth);

        initData();
    }

    private void initData(){
        handler = new MainThreadHandler(this);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Intent intent = new Intent(MainActivity.this, VideoActivity.class);
                Intent intent = new Intent(MainActivity.this,SimpleVideoActivity.class);
                startActivity(intent);
//                if (!binder.getClientSockets().isEmpty()){
//                    Toast.makeText(MainActivity.this, "has" ,Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(MainActivity.this, "none" ,Toast.LENGTH_SHORT).show();
//                }
            }
        });

        qrCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, QrcodeActivity.class);
                startActivity(intent);
            }
        });

        btnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, BluetoothActivity.class), REQ_BLUETOOTH);
            }
        });

        findViewById(R.id.btn_asr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaiduVoiceHelper.startBaiduVoiceDialogForResult(MainActivity.this, CONSTANT.API_KEY, CONSTANT.SECRET_KEY, REQ_ASR);
            }
        });
    }

    public final static class MainThreadHandler extends Handler{
        private WeakReference<MainActivity> reference;
        public MainThreadHandler(MainActivity activity) {
            super();
            reference = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = reference.get();
            switch (msg.what){
                case CONSTANT.MSG_IP_ADDR:
                    String ip = msg.getData().getString(CONSTANT.KEY_IP_ADDR);
                    GlobalEnv.put(CONSTANT.GLOBAL_IP_ADDRESS,ip);
                    activity.tv_addr.setText(ip);
                    break;
                case CONSTANT.MSG_NEW_MSG:
                    com.brotherjing.utils.bean.Message message = new Gson().fromJson(msg.getData().getString(CONSTANT.KEY_MSG_DATA), com.brotherjing.utils.bean.Message.class);
                    if(message.getMsgType()== Protocol.MSG_TYPE_TEXT){
                        TextMessage textMessage = new Gson().fromJson(msg.getData().getString(CONSTANT.KEY_MSG_DATA),TextMessage.class);
                        Logger.i(msg.getData().getString(CONSTANT.KEY_MSG_DATA));
                        activity.tv_content.setText(textMessage.getText()+"\n"+activity.tv_content.getText().toString());
                    }else{
                        CommandMessage cmd = new Gson().fromJson(msg.getData().getString(CONSTANT.KEY_MSG_DATA),CommandMessage.class);
                        if(activity.carController!=null){
                            activity.carController.processCommand(cmd.getCommand());
                        }
                    }

                    break;
                default:break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(receiver, intentFilter);
        bindService(new Intent(this, TCPServer.class), conn, BIND_AUTO_CREATE);
        if(isBluetoothServiceBinded){
            bindService(new Intent(this,BluetoothService.class),bluetoothConn,BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
        unbindService(conn);
        if(isBluetoothServiceBinded)
            unbindService(bluetoothConn);
        //handler = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler = null;
        //stopService(new Intent(this,TCPServer.class));
    }


    //broadcast receiver listening to server events
    private class MainThreadReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            //if server is up and run, it will send ip address back
            Message msg = handler.obtainMessage();
            Bundle bundle = new Bundle();
            if(intent.getAction().equals(CONSTANT.ACTION_SERVER_UP)){
                msg.what = CONSTANT.MSG_IP_ADDR;
                bundle.putString(CONSTANT.KEY_IP_ADDR, intent.getStringExtra(CONSTANT.KEY_IP_ADDR));
            }
            else if(intent.getAction().equals(CONSTANT.ACTION_NEW_MSG)){
                msg.what=CONSTANT.MSG_NEW_MSG;
                bundle.putString(CONSTANT.KEY_MSG_DATA, intent.getStringExtra(CONSTANT.KEY_MSG_DATA));
            }else if(intent.getAction().equals(CONSTANT.ACTION_NEW_CLIENT)){
                msg.what=CONSTANT.MSG_NEW_CLIENT;
                bundle.putString(CONSTANT.KEY_CLIENT_NAME, intent.getStringExtra(CONSTANT.KEY_CLIENT_NAME));
            }else if(intent.getAction().equals(CONSTANT.ACTION_NEW_REQ)){
                //GO TO VIDEO ACTIVITY
                int type = intent.getIntExtra(CONSTANT.KEY_REQ_TYPE,0);
                if(type==CONSTANT.REQ_TYPE_VIDEO) {
                    Intent intent1 = new Intent(MainActivity.this, SimpleVideoActivity.class);
                    startActivity(intent1);
                }else if(type==CONSTANT.REQ_TYPE_AUDIO){
                    BaiduVoiceHelper.startBaiduVoiceDialogForResult(MainActivity.this, CONSTANT.API_KEY, CONSTANT.SECRET_KEY, REQ_ASR);
                }
            }else{
                return;
            }
            msg.setData(bundle);
            msg.sendToTarget();
        }
    }

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //when server is bonded, request the ip address
            binder = (TCPServer.MyBinder)iBinder;
            String ip;
            if((ip=binder.getIP())!=null){
                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putInt(CONSTANT.KEY_MSG_TYPE,CONSTANT.MSG_IP_ADDR);
                bundle.putString(CONSTANT.KEY_IP_ADDR, ip);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private ServiceConnection bluetoothConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bluetoothBinder = (BluetoothService.MyBinder)service;
            carController = new BluetoothCarController(bluetoothBinder);
            Toast.makeText(MainActivity.this,"getString bluetooth binder",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQ_BLUETOOTH){
            if(resultCode==RESULT_OK){
                GlobalEnv.put(CONSTANT.GLOBAL_IS_BLUETOOTH_CONNECTED,true);
                BluetoothDevice device = data.getParcelableExtra(CONSTANT.KEY_DEVICE);
                Toast.makeText(this,device.getName(),Toast.LENGTH_SHORT).show();
                bindService(new Intent(this, BluetoothService.class), bluetoothConn, BIND_AUTO_CREATE);
                isBluetoothServiceBinded = true;
            }
        }else if(requestCode==REQ_ASR){
            if (resultCode == RESULT_OK) {
                ArrayList<String> results = data.getStringArrayListExtra(SpeechRecognizer.RESULTS_RECOGNITION);
                String res = "";
                for(String i : results){
                    res+=i+"\n";
                }
                Toast.makeText(this,res,Toast.LENGTH_SHORT).show();
            }
        }
    }
}
