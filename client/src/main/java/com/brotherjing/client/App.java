package com.brotherjing.client;

import android.app.Application;

import com.brotherjing.utils.ImageCache;

/**
 * Created by Brotherjing on 2015/10/13.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ImageCache.init(this);
    }
}
