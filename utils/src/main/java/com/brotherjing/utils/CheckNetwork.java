package com.brotherjing.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Brotherjing on 2015/9/20.
 */
public class CheckNetwork {

    public static final int CONNECTED = 1;
    public static final int DIS_CONNECTED = 2;

    public static int getNetworkState(Context context){

        ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State wifi_state = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if(wifi_state== NetworkInfo.State.CONNECTED){
            return CONNECTED;
        }
        return DIS_CONNECTED;
    }

}
