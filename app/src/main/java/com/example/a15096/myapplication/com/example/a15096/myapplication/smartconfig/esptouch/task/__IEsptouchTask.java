package com.example.a15096.myapplication.com.example.a15096.myapplication.smartconfig.esptouch.task;

import com.example.a15096.myapplication.com.example.a15096.myapplication.smartconfig.esptouch.IEsptouchResult;

/**
 * Created by 15096 on 2017/12/30.
 */

public interface __IEsptouchTask {

    /**
     * Interrupt the Esptouch Task when User tap back or close the Application.
     */
    void interrupt();

    /**
     * Note: !!!Don't call the task at UI Main Thread or RuntimeException will be thrown Execute the Esptouch Task and
     * return the result
     *
     * @return the IEsptouchResult
     * @throws RuntimeException
     */
    IEsptouchResult executeForResult()
            throws RuntimeException;

    /**
     * Turn on or off the log.
     */
    static final boolean DEBUG = true;

    boolean isCancelled();
}
