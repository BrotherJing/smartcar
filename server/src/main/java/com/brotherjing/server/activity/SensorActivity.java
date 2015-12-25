package com.brotherjing.server.activity;

import android.bluetooth.BluetoothDevice;
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
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.speech.SpeechRecognizer;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.brotherjing.server.CONSTANT;
import com.brotherjing.server.GlobalEnv;
import com.brotherjing.server.R;
import com.brotherjing.server.controller.BluetoothCarController;
import com.brotherjing.server.service.BluetoothService;
import com.brotherjing.server.service.ClientThread;
import com.brotherjing.server.service.TCPServer;
import com.brotherjing.utils.bean.TextMessage;
import com.dxjia.library.BaiduVoiceHelper;
import com.google.gson.Gson;

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

    private Timer task,clientTask;

    private SensorManager sensorManager;

    private TextView tv,tv_client,tv_diff;
    private ImageView iv_dir;

    final static int REQ_ASR = 2;
    IntentFilter intentFilter;
    MainThreadReceiver receiver;
    TCPServer.MyBinder binder;
    BluetoothService.MyBinder bluetoothBinder;
    BluetoothCarController carController = null;
    boolean isBluetoothConnected = false;

    double clientOri = 0;
    double serverDestinationOri = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        isBluetoothConnected = GlobalEnv.getBoolean(CONSTANT.GLOBAL_IS_BLUETOOTH_CONNECTED, false);
        intentFilter = new IntentFilter();
        intentFilter.addAction(CONSTANT.ACTION_NEW_MSG);
        intentFilter.addAction(CONSTANT.ACTION_NEW_REQ);
        receiver = new MainThreadReceiver();

        tv = (TextView)findViewById(R.id.tv);
        tv_client = (TextView)findViewById(R.id.tv_client);
        tv_diff = (TextView)findViewById(R.id.tv_diff);
        iv_dir = (ImageView)findViewById(R.id.iv_dir);

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
        /*clientTask = new Timer();
        clientTask.scheduleAtFixedRate(new GetClientOrientationTask(),1000,60);*/

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
                    tv.setText((fused_orien[0]*180.0/Math.PI+180.0)+"");
                }
            });
        }
    }

    private class GetClientOrientationTask extends TimerTask{
        @Override
        public void run() {
            if(binder!=null){
                List<ClientThread> clientThreads = binder.getClients();
                try {
                    for (ClientThread clientThread : clientThreads) {
                        clientThread.send(new TextMessage(" "));//request for orientation
                    }
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(receiver, intentFilter);
        bindService(new Intent(this, TCPServer.class), conn, BIND_AUTO_CREATE);
        if(isBluetoothConnected){
            bindService(new Intent(this,BluetoothService.class),bluetoothConn,BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
        unbindService(conn);
        if(isBluetoothConnected)
            unbindService(bluetoothConn);
        //handler = null;
        task.cancel();
        //clientTask.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //broadcast receiver listening to server events
    private class MainThreadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //if server is up and run, it will send ip address back
            if(intent.getAction().equals(CONSTANT.ACTION_NEW_MSG)){
                String clientDir =  new Gson().fromJson(intent.getStringExtra(CONSTANT.KEY_MSG_DATA), TextMessage.class).getText();
                processClientOrientation(clientDir);
            }else if(intent.getAction().equals(CONSTANT.ACTION_NEW_REQ)){
                int type = intent.getIntExtra(CONSTANT.KEY_REQ_TYPE,0);
                if(type==CONSTANT.REQ_TYPE_AUDIO){
                    BaiduVoiceHelper.startBaiduVoiceDialogForResult(SensorActivity.this, CONSTANT.API_KEY, CONSTANT.SECRET_KEY, REQ_ASR);
                }else if(type==CONSTANT.REQ_TYPE_STOP_FOLLOW){
                    finish();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQ_ASR){
            if (resultCode == RESULT_OK) {
                processASR(data.getStringArrayListExtra(SpeechRecognizer.RESULTS_RECOGNITION));
            }
        }
    }

    private void processClientOrientation(String orientation){
        try {
             clientOri = Double.parseDouble(orientation);
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
        double selfOri = fused_orien[0]*180.0/Math.PI+180;
        /*diff = serverDestinationOri - selfOri;
        //diff=diff>180?360-diff:diff;
        if(diff>180)diff=360-diff;
        else if(diff<-180)diff=diff+360;*/

        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_client.setText(clientOri + "");
                tv_diff.setText(diff + "");
            }
        });*/

        if(clientOri<180){
            serverDestinationOri = clientOri+180;
            if(selfOri>clientOri&&selfOri<serverDestinationOri){
                if(serverDestinationOri-selfOri<45)forward();
                else turnRight();
            }
            else {
                if((selfOri-serverDestinationOri+360)%360<45)forward();
                else turnLeft();
            }
        }
        else if(clientOri>180){
            serverDestinationOri = clientOri-180;
            if(selfOri<clientOri&&selfOri>serverDestinationOri) {
                if(selfOri-serverDestinationOri<45)forward();
                else turnLeft();
            }
            else {
                if((serverDestinationOri-selfOri+360)%360<45)forward();
                else turnRight();
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_client.setText(clientOri + "");
                tv_diff.setText(serverDestinationOri + "");
            }
        });
    }

    private void forward(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iv_dir.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_up_bold_black_48dp));
            }
        });
        if(carController==null ||!isBluetoothConnected)return;
        carController.forward();
    }

    private void turnRight(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iv_dir.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_right_bold_black_48dp));
            }
        });
        if(carController==null||!isBluetoothConnected)return;
        carController.turnRight();
    }

    private void turnLeft(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iv_dir.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_left_bold_black_48dp));
            }
        });
        if(carController==null ||!isBluetoothConnected)return;
        carController.turnLeft();
    }

    private void processASR(List<String> results){
        if(carController==null)return;
        String res = "";
        for(String i : results){
            res+=i+"\n";
            if(i.contains("前")){
                carController.forward();
            }else if(i.contains("后")){
                carController.backward();
            }else if(i.contains("停")){
                carController.stop();
                this.finish();
            }else if(i.contains("左")){
                carController.turnLeft();
            }else if(i.contains("右")){
                carController.turnRight();
            }else{
                continue;
            }
            break;
        }
        //Toast.makeText(this,res,Toast.LENGTH_SHORT).show();
    }

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //when server is bonded, request the ip address
            binder = (TCPServer.MyBinder)iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private ServiceConnection bluetoothConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bluetoothBinder = (BluetoothService.MyBinder)service;
            carController = new BluetoothCarController(bluetoothBinder);
            Toast.makeText(SensorActivity.this, "getString bluetooth binder", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

}
