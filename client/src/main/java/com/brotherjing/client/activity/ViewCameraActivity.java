package com.brotherjing.client.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.brotherjing.client.CONSTANT;
import com.brotherjing.client.R;
import com.brotherjing.client.service.TCPClient;
import com.brotherjing.client.service.UDPClient;
import com.brotherjing.client.view.ImageSurfaceView;
import com.brotherjing.utils.ImageCache;

public class ViewCameraActivity extends Activity {

    ImageSurfaceView surfaceView;
    UIReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_view_camera);

        surfaceView = (ImageSurfaceView)findViewById(R.id.surfaceview_camera);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CONSTANT.ACTION_NEW_IMG);
        receiver = new UIReceiver();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService(new Intent(ViewCameraActivity.this, UDPClient.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopService(new Intent(ViewCameraActivity.this, UDPClient.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    //broadcast receiver listening to server events
    private class UIReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //if server is up and run, it will send ip address back
            if(intent.getAction().equals(CONSTANT.ACTION_NEW_IMG)){
                surfaceView.drawBitmap(ImageCache.getBitmapFromMemoryCache(intent.getStringExtra(CONSTANT.KEY_MSG_DATA)));
            }
        }
    }

}
