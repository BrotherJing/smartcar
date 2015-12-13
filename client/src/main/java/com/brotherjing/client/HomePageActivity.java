package com.brotherjing.client;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.brotherjing.client.Direction.DirectionActivity;
import com.brotherjing.client.QRcode.MipcaActivityCapture;
import com.brotherjing.client.activity.ViewCameraActivity;
import com.brotherjing.client.activity.ViewDirectionActivity;
import com.brotherjing.client.service.TCPClient;
import com.brotherjing.client.vuforia.ImageTargets.ImageTargets;
import com.brotherjing.utils.bean.TextMessage;
import com.dxjia.library.BaiduVoiceHelper;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import java.util.ArrayList;

public class HomePageActivity extends AppCompatActivity {

    private static final String TAG = HomePageActivity.class.getCanonicalName();

    private static final int PROFILE_SETTING = 1;
    private final int REQUEST_UI = 1;
    private final static int SCANNIN_GREQUEST_CODE = 2;

    //save our header or result
    private AccountHeader headerResult = null;
    private Drawer result = null;


    private TCPClient.MyBinder binder;
    private TCPClientConnection conn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // Handle Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //set the back arrow in the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("SJTU-SmartCar");

        // Create a few sample profile
        final IProfile profile = new ProfileDrawerItem().withName("杨靖").withEmail("yangjing").withIdentifier(100);
        final IProfile profile2 = new ProfileDrawerItem().withName("戚文韬").withEmail("qiwentao").withIdentifier(101);
        final IProfile profile3 = new ProfileDrawerItem().withName("吴思禹").withEmail("wushiyu").withIdentifier(102);

        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .withTranslucentStatusBar(false)
                .addProfiles(
                        profile,
                        profile2,
                        profile3
                        //don't ask but google uses 14dp for the add account icon in gmail but 20dp for the normal icons (like manage account)
                )
                .withSavedInstance(savedInstanceState)
                .build();

        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
//                .withTranslucentStatusBar(false)
                .withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.voice_recognition).withIcon(R.drawable.ic_action_call).withIdentifier(1).withSelectable(true),
                        new PrimaryDrawerItem().withName(R.string.augmented_reality).withIcon(R.drawable.ic_action_new_picture).withIdentifier(2).withSelectable(true),
                        new PrimaryDrawerItem().withName(R.string.video_send_back).withIcon(R.drawable.ic_action_camera).withIdentifier(3).withSelectable(true),
                        new PrimaryDrawerItem().withName(R.string.qrcode).withIcon(R.drawable.ic_action_download).withIdentifier(4).withSelectable(true),
                        new PrimaryDrawerItem().withName(R.string.direction_controller).withIcon(R.drawable.ic_action_gamepad).withIdentifier(5).withSelectable(true),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName(R.string.ip_port_setting).withIcon(R.drawable.ic_action_settings).withIdentifier(20).withSelectable(true)
                ) // add the items we want to use with our Drawer
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem != null) {
                            Intent intent = null;
                            if (drawerItem.getIdentifier() == 1){

                                /* 语音识别 */
                                BaiduVoiceHelper.startBaiduVoiceDialogForResult(
                                        HomePageActivity.this,
                                        CONSTANT.API_KEY,
                                        CONSTANT.SECRET_KEY, REQUEST_UI);


                            } else if (drawerItem.getIdentifier() == 2){
                                /* 增强现实 */
                                startActivity(new Intent(HomePageActivity.this, ImageTargets.class));

                            } else if (drawerItem.getIdentifier() == 3){

                                /* 视频回传 */
                                binder.send(new TextMessage("[req]"));
                                startActivity(new Intent(HomePageActivity.this, ViewDirectionActivity.class));

                            } else if (drawerItem.getIdentifier() == 4){

                                /* 二维码扫描 */
                                startActivityForResult(
                                        new Intent(HomePageActivity.this, MipcaActivityCapture.class),
                                        SCANNIN_GREQUEST_CODE);

                            } else if (drawerItem.getIdentifier() == 5){

                                /* 小车方向控制 */
                                startActivity(new Intent(HomePageActivity.this, DirectionActivity.class));

                            } else if (drawerItem.getIdentifier() == 20) {
                                /* 端口设置 */
                                intent = new Intent(HomePageActivity.this, IpPortActivity.class);
                                startActivity(intent);
                            }
                        }
                        return true;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .buildView();

        ((ViewGroup) findViewById(R.id.frame_container)).addView(result.getSlider());
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(HomePageActivity.this, TCPClient.class), conn = new TCPClientConnection(), BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(conn);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SCANNIN_GREQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    binder.connectServer(
                            bundle.getString("result"),
                            "12345",
                            "fucker"
                            );
                }
                break;
            default:
                if (resultCode == RESULT_OK) {
                    ArrayList<String> results = data.getStringArrayListExtra(SpeechRecognizer.RESULTS_RECOGNITION);
                    String res = "";
                    for (String i : results) {
                        res += i + "\n";
                    }
                    Toast.makeText(HomePageActivity.this, res, Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }
    private class TCPClientConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (TCPClient.MyBinder)iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }

    private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(IDrawerItem drawerItem, CompoundButton buttonView, boolean isChecked) {
            if (drawerItem instanceof Nameable) {
                Log.i("material-drawer", "DrawerItem: " + ((Nameable) drawerItem).getName() + " - toggleChecked: " + isChecked);
            } else {
                Log.i("material-drawer", "toggleChecked: " + isChecked);
            }
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = result.saveInstanceState(outState);
        //add the values which need to be saved from the accountHeader to the bundle
        outState = headerResult.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handle the click on the back arrow click
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
