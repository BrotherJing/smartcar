package com.brotherjing.server;

import android.app.Application;
import android.content.Intent;

import com.brotherjing.server.service.BluetoothService;
import com.brotherjing.server.service.TCPServer;

/**
 * Created by Brotherjing on 2015/10/10.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        GlobalEnv.init();

        startService(new Intent(this, TCPServer.class));
        startService(new Intent(this, BluetoothService.class));
    }
}
