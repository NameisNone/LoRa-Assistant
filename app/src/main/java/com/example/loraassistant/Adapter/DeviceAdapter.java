package com.example.loraassistant.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;
import com.example.loraassistant.R;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends BaseAdapter {

    private Context context;
    private List<BleDevice> bleDeviceList;

    public DeviceAdapter(Context context){
        this.context = context;
        this.bleDeviceList = new ArrayList<>();
    }
    public void addDevice(BleDevice bleDevice) {
        //先清除再添加，防止添加相同的设备
        removeDevice(bleDevice);
        bleDeviceList.add(bleDevice);
    }

    public void removeDevice(BleDevice bleDevice) {
        for (int i = 0; i < bleDeviceList.size(); i++) {
            BleDevice device = bleDeviceList.get(i);
            if (bleDevice.getKey().equals(device.getKey())) {
                bleDeviceList.remove(i);
            }
        }
    }

    public void clearConnectedDevice() {
        for (int i = 0; i < bleDeviceList.size(); i++) {
            BleDevice device = bleDeviceList.get(i);
            if (BleManager.getInstance().isConnected(device)) {
                bleDeviceList.remove(i);
            }
        }
    }
    @Override
    public int getCount() {
        return bleDeviceList.size();
    }

    @Override
    public Object getItem(int position) {
        if(position > bleDeviceList.size())
            return null;
        return  bleDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView != null){
            holder = (ViewHolder) convertView.getTag();
        }else{
            convertView = View.inflate(context, R.layout.adapter_device,null);
            holder = new ViewHolder();
            convertView.setTag(holder);
            holder.img_bluetooth = convertView.findViewById(R.id.img_blue);
            holder.txt_name = (TextView) convertView.findViewById(R.id.txt_name);
            holder.txt_mac = (TextView) convertView.findViewById(R.id.txt_mac);
            holder.txt_rssi = (TextView) convertView.findViewById(R.id.txt_rssi);
            holder.layout_idle = (LinearLayout) convertView.findViewById(R.id.layout_idle);
            holder.layout_connected = (LinearLayout) convertView.findViewById(R.id.layout_connected);
            holder.btn_disconnect = (Button) convertView.findViewById(R.id.btn_disconnect);
            holder.btn_connect = (Button) convertView.findViewById(R.id.btn_connect);
        }

        final BleDevice bleDevice = (BleDevice) getItem(position);
        if(bleDevice != null){
            boolean isConnected = BleManager.getInstance().isConnected(bleDevice);
            String name = bleDevice.getName();
            String mac = bleDevice.getMac();
            int rssi = bleDevice.getRssi();
            holder.txt_name.setText(name);
            holder.txt_mac.setText(mac);
            holder.txt_rssi.setText(String.valueOf(rssi));
            holder.layout_idle.setVisibility(View.GONE);
            holder.layout_connected.setVisibility(View.VISIBLE);
            if (isConnected) {
                holder.img_bluetooth.setImageResource(R.mipmap.ic_blue_connected);
                holder.txt_name.setTextColor(0xFF1DE9B6);
                holder.txt_mac.setTextColor(0xFF1DE9B6);
                holder.layout_idle.setVisibility(View.GONE);
                holder.layout_connected.setVisibility(View.VISIBLE);
            } else {
                holder.img_bluetooth.setImageResource(R.mipmap.ic_blue_remote);
                holder.txt_name.setTextColor(0xFF000000);
                holder.txt_mac.setTextColor(0xFF000000);
                holder.layout_idle.setVisibility(View.VISIBLE);
                holder.layout_connected.setVisibility(View.GONE);
            }
        }
        holder.btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onConnect(bleDevice);
                }
            }
        });

        holder.btn_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onDisConnect(bleDevice);
                }
            }
        });

//        holder.btn_detail.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (mListener != null) {
//                    mListener.onDetail(bleDevice);
//                }
//            }
//        });

        return convertView;
    }

    private class ViewHolder {
        ImageView img_bluetooth;
        TextView txt_name;
        TextView txt_mac;
        TextView txt_rssi;
        LinearLayout layout_idle;
        LinearLayout layout_connected;
        Button btn_disconnect;
        Button btn_connect;
//        Button btn_detail;
    }

    public interface OnDeviceClickListener {
        void onConnect(BleDevice bleDevice);

        void onDisConnect(BleDevice bleDevice);

//        void onDetail(BleDevice bleDevice);
    }
    //点击事件接口
    private OnDeviceClickListener mListener;

     public void setOnDeviceClickListener(OnDeviceClickListener listener){
         this.mListener = listener;
     }
}
