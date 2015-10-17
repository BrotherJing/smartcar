package com.brotherjing.client;

import android.app.Application;
import android.content.Intent;

import com.brotherjing.client.service.TCPClient;
import com.brotherjing.utils.ImageCache;

/**
 * Created by Brotherjing on 2015/10/13.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ImageCache.init(this);

        startService(new Intent(this, TCPClient.class));
    }
}
