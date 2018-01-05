package com.example.a15096.myapplication;

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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.example.a15096.myapplication.ListItemAdapter.InnerItemOnclickListener;
import com.example.a15096.myapplication.com.example.a15096.myapplication.smartconfig.SmartConfigActivity;

public class controlDeviceActivity extends AppCompatActivity implements InnerItemOnclickListener,
        OnItemClickListener {
    private ListView list_one;
    private List<String> mDataList;
    private ListItemAdapter mAdapter;
    //private static final String[] Datas = {"客厅灯", "主卧室灯", "厨房灯", "卫生间灯", "次卧灯", "餐厅灯"};
    private Context mContext = null;
    /***数据持久化**/
    private final static String PREFRENCE_FILE_KEY = "com.example.a15096.shared_preferences";
    private SharedPreferences mSharedPreferences;
    Socket socket = null;
    private SendAsyncTask mSendAsyncTask;
    private static final String IP = "192.168.0.109";
    private static final int PORT = 8266;
    private static String status = "offline";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_device);
        mContext = controlDeviceActivity.this;
        mDataList = new ArrayList<String>();
        initView();
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
       Button buttonTest = (Button) findViewById(R.id.buttonTest);
       buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateStatus(0);
            }
        });
        checkStatus();
    }
    private void updateStatus(int targetIndex) {
      // String add = mSharedPreferences.getString(mDataList.get(0),"");
      // getConnectSocket("check",add,true);

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

    private void initView() {
        list_one = (ListView) findViewById(R.id.lv);
    }

    private void getPreferencesData() {
        mSharedPreferences = getSharedPreferences(PREFRENCE_FILE_KEY, Context.MODE_PRIVATE);
        if (!mSharedPreferences.getAll().isEmpty()) {
            Map<String, ?> map = mSharedPreferences.getAll();
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

                String add = mSharedPreferences.getString(mDataList.get(position),"");
              //  Switch sw = (Switch) v.findViewById(R.id.switchlight);
                CheckBox checklight = (CheckBox) v.findViewById(R.id.checkboxlight);
                    try{
                        if(checklight.isChecked())
                        {
                            getConnectSocket("on",add,false);
                        }
                        else
                        {
                            getConnectSocket("off",add,false);
                        }

                    }catch (Exception e)
                    {
                       // Toast.makeText(getApplicationContext(), "注冊失敗", Toast.LENGTH_SHORT).show();
                    }
                /////////////////////
                Log.e("内部item--1-->", position + "" + checklight.isChecked() + mDataList.get(position));
                break;
            case R.id.buttonDelete:
                Log.e("内部item--2-->", position + " delete");
                showDeleteDialog(position);
                break;
            default:
                break;
        }
    }

    public void getConnectSocket(String msg,String add,boolean isReceive)
    {
        try{
            TimeUnit.MILLISECONDS.sleep(500);
            SetSocketThread myThread  = new SetSocketThread(add);
            myThread.start();
            myThread.join();
            TimeUnit.MILLISECONDS.sleep(500);
            if (socket.isConnected()) {
                if (!socket.isOutputShutdown()) {
                    mSendAsyncTask = new SendAsyncTask(socket,isReceive);
                    mSendAsyncTask.execute(msg);
                }
            }
        }
        catch (Exception e)
        {

        }
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
                        mSharedPreferences = getSharedPreferences(PREFRENCE_FILE_KEY, Context.MODE_PRIVATE);
                        String add = mSharedPreferences.getString(mDataList.get(pos),"");
                        getConnectSocket("reset",add,false);
                        Editor editor = mSharedPreferences.edit();
                        editor.remove(mAdapter.getItem(pos).toString());
                        editor.commit();// 提交修改
                        mAdapter.deleteItem(pos);
                    }
                })
                .create().show();
    }


    public class StatusThread extends Thread {
        String ip = null;
        int index;
        StatusThread (String ip,int index)
        {
            this.index=  index;
            this.ip = ip;
        }
        public void run() {
            try {
                Socket socket=null;
                try {
                    socket=new Socket(this.ip, 8266);
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
                        status = "开";
                    }
                    else if(str.equals("isof"))
                    {
                        mAdapter.setStatusItem(index,"在线",false,true);
                        status = "关";
                    }
                    else
                    {

                    }
                    is.close();
                    //os.close();
                } catch (Exception e) {
                    mAdapter.setStatusItem(index,"离线",false,false);
                    status = "不可用";
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

    private void checkStatus()
    {
        try {
            List<String> ipList = new ArrayList<String>();
            mSharedPreferences = getSharedPreferences(PREFRENCE_FILE_KEY, Context.MODE_PRIVATE);
            if (!mSharedPreferences.getAll().isEmpty()) {
                Map<String, ?> map = mSharedPreferences.getAll();
                Iterator iter = map.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String key = (String) entry.getKey();
                    String value =(String) entry.getValue();
                    ipList.add(value);
                }
            }

            for(int i = 0 ; i <ipList.size(); i++)
            {
                StatusThread timer = new StatusThread(ipList.get(i),i);
                timer.start();
                timer.join();
                if(status.equals("online"))
                {
                   // mAdapter.updataView(i, list_one,"online");
                    //mAdapter.setGreenItem(i,"在线");
                }
                else
                {
                    //mAdapter.setGreenItem(i,"离线");
                //    mAdapter.updataView(i, list_one,"offline");
                }
                mAdapter.notifyDataSetChanged();
            }
        }
        catch (Exception e)
        {

        }
    }

}
