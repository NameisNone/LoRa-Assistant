package com.example.loraassistant;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.loraassistant.Common.MyApp;
import com.example.loraassistant.Fragment.Fragment_Home;
import com.example.loraassistant.Fragment.Fragment_Setting;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    private enum UsbPermission { Unknown, Requested, Granted, Denied } //usb权限枚举类

    private static final String ACTION_USB_PERMISSION = BuildConfig.APPLICATION_ID + ".USB_PERMISSION";
    private static final String ACTION_USB_INSERT     = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    private static final String ACTION_USB_DETACHED   = "android.hardware.usb.action.USB_DEVICE_DETACHED";

    private FragmentManager fragmentManager;
    private Fragment_Home fg_home;
    private Fragment_Setting fg_setting;

    private RadioGroup mRg_tab;
    private RadioButton mRb_home;
    private RadioButton mRb_setting;

    private BroadcastReceiver usbPermissionBroadcastReceiver;
    private BroadcastReceiver usbInsertBroadcastReceiver;
    private BroadcastReceiver usbDetachedBroadcastReceiver;

    private UsbPermission usbPermission = UsbPermission.Unknown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getFragmentManager();

        //初始化UI控件
        initUI();
        InitUsbUart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(usbPermissionBroadcastReceiver);
        unregisterReceiver(usbInsertBroadcastReceiver);
        unregisterReceiver(usbDetachedBroadcastReceiver);
    }

    private void InitUsbUart(){
        //创建CH34x设备对象
        MyApp.ch34x_driver = new CH34xUARTDriver(
                (UsbManager) getSystemService(Context.USB_SERVICE),
                getApplicationContext(),
                ACTION_USB_PERMISSION);

        //创建usb授权广播接收者,授权成功后会进入此处
        usbPermissionBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
//                Toast.makeText(getApplicationContext(), "授权成功", Toast.LENGTH_SHORT).show();
                if(ACTION_USB_PERMISSION.equals(intent.getAction())) {
                    UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
                    for (UsbDevice usbDevice : usbManager.getDeviceList().values()){
                        if(usbDevice.getVendorId()==6790 && usbDevice.getProductId()==29987){
                            MyApp.uart_device_name = "CH34x_Device";
                            usbPermission = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                                    ? UsbPermission.Granted : UsbPermission.Denied;
                            InitCH34xUart();
                        }
                    }
                }
            }
        };
        registerReceiver(usbPermissionBroadcastReceiver,new IntentFilter(ACTION_USB_PERMISSION));
        //创建usb插入广播接收者，usb串口设备插入后会进入此处
        usbInsertBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(ACTION_USB_INSERT)){
//                    Toast.makeText(getApplicationContext(), "设备插入", Toast.LENGTH_SHORT).show();
                    UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
                    for (UsbDevice usbDevice : usbManager.getDeviceList().values()){
                        if(usbDevice.getVendorId()==6790 && usbDevice.getProductId()==29987){
                            MyApp.ch34x_driver.ResumeUsbPermission();//请求usb授权
                        }
                    }
                }
            }
        };
        registerReceiver(usbInsertBroadcastReceiver,new IntentFilter(ACTION_USB_INSERT));
        //创建usb移除广播接收者，usb串口设备移除后会进入此处
        usbDetachedBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(ACTION_USB_DETACHED)){
//                    Toast.makeText(getApplicationContext(), "设备移除", Toast.LENGTH_SHORT).show();
                    if(MyApp.isUartEnable){ //串口已打开
                        MyApp.ch34x_driver.CloseDevice();//关闭设备.
                        MyApp.isUartEnable = false;//标记串口使能状态为未使能
                    }
                }
            }
        };
        registerReceiver(usbDetachedBroadcastReceiver,new IntentFilter(ACTION_USB_DETACHED));
        //创建读handler
        MyApp.ch34x_read_handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                MyApp.ReadBuff = new String();
                MyApp.ReadBuff = (String) msg.obj;
                MyApp.isUartReadBuffNull = false;
            }
        };
        //尝试请求usb权限，请求成功说明有设备连接
        MyApp.ch34x_driver.ResumeUsbPermission();
    }

    private void initUI() {
        mRg_tab = findViewById(R.id.rg_tab);
        mRg_tab.setOnCheckedChangeListener(this);

        //选中home RadioButton会触发onCheckedChanged事件
        mRb_home = findViewById(R.id.rb_home);
        mRb_home.setChecked(true);
    }

    //初始化CH34x串口设备
    private void InitCH34xUart(){
        if(usbPermission == UsbPermission.Granted){//USB已经授权过
            int ret = MyApp.ch34x_driver.ResumeUsbList();
            if(ret == -1){
                Toast.makeText(getApplicationContext(),"获取设备列表失败!",Toast.LENGTH_SHORT).show();
                MyApp.ch34x_driver.CloseDevice();
            }else if (ret == 0){
                if(MyApp.ch34x_driver.mDeviceConnection != null){
                    if(!MyApp.ch34x_driver.UartInit()){
                        Toast.makeText(getApplicationContext(),"初始化串口失败!",Toast.LENGTH_SHORT).show();
                        return;
                    }
//                    Toast.makeText(getApplicationContext(),"串口初始化成功!",Toast.LENGTH_SHORT).show();
                    if(!MyApp.ch34x_driver.SetConfig(115200, (byte) 8, (byte)0,(byte)0,(byte)0)){
                        Toast.makeText(getApplicationContext(),"串口配置失败!",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    MyApp.isUartEnable = true;
                    CH34xReadData();
                }
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                builder.setTitle("未授权限");
                builder.setMessage("确认退出吗?");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                });
                builder.setNegativeButton("返回", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                });
                builder.show();
            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        hideAllFragment(fragmentTransaction);
        switch (checkedId) {
            case R.id.rb_home:
                if (fg_home == null) {
                    fg_home = new Fragment_Home();
                    fragmentTransaction.add(R.id.FrameLayout_content, fg_home);//2、add
                } else {
                    fragmentTransaction.show(fg_home);
                }
                break;
            case R.id.rb_setting:
                if (fg_setting == null) {
                    fg_setting = new Fragment_Setting();
                    fragmentTransaction.add(R.id.FrameLayout_content, fg_setting);//2、add
                } else {
                    fragmentTransaction.show(fg_setting);
                }
                break;
            default:
                break;
        }
        fragmentTransaction.commitAllowingStateLoss();
    }

    private void hideAllFragment(FragmentTransaction fragmentTransaction) {
        if (fg_home != null) fragmentTransaction.hide(fg_home);
        if (fg_setting != null) fragmentTransaction.hide(fg_setting);
    }

    //读数据
    private void CH34xReadData() {
        //创建一个新的线程来读取数据，将读取的数据通过handler发送出去进行处理
        new Thread() {
            @Override
            public void run() {
                super.run();
                byte[] buffer = new byte[4096];
                while (true){ //一直循环读取数据
                    Message message = new Message();
                    int len =MyApp.ch34x_driver.ReadData(buffer, 4096);
                    if(len>0){
//                        String recv = toHexString(buffer, len);	//以16进制形式输出
                        String recv = new String(buffer, 0, len);//以字符串形式输出
                        message.obj = recv;
                        //使用handler将收到的数据发送出去
                        MyApp.ch34x_read_handler.sendMessage(message);
                    }
                }
            }
        }.start();
    }
}
