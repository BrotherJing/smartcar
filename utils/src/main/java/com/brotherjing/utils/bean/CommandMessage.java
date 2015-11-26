package com.brotherjing.utils.bean;

import com.brotherjing.utils.Protocol;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Brotherjing on 2015-11-26.
 */
public class CommandMessage extends Message{

    @SerializedName("text")
    Command command;

    public CommandMessage(String from, String timestamp,int cmdType,int cmdArg) {
        super(from, timestamp, Protocol.MSG_TYPE_CMD);
        command = new Command(cmdType,cmdArg);
    }

    public CommandMessage(int cmdType,int cmdArg) {
        super("", "", Protocol.MSG_TYPE_CMD);
        command = new Command(cmdType,cmdArg);
    }
}
