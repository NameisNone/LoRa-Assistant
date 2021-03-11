package com.example.loraassistant.Adapter;

import android.app.AlarmManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.loraassistant.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AlarmAdapter extends BaseAdapter {

    private LinkedList<Alarm> mAlarmData;
    private Context mContext;
    private LayoutInflater mLayoutInflater;


    public AlarmAdapter(Context context, LinkedList<Alarm> alarmData) {
        this.mAlarmData = alarmData;
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);//从context中，获取布局填充器
    }

    @Override
    public int getCount() {
        return mAlarmData.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.listview_item, null);
            holder = new ViewHolder();
            holder.tvTime = convertView.findViewById(R.id.tv_time);
            holder.tvPeriod = convertView.findViewById(R.id.tv_period);
            holder.tvLedState = convertView.findViewById(R.id.tv_state);
            holder.img_AlarmSwitch = convertView.findViewById(R.id.img_switch);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvTime.setText(mAlarmData.get(position).getTimeString());
        if(mAlarmData.get(position).getPeriod()){
            holder.tvPeriod.setText("执行一次");
        }else if(!mAlarmData.get(position).getPeriod()){
            holder.tvPeriod.setText("每天执行");
        }
        if(mAlarmData.get(position).getLedState()){
            String str = String.format("开, 亮度: %d%%",mAlarmData.get(position).getLedLight());
            holder.tvLedState.setText(str);
        }else if(!mAlarmData.get(position).getLedState()){
            String str = String.format("关");
            holder.tvLedState.setText(str);
        }
//      //按钮监听动作
        holder.img_AlarmSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.img_AlarmSwitch.getTag().equals("key_off")) {
                    holder.img_AlarmSwitch.setTag("key_on");
                    holder.img_AlarmSwitch.setImageResource(R.mipmap.button_on);
                } else if (holder.img_AlarmSwitch.getTag().equals("key_on")) {
                    holder.img_AlarmSwitch.setTag("key_off");
                    holder.img_AlarmSwitch.setImageResource(R.mipmap.button_off);
                }
            }
        });
        return convertView;
    }

    static class ViewHolder {
        public TextView tvTime, tvPeriod, tvLedState;
        public ImageView img_AlarmSwitch;
    }
}
