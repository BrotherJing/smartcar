package com.brotherjing.server.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.brotherjing.server.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brotherjing on 2015/10/11.
 */
public class DeviceListAdapter extends BaseAdapter {

    private List<BluetoothDevice> deviceList;
    private Context context;

    public DeviceListAdapter(Context context) {
        super();
        deviceList = new ArrayList<>();
        this.context = context;
    }

    public void addItem(BluetoothDevice bd){
        deviceList.add(bd);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public BluetoothDevice getItem(int i) {
        return deviceList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;
        Holder holder;
        if(v==null){
            v = LayoutInflater.from(context).inflate(R.layout.item_btdevice, null);
            holder = new Holder();
            holder.name = (TextView)v.findViewById(R.id.tv_name);
            holder.addr = (TextView)v.findViewById(R.id.tv_addr);
            v.setTag(holder);
        }else{
            holder = (Holder)v.getTag();
        }
        holder.name.setText(deviceList.get(i).getName());
        holder.addr.setText(deviceList.get(i).getAddress());

        return v;
    }

    class Holder{
        TextView name;
        TextView addr;
    }
}
