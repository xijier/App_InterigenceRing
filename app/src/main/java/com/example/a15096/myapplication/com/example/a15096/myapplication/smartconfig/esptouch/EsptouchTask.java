package com.example.a15096.myapplication.com.example.a15096.myapplication.smartconfig.esptouch;

import android.content.Context;

import com.example.a15096.myapplication.com.example.a15096.myapplication.smartconfig.esptouch.task.EsptouchTaskParameter;
import com.example.a15096.myapplication.com.example.a15096.myapplication.smartconfig.esptouch.task.IEsptouchTaskParameter;
import com.example.a15096.myapplication.com.example.a15096.myapplication.smartconfig.esptouch.task.__EsptouchTask;

/**
 * Created by 15096 on 2017/12/30.
 */

public class EsptouchTask implements IEsptouchTask {
    public __EsptouchTask _mEsptouchTask;

    private IEsptouchTaskParameter _mParameter;

    /**
     * Constructor of EsptouchTask
     *
     * @param apSsid the Ap's ssid
     * @param apBssid the Ap's bssid
     * @param apPassword the Ap's password
     * @param isSsidHidden whether the Ap's ssid is hidden
     * @param context the Context of the Application
     */
    public EsptouchTask(String apSsid, String apBssid, String apPassword, boolean isSsidHidden, Context context)
    {
        _mParameter = new EsptouchTaskParameter();
        _mEsptouchTask = new __EsptouchTask(apSsid, apBssid, apPassword, context, _mParameter, isSsidHidden);
    }

    /**
     * Constructor of EsptouchTask
     *
     * @param apSsid the Ap's ssid
     * @param apBssid the Ap's bssid
     * @param apPassword the Ap's password
     * @param isSsidHidden whether the Ap's ssid is hidden
     * @param timeoutMillisecond(it should be >= 10000+8000) millisecond of total timeout
     * @param context the Context of the Application
     */
    public EsptouchTask(String apSsid, String apBssid, String apPassword, boolean isSsidHidden, int timeoutMillisecond,
                        Context context)
    {
        _mParameter = new EsptouchTaskParameter();
        _mParameter.setWaitUdpTotalMillisecond(timeoutMillisecond);
        _mEsptouchTask = new __EsptouchTask(apSsid, apBssid, apPassword, context, _mParameter, isSsidHidden);
    }

    @Override
    public void interrupt()
    {
        _mEsptouchTask.interrupt();
    }

    @Override
    public IEsptouchResult executeForResult()
            throws RuntimeException
    {
        return _mEsptouchTask.executeForResult();
    }

    @Override
    public boolean isCancelled()
    {
        return _mEsptouchTask.isCancelled();
    }
}
