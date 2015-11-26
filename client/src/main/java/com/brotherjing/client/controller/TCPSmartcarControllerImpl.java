package com.brotherjing.client.controller;

import com.brotherjing.client.service.TCPClient;
import com.brotherjing.utils.Protocol;
import com.brotherjing.utils.bean.CommandMessage;

/**
 * Created by Brotherjing on 2015-11-26.
 */
public class TCPSmartcarControllerImpl implements SmartcarController {

    private TCPClient.MyBinder binder;

    public TCPSmartcarControllerImpl(TCPClient.MyBinder binder) {
        this.binder = binder;
    }

    @Override
    public void turnLeft(int degree) {
        binder.send(new CommandMessage(Protocol.CMD_TYPE_TURNLEFT,degree));
    }

    @Override
    public void turnRight(int degree) {
        binder.send(new CommandMessage(Protocol.CMD_TYPE_TURNRIGHT,degree));
    }

    @Override
    public void speedUp(int amount) {
        binder.send(new CommandMessage(Protocol.CMD_TYPE_SPEEDUP,amount));
    }

    @Override
    public void speedDown(int amount) {
        binder.send(new CommandMessage(Protocol.CMD_TYPE_SPEEDDOWN,amount));
    }
}
