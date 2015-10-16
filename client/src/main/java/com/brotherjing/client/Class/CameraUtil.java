package com.brotherjing.client.Class;

import android.hardware.Camera;

/**
 * Created by apple on 10/14/15.
 */
public class CameraUtil {

    @SuppressWarnings("deprecation")
    public static Camera getCameraInstance(){
        Camera camera = null;
        try {
            camera = Camera.open(0);
        } catch (Exception e){

        }
        return camera;
    }

}
