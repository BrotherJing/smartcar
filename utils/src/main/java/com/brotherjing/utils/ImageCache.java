package com.brotherjing.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by Brotherjing on 2015/10/13.
 */
public class ImageCache {

    private static LruCache<String,Bitmap> mMemoryCache=null;

    public static void init(Context context){
        if(mMemoryCache!=null)return;
        int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        int cacheSize = 1024 * 1024 * memClass / 8;
        mMemoryCache = new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    public static void addBitmap(String key,Bitmap bitmap){
        if(mMemoryCache!=null){
            mMemoryCache.put(key,bitmap);
        }
    }

    public static Bitmap getBitmapFromMemoryCache(String key){
        return mMemoryCache.get(key);
    }

}
