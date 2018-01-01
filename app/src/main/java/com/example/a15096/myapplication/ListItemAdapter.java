package com.example.a15096.myapplication;

/**
 * Created by 15096 on 2017/12/9.
 */

import java.util.List;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

public class ListItemAdapter extends BaseAdapter implements OnClickListener {
    private List<String> mList;
    private Context mContext;
    private InnerItemOnclickListener mListener;
    private String statusValue;
    private int mupdatePostion = -1;

    public ListItemAdapter(List<String> mList, Context mContext) {
        this.mList = mList;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        // TODO 自动生成的方法存根
        return mList.size();
    }

    public void setGreenItem(int position,String value) {
        mupdatePostion = position;
        statusValue = value;
        // 注意为了提高UI效率这个直接调用notifyDataSetChange（）；
    }

    @Override
    public Object getItem(int position) {
        // TODO 自动生成的方法存根
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO 自动生成的方法存根
        return position;
    }

    public void deleteItem(int position) {
        this.mList.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list,
                    null);
            //viewHolder.switchlight = (Switch) convertView.findViewById(R.id.switchlight);
            viewHolder.checkboxlight = (CheckBox) convertView.findViewById(R.id.checkboxlight);
            viewHolder.deletedevice = (Button) convertView.findViewById(R.id.buttonDelete);
            viewHolder.deviceDecription = (TextView) convertView.findViewById(R.id.deviceDecription);
            viewHolder.status = (TextView) convertView.findViewById(R.id.status);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
       // viewHolder.switchlight.setOnClickListener(this);
        viewHolder.checkboxlight.setOnClickListener(this);
        viewHolder.deletedevice.setOnClickListener(this);
        viewHolder.deviceDecription.setOnClickListener(this);
        viewHolder.status.setOnClickListener(this);
        //viewHolder.switchlight.setTag(position);
        viewHolder.checkboxlight.setTag(position);
        viewHolder.deletedevice.setTag(position);
        viewHolder.status.setTag(position);

        viewHolder.deviceDecription.setText(mList.get(position));
        if (position == mupdatePostion) {
            viewHolder.status.setText(statusValue);
        } else {
            //viewHolder.status.setBackgroundColor(Color.WHITE);
        }
        return convertView;
    }

    public void updataView(int posi, ListView listView,String value) {
        View view = listView.getChildAt(posi);
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.status.setText(value);
    }

    public final static class ViewHolder {
        Button deletedevice;
       // Switch switchlight;
        CheckBox checkboxlight;
        TextView deviceDecription;
        TextView status;
    }

    interface InnerItemOnclickListener {
        void itemClick(View v);
    }

    public void setOnInnerItemOnClickListener(InnerItemOnclickListener listener){
        this.mListener=listener;
    }

    @Override
    public void onClick(View v) {
        mListener.itemClick(v);
    }
}
