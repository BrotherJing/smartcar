package com.brotherjing.client.Direction;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.TextViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.brotherjing.client.CONSTANT;
import com.brotherjing.client.R;
import com.brotherjing.client.controller.TCPSmartcarControllerImpl;
import com.brotherjing.client.vuforia.SampleApplication.utils.LoadingDialogHandler;

import java.lang.reflect.GenericArrayType;

public class GravitySensorFragment extends Fragment {

    private static final String TAG = GravitySensorFragment.class.getCanonicalName();
    private boolean isViewShown = false;

    private TCPSmartcarControllerImpl mTCPSmartcarController;
    private SensorManager mSensorManager;
    private MySensorEventLisener mMySensorEventLisener;

    private ImageView mImageView;

    public GravitySensorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mMySensorEventLisener = new MySensorEventLisener();
        mTCPSmartcarController = new TCPSmartcarControllerImpl(((DirectionActivity)getActivity()).getBinder());
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
        } else {
            if (mSensorManager != null) {
                mSensorManager.unregisterListener(mMySensorEventLisener);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gravity_sensor, container, false);
//        mTextView = (TextView) view.findViewById(R.id.textView);
        mImageView = (ImageView) view.findViewById(R.id.imgArrow);
        return view;
    }

    @Override
    public void onResume() {
        Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(mMySensorEventLisener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mMySensorEventLisener);
        Log.d(TAG, "onPause");

    }

    private class MySensorEventLisener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = sensorEvent.values[SensorManager.DATA_X];
                float y = sensorEvent.values[SensorManager.DATA_Y];
//                float z = sensorEvent.values[SensorManager.DATA_Z];
                switch (getDirecation(x, y)) {
                    case CONSTANT.FORWARDING: {
//                        mTextView.setText("FORWARDING");
                        Log.d(TAG, "forward");
                        mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_up_bold_black_48dp));
                        if (getActivity() != null) {
                            ((DirectionActivity)getActivity()).getTCPSmartcarController().forward();
                        }
                        break;
                    }
                    case CONSTANT.BACK: {
                        Log.d(TAG, "back");
//                        mTextView.setText("BACK");
                        mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_down_bold_black_48dp));
                        if (getActivity() != null) {
                            ((DirectionActivity)getActivity()).getTCPSmartcarController().backward();
                        }

                        break;
                    }
                    case CONSTANT.LEFT: {
                        Log.d(TAG, "left");
//                        mTextView.setText("LEFT");
                        mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_left_bold_black_48dp));
                        if (getActivity() != null) {
                            ((DirectionActivity)getActivity()).getTCPSmartcarController().turnLeft(0);
                        }

                        break;
                    }
                    case CONSTANT.RIGHT: {
                        Log.d(TAG, "right");
//                        mTextView.setText("RIGHT");
                        mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_right_bold_black_48dp));
                        if (getActivity() != null) {
                            ((DirectionActivity)getActivity()).getTCPSmartcarController().turnRight(0);
                        }

                        break;
                    }
                    default:
                        if (getActivity() != null) {
                            ((DirectionActivity)getActivity()).getTCPSmartcarController().stop();
                            mImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_remove_circle_black_48dp));
                        }
                        break;

                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }

    private int getDirecation(float x, float y) {
        if (-1 < x && x < 1) {
            if (y < -3) { //forwarding
                return CONSTANT.FORWARDING;
            } else if (y > 3) { //back
                return CONSTANT.BACK;
            }
        } else if (x > 4) { //left
            return CONSTANT.LEFT;
        } else if (x < -4) { //right
            return CONSTANT.RIGHT;
        }
        return -1;
    }


}
