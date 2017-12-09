package com.example.a15096.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
                adddeviceSet();
            }
        });
    }

    private void adddeviceSet()
    {
        //mData_5 = new Data(R.mipmap.ic_launcher,"我的设备01" + flag);
        flag++;
    }
}
