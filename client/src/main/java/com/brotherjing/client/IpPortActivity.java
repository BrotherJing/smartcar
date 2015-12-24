package com.brotherjing.client;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.brotherjing.client.service.TCPClient;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;
import com.rengwuxian.materialedittext.MaterialEditText;

public class IpPortActivity extends AppCompatActivity {

    private static final String TAG = IpPortActivity.class.getSimpleName();
    private MaterialAutoCompleteTextView mEditTextIp, mEditTextPort;
    private Button mButtonConnect;
    private static final String [] array_ip = new String[]{
            "192.168.1.101", "192.168.1.104", "192.168.1.105"};
    private static final String [] array_port = new String[]{
            "12345"};

    private TCPClient.MyBinder binder;
    private TCPClientConnection conn;
    private String ip = null;
    private String port = null;
    private String name = "fucker";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip_port);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("设置IP端口");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(false);

        mEditTextIp = (MaterialAutoCompleteTextView) findViewById(R.id.edt_ip);
        ArrayAdapter<String> adapterIp = new ArrayAdapter<String>(
                IpPortActivity.this,
                android.R.layout.simple_dropdown_item_1line,
                array_ip);
        mEditTextIp.setAdapter(adapterIp);
        mEditTextIp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mEditTextPort = (MaterialAutoCompleteTextView) findViewById(R.id.edt_port);
        ArrayAdapter<String> adapterPort = new ArrayAdapter<String>(
                IpPortActivity.this,
                android.R.layout.simple_dropdown_item_1line,
                array_port);
        mEditTextPort.setAdapter(adapterPort);
        mEditTextPort.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        mButtonConnect = (Button) findViewById(R.id.btn_connect);
        mButtonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ip = mEditTextIp.getText().toString();
                port = mEditTextPort.getText().toString();
                Log.d(TAG, "ip: " + ip + "\n" + "port: " + port);
                binder.connectServer(ip, port, name);
                Toast.makeText(IpPortActivity.this, "Connected Success", Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(IpPortActivity.this, TCPClient.class), conn = new TCPClientConnection(), BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(conn);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class TCPClientConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (TCPClient.MyBinder)iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }

}