package com.brotherjing.server;

import android.app.Application;

/**
 * Created by Brotherjing on 2015/10/10.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        GlobalEnv.init();
    }
}
