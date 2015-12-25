package com.brotherjing.client.Direction;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.brotherjing.client.R;
import com.brotherjing.client.controller.TCPSmartcarControllerImpl;
import com.brotherjing.client.service.TCPClient;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentStatePagerItemAdapter;

public class DirectionActivity extends AppCompatActivity {

    private static final String TAG = DirectionActivity.class.getCanonicalName();
    private final CharSequence[] mCharSequences = {"重力感应", "方向键控制", "手柄操作"};

    /*Service*/
    private TCPClient.MyBinder binder;
    private TCPClientConnection conn;
    private TCPSmartcarControllerImpl mTCPSmartcarController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("方向控制");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        ViewGroup tab = (ViewGroup) findViewById(R.id.tab);
//        tab.addView(LayoutInflater.from(this).inflate(demo.layoutResId, tab, false));

        final ViewPager viewPager = (NoScrollViewPager) findViewById(R.id.viewpager);
        final SmartTabLayout viewPagerTab = (SmartTabLayout) findViewById(R.id.viewpagertab);
//        demo.setup(viewPagerTab);

        FragmentPagerItems pages = new FragmentPagerItems(this);
        pages.add(FragmentPagerItem.of(mCharSequences[0], GravitySensorFragment.class));
        pages.add(FragmentPagerItem.of(mCharSequences[1], DirectionFragment.class));
        pages.add(FragmentPagerItem.of(mCharSequences[2], JoyStickFragment.class));

        FragmentStatePagerItemAdapter adapter = new FragmentStatePagerItemAdapter(
                getSupportFragmentManager(), pages);

        viewPager.setAdapter(adapter);
        viewPagerTab.setViewPager(viewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(DirectionActivity.this, TCPClient.class), conn = new TCPClientConnection(), BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(conn);
    }

    private class TCPClientConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (TCPClient.MyBinder) iBinder;
            mTCPSmartcarController = new TCPSmartcarControllerImpl(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }

    public TCPClient.MyBinder getBinder() {
        return this.binder;
    }

    public TCPSmartcarControllerImpl getTCPSmartcarController() {return this.mTCPSmartcarController;}
}
