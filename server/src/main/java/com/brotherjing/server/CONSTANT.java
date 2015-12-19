package com.brotherjing.server;

/**
 * Created by Brotherjing on 2015/9/20.
 */
public interface CONSTANT {

    //tcp----------------------------
    int PORT = 12345;

    String KEY_MSG_TYPE = "type";

    //ui thread handler
    String KEY_IP_ADDR = "ip_addr";
    int MSG_IP_ADDR = 1;
    int MSG_NEW_MSG = 2;
    int MSG_NEW_CLIENT = 3;
    String KEY_MSG_DATA = "msg_data";

    //broadcast action
    String ACTION_SERVER_UP = "server_up";
    String ACTION_NEW_CLIENT = "new_client";
    String ACTION_NEW_MSG = "new_msg";
    String ACTION_NEW_REQ = "new_req";

    //server thread handler
    int MSG_TEST = 1;

    //Global environment
    String GLOBAL_IP_ADDRESS = "ip_addr";
    String GLOBAL_AUDIENCE_ADDR = "audience_addr";
    String GLOBAL_IS_BLUETOOTH_CONNECTED = "is_blt_connected";

    //bluetooth-----------------------
    String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    int SEND_MSG = 1;
    int RECEIVE_MSG = 2;
    int CONNECTED = 3;

    //broadcast action
    String ACTION_DEVICE_CONNECTED = "connected";
    String ACTION_NEW_MSG_BT = "new_msg_bt";

    //key
    String KEY_MSG_CONTENT = "content";
    String KEY_DEVICE = "device";
    String KEY_CLIENT_NAME = "client_name";
    String KEY_REQ_TYPE = "req_type";

    //key value
    int REQ_TYPE_VIDEO = 1;
    int REQ_TYPE_AUDIO = 2;
    int REQ_TYPE_END_VIDEO = 3;

    //baidu asr
    int APP_ID = 7031386;
    String API_KEY = "gxF67xov7DA1QE2Hb9CC5UFu";
    String SECRET_KEY = "9f3577b4430c60e1bbaa98bdeb504f90";
}
