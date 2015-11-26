package com.brotherjing.utils.bean;

import com.brotherjing.utils.Protocol;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Brotherjing on 2015/10/10.
 */
public class TextMessage extends Message{

    @SerializedName("text")
    String text;

    public TextMessage(String from, String timestamp, String text) {
        super(from,timestamp, Protocol.MSG_TYPE_TEXT);
        this.text = text;
    }

    public TextMessage(String text) {
        super("","", Protocol.MSG_TYPE_TEXT);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
