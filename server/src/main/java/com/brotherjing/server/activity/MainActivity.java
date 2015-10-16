package com.brotherjing.server.activity;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.brotherjing.server.CONSTANT;
import com.brotherjing.server.GlobalEnv;
import com.brotherjing.server.R;
import com.brotherjing.server.service.TCPServer;
import com.brotherjing.utils.Logger;
import com.brotherjing.utils.bean.TCPMessage;
import com.google.gson.Gson;

import javax.crypto.Mac;


public class MainActivity extends ActionBarActivity {

    //SectionsPagerAdapter mSectionsPagerAdapter;
    //List<Fragment> fragments;
    int currentIndex;

    //ViewPager mViewPager;
    TextView tv_addr,tv_content;
    Button mButton;

    MainThreadHandler handler;
    MainThreadReceiver receiver;
    TCPServer.MyBinder binder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //register broadcast listening to server event
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CONSTANT.ACTION_SERVER_UP);
        intentFilter.addAction(CONSTANT.ACTION_NEW_MSG);
        receiver = new MainThreadReceiver();
        registerReceiver(receiver,intentFilter);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        //initFragments();
        tv_addr = (TextView)findViewById(R.id.tv_ipaddr);
        tv_content = (TextView)findViewById(R.id.tv_content);
        mButton = (Button) findViewById(R.id.button_capture_image);

        initData();
    }

    /*private void initFragments(){

        fragments = new ArrayList<>();
        fragments.add(ServerFragment.newInstance());
        fragments.add(PlaceholderFragment.newInstance(1));
        fragments.add(PlaceholderFragment.newInstance(2));
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),fragments);

        //default page is first page
        currentIndex = 0;

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentIndex = position;
                if(position==0){
                    String ip = GlobalEnv.get(CONSTANT.GLOBAL_IP_ADDRESS);
                    if(ip!=null)
                        ((ServerFragment) fragments.get(currentIndex)).refreshIpAddr(ip);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }*/

    private void initData(){
        handler = new MainThreadHandler(this);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, VideoActivity.class);
                startActivity(intent);
//                if (!binder.getClientSockets().isEmpty()){
//                    Toast.makeText(MainActivity.this, "has" ,Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(MainActivity.this, "none" ,Toast.LENGTH_SHORT).show();
//                }
            }
        });
    }

    public final static class MainThreadHandler extends Handler{
        private WeakReference<MainActivity> reference;
        public MainThreadHandler(MainActivity activity) {
            super();
            reference = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = reference.get();
            switch (msg.what){
                case CONSTANT.MSG_IP_ADDR:
                    String ip = msg.getData().getString(CONSTANT.KEY_IP_ADDR);
                    GlobalEnv.put(CONSTANT.GLOBAL_IP_ADDRESS,ip);
                    /*Fragment currentFragment = activity.fragments.get(activity.currentIndex);
                    if(currentFragment instanceof ServerFragment){
                        ((ServerFragment) currentFragment).refreshIpAddr(ip);
                    }*/
                    activity.tv_addr.setText(ip);
                    break;
                case CONSTANT.MSG_NEW_MSG:
                    TCPMessage tcpMessage = new Gson().fromJson(msg.getData().getString(CONSTANT.KEY_MSG_DATA),TCPMessage.class);
                    Logger.i(msg.getData().getString(CONSTANT.KEY_MSG_DATA));
                    /*Fragment cf = activity.fragments.get(activity.currentIndex);
                    if(cf instanceof ServerFragment){
                        Logger.i("is server fragment");
                        ((ServerFragment) cf).newMessage(tcpMessage);
                    }*/
                    activity.tv_content.setText(tcpMessage.getText()+"\n"+activity.tv_content.getText().toString());
                    break;
                default:break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, TCPServer.class), conn, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.i("stop");
        unbindService(conn);
        handler = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.i("destroy");
        handler = null;
        stopService(new Intent(this,TCPServer.class));
        unregisterReceiver(receiver);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragmentList;

        public SectionsPagerAdapter(FragmentManager fm,List<Fragment> list) {
            super(fm);
            this.fragmentList = list;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    //broadcast receiver listening to server events
    private class MainThreadReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            //if server is up and run, it will send ip address back
            Message msg = handler.obtainMessage();
            Bundle bundle = new Bundle();
            if(intent.getAction().equals(CONSTANT.ACTION_SERVER_UP)){
                msg.what = CONSTANT.MSG_IP_ADDR;
                bundle.putString(CONSTANT.KEY_IP_ADDR, intent.getStringExtra(CONSTANT.KEY_IP_ADDR));
            }
            else if(intent.getAction().equals(CONSTANT.ACTION_NEW_MSG)){
                msg.what=CONSTANT.MSG_NEW_MSG;
                bundle.putString(CONSTANT.KEY_MSG_DATA, intent.getStringExtra(CONSTANT.KEY_MSG_DATA));
            }else{
                return;
            }
            msg.setData(bundle);
            msg.sendToTarget();
        }
    }

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //when server is bonded, request the ip address
            binder = (TCPServer.MyBinder)iBinder;
            String ip;
            if((ip=binder.getIP())!=null){
                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putInt(CONSTANT.KEY_MSG_TYPE,CONSTANT.MSG_IP_ADDR);
                bundle.putString(CONSTANT.KEY_IP_ADDR, ip);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
}
