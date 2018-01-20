package com.example.a15096.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.a15096.myapplication.com.example.a15096.myapplication.smartconfig.esptouch.IEsptouchResult;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * Created by 15096 on 2018/1/5.
 */

public class CheckStatusAsyncTask extends AsyncTask<String, Void, Boolean> {

    private Socket client = null;

    private Activity mActivity;

    private ProgressDialog mDialog;
    private List<String> mipList;
    private ListItemAdapter mAdapter;
    CheckStatusAsyncTask(Activity activity, ListItemAdapter Adapter, List<String> ipList)
    {
        this.client = client;
        mActivity = activity;
        mAdapter = Adapter;
        mipList = ipList;
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
            //setClient(str);
            for(int i = 0 ; i <mipList.size(); i++)
            {
                setClient(mipList.get(i),i);
            }
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
        mAdapter.notifyDataSetChanged();
        int toastMsg;

       // Toast.makeText(mActivity, toastMsg, Toast.LENGTH_LONG).show();
    }

    protected void setClient(String ip,int index)
    {
        try {
            Socket socket=null;
            try {
                socket=new Socket(ip, 8266);
                socket.setSoTimeout(1000);
                //接受服务端消息并打印
                InputStream is=socket.getInputStream();
                byte b[]=new byte[1024];
                is.read(b);
                String str =new String(b);
                str = str.substring(0, 4);
                if(str.equals("ison"))
                {
                    mAdapter.setStatusItem(index,"在线",true,true);
                }
                else if(str.equals("isof"))
                {
                    mAdapter.setStatusItem(index,"在线",false,true);
                }
                else
                {

                }
                is.close();
                //os.close();
            } catch (Exception e) {
                mAdapter.setStatusItem(index,"离线",true,true);
              //  mAdapter.setStatusItem(index,"离线",false,false);
                Log.e(e.getMessage(), "setClient: ", e.getCause());
                e.printStackTrace();
            } finally {
                //操作结束，关闭socket
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }
}
