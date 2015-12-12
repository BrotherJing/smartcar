package com.brotherjing.server.controller;

import com.brotherjing.server.service.BluetoothService;
import com.brotherjing.utils.Protocol;
import com.brotherjing.utils.bean.Command;

/**
 * Created by Brotherjing on 2015-12-12.
 */
public class BluetoothCarController {

    private BluetoothService.MyBinder binder;

    public BluetoothCarController(BluetoothService.MyBinder binder) {
        this.binder = binder;
    }

    public void turnLeft(){
        binder.send("4");
    }

    public void turnRight(){
        binder.send("3");
    }

    public void forward(){
        binder.send("1");
    }

    public void backward(){
        binder.send("2");
    }

    public void setSpeed(int speed){
        if(speed<1||speed>4)return;
        binder.send(speed+"");
    }

    public void stop(){
        binder.send("0");
    }

    public void processCommand(Command command){
        switch (command.getCmdType()){
            case Protocol.CMD_TYPE_TURNLEFT:{turnLeft();break;}
            case Protocol.CMD_TYPE_TURNRIGHT:{turnRight();break;}
            case Protocol.CMD_TYPE_FORWARD:{forward();break;}
            case Protocol.CMD_TYPE_BACKWARD:{backward();break;}
            case Protocol.CMD_TYPE_STOP:{stop();break;}
            case Protocol.CMD_TYPE_SETSPEED:
                setSpeed(command.getCmtArg());
        }
    }

}
