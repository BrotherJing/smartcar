package com.brotherjing.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.SizeF;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brotherjing.utils.Logger;


public class MainActivity extends ActionBarActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    List<Fragment> fragments;
    int currentIndex;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    MainThreadHandler handler;
    ServerThread serverThread;
    ServerThread.ServerHandler serverHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        fragments = new ArrayList<>();
        fragments.add(ServerFragment.newInstance());
        fragments.add(PlaceholderFragment.newInstance(1));
        fragments.add(PlaceholderFragment.newInstance(2));
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),fragments);
        //currentFragment = mSectionsPagerAdapter.getItem(0);
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
                //currentFragment = mSectionsPagerAdapter.getItem(position);
                currentIndex = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        initData();

    }

    private void initData(){
        handler = new MainThreadHandler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initServerThread();
            }
        },500);
    }

    private void initServerThread(){
        serverThread = new ServerThread("server",this);
        serverThread.setMainThreadHandler(handler);
        serverThread.initAndStart();
    }

    public class MainThreadHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.getData().getInt(CONSTANT.KEY_MSG_TYPE)){
                case CONSTANT.MSG_IP_ADDR:
                    Fragment currentFragment = fragments.get(currentIndex);
                    if(currentFragment instanceof ServerFragment){
                            ((ServerFragment) currentFragment).refreshIpAddr(msg.getData().getString(CONSTANT.KEY_IP_ADDR));
                    }
                    break;
                default:break;
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
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

}
