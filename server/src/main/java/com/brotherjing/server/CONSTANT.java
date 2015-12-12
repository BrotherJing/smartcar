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
    String KEY_MSG_DATA = "msg_data";

    //broadcast action
    String ACTION_SERVER_UP = "server_up";
    String ACTION_NEW_MSG = "new_msg";

    //server thread handler
    int MSG_TEST = 1;

    //Global environment
    String GLOBAL_IP_ADDRESS = "ip_addr";
    String GLOBAL_AUDIENCE_ADDR = "audience_addr";

    //bluetooth-----------------------
    String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    int SEND_MSG = 1;
    int RECEIVE_MSG = 2;
    int CONNECTED = 3;

    //broadcast action
    String ACTION_CONNECTED = "connected";
    String ACTION_NEW_MSG_BT = "new_msg_bt";

    //key
    String KEY_MSG_CONTENT = "content";
    String KEY_DEVICE = "device";
}
