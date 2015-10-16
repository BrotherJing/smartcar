package com.brotherjing.server.Camera;

import android.content.pm.PackageManager;
import android.hardware.Camera;

import com.brotherjing.utils.Logger;

/**
 * Created by apple on 10/16/15.
 */
public class CameraUtils {


    @SuppressWarnings("deprecation")
    public static Camera getCameraInstance(){
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e){
            Logger.i(e.getMessage());
        }
        return camera;
    }

}
