package com.example.a15096.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class deviceSetActivity extends AppCompatActivity {
    // private ListView list_one;
    //private MyAdapter mAdapter = null;
    private List<Data> mData = null;
    private Context mContext = null;
    private Button btn_add;
    private Button btn_remove;
    private int flag = 1;
    private Data mData_5 = null;   //用来临时放对象的
    private final static String PREFRENCE_FILE_KEY = "com.example.a15096.shared_preferences";
    private SharedPreferences mSharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_set);
        mContext = deviceSetActivity.this;
        mData = new LinkedList<Data>();
        btn_add = (Button) findViewById(R.id.btn_add);

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btn_add.getText().equals("添加设备"))
                {
                    adddeviceSet();
                }
                else
                {
                    deviceControlPage();
                }
            }
        });
    }
    /**
     * Device Set page
     */
    private void deviceControlPage() {
        Intent intent = new Intent(this, controlDeviceActivity.class);
        startActivity(intent);
    }

    private void adddeviceSet()
    {
        TextView deviceSetname = (TextView) findViewById(R.id.deviceSetname);
        TextView wifipassword = (TextView) findViewById(R.id.wifipassword);
        TextView deviceaddStatus = (TextView) findViewById(R.id.deviceaddStatus);
        mSharedPreferences = getSharedPreferences(PREFRENCE_FILE_KEY, Context.MODE_PRIVATE);
        String deviceName = deviceSetname.getText().toString();
        if(!mSharedPreferences.getAll().containsKey(deviceName)&&!deviceName.isEmpty())
        {
            Editor editor = mSharedPreferences.edit();
            editor.putString(deviceSetname.getText().toString(), wifipassword.getText().toString());
            editor.commit();
            deviceaddStatus.setVisibility(View.VISIBLE);
            deviceaddStatus.setText("设备 "+deviceSetname.getText().toString() +" 添加成功");
            deviceaddStatus.setKeyListener(null);
            btn_add.setText("返回");
        }
        else if(deviceName.isEmpty())
        {
            deviceaddStatus.setVisibility(View.VISIBLE);
            deviceaddStatus.setText("设备名称不能为空");
            deviceaddStatus.setKeyListener(null);
        }
        else
        {
            deviceaddStatus.setVisibility(View.VISIBLE);
            deviceaddStatus.setText("设备 "+deviceSetname.getText().toString() +" 已经存在");
            deviceaddStatus.setKeyListener(null);
        }
    }
}
