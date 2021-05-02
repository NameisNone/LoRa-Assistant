package com.example.loraassistant.Fragment;

import android.app.Fragment;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.example.loraassistant.BleActivity;
import com.example.loraassistant.Common.MyApp;
import com.example.loraassistant.CtrlActivity;
import com.example.loraassistant.R;

public class Fragment_Home extends Fragment implements View.OnClickListener {

    private ImageView mImgLed1, mImgLed2, mImgLed3, mImgLed4;
    private ImageView mImgKey1, mImgKey2, mImgKey3, mImgKey4;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().findViewById(R.id.ll_1).setOnClickListener(this);
        getActivity().findViewById(R.id.ll_2).setOnClickListener(this);
        getActivity().findViewById(R.id.ll_3).setOnClickListener(this);
        getActivity().findViewById(R.id.ll_4).setOnClickListener(this);

        mImgLed1 = getActivity().findViewById(R.id.img_led1);
        mImgLed2 = getActivity().findViewById(R.id.img_led2);
        mImgLed3 = getActivity().findViewById(R.id.img_led3);
        mImgLed4 = getActivity().findViewById(R.id.img_led4);

        mImgKey1 = getActivity().findViewById(R.id.Img_key_on1);
        mImgKey2 = getActivity().findViewById(R.id.Img_key_on2);
        mImgKey3 = getActivity().findViewById(R.id.Img_key_on3);
        mImgKey4 = getActivity().findViewById(R.id.Img_key_on4);

