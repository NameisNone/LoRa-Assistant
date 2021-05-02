package com.example.loraassistant;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleMtuChangedCallback;
import com.clj.fastble.callback.BleRssiCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.example.loraassistant.Adapter.DeviceAdapter;
import com.example.loraassistant.Common.ObserverManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BleActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;
    public static BluetoothGattService mBluetoothGattService;
    private static BluetoothGattCharacteristic mBluetoothGattCharacteristic;
    private static BleDevice mBleDevice;
    private String target_uuid_service = "000000ff-0000-1000-8000-00805f9b34fb";
    private String target_uuid_characteristic = "0000ff01-0000-1000-8000-00805f9b34fb";

    private Button mBtn_scan;
    private static boolean isBleReady = false;
    private ImageView mImg_loading;
    private Animation operatingAnim;
    private DeviceAdapter mDeviceAdapter;
    private ImageView mImg_led;
    private SeekBar mSb_light;
    private TextView mTv_light;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);
        initView();
        initBLE();
    }

    public void initBLE() {
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)//是否使能框架内部的日志打印
                .setReConnectCount(1, 5000)//设置重连次数和间隔时间
                .setOperateTimeout(5000);//设置操作超时时间
    }

    private void initView() {
        mToolbar = findViewById(R.id.toolbar_ble);
////        //设置toolbar旁边的返回键
//        setSupportActionBar(mToolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeButtonEnabled(true);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setHomeButtonEnabled(true);
//        }


        mBtn_scan = findViewById(R.id.btn_scan);
        mBtn_scan.setOnClickListener(this);

        mImg_loading = findViewById(R.id.img_loading);

        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        operatingAnim.setInterpolator(new LinearInterpolator());

        connectProgressDialog = new ProgressDialog(this);

        mDeviceAdapter = new DeviceAdapter(this);
        mDeviceAdapter.setOnDeviceClickListener(new DeviceAdapter.OnDeviceClickListener() {
            @Override
            public void onConnect(BleDevice bleDevice) {
                if(!BleManager.getInstance().isConnected(bleDevice)){
                    BleManager.getInstance().cancelScan();
                    connect(bleDevice);
                }
            }

            @Override
            public void onDisConnect(BleDevice bleDevice) {
                if(BleManager.getInstance().isConnected(bleDevice)){
                    BleManager.getInstance().disconnect(bleDevice);
                }
            }
        });

        ListView listView_device = findViewById(R.id.lv_devices);
        listView_device.setAdapter(mDeviceAdapter); //设置适配器mDeviceAdapter
    }

    private ProgressDialog connectProgressDialog; //连接进度条对话框
    private void connect(final BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                connectProgressDialog.setTitle("正在连接");
                connectProgressDialog.show();
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                Toast.makeText(getApplicationContext(), "连接失败", Toast.LENGTH_LONG).show();
                if(connectProgressDialog != null){
                    connectProgressDialog.dismiss();
                }
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "连接成功", Toast.LENGTH_LONG).show();
                if(connectProgressDialog != null){
                    connectProgressDialog.dismiss();
                }
                //找到对应的service
                List<BluetoothGattService> serviceList = gatt.getServices();
                for (BluetoothGattService service : serviceList) {
                    UUID uuid_service = service.getUuid();
//                    Log.i("UUID",uuid_service.toString());
                    if(uuid_service.toString().equals(target_uuid_service)){
                        List<BluetoothGattCharacteristic> characteristicList= service.getCharacteristics();
                        for(BluetoothGattCharacteristic characteristic : characteristicList) {
                            UUID uuid_chara = characteristic.getUuid();
//                            Log.i("uuid_chara",uuid_chara.toString());
                            if(uuid_chara.toString().equals(target_uuid_characteristic)){
                                mBluetoothGattService = service;
                                mBluetoothGattCharacteristic = characteristic;
                                mBleDevice = bleDevice;
                                isBleReady = true;
                            }
                        }
                    }
                }
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                mBluetoothGattService = null;
                mBluetoothGattCharacteristic = null;
                mBleDevice = null;
                isBleReady = false;

                mDeviceAdapter.removeDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
                if(connectProgressDialog != null){
                    connectProgressDialog.dismiss();
                }
                if (isActiveDisConnected) { //调用disconnnect断开连接isActiveDisConnected为true
                    Toast.makeText(getApplicationContext(), "已断开", Toast.LENGTH_LONG).show();
                } else { //意外断开
                    Toast.makeText(getApplicationContext(), "连接断开", Toast.LENGTH_LONG).show();
                    Log.i("onDisConnected","连接断开");
                    ObserverManager.getInstance().notifyObserver(bleDevice);
                }
            }
        });
    }

    public static BleDevice getBleDevice(){
        if(isBleReady){
            return mBleDevice;
        }else{
            return null;
        }
    }

    public static BluetoothGattService getGattService() {
        if(isBleReady){
            return mBluetoothGattService;
        }else{
            return null;
        }
    }

    public static BluetoothGattCharacteristic getCharacteristic() {
        if(isBleReady){
            return mBluetoothGattCharacteristic;
        }else{
            return null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_scan:
                if (mBtn_scan.getText().equals("扫描设备")) {
                    checkPermissions();
                } else if (mBtn_scan.getText().equals("停止扫描")){
                    BleManager.getInstance().cancelScan();
                }
                break;
            default:break;
        }
    }

    //检查应用权限
    private void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
