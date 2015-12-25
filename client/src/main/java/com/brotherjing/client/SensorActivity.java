package com.brotherjing.client;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.brotherjing.client.controller.TCPSmartcarControllerImpl;
import com.brotherjing.client.service.TCPClient;
import com.brotherjing.client.service.UDPClient;
import com.brotherjing.utils.ImageCache;
import com.brotherjing.utils.Protocol;
import com.brotherjing.utils.bean.TextMessage;
import com.dxjia.library.BaiduVoiceHelper;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class SensorActivity extends ActionBarActivity implements SensorEventListener{

    public static final float EPSILON = 0.000000001f;
    public static final float NS2S = 1.0f/1000000000f;
    public static final float COEF = 0.1f;
    public static final float ONE_MINUS_COEF = 0.9f;

    private boolean isFirst = false;
    private long timestamp;

    private float gyro[] = new float[3];
    private float gyro_matrix[] = new float[9];
    private float gyro_orien[] = new float[3];
    private float acc[] = new float[3];
    private float magnet[] = new float[3];
    private float acc_meg_orien[] = new float[3];
    private float rotation_matrix[] = new float[9];
    private float fused_orien[] = new float[3];

    private Timer task,oriTask;

    private SensorManager sensorManager;

    private TextView tv;

    private TCPClient.MyBinder binder;
    private TCPClientConnection conn;
//    private MainThreadReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        tv = (TextView)findViewById(R.id.tv);

        findViewById(R.id.iv_audio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binder!=null){
                    binder.send(new TextMessage(Protocol.REQ_AUDIO));
                }
                /*BaiduVoiceHelper.startBaiduVoiceDialogForResult(
                        SensorActivity.this,
                        CONSTANT.API_KEY,
                        CONSTANT.SECRET_KEY, 1);*/
            }
        });

        findViewById(R.id.iv_follow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binder!=null){
                    binder.send(new TextMessage(Protocol.REQ_FOLLOW));
                }
            }
        });

        isFirst = true;

        gyro[0] = 0.0f;
        gyro[1] = 0.0f;
        gyro[2] = 0.0f;

        // initialise gyroMatrix with identity matrix
        gyro_matrix[0] = 1.0f; gyro_matrix[1] = 0.0f; gyro_matrix[2] = 0.0f;
        gyro_matrix[3] = 0.0f; gyro_matrix[4] = 1.0f; gyro_matrix[5] = 0.0f;
        gyro_matrix[6] = 0.0f; gyro_matrix[7] = 0.0f; gyro_matrix[8] = 1.0f;

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        initListener();

        task = new Timer();
        task.scheduleAtFixedRate(new FusionTask(),1000,30);

        //register broadcast listening to server event
        /*IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CONSTANT.ACTION_NEW_MSG);
        //intentFilter.addAction(CONSTANT.ACTION_NEW_IMG);
        receiver = new MainThreadReceiver();
        registerReceiver(receiver, intentFilter);*/
    }

    private void initListener(){
        sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(sensorEvent.values,0,acc,0,3);
                calculateAccMegOrientation();
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroFunction(sensorEvent);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(sensorEvent.values,0,magnet,0,3);
                break;
            default:break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void calculateAccMegOrientation(){
        if(SensorManager.getRotationMatrix(rotation_matrix,null,acc,magnet)){
            SensorManager.getOrientation(rotation_matrix,acc_meg_orien);
        }
    }

    private void gyroFunction(SensorEvent event){
        if(acc_meg_orien==null)return;

        if(isFirst){
            isFirst = false;
            float initstate[] = new float[9];
            System.arraycopy(rotation_matrix,0,initstate,0,9);
            gyro_matrix = matrixMultiplication(gyro_matrix,initstate);
        }

        float delta[] = new float[4];
        if(timestamp!=0){
            final float dt = (event.timestamp - timestamp)*NS2S;
            getDeltaRotationMatrix(gyro,delta,dt);
        }
        timestamp = event.timestamp;

        float delta_matrix[] = new float[9];
        SensorManager.getRotationMatrixFromVector(delta_matrix,delta);
        gyro_matrix = matrixMultiplication(gyro_matrix,delta_matrix);
        SensorManager.getOrientation(gyro_matrix,gyro_orien);
    }


    private void getDeltaRotationMatrix(float[] gyro,float[] delta,float time){
        float angular_speed = (float)Math.sqrt(gyro[0]*gyro[0]+gyro[1]*gyro[1]+gyro[2]*gyro[2]);
        float norm[] = new float[3];
        if(angular_speed>EPSILON){
            norm[0] = gyro[0]/angular_speed;
            norm[1] = gyro[1]/angular_speed;
            norm[2] = gyro[2]/angular_speed;
        }
        float sin_theta = (float)Math.sin(angular_speed * time);
        float cos_theta = (float)Math.cos(angular_speed*time);
        delta[0] = sin_theta*norm[0];
        delta[1] = sin_theta*norm[1];
        delta[2] = sin_theta*norm[2];
        delta[3] = cos_theta;
    }

    private float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];

        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

        return result;
    }

    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];

        float sinX = (float)Math.sin(o[1]);
        float cosX = (float)Math.cos(o[1]);
        float sinY = (float)Math.sin(o[2]);
        float cosY = (float)Math.cos(o[2]);
        float sinZ = (float)Math.sin(o[0]);
        float cosZ = (float)Math.cos(o[0]);

        // rotation about x-axis (pitch)
        xM[0] = 1.0f; xM[1] = 0.0f; xM[2] = 0.0f;
        xM[3] = 0.0f; xM[4] = cosX; xM[5] = sinX;
        xM[6] = 0.0f; xM[7] = -sinX; xM[8] = cosX;

        // rotation about y-axis (roll)
        yM[0] = cosY; yM[1] = 0.0f; yM[2] = sinY;
        yM[3] = 0.0f; yM[4] = 1.0f; yM[5] = 0.0f;
        yM[6] = -sinY; yM[7] = 0.0f; yM[8] = cosY;

        // rotation about z-axis (azimuth)
        zM[0] = cosZ; zM[1] = sinZ; zM[2] = 0.0f;
        zM[3] = -sinZ; zM[4] = cosZ; zM[5] = 0.0f;
        zM[6] = 0.0f; zM[7] = 0.0f; zM[8] = 1.0f;

        // rotation order is y, x, z (roll, pitch, azimuth)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);
        return resultMatrix;
    }

    private class FusionTask extends TimerTask{
        @Override
        public void run() {
            fused_orien[0] = acc_meg_orien[0]*ONE_MINUS_COEF+gyro_orien[0]*COEF;
            fused_orien[1] = acc_meg_orien[1]*ONE_MINUS_COEF+gyro_orien[1]*COEF;
            fused_orien[2] = acc_meg_orien[2]*ONE_MINUS_COEF+gyro_orien[2]*COEF;
            gyro_matrix = getRotationMatrixFromOrientation(fused_orien);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //tv.setText(acc_meg_orien[0]*360.0/Math.PI+"\n"+acc_meg_orien[1]+"\n"+acc_meg_orien[2]);
                    tv.setText((fused_orien[0]*180.0/Math.PI+180)+"\n"+(fused_orien[1]*180.0/Math.PI+180)+"\n"+(fused_orien[2]*180.0/Math.PI+180));
                }
            });
        }
    }

    private class GetClientOrientationTask extends TimerTask{
        @Override
        public void run() {
            if(binder!=null){
                //send the orientation data
                binder.send(new TextMessage((fused_orien[0]*180.0/Math.PI+180)+""));
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, TCPClient.class), conn = new TCPClientConnection(), BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        task.cancel();
        oriTask.cancel();
        binder.send(new TextMessage(Protocol.REQ_STOP_FOLLOW));
        unbindService(conn);
        //unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class TCPClientConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (TCPClient.MyBinder)iBinder;
            //mTCPSmartcarController = new TCPSmartcarControllerImpl(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }

    //broadcast receiver listening to server events
    /*private class MainThreadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(CONSTANT.ACTION_NEW_MSG)){
                //Toast.makeText(SensorActivity.this,intent.getStringExtra(CONSTANT.KEY_MSG_DATA),Toast.LENGTH_SHORT).show();
                if(binder!=null){
                    //send the orientation data
                    binder.send(new TextMessage((fused_orien[0]*180.0/Math.PI+180)+""));
                }
            }
        }
    }*/
}
