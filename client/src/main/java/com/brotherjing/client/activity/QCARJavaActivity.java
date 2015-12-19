package com.brotherjing.client.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.brotherjing.client.CONSTANT;
import com.brotherjing.client.Direction.NewJoyStickFragment;
import com.brotherjing.client.R;
import com.brotherjing.client.controller.TCPSmartcarControllerImpl;
import com.brotherjing.client.service.TCPClient;
import com.brotherjing.utils.Logger;
import com.qualcomm.QCARUnityPlayer.DebugLog;
import com.unity3d.player.UnityPlayerNativeActivity;

public class QCARJavaActivity extends UnityPlayerNativeActivity implements NewJoyStickFragment.OnDirectionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final long delay = 5000;//ms

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            public void run() {
                ViewGroup rootView = (ViewGroup)QCARJavaActivity.this.findViewById
                        (android.R.id.content);

                // find the first leaf view (i.e. a view without children)
                // the leaf view represents the topmost view in the view stack
                View topMostView = getLeafView(rootView);

                // let's add a sibling to the leaf view
                ViewGroup leafParent = (ViewGroup)topMostView.getParent();
                /*Button sampleButton = new Button(QCARJavaActivity.this);
                sampleButton.setText("Press Me");
                leafParent.addView(sampleButton, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));*/

                RelativeLayout mUILayout = (RelativeLayout) View.inflate(QCARJavaActivity.this, R.layout.camera_overlay,
                        null);

                mUILayout.setVisibility(View.VISIBLE);
                //mUILayout.setBackgroundColor(Color.BLACK);

                mUILayout.findViewById(R.id.loading_indicator).setVisibility(View.GONE);

                mUILayout.findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QCARJavaActivity.this.finish();
                    }
                });
                /*// Gets a reference to the loading dialog


                // Shows the loading indicator at start
                loadingDialogHandler
                        .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);*/

                // Adds the inflated layout to the view
                leafParent.addView(mUILayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

                FragmentManager manager = getFragmentManager();
                FragmentTransaction fragmentTransaction = manager.beginTransaction();
                joyStickFragment = new NewJoyStickFragment();
                fragmentTransaction.replace(R.id.fl_joystick,joyStickFragment).commit();
                joyStickFragment.setOnDirectionListener(QCARJavaActivity.this);

            }
        };

        handler.postDelayed(runnable, delay);
    }

    private View getLeafView(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup)view;
            for (int i = 0; i < vg.getChildCount(); ++i) {
                View chview = vg.getChildAt(i);
                View result = getLeafView(chview);
                if (result != null)
                    return result;
            }
            return null;
        }
        else {
            DebugLog.LOGE("Found leaf view");
            return view;
        }
    }

    private TCPClientConnection conn;
    private TCPClient.MyBinder binder;
    private TCPSmartcarControllerImpl mTCPSmartcarController;
    NewJoyStickFragment joyStickFragment;

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, TCPClient.class), conn = new TCPClientConnection(), BIND_AUTO_CREATE);
    }

    private class TCPClientConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (TCPClient.MyBinder)iBinder;
            mTCPSmartcarController = new TCPSmartcarControllerImpl(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(conn);
    }

    @Override
    public void onDirection(int direction, float offset) {
        if(mTCPSmartcarController==null)return;
        Logger.i(direction + " " + offset);
        switch (direction) {
            case CONSTANT.FORWARDING : {
                mTCPSmartcarController.forward();
                break;
            }
            case CONSTANT.LEFT: {
                mTCPSmartcarController.turnLeft(0);
                break;
            }
            case CONSTANT.RIGHT : {
                mTCPSmartcarController.turnRight(0);
                break;
            }
            case CONSTANT.BACK : {
                mTCPSmartcarController.backward();
                break;
            }
            case -1:
                mTCPSmartcarController.stop();
                break;
        }
        if(offset<0.1f)mTCPSmartcarController.stop();
    }

}