//            Toast.makeText(this, "请先打开蓝牙", Toast.LENGTH_LONG).show();
//            return;
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 0x01);
        }

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }
    //授予相应权限
    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new AlertDialog.Builder(this)
                            .setTitle("提示")
                            .setMessage("当前手机扫描蓝牙需要打开定位功能")
                            .setNegativeButton("取消",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                            .setPositiveButton("前往设置",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                        }
                                    })

                            .setCancelable(false)
                            .show();
                } else {
                    setScanRule();
                    startScan();
                }
                break;
        }
    }

    //开始扫描周围蓝牙ble设备
    private void startScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                mDeviceAdapter.clearConnectedDevice();
                mDeviceAdapter.notifyDataSetChanged();
//                Toast.makeText(getApplicationContext(),"onScanStarted",Toast.LENGTH_SHORT).show();
                mImg_loading.startAnimation(operatingAnim);
                mImg_loading.setVisibility(View.VISIBLE);
                mBtn_scan.setText("停止扫描");
            }
            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
//                Toast.makeText(getApplicationContext(),"onLeScan",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onScanning(BleDevice bleDevice) {
//                Toast.makeText(getApplicationContext(),"onScanning",Toast.LENGTH_SHORT).show();
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
            }
            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
//                Toast.makeText(getApplicationContext(),"onScanFinished",Toast.LENGTH_SHORT).show();
                mImg_loading.clearAnimation();
                mImg_loading.setVisibility(View.INVISIBLE);
                mBtn_scan.setText("扫描设备");
            }
        });
    }
    //设置扫描规则
    private void setScanRule() {
        //蓝牙设备的参数设置
        String devices_names = "LoRa"; //设备的广播名
        boolean isAutoConnect = false;//是否自动连接
        String mac = null;//设备的物理地址
        long scanTimeOut = 10000;//扫描超时时间

        //设置扫描规则
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setServiceUuids(null)      // 只扫描指定的服务的设备，可选
                .setDeviceName(true,devices_names) // 只扫描指定广播名的设备，可选
                .setDeviceMac(mac)                  // 只扫描指定mac的设备，可选
                .setAutoConnect(isAutoConnect)      // 连接时的autoConnect参数，可选，默认false
                .setScanTimeOut(scanTimeOut)        // 扫描超时时间，可选，默认10秒
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    //检查GPS定位是否打开，Android 6.0以上蓝牙需要打开定位功能
    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }
    //读取rssi信号值
    private void readRssi(BleDevice bleDevice) {
        BleManager.getInstance().readRssi(bleDevice, new BleRssiCallback() {
            @Override
            public void onRssiFailure(BleException exception) {
                Log.i("readRssi", "onRssiFailure" + exception.toString());
            }

            @Override
            public void onRssiSuccess(int rssi) {
                Log.i("onRssiSuccess", "onRssiSuccess: " + rssi);
            }
        });
    }
    //设置最大传输数据单元
    private void setMtu(BleDevice bleDevice, int mtu) {
        BleManager.getInstance().setMtu(bleDevice, mtu, new BleMtuChangedCallback() {
            @Override
            public void onSetMTUFailure(BleException exception) {
                Log.i("setMtu", "onsetMTUFailure" + exception.toString());
            }

            @Override
            public void onMtuChanged(int mtu) {
                Log.i("setMtu", "onMtuChanged: " + mtu);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Toast.makeText(getApplicationContext(),String.format("onActivityResult requestCode:%d",requestCode),Toast.LENGTH_SHORT).show();
        Log.i("onActivityResult",String.format("requestCode:%d",requestCode));
        if(requestCode == REQUEST_CODE_OPEN_GPS){//检查gps是否打开成功
            if(checkGPSIsOpen() && BleManager.getInstance().isBlueEnable()){//确认GPS是打开的
                setScanRule();//设置扫描规则
                startScan();//开始扫描蓝牙设备
            }
        }
    }
}
