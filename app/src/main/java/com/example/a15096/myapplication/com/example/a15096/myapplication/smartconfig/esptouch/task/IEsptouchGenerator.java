package com.example.a15096.myapplication.com.example.a15096.myapplication.smartconfig.esptouch.task;

/**
 * Created by 15096 on 2017/12/30.
 */

public interface IEsptouchGenerator {
    /**
     * Get guide code by the format of byte[][]
     *
     * @return guide code by the format of byte[][]
     */
    byte[][] getGCBytes2();

    /**
     * Get data code by the format of byte[][]
     *
     * @return data code by the format of byte[][]
     */
    byte[][] getDCBytes2();
}
