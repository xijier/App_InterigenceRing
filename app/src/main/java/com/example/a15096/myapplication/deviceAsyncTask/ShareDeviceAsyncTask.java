package com.example.a15096.myapplication.deviceAsyncTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.a15096.myapplication.ListItemAdapter;
import com.example.a15096.myapplication.controlDeviceActivity;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * Created by 15096 on 2018/1/29.
 */

public class ShareDeviceAsyncTask  extends AsyncTask<String, Void, Boolean> {

    private boolean sharingFlag = false;
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
        mDialog.setMessage("正在分享设备信息，请保持对方手机已接受分享....");
        mDialog.setCanceledOnTouchOutside(false);
        //  mDialog.setOnCancelListener(this);
        mDialog.show();
    }
    @Override
    protected Boolean doInBackground(String... params) {
        String str = params[0];
        try {
            return  sharingDevice(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private  boolean sharingDevice(String deviceInfo)
    {
        DatagramPacket dataPacket = null;
        String handlemessage;
        boolean isSuccess = false;
        try {
            MulticastSocket ms;
            getUdpSocket myThread  = new getUdpSocket(deviceInfo);
            myThread.start();
            int count = 0;
            while(true)
            {
                ms = new MulticastSocket();
                ms.setTimeToLive(5);
                //将本机的IP（这里可以写动态获取的IP）地址放到数据包里，其实server端接收到数据包后也能获取到发包方的IP的
                byte[] data = deviceInfo.getBytes();
                //224.0.0.1为广播地址
                InetAddress address = InetAddress.getByName("224.0.0.1");
                //这个地方可以输出判断该地址是不是广播类型的地址
                System.out.println(address.isMulticastAddress());
                dataPacket = new DatagramPacket(data, data.length, address,8267);
                ms.send(dataPacket);
                TimeUnit.MILLISECONDS.sleep(500);
                if(sharingFlag ==true)
                {
                    isSuccess = true;
                    break;
                }
                if(count>20)
                {
                    isSuccess = false;
                    break;
                }
                count++;
            }
            myThread.join();
            ms.close();
        } catch (Exception e) {
            isSuccess = false;
            e.printStackTrace();
        }
        return  isSuccess;
    }

    @Override
    protected void onPostExecute(final Boolean result)
    {
        mDialog.dismiss();
        // String add1 = result.getInetAddress().toString();
        // Toast.makeText(mActivity, toastMsg, Toast.LENGTH_LONG).show();
        if(result)
        {
            Toast.makeText(mActivity, "分享成功", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(mActivity, "网络状态不佳，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    public class getUdpSocket extends Thread {
        private  String mdeviceInfo;
        public getUdpSocket(String deviceInfo )
        {
            mdeviceInfo= deviceInfo;
        }
        public void run() {
            ServerSocket serverSocket=null;
            Socket socket=null;
            try {
                serverSocket=new ServerSocket(8268);
                serverSocket.setSoTimeout(20000);
                //建立跟客户端的连接
                socket=serverSocket.accept();
                socket.setSoTimeout(20000);
                //向客户端发送消息
                OutputStream os=socket.getOutputStream();
                os.write(mdeviceInfo.getBytes());
                InputStream is=socket.getInputStream();
                //接受客户端的响应
                byte[] b=new byte[is.available()];
                is.read(b);
                String str = new String(b);
                System.out.println(str.trim()+" "+str.length());
                serverSocket.close();
                socket.close();
                if(str.equals(mdeviceInfo))
                {
                    sharingFlag = true;
                }
            } catch (Exception e) {
                sharingFlag = false;
                e.printStackTrace();
            } finally {
                //操作结束，关闭socket
                try {
                    serverSocket.close();
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
