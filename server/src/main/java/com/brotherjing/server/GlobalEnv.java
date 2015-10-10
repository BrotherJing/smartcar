package com.brotherjing.server;

import java.util.HashMap;

/**
 * Created by Brotherjing on 2015/10/10.
 */
public class GlobalEnv {

    public static HashMap<String,String> stringValues;

    public static void init(){
        stringValues = new HashMap<>();
    }

    public static String get(String key){
        return stringValues.get(key);
    }

    public static void put(String key,String value){
        stringValues.put(key,value);
    }

}
