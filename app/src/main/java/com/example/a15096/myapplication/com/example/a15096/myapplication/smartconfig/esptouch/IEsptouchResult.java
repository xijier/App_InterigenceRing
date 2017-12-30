package com.example.a15096.myapplication.com.example.a15096.myapplication.smartconfig.esptouch;

import java.net.InetAddress;

/**
 * Created by 15096 on 2017/12/30.
 */

public interface IEsptouchResult {
    /**
     * check whether the esptouch task is executed suc
     *
     * @return whether the esptouch task is executed suc
     */
    boolean isSuc();

    /**
     * get the device's bssid
     *
     * @return the device's bssid
     */
    String getBssid();

    /**
     * check whether the esptouch task is cancelled by user
     *
     * @return whether the esptouch task is cancelled by user
     */
    boolean isCancelled();

    /**
     * get the ip address of the device
     *
     * @return the ip device of the device
     */
    InetAddress getInetAddress();
}
