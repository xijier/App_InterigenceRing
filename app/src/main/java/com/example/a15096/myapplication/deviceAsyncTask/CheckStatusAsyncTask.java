package com.example.a15096.myapplication.deviceAsyncTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.a15096.myapplication.ListItemAdapter;
import com.example.a15096.myapplication.com.example.a15096.myapplication.smartconfig.esptouch.IEsptouchResult;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by 15096 on 2018/1/5.
 */

public class CheckStatusAsyncTask extends AsyncTask<String, Void, Boolean> {
    private Activity mActivity;
    private String maddress;
    private ProgressDialog mDialog;
    private List<String> mipList;
    private ListItemAdapter mAdapter;
    public CheckStatusAsyncTask(Activity activity, ListItemAdapter Adapter, List<String> ipList,String address)
    {
        mActivity = activity;
        mAdapter = Adapter;
        mipList = ipList;
        maddress = address;
    }

    @Override
    protected void onPreExecute()
    {
        mDialog = new ProgressDialog(mActivity);
        mDialog.setMessage("正在获取设备信息...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String msg = params[0];
        try {
            for(int i = 0 ; i <mipList.size(); i++)
            {
               setClient(mipList.get(i),i,msg);
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
        mAdapter.notifyDataSetChanged();
       // Toast.makeText(mActivity, toastMsg, Toast.LENGTH_LONG).show();
    }

    protected void setClient(String ip,int index,String msg)
    {
        try {
            Socket socket=null;
            try {
                //TimeUnit.MILLISECONDS.sleep(1000);
                socket=new Socket(ip, 8266);
               // socket.setSoTimeout(1000);
                //给服务端发送响应信息
                OutputStream os=socket.getOutputStream();
                os.write(msg.getBytes());
                //接受服务端消息并打印
                InputStream is=socket.getInputStream();
                String str="";
                int i = 0;
                long v1 =System.currentTimeMillis();
                while(str.isEmpty()) {
                    byte b[] = new byte[is.available()];
                    is.read(b);
                    str = new String(b);
                    if(i>100)
                    {
                        break;
                    }
                    i++;
                    TimeUnit.MILLISECONDS.sleep(100);
                }
                long v2 =System.currentTimeMillis();
                long temp = v2 - v1;
                if(str.equals("ison"))
                {
                    mAdapter.setStatusItem(index,"在线",false,true);
                }
                else if(str.equals("isoff"))
                {
                    mAdapter.setStatusItem(index,"在线",true,true);
                }
                else
                {
                    mAdapter.setStatusItem(index,"离线",false,false);
                }
                is.close();
                os.close();
            } catch (Exception e) {
              //  mAdapter.setStatusItem(index,"离线",true,true);
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
