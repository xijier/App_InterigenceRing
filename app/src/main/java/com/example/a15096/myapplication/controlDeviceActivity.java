package com.example.a15096.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.widget.AdapterView.OnItemClickListener;

import com.example.a15096.myapplication.ListItemAdapter.InnerItemOnclickListener;

public class controlDeviceActivity extends AppCompatActivity implements InnerItemOnclickListener,
        OnItemClickListener {
    private ListView list_one;
    private List<String> mDataList;
    private ListItemAdapter mAdapter;
    private static final String[] Datas = {"客厅灯", "主卧室灯", "厨房灯", "卫生间灯", "次卧灯", "餐厅灯"};
    private Context mContext = null;
    private final static String PREFRENCE_FILE_KEY = "com.example.a15096.shared_preferences";
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_device);
        mContext = controlDeviceActivity.this;
        initView();
        mDataList = new ArrayList<String>();
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

        for (int i = 0; i < Datas.length; i++) {
          // mDataList.add(Datas[i]);
        }
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
    }

    /**
     * Device Set page
     */
    private void deviceSetPage() {
        Intent intent = new Intent(this, deviceSetActivity.class);
        startActivity(intent);
    }

    private void initView() {
        list_one = (ListView) findViewById(R.id.lv);
    }

    @Override
    public void itemClick(View v) {
        int position;
        position = (Integer) v.getTag();

        switch (v.getId()) {
            case R.id.switchlight:
                Switch sw = (Switch) v.findViewById(R.id.switchlight);
                Log.e("内部item--1-->", position + "" + sw.isChecked() + Datas[position]);
                break;
            case R.id.buttonDelete:
                Log.e("内部item--2-->", position + " delete");
                showDeleteDialog(position);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Log.e("整体item----->", position + "");
    }

    private void showDeleteDialog(int position) {
        final  int pos = position;
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
                        Editor editor = mSharedPreferences.edit();
                        editor.remove(mAdapter.getItem(pos).toString());
                        editor.commit();// 提交修改
                        mAdapter.deleteItem(pos);
                    }
                })
                .create().show();
    }
}
