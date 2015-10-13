package com.brotherjing.client;

/**
 * Created by Brotherjing on 2015/9/20.
 */
public interface CONSTANT {

    int PORT = 12345;

    String KEY_MSG_TYPE = "type";

    //ui thread handler
    String KEY_IP_ADDR = "ip_addr";
    int MSG_IP_ADDR = 1;
    int MSG_SEND_MSG = 2;
    int MSG_NEW_IMG = 3;
    int MSG_NEW_MSG = 4;
    String KEY_MSG_DATA = "msg_data";

    //broadcast action
    String ACTION_SERVER_UP = "server_up";
    String ACTION_NEW_MSG = "new_msg";
    String ACTION_NEW_IMG = "new_img";

    //Global environment
    String GLOBAL_IP_ADDRESS = "ip_addr";

}
