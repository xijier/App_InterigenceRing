package com.example.a15096.myapplication.deviceAsyncTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.example.a15096.myapplication.ListItemAdapter;

import java.net.Socket;

/**
 * Created by 15096 on 2018/1/29.
 */

public class ShareDeviceAsyncTask  extends AsyncTask<String, Void, Boolean> {

    private ProgressDialog mDialog;
    private Activity mActivity;

    public ShareDeviceAsyncTask(Activity activity)
    {
        mActivity = activity;
    }

    @Override
    protected void onPreExecute()
    {
        mDialog = new ProgressDialog(mActivity);
        mDialog.setMessage("正在获取设备信息...");
        mDialog.setCanceledOnTouchOutside(false);
        //  mDialog.setOnCancelListener(this);
        mDialog.show();
    }
    @Override
    protected Boolean doInBackground(String... params) {
        String str = params[0];
        try {
            // setServer(str);
            return  true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(final Boolean result)
    {
        mDialog.dismiss();
        //  String add1 = result.getInetAddress().toString();
        // Toast.makeText(mActivity, toastMsg, Toast.LENGTH_LONG).show();
    }
}
