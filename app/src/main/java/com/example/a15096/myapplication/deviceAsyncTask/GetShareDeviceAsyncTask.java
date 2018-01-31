package com.example.a15096.myapplication.deviceAsyncTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.example.a15096.myapplication.ListItemAdapter;
import com.example.a15096.myapplication.com.example.a15096.myapplication.smartconfig.SmartConfigActivity;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by 15096 on 2018/1/30.
 */

public class GetShareDeviceAsyncTask extends AsyncTask<String, Void, Boolean> {

    private Activity mActivity;
    private ProgressDialog mDialog;
    private MulticastSocket ds;
    private String multicastHost = "224.0.0.1";
    private InetAddress receiveAddress;
    private String reciveData = "";
    private InetAddress iaddress;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences mSharedPreferencesDeviceName;

    public GetShareDeviceAsyncTask(Activity activity,SharedPreferences SharedPreferences,SharedPreferences SharedPreferencesDeviceName)
    {
        mActivity = activity;
        mSharedPreferences = SharedPreferences;
        mSharedPreferencesDeviceName = SharedPreferencesDeviceName;
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
        String msg = params[0];
        try {
            String str = msg;
            str += "1";
         //   getSharing(msg);
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
        int toastMsg;

        // Toast.makeText(mActivity, toastMsg, Toast.LENGTH_LONG).show();
    }

    protected void getSharing(String msg)
    {
        try {
            ds = new MulticastSocket(8267);
            multicastHost = "224.0.0.1";
            receiveAddress = InetAddress.getByName(multicastHost);
            ds.joinGroup(receiveAddress);
            getSharingSocket myThread = new getSharingSocket();
            myThread.start();
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public class ShareUdpSocket extends Thread {
        public void run() {
            Socket socket = null;
            try {
                socket = new Socket(iaddress, 8268);
                //接受服务端消息并打印
                //  InputStream is=socket.getInputStream();
                // byte b[]=new byte[1024];
                //  is.read(b);
                //给服务端发送响应信息
                OutputStream os = socket.getOutputStream();
                os.write(reciveData.getBytes());
                os.close();
                socket.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    public String splitData(String str, String strStart, String strEnd) {
        String tempStr;
        tempStr = str.substring(str.indexOf(strStart) + strStart.length(), str.lastIndexOf(strEnd));
        return tempStr;
    }

    private void saveShareData(String deviceName, String address, String deviceId) {
        if (!mSharedPreferencesDeviceName.getAll().containsKey(deviceName) && !deviceName.isEmpty()) {
            SharedPreferences.Editor editor = mSharedPreferencesDeviceName.edit();
            editor.putString(deviceName, address);
            editor.commit();
        } else if (deviceName.isEmpty()) {
            int i = 1;
            while (mSharedPreferencesDeviceName.getAll().containsKey(deviceName + String.valueOf(i))) {
                i++;
            }
            deviceName = deviceName + String.valueOf(i);
            SharedPreferences.Editor editor = mSharedPreferencesDeviceName.edit();
            editor.putString(deviceName, address);
            editor.commit();
        } else {
            int i = 1;
            while (mSharedPreferencesDeviceName.getAll().containsKey("智能环设备" + String.valueOf(i))) {
                i++;
            }
            deviceName = "智能环设备" + String.valueOf(i);
            SharedPreferences.Editor editor = mSharedPreferencesDeviceName.edit();
            editor.putString(deviceName, address);
            editor.commit();
        }
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        Set<String> setValue = new HashSet<String>();
        setValue.add("deviceName:" + deviceName);
        setValue.add("address:" + address);
        editor.putStringSet(deviceId, setValue);
        editor.commit();
    }

    public class getSharingSocket extends Thread {
        String ip = "";
        public  getSharingSocket()
        {

        }
        public void run() {
            try {
                byte buf[] = new byte[1024];
                DatagramPacket dp = new DatagramPacket(buf, 1024);
                while (ip.isEmpty()) {
                    try {
                        ds.receive(dp);
                        reciveData = new String(buf, 0, dp.getLength());
                        ip = dp.getAddress().toString();
                        iaddress = dp.getAddress();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //Toast.makeText(SmartConfigActivity.this, reciveData, Toast.LENGTH_LONG).show();
                String deviceName = splitData(reciveData, "name:", "address:");
                String address = splitData(reciveData, "address:", "end:");
                String deviceId = splitData(reciveData, "id:", "name:");
                saveShareData(deviceName, address, deviceId);
                ShareUdpSocket mThread = new ShareUdpSocket();
                mThread.start();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
        }
    }
}