package com.brotherjing.client.Direction;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brotherjing.client.CONSTANT;
import com.brotherjing.client.R;
import com.brotherjing.client.controller.TCPSmartcarControllerImpl;
import com.jmedeisis.bugstick.Joystick;
import com.jmedeisis.bugstick.JoystickListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewJoyStickFragment extends Fragment {

    private static final String TAG = NewJoyStickFragment.class.getCanonicalName();

    //private TCPSmartcarControllerImpl mTCPSmartcarController;
    public NewJoyStickFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mTCPSmartcarController = new TCPSmartcarControllerImpl(((DirectionActivity)getActivity()).getBinder());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_joy_stick, container, false);

        /*final TextView angleView = (TextView) view.findViewById(R.id.tv_angle);
        final TextView offsetView = (TextView) view.findViewById(R.id.tv_offset);
        angleView.setVisibility(View.GONE);
        offsetView.setVisibility(View.GONE);*/


        final String angleNoneString = getString(R.string.angle_value_none);
        final String angleValueString = getString(R.string.angle_value);
        final String offsetNoneString = getString(R.string.offset_value_none);
        final String offsetValueString = getString(R.string.offset_value);

        Joystick joystick = (Joystick) view.findViewById(R.id.joystick);
        joystick.setJoystickListener(new JoystickListener() {
            @Override
            public void onDown() {
            }

            @Override
            public void onDrag(float degrees, float offset) {
                int direction = getTheDirection(degrees);
                if(listener!=null)listener.onDirection(direction,offset);
                /*switch (direction) {
                    case CONSTANT.FORWARDING : {
                        if (getActivity() != null) {
                            ((DirectionActivity)getActivity()).getTCPSmartcarController().forward();
                        }
                        break;
                    }
                    case CONSTANT.LEFT: {
                        if (getActivity() != null) {
                            ((DirectionActivity)getActivity()).getTCPSmartcarController().turnLeft(0);
                        }
                        break;
                    }
                    case CONSTANT.RIGHT : {
                        if (getActivity() != null) {
                            ((DirectionActivity)getActivity()).getTCPSmartcarController().turnRight(0);
                        }
                        break;
                    }
                    case CONSTANT.BACK : {
                        if (getActivity() != null) {
                            ((DirectionActivity)getActivity()).getTCPSmartcarController().backward();
                        }
                        break;
                    }
                    case -1:
                        if (getActivity() != null) {
                            ((DirectionActivity)getActivity()).getTCPSmartcarController().stop();
                        }
                        break;
                }*/

            }

            @Override
            public void onUp() {
                //stop
                //angleView.setText(angleNoneString);
                //offsetView.setText(offsetNoneString);
                if(listener!=null)listener.onDirection(0,0);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        //Log.d(TAG, "onPause");
    }

    private int getTheDirection(float degrees) {
        if ( 70 < degrees && degrees < 110) {
            return CONSTANT.FORWARDING;
        } else if ((160 < degrees && degrees < 180) || (-180 < degrees && degrees < -160)) {
            return CONSTANT.LEFT;
        } else if (-110 < degrees && degrees < -70) {
            return CONSTANT.BACK;
        } else if (-20 < degrees && degrees < 20) {
            return CONSTANT.RIGHT;
        }
        else return -1;
    }

    public interface OnDirectionListener{
        void onDirection(int direction,float offset);
    }

    private OnDirectionListener listener;

    public void setOnDirectionListener(OnDirectionListener listener){
        this.listener = listener;
    }

}
