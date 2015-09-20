package com.brotherjing.server;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brotherjing.utils.Logger;


/**
 * A simple {@link Fragment} subclass.
 */
public class ServerFragment extends Fragment {

    private TextView tv_ip_addr;

    public static ServerFragment newInstance(){
        ServerFragment fragment = new ServerFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    public ServerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_server, container, false);
        tv_ip_addr = (TextView)mainView.findViewById(R.id.tv_ip_addr);
        return mainView;
    }

    public void refreshIpAddr(String ip){
        Logger.i(ip);
        tv_ip_addr.setText(ip);
    }

}
