package com.brotherjing.utils.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Brotherjing on 2015-11-26.
 */
public class Message implements Serializable{

    @SerializedName("from")
    String from;

    @SerializedName("timestamp")
    String timestamp;

    @SerializedName("msgType")
    int msgType;

    public Message(String from, String timestamp,int msgType) {
        this.from = from;
        this.timestamp = timestamp;
        this.msgType = msgType;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }
}
