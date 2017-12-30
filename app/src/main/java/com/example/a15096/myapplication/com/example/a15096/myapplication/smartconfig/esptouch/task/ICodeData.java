package com.example.a15096.myapplication.com.example.a15096.myapplication.smartconfig.esptouch.task;

/**
 * Created by 15096 on 2017/12/30.
 */

public interface ICodeData {

    /**
     * Get the byte[] to be transformed.
     *
     *
     * @return the byte[] to be transfromed
     */
    byte[] getBytes();

    /**
     * Get the char[](u8[]) to be transfromed.
     *
     * @return the char[](u8) to be transformed
     */
    char[] getU8s();
}
