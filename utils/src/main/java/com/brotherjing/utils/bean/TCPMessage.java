package com.brotherjing.utils.bean;

/**
 * Created by Brotherjing on 2015/10/10.
 */
public class TCPMessage {

    String from;
    String timestamp;
    String text;

    public TCPMessage(String from, String timestamp, String text) {
        this.from = from;
        this.timestamp = timestamp;
        this.text = text;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
