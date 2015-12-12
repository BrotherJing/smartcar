package com.brotherjing.utils;

/**
 * Created by Brotherjing on 2015/10/11.
 */
public interface Protocol {

    int TYPE_JSON = 1;
    int TYPE_IMAGE = 2;
    int TYPE_VIDEO = 3;

    int UDP_CLIENT_PORT = 12315;
    int UDP_SERVER_PORT = 15324;

    int MSG_TYPE_TEXT = 1;
    int MSG_TYPE_CMD = 2;

    //smart car controller
    int CMD_TYPE_TURNLEFT = 1;
    int CMD_TYPE_TURNRIGHT = 2;
    int CMD_TYPE_SPEEDUP = 3;
    int CMD_TYPE_SPEEDDOWN = 4;
    int CMD_TYPE_SETSPEED = 5;
    int CMD_TYPE_FORWARD = 6;
    int CMD_TYPE_BACKWARD = 7;

}
