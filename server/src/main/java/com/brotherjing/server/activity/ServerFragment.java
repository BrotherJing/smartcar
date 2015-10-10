package com.brotherjing.server.activity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.brotherjing.server.R;
import com.brotherjing.utils.DateUtil;
import com.brotherjing.utils.Logger;
import com.brotherjing.utils.bean.TCPMessage;

import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class ServerFragment extends Fragment {

    private final static String KEY_IP_ADDR = "ip_addr";

    private TextView tv_ip_addr,tv_msg_list;
    //private ListView lv_messages;

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
        tv_msg_list = (TextView)mainView.findViewById(R.id.tv_msg_list);
        //lv_messages = (ListView)mainView.findViewById(R.id.lv_messages);

        if(savedInstanceState!=null){
            String ip = savedInstanceState.getString(KEY_IP_ADDR);
            Logger.i(ip);
            tv_ip_addr.setText(ip);
        }

        return mainView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_IP_ADDR, tv_ip_addr.getText().toString());
        Logger.i(tv_ip_addr.getText().toString());
    }

    public void refreshIpAddr(String ip){
        Logger.i(ip);
        tv_ip_addr.setText(ip);
    }

    public void newMessage(TCPMessage msg){
        tv_msg_list.setText(msg.getFrom()+":"+msg.getText()+"["+
                DateUtil.getDateTime(new Date(Long.parseLong(msg.getTimestamp())))+
                "]\n"+tv_msg_list.getText().toString());
    }

}