        OnClick onClick = new OnClick();
        mImgKey1.setOnClickListener(onClick);
        mImgKey2.setOnClickListener(onClick);
        mImgKey3.setOnClickListener(onClick);
        mImgKey4.setOnClickListener(onClick);

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();//传参
        switch (v.getId()) {
            case R.id.ll_1:
                intent.setClass(getActivity(), CtrlActivity.class);
                bundle.putString("nodename", "节点1");
                bundle.putString("filename", "Ctrl1_Param.txt");
                bundle.putString("listfilename", "Ctrl1.txt");
                intent.putExtras(bundle);
                break;
            case R.id.ll_2:
                intent.setClass(getActivity(), CtrlActivity.class);
                bundle.putString("nodename", "节点2");
                bundle.putString("filename", "Ctrl2_Param.txt");
                bundle.putString("listfilename", "Ctrl2.txt");
                intent.putExtras(bundle);
                break;
            case R.id.ll_3:
                intent.setClass(getActivity(), CtrlActivity.class);
                bundle.putString("nodename", "节点3");
                bundle.putString("filename", "Ctrl3_Param.txt");
                bundle.putString("listfilename", "Ctrl3.txt");
                intent.putExtras(bundle);
                break;
            case R.id.ll_4:
                intent.setClass(getActivity(), CtrlActivity.class);
                bundle.putString("nodename", "节点4");
                bundle.putString("filename", "Ctrl4_Param.txt");
                bundle.putString("listfilename", "Ctrl4.txt");
                intent.putExtras(bundle);
                break;
            default:
                break;
        }
        startActivity(intent);
    }

    private void CtrlLed(int nodeNum, boolean ledState){
        byte dataState = (byte) (ledState ? 0x01 : 0x00);
        byte[] data = {(byte) nodeNum,dataState};//BLE要发送的数据：第1个字节是节点号，第1个字节是led状态
        if (MyApp.isUartEnable){
            Toast.makeText(getActivity().getApplicationContext(),
                    String.format("0x%02x 0x%02x",data[0],data[1]),
                    Toast.LENGTH_SHORT).show();
            MyApp.ch34x_driver.WriteData(data,data.length);
        } else if(BleActivity.getBleDevice() != null){
//            Log.i("CtrlLed", String.format("0x%02x",nodeNum));
            BluetoothGattService gattService = BleActivity.getGattService();
            BluetoothGattCharacteristic characteristic = BleActivity.getCharacteristic();
            BleDevice bleDevice = BleActivity.getBleDevice();
            if(gattService != null && characteristic != null){
                BleManager.getInstance().write(bleDevice, gattService.getUuid().toString(), characteristic.getUuid().toString(), data, new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
//                    Toast.makeText(getApplicationContext(),"执行成功",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
//                    Toast.makeText(getApplicationContext(),"执行失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void CtrlLed(int nodeNum, boolean ledState, int brightness){
        byte dataState = (byte) (ledState ? 0x01 : 0x00);
        byte lightValue = (byte) brightness;
        byte[] data = {(byte) nodeNum,dataState,lightValue};//BLE要发送的数据：第1个字节是节点号，第1个字节是led状态
        if (MyApp.isUartEnable){
            MyApp.ch34x_driver.WriteData(data,data.length);
        } else if(BleActivity.getBleDevice() != null){
//            Log.i("CtrlLed", String.format("0x%02x",nodeNum));
            BluetoothGattService gattService = BleActivity.getGattService();
            BluetoothGattCharacteristic characteristic = BleActivity.getCharacteristic();
            BleDevice bleDevice = BleActivity.getBleDevice();
            if(gattService != null && characteristic != null){
                BleManager.getInstance().write(bleDevice, gattService.getUuid().toString(), characteristic.getUuid().toString(), data, new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
//                    Toast.makeText(getApplicationContext(),"执行成功",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
//                    Toast.makeText(getApplicationContext(),"执行失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private class OnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Img_key_on1:
                    if(mImgKey1.getTag().equals("on")){
                        CtrlLed(1,false);
                        mImgKey1.setImageResource(R.mipmap.key_off);
                        mImgKey1.setTag("off");
                        mImgLed1.setImageResource(R.mipmap.led_off);
                    }else if(mImgKey1.getTag().equals("off")){
                        CtrlLed(1,true);
                        mImgKey1.setImageResource(R.mipmap.key_on);
                        mImgKey1.setTag("on");
                        mImgLed1.setImageResource(R.mipmap.led_on);
                    }
                    break;
                case R.id.Img_key_on2:
                    if(mImgKey2.getTag().equals("on")){
                        mImgKey2.setImageResource(R.mipmap.key_off);
                        mImgKey2.setTag("off");
                        mImgLed2.setImageResource(R.mipmap.led_off);
                        CtrlLed(2,false);
                    }else if(mImgKey2.getTag().equals("off")){
                        mImgKey2.setImageResource(R.mipmap.key_on);
                        mImgKey2.setTag("on");
                        mImgLed2.setImageResource(R.mipmap.led_on);
                        CtrlLed(2,true);
                    }
                    break;
                case R.id.Img_key_on3:
                    if(mImgKey3.getTag().equals("on")){
                        mImgKey3.setImageResource(R.mipmap.key_off);
                        mImgKey3.setTag("off");
                        mImgLed3.setImageResource(R.mipmap.led_off);
                        CtrlLed(3,false);
                    }else if(mImgKey3.getTag().equals("off")){
                        mImgKey3.setImageResource(R.mipmap.key_on);
                        mImgKey3.setTag("on");
                        mImgLed3.setImageResource(R.mipmap.led_on);
                        CtrlLed(3,true);
                    }
                    break;
                case R.id.Img_key_on4:
                    if(mImgKey4.getTag().equals("on")){
                        mImgKey4.setImageResource(R.mipmap.key_off);
                        mImgKey4.setTag("off");
                        mImgLed4.setImageResource(R.mipmap.led_off);
                        CtrlLed(4,false);
                    }else if(mImgKey4.getTag().equals("off")){
                        mImgKey4.setImageResource(R.mipmap.key_on);
                        mImgKey4.setTag("on");
                        mImgLed4.setImageResource(R.mipmap.led_on);
                        CtrlLed(4,true);
                    }
                    break;
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
}
