package com.brotherjing.client.Direction;


import android.os.Bundle;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class DirectionFragment extends Fragment {

    private static final String TAG = DirectionFragment.class.getCanonicalName();
    private TCPSmartcarControllerImpl mTCPSmartcarController;

    public DirectionFragment() {
        // Required empty public constructor
    }

    private ImageView btn_forward, btn_left, btn_right, btn_back;

    private ArrayList<Button> buttonList = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTCPSmartcarController = new TCPSmartcarControllerImpl(((DirectionActivity)getActivity()).getBinder());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_direction, container, false);

//        forwarding = (Button) view.findViewById(R.id.forwarding);
//        left = (Button) view.findViewById(R.id.left);
//        stop = (Button) view.findViewById(R.id.stop);
//        right = (Button) view.findViewById(R.id.right);
//        back = (Button) view.findViewById(R.id.back);

        btn_forward = (ImageView) view.findViewById(R.id.forwarding);
        btn_forward.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(TAG, "forward");
                return false;
            }
        });
        btn_left = (ImageView) view.findViewById(R.id.left);
        btn_left.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(TAG, "left");
                return false;
            }
        });
        btn_right = (ImageView) view.findViewById(R.id.right);
        btn_right.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(TAG, "right");
                return false;
            }
        });
        btn_back = (ImageView) view.findViewById(R.id.back);
        btn_back.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(TAG, "back");
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


}
