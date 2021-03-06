package com.brotherjing.server.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.brotherjing.server.CONSTANT;
import com.brotherjing.server.R;
import com.brotherjing.server.service.TCPServer;
import com.brotherjing.utils.Logger;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Hashtable;

public class QrcodeActivity extends AppCompatActivity {

    private final int QR_WIDTH = 300;
    private final int QR_HEIGHT = 300;

    TCPServer.MyBinder binder;
    private String ipAddr;
    private ImageView mImageView;

    private Receiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        mImageView = (ImageView) findViewById(R.id.img_qrcode);


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CONSTANT.ACTION_NEW_CLIENT);
        receiver = new Receiver();
        registerReceiver(receiver,intentFilter);
    }

    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, TCPServer.class), conn, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.i("stop");
        unbindService(conn);
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.i("destroy");
        //stopService(new Intent(this, TCPServer.class));
    }

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //when server is bonded, request the ip address
            binder = (TCPServer.MyBinder) iBinder;
            if ((ipAddr = binder.getIP()) != null) {
                Logger.i(ipAddr);
                createQRImage(ipAddr, mImageView);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    //broadcast receiver listening to server events
    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(CONSTANT.ACTION_NEW_CLIENT)){
                QrcodeActivity.this.finish();
            }
        }
    }

    private void createQRImage (String url, ImageView imageView){
        try {
            if (url == null || "".equals(url) || url.length() < 1) {
                return;
            }

            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
            //下面这里按照二维码的算法，逐个生成二维码的图片，
            //两个for循环是图片横列扫描的结果
            for (int y = 0; y < QR_HEIGHT; y++)
            {
                for (int x = 0; x < QR_WIDTH; x++)
                {
                    if (bitMatrix.get(x, y))
                    {
                        pixels[y * QR_WIDTH + x] = 0xff000000;
                    }
                    else
                    {
                        pixels[y * QR_WIDTH + x] = 0xffffffff;
                    }
                }
            }
            //生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
            //显示到一个ImageView上面
            imageView.setImageBitmap(bitmap);

        } catch (WriterException e){
            Logger.i(e.getMessage());
        }
    }
}
