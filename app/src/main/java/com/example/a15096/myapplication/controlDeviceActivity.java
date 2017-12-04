package com.example.a15096.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.Iterator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class controlDeviceActivity extends AppCompatActivity {
    private ListView list_one;
    private MyAdapter mAdapter = null;
    private List<Data> mData = null;
    private Context mContext = null;
    private Button btn_add;
    private Button btn_remove;
    private int flag = 1;
    private Data mData_5 = null;
    private final static String PREFRENCE_FILE_KEY = "com.example.a15096.shared_preferences";
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_device);
        mContext = controlDeviceActivity.this;
        mSharedPreferences=getSharedPreferences(PREFRENCE_FILE_KEY, Context.MODE_PRIVATE);
        bindViews();
        mData = new LinkedList<Data>();
        mAdapter = new MyAdapter((LinkedList<Data>) mData,mContext);
        list_one.setAdapter(mAdapter);
        Button button_DeviceSetButton = (Button) findViewById(R.id.buttonDeviceSet);
        button_DeviceSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceSetPage();
            }
        });

        Button buttonDeviceControltButton = (Button) findViewById(R.id.buttonDeviceControl);
        buttonDeviceControltButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceControlPage();
            }
        });

        if(!mSharedPreferences.getAll().isEmpty())
        {
            Map<String, ?> map = mSharedPreferences.getAll();
            Iterator iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
               String key = (String) entry.getKey();
                //String val = (String) entry.getValue();
                mData_5 = new Data(R.mipmap.ic_launcher, key + flag);
                mAdapter.add(mData_5);
                flag++;
            }
        }
    }

    /**
     * Device Set page
     */
    private void deviceSetPage()
    {
        Intent intent=new Intent(this,deviceSetActivity.class);
        startActivity(intent);
    }

    /**
     * Device Control page
     */
    private void deviceControlPage()
    {
        Intent intent=new Intent(this,controlDeviceActivity.class);
        startActivity(intent);
    }

    private void bindViews(){
        list_one = (ListView) findViewById(R.id.list_one);
    }
}
