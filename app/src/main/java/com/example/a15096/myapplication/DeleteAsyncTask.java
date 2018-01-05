package com.example.a15096.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * Created by 15096 on 2018/1/6.
 */

public class DeleteAsyncTask extends AsyncTask<String, Void, Boolean> {

    //这里是连接ESP8266的IP和端口号，IP是通过指令在单片机开发板查询到，而端口号可以自行设置，也可以使用默认的，333就是默认的
    private Socket client = null;
    private PrintStream out = null;
    private boolean isReceive = false;
    private ProgressDialog mDialog;
    private ListItemAdapter mAdapter;
    private Activity mActivity;
    private int mpos;
    DeleteAsyncTask(Activity activity,Socket client,ListItemAdapter Adapter,int pos,boolean isReceive)
    {
        this.client = client;
        this.isReceive= isReceive;
        mAdapter= Adapter;
        mActivity = activity;
        mpos = pos;
    }

    @Override
    protected void onPreExecute()
    {
        mDialog = new ProgressDialog(mActivity);
        mDialog.setMessage("正在删除，请稍后...");
        mDialog.setCanceledOnTouchOutside(false);
        //  mDialog.setOnCancelListener(this);
        mDialog.show();
    }

    @Override
    protected void onPostExecute(final Boolean result)
    {
        mDialog.dismiss();
        //  String add1 = result.getInetAddress().toString();

        if(result==false)
        {
            mAdapter.setStatusItem(mpos,"离线",false,false);
            mAdapter.notifyDataSetChanged();
        }
        // Toast.makeText(mActivity, toastMsg, Toast.LENGTH_LONG).show();
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String str = params[0];
        try {
            boolean isSuccess =setClient(str);
            return  isSuccess;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected  boolean setClient(String msg)
    {
        boolean isSuccess = false;
        // Socket socket=null;
        try {
            //socket=new Socket("192.168.0.109", 8266);
            //接受服务端消息并打印
            if(isReceive)
            {
                InputStream is=client.getInputStream();
                byte b[]=new byte[1024];
                is.read(b);
                String str = new String(b);
                is.close();
            }
            //给服务端发送响应信息
            OutputStream os=client.getOutputStream();
            os.write(msg.getBytes());
            //is.close();
            os.close();
            isSuccess = true;
        } catch (Exception e) {
            isSuccess = false;
            Log.e(e.getMessage(), "setClient: ", e.getCause());
            e.printStackTrace();
        } finally {
            //操作结束，关闭socket
            try {
                client.close();
                return  isSuccess;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return isSuccess;
    }
}
