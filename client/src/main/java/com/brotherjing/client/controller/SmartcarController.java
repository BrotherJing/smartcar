package com.brotherjing.client.controller;

/**
 * Created by Brotherjing on 2015-11-26.
 */
public interface SmartcarController {

    void turnLeft(int degree);
    void turnRight(int degree);
    void speedUp(int amount);
    void speedDown(int amount);

}
