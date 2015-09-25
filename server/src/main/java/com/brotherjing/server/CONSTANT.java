package com.brotherjing.server;

/**
 * Created by Brotherjing on 2015/9/20.
 */
public interface CONSTANT {

    int PORT = 12345;

    String KEY_MSG_TYPE = "type";

    //ui thread handler
    String KEY_IP_ADDR = "ip_addr";
    int MSG_IP_ADDR = 1;
    String ACTION_SERVER_UP = "server_up";

    //server thread handler
    int MSG_TEST = 1;

}
