package com.brotherjing.client.Direction;


import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.brotherjing.client.R;
import com.brotherjing.client.controller.TCPSmartcarControllerImpl;
import com.brotherjing.client.service.TCPClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class DirectionFragment extends Fragment {

    private static final String TAG = DirectionFragment.class.getCanonicalName();
    private ImageView btn_forward, btn_left, btn_right, btn_back, btn_stop;


    public DirectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_direction, container, false);


        btn_forward = (ImageView) view.findViewById(R.id.forwarding);
        btn_forward.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(TAG, "forward");
                if (getActivity() != null) {
                    ((DirectionActivity)getActivity()).getTCPSmartcarController().forward();
                }
                return false;
            }
        });
        btn_left = (ImageView) view.findViewById(R.id.left);
        btn_left.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (getActivity() != null) {
                    ((DirectionActivity)getActivity()).getTCPSmartcarController().turnLeft(0);
                }
                return false;
            }
        });
        btn_right = (ImageView) view.findViewById(R.id.right);
        btn_right.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (getActivity() != null) {
                    ((DirectionActivity)getActivity()).getTCPSmartcarController().turnRight(0);
                }
                return false;
            }
        });
        btn_back = (ImageView) view.findViewById(R.id.back);
        btn_back.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (getActivity() != null) {
                    ((DirectionActivity)getActivity()).getTCPSmartcarController().backward();
                }
                return false;
            }
        });
        btn_stop = (ImageView) view.findViewById(R.id.stop);
        btn_stop.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (getActivity() != null) {
                    Log.d(TAG, "stop");
                    ((DirectionActivity)getActivity()).getTCPSmartcarController().stop();
                }
                return false;
            }
        });



//        buttonList.add(forwarding);
//        buttonList.add(left);
//        buttonList.add(stop);
//        buttonList.add(right);
//        buttonList.add(back);

        return view;
    }


//    public void setBinder(TCPClient.MyBinder binderFromActivity) {
//        binder = binderFromActivity;
//        mTCPSmartcarController = new TCPSmartcarControllerImpl(binder);
//    }


}
