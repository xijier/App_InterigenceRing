package com.example.a15096.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Switch;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.example.a15096.myapplication.ListItemAdapter.InnerItemOnclickListener;
import com.example.a15096.myapplication.com.example.a15096.myapplication.smartconfig.SmartConfigActivity;
import com.example.a15096.myapplication.mqtt.CheckStatusAsyncMqttTask;

public class controlDeviceActivity extends AppCompatActivity implements InnerItemOnclickListener,
        OnItemClickListener {
    private ListView list_one;
    private List<String> mDataList;
    private ListItemAdapter mAdapter;
    //private static final String[] Datas = {"客厅灯", "主卧室灯", "厨房灯", "卫生间灯", "次卧灯", "餐厅灯"};
    private Context mContext = null;
    /***数据持久化**/
    private final static String PREFRENCE_FILE_KEY = "com.example.a15096.shared_preferences";
    private final static String PREFRENCE_Device_KEY = "com.example.a15096.shared_preferences_devicename";
    private SharedPreferences mSharedPreferencesDeviceName;
    private SharedPreferences mSharedPreferences;
    Socket socket = null;
    private SendAsyncTask mSendAsyncTask;
    private SendAsyncMqttTask mSendAsyncMqttTask;
    private CheckStatusAsyncTask mCheckStatusAsyncTask;
    private CheckStatusAsyncMqttTask mCheckStatusAsyncMqttTask;
    private static final int PORT = 8266;
    private static String status = "offline";
    private static boolean standalone = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_device);
        mContext = controlDeviceActivity.this;
        Intent intent = getIntent();
        //从Intent当中根据key取得value
        if (intent != null) {
            boolean  value = intent.getBooleanExtra("key", false);
            standalone = value;
        }
        mDataList = new ArrayList<String>();
        mSharedPreferences = getSharedPreferences(PREFRENCE_FILE_KEY, Context.MODE_PRIVATE);
        list_one = (ListView) findViewById(R.id.lv);
        getPreferencesData();
        mAdapter = new ListItemAdapter(mDataList, this);
        mAdapter.setOnInnerItemOnClickListener(this);
        list_one.setAdapter(mAdapter);
        list_one.setOnItemClickListener(this);
        Button addDeviceSet = (Button) findViewById(R.id.addDeviceSet);
        addDeviceSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceSetPage();
            }
        });
        checkStatus();
    }
    /**
     * Device Set page
     */
    private void deviceSetPage() {
        finish();
        Intent intent = new Intent(this, SmartConfigActivity.class);
        startActivity(intent);
        //Intent intent = new Intent(this, deviceSetActivity.class);
        //startActivity(intent);
    }

    private void getPreferencesData() {

        mSharedPreferencesDeviceName = getSharedPreferences(PREFRENCE_Device_KEY, Context.MODE_PRIVATE);
        if (!mSharedPreferencesDeviceName.getAll().isEmpty()) {
            Map<String, ?> map = mSharedPreferencesDeviceName.getAll();
            Iterator iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String) entry.getKey();
                mDataList.add(key);
            }
        }
    }

    @Override
    public void itemClick(View v) {
        int position;
        position = (Integer) v.getTag();
        switch (v.getId()) {
            case R.id.checkboxlight:
                String name =  mDataList.get(position);
                String add = mSharedPreferencesDeviceName.getString(name,"");
                String deviceId = getDeviceId(name);
                CheckBox checklight = (CheckBox) v.findViewById(R.id.checkboxlight);
                    try{
                        if(checklight.isChecked())
                        {
                            if(standalone)
                            {
                                getConnectSocket("ison",add,false);
                            }
                            else
                            {
                                getConnectMqtt("on",deviceId,false);
                            }
                            //
                        }
                        else
                        {
                            if(standalone)
                            {
                                getConnectSocket("isoff",add,false);
                            }
                            else
                            {
                                getConnectMqtt("off",deviceId,false);
                            }
                            //
                        }

                    }catch (Exception e)
                    {
                       // Toast.makeText(getApplicationContext(), "注冊失敗", Toast.LENGTH_SHORT).show();
                    }
                /////////////////////
                Log.e("内部item--->", position + "" + checklight.isChecked() + mDataList.get(position));
                break;
            case R.id.buttonDelete:
                Log.e("内部item--->", position + " delete");
                showDeleteDialog(position);
                break;
            case R.id.buttonSharing:
                Log.e("内部item--->", position + " Sharing");
                shareingDevice(position);
                break;
            default:
                break;
        }
    }

    private String udpmessage;
    private boolean sharingFlag = false;
    private String deviceInfo;
    private void shareingDevice(int position)
    {
        String id = getDeviceId(mDataList.get(position));
        String add = mSharedPreferencesDeviceName.getString(mDataList.get(position),"");

        deviceInfo = "id:"+id + "name:"+mDataList.get(position)+"address:"+add+"end:";
        DatagramPacket dataPacket = null;
        String handlemessage;
        try {
            MulticastSocket ms;
            getUdpSocket myThread  = new getUdpSocket();
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
                    break;
                }
                if(count>20)
                {
                    handlemessage = "网络状态不佳，请重试";
                    Toast.makeText(mContext, handlemessage, Toast.LENGTH_SHORT).show();
                    break;
                }
                count++;
            }
            ms.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class getUdpSocket extends Thread {

        public void run() {
            ServerSocket serverSocket=null;
            Socket socket=null;
            try {
                serverSocket=new ServerSocket(8268);
                //建立跟客户端的连接
                socket=serverSocket.accept();
                //向客户端发送消息
                OutputStream os=socket.getOutputStream();
                os.write(deviceInfo.getBytes());
                InputStream is=socket.getInputStream();
                //接受客户端的响应
                byte[] b=new byte[is.available()];
                is.read(b);
                String str = new String(b);
                System.out.println(str.trim()+" "+str.length());
                serverSocket.close();
                socket.close();
                if(str.equals(deviceInfo))
                {
                    sharingFlag = true;
                }

            } catch (Exception e) {
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

    private String getDeviceId(String name)
    {
        String deviceId = "";
        if (!mSharedPreferences.getAll().isEmpty()) {
            Map<String, ?> map = mSharedPreferences.getAll();
            Iterator iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String) entry.getKey();
                Set<String> set=new HashSet<String>(mSharedPreferences.getStringSet(key, new HashSet<String>()));
                for (String str : set) {
                    if(str.contains("deviceName:"))
                    {
                        String temp = str.substring("deviceName:".length(), str.length());
                        if(temp.equals(name))
                        {
                            deviceId = key;
                            // break;
                        }
                    }
                }
            }
        }
        return deviceId;
    }
    public Boolean getDeleteDeviceSocket(String msg,String add,int pos,boolean isReceive)
    {
        Boolean isSucces = false;
        try{
            TimeUnit.MILLISECONDS.sleep(500);
            SetSocketThread myThread  = new SetSocketThread(add);
            myThread.start();
            myThread.join();
            TimeUnit.MILLISECONDS.sleep(500);
            if (socket.isConnected()) {
                if (!socket.isOutputShutdown()) {
                    DeleteAsyncTask mDeleteAsyncTask = new DeleteAsyncTask(this,socket, mAdapter,pos,isReceive);
                    isSucces =  mDeleteAsyncTask.execute(msg).get();
                }
            }
        }
        catch (Exception e)
        {

        }
        return isSucces;
    }

    public Boolean getConnectMqtt(String msg,String deviceId,boolean isReceive)
    {
        Boolean isSucces = false;
        try{
            TimeUnit.MILLISECONDS.sleep(500);
             mSendAsyncMqttTask = new SendAsyncMqttTask(isReceive);
             isSucces =  mSendAsyncMqttTask.execute(msg,deviceId).get();
        }
        catch (Exception e)
        {

        }
        return isSucces;
    }

    public Boolean getConnectSocket(String msg,String add,boolean isReceive)
    {
        Boolean isSucces = false;
        try{
            TimeUnit.MILLISECONDS.sleep(500);
            SetSocketThread myThread  = new SetSocketThread(add);
            myThread.start();
            myThread.join();
            TimeUnit.MILLISECONDS.sleep(500);
            if (socket.isConnected()) {
                if (!socket.isOutputShutdown()) {
                    mSendAsyncTask = new SendAsyncTask(socket,isReceive);
                    isSucces =  mSendAsyncTask.execute(msg).get();
                }
            }
        }
        catch (Exception e)
        {

        }
        return isSucces;
    }

    public class SetSocketThread extends Thread {
        String ip = null;
        SetSocketThread(String ip)
        {
            this.ip = ip;
        }
        public void run() {
            try {
                socket  =new Socket(this.ip, PORT);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Log.e("整体item----->", position + "");
    }

    private void showDeleteDialog(int position) {
        final int pos = position;
        new AlertDialog.Builder(this)
                .setTitle("确定删除")
                .setMessage(mDataList.get(pos))
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 清除sharedpreferences的数据
                        String add = mSharedPreferencesDeviceName.getString(mDataList.get(pos),"");
                        //String add = mSharedPreferences.getString(mDataList.get(pos),"");
                        Boolean isSuccess= getDeleteDeviceSocket("reset",add,pos,false);
                        if(isSuccess)
                        {
                            String id = getDeviceId(mDataList.get(pos));
                            Editor editorid = mSharedPreferences.edit();
                            editorid.remove(id);
                            editorid.commit();// 提交修改

                            Editor editor = mSharedPreferencesDeviceName.edit();
                            editor.remove(mAdapter.getItem(pos).toString());
                            editor.commit();// 提交修改
                            mAdapter.deleteItem(pos);
                        }
                        else
                        {
                            Toast.makeText(mContext,"设备已离线，请关闭重试", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .create().show();
    }

    private void checkStatus()
    {
        try {
            if(standalone)
            {
            List<String> ipList = new ArrayList<String>();
                for(int i=0 ; i< mDataList.size() ; i++)
                {
                    String add = mSharedPreferencesDeviceName.getString(mDataList.get(i),"");
                    ipList.add(add);
                }
            mCheckStatusAsyncTask = new CheckStatusAsyncTask(this,mAdapter,ipList);
            mCheckStatusAsyncTask.execute("checkStatus");
            }
            else
            {
                HashMap<String,String> deviceIdMap = new HashMap<String,String>();
                for(int i=0 ; i< mDataList.size() ; i++)
                {
                    String id = getDeviceId(mDataList.get(i));
                    deviceIdMap.put(mDataList.get(i),id);
                }
                mCheckStatusAsyncMqttTask = new CheckStatusAsyncMqttTask(this,mAdapter,deviceIdMap,mDataList);
                mCheckStatusAsyncMqttTask.execute("");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
