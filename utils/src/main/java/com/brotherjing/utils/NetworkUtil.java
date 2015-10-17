package com.brotherjing.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by Brotherjing on 2015/9/20.
 */
public class NetworkUtil {

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

    public static String getLocalIP(Context context){
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        if(ipAddress==0)return null;
        return ((ipAddress & 0xff)+"."+(ipAddress>>8 & 0xff)+"."
                +(ipAddress>>16 & 0xff)+"."+(ipAddress>>24 & 0xff));
    }

}
