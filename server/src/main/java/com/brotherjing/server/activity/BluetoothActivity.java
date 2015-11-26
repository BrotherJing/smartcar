package com.brotherjing.server.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.brotherjing.server.CONSTANT;
import com.brotherjing.server.R;
import com.brotherjing.server.adapter.DeviceListAdapter;
import com.brotherjing.server.service.BluetoothService;
import com.brotherjing.server.service.TCPServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {
    final int REQUEST_ENABLE_BT = 1;

    LinearLayout llContent;
    TextView tvContent;
    EditText etInput;
    Button btnSend;
    ListView lvDevices;
    BluetoothDevice chosenDevice;

    DeviceListAdapter listAdapter;
    BluetoothAdapter adapter;

    BluetoothReceiver mReceiver;

    Thread clientThread;
    HandlerThread handlerThread;
    Handler clientHandler,uiHandler;
    DataInputStream dis;
    DataOutputStream dos;

    BluetoothService.MyBinder binder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //register a receiver listening to bluetooth events(discovery devices)
        mReceiver = new BluetoothReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        //customized action
        filter.addAction(CONSTANT.ACTION_CONNECTED);
        filter.addAction(CONSTANT.ACTION_NEW_MSG_BT);
        registerReceiver(mReceiver, filter);

        initView();

        initData();
    }

    private void initData(){
        adapter = BluetoothAdapter.getDefaultAdapter();
        if(!adapter.isEnabled()){
            //open bluetooth
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,REQUEST_ENABLE_BT);
        }else
            adapter.startDiscovery();

        for(BluetoothDevice device:adapter.getBondedDevices()){
            listAdapter.addItem(device);
        }

        /*uiHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what==CONSTANT.RECEIVE_MSG){
                    tvContent.setText(tvContent.getText().toString()+msg.getData().getString(CONSTANT.KEY_MSG_CONTENT)+"\n");
                }else if(msg.what==CONSTANT.CONNECTED){
                    Toast.makeText(BluetoothActivity.this, R.string.connected, Toast.LENGTH_SHORT).show();
                }
            }
        };*/
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mReceiver);
    }

    private void initView(){
        lvDevices = (ListView)findViewById(R.id.lv_devices);
        llContent = (LinearLayout)findViewById(R.id.ll_chat);
        tvContent = (TextView)findViewById(R.id.tv_content);
        etInput = (EditText)findViewById(R.id.et_input);
        btnSend = (Button)findViewById(R.id.btn_send);

        //llContent.setVisibility(View.GONE);

        listAdapter = new DeviceListAdapter(this);
        lvDevices.setAdapter(listAdapter);

        lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //startThread(listAdapter.getItem(i));
                chosenDevice = listAdapter.getItem(i);
                lvDevices.setVisibility(View.GONE);
                Intent intent = new Intent(BluetoothActivity.this, BluetoothService.class);
                intent.putExtra(CONSTANT.KEY_DEVICE, listAdapter.getItem(i));
                startService(intent);
                //llContent.setVisibility(View.VISIBLE);
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = etInput.getText().toString();
                tvContent.setText(tvContent.getText().toString()+input+"\n");
                Message message = clientHandler.obtainMessage(CONSTANT.SEND_MSG);
                Bundle bundle = new Bundle();
                bundle.putString(CONSTANT.KEY_MSG_CONTENT,input);
                message.setData(bundle);
                message.sendToTarget();
            }
        });
    }

    private class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //找到设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    listAdapter.addItem(device);
                }
            }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                setTitle(R.string.finish_discovery);
                Toast.makeText(BluetoothActivity.this,R.string.finish_discovery,Toast.LENGTH_SHORT).show();
            }else if(CONSTANT.ACTION_CONNECTED.equals(action)){
                Toast.makeText(BluetoothActivity.this,R.string.connected,Toast.LENGTH_SHORT).show();
                //BluetoothActivity.this.setResult();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_ENABLE_BT){
            if(resultCode==RESULT_OK){
                adapter.startDiscovery();
            }
        }
    }

    /*private void startThread(BluetoothDevice device){
        clientThread = new ClientThread(device);
        clientThread.start();
        handlerThread = new HandlerThread(device.getName());
        handlerThread.start();
        clientHandler = new Handler(handlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what==CONSTANT.SEND_MSG){
                    try {
                        //dos.writeUTF(msg.getData().getString(Constant.KEY_MSG_CONTENT));
                        dos.writeBytes(new String(msg.getData().getString(CONSTANT.KEY_MSG_CONTENT).getBytes("utf-8"),"iso-8859-1"));
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private class ClientThread extends Thread{

        BluetoothDevice device;

        public ClientThread(BluetoothDevice device) {
            super();
            this.device = device;
        }

        @Override
        public void run() {
            try {
                Log.i("yj", device.getName() + " " + device.getAddress());
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString(CONSTANT.MY_UUID));
                adapter.cancelDiscovery();
                socket.connect();
                uiHandler.sendEmptyMessage(CONSTANT.CONNECTED);
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
                while (true){
                    String str = dis.readUTF();
                    String ascii = new String(str.getBytes("utf-8"),"iso-8859-1");
                    Log.i("yj",ascii);
                    Log.i("yj",str);
                    Message message = uiHandler.obtainMessage(CONSTANT.RECEIVE_MSG);
                    Bundle bundle = new Bundle();
                    bundle.putString(CONSTANT.KEY_MSG_CONTENT,str);
                    message.setData(bundle);
                    message.sendToTarget();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }*/
}
