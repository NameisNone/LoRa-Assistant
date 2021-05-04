package com.example.loraassistant;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.CustomListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.example.loraassistant.Adapter.Alarm;
import com.example.loraassistant.Adapter.AlarmAdapter;
import com.example.loraassistant.Common.MyApp;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CtrlActivity extends Activity {

    private byte light_value = 0;

    private SeekBar mSb_Light;
    private SeekBarListener sbListener;
    private OnClick onClick;
    private ImageView mImg_Switch;
//    private boolean led_state = false;

    private List<Alarm> mListData = null;
    private AlarmAdapter mAlarmAdapter = null;
    private Context mContext;
    private ListView mListView;
    private TextView mTv_AddTimeEvent;

    private TextView mTv_NodeTitle;
    private String mFileName = null;
    private String mListFileName = null;

    private TimePickerView pvCustomTime;

    private byte[] dataOn = {0x01};
    private byte[] dataOff = {0x01};
    private int mNodeNum;

    public CtrlActivity() {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ctrl);

        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initAdapter();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SaveListData((LinkedList<Alarm>) mListData);
    }

    private void initUI() {
        mSb_Light = findViewById(R.id.sb_light);

        mSb_Light.setProgress(light_value);
        sbListener = new SeekBarListener();
        mSb_Light.setOnSeekBarChangeListener(sbListener);

        onClick = new OnClick();
        findViewById(R.id.tv_add).setOnClickListener(onClick);
        findViewById(R.id.tv_sub).setOnClickListener(onClick);

        mImg_Switch = findViewById(R.id.img_switch);
        mImg_Switch.setOnClickListener(onClick);

        mListView = findViewById(R.id.lv_timedEvent);

        mTv_AddTimeEvent = findViewById(R.id.tv_addTimedEvent);
        mTv_AddTimeEvent.setOnClickListener(onClick);

        ListViewItemClick lvItemClick = new ListViewItemClick();
        mListView.setOnItemClickListener(lvItemClick);
        mListView.setOnItemLongClickListener(lvItemClick);

        mTv_NodeTitle = findViewById(R.id.tv_nodetitle);

    }

    private void updateNodeParam() {
        Intent intent = getIntent();
        String nodename = intent.getStringExtra("nodename");
        String filename = intent.getStringExtra("filename");
        String listfilename = intent.getStringExtra("listfilename");

        mTv_NodeTitle.setText(nodename);
        mFileName = filename;
        mListFileName = listfilename;

        if(nodename.equals("节点1"))          mNodeNum = 1;
        else if (nodename.equals("节点2"))    mNodeNum = 2;
        else if (nodename.equals("节点3"))    mNodeNum = 3;
        else if (nodename.equals("节点4"))    mNodeNum = 4;
    }

    private void initAdapter() {
        updateNodeParam();//从Fragment中获取参数

        mContext = CtrlActivity.this;
        mListData = new LinkedList<Alarm>();

        String jsonstr = ReadListData();
        Log.i("File", jsonstr);
        LoadData(jsonstr);
//        mListData.add(new Alarm(10, 30, true, true, 90,true));
//        mListData.add(new Alarm(18, 30, false, false, 10,true));
        mAlarmAdapter = new AlarmAdapter(mContext, (LinkedList<Alarm>) mListData);

        mListView.setAdapter(mAlarmAdapter);
    }


    //ListView item点击事件处理类
    private class ListViewItemClick implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            Log.i("item","单击："+position);
            showAlarmContentCustomTimePicker(position);
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
//            Log.i("item","长按："+position);
            new SweetAlertDialog(CtrlActivity.this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("是否删除此定时事件?")
                    .setContentText("删除后将不可恢复!")
                    .setConfirmText("确定")
                    .setCancelText("取消")
                    //"确定"点击事件
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            mListData.remove(position);
                            mListView.setAdapter(mAlarmAdapter);
                            sweetAlertDialog.hide();
                        }
                    })
                    .show();
            return true;
        }
    }

    //拖动条的监听事件类
    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//            Toast.makeText(getApplicationContext(),String.format("SeekBar进度值:%d",progress),Toast.LENGTH_SHORT).show();
            light_value = (byte) progress;
            TextView tv_light = findViewById(R.id.tv_light_value);
            tv_light.setText(String.format("%d", progress));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
//            Toast.makeText(getApplicationContext(),"按下SeekBar",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
//            Toast.makeText(getApplicationContext(),"释放SeekBar",Toast.LENGTH_SHORT).show();
            //发送亮度数据
            MyApp.CtrlLed(mNodeNum, light_value);
        }
    }

    private class OnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_add:
                    if (light_value < 100)
                        mSb_Light.setProgress(++light_value);
                    else
                        mSb_Light.setProgress(100);
                    break;
                case R.id.tv_sub:
                    if (light_value > 0)
                        mSb_Light.setProgress(--light_value);
                    else
                        mSb_Light.setProgress(0);
                    break;
                case R.id.img_switch://灯的开关按钮
                    if(mImg_Switch.getTag().equals("off")){
                        mImg_Switch.setImageResource(R.mipmap.button_on);
                        mImg_Switch.setTag("on");
                        MyApp.CtrlLed(mNodeNum, MyApp.LedState.LedOn);//开灯
                    }else if(mImg_Switch.getTag().equals("on")){
                        mImg_Switch.setImageResource(R.mipmap.button_off);
                        mImg_Switch.setTag("off");
                        MyApp.CtrlLed(mNodeNum, MyApp.LedState.LedOff);//关灯
                    }
                    break;
                case R.id.tv_addTimedEvent:
                    showCustomTimePicker();
                    break;
            }
        }
    }

    private void BleLedCtrl(boolean ledState) {
        byte dataState = (byte) (ledState ? 0x00 : 0x01);
//        Log.i("dataState", String.format("0x%02x",dataState));
        byte[] data = {(byte) mNodeNum,dataState};//BLE要发送的数据：第1个字节是节点号，第1个字节是led状态
        Log.i("dataState", String.format("0x%02x",mNodeNum));

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

    private TimePickerView pvAlarmContentCustomTime;
    private int mContentHour = 0;
    private int mContentMin = 0;
    private boolean mContentPeriod = true;//执行一次
    private boolean mContentState = false;//灯状态关
    private int mContentLightvalue = 0;//执行一次

    //定时事件内容详情显示 对话框
    private void showAlarmContentCustomTimePicker(final int position) {
        final Alarm pvAlarm = mListData.get(position);//获取当前定时事件
        Calendar selectedDate = Calendar.getInstance();//系统当前时间
        selectedDate.set(Calendar.HOUR_OF_DAY, pvAlarm.getHour());
        selectedDate.set(Calendar.MINUTE, pvAlarm.getMin());
        selectedDate.set(Calendar.SECOND, 0);
        Calendar startDate = Calendar.getInstance();
        startDate.set(2014, 0, 0);
        Calendar endDate = Calendar.getInstance();
        endDate.set(2027, 11, 30);
        //时间选择器 ，自定义布局
        pvAlarmContentCustomTime = new TimePickerBuilder(this, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {//选中事件回调
                mContentHour = getPickViewHour(date);
                mContentMin = getPickViewMin(date);

                pvAlarm.setTime(mContentHour, mContentMin);
                pvAlarm.setPeriod(mContentPeriod);
                pvAlarm.setLedState(mContentState);
                pvAlarm.setLightValue(mContentLightvalue);
                mListData.set(position, pvAlarm);
                mListView.setAdapter(mAlarmAdapter);
            }
        })
                .setDate(selectedDate)//设置当前定时事件的时间
                .setOutSideCancelable(false)
                .setRangDate(startDate, endDate)
                .setLayoutRes(R.layout.pickerview_custom_time, new CustomListener() {
                    @Override
                    public void customLayout(View v) {
                        final TextView tv_PickerViewTitle = (TextView) v.findViewById(R.id.tv_PickerViewTitle);
                        tv_PickerViewTitle.setText("设置定时事件");
                        final TextView tvSubmit = (TextView) v.findViewById(R.id.tv_finish);
                        final TextView ivCancel = v.findViewById(R.id.tv_cancel);
                        final LinearLayout llPickerView = v.findViewById(R.id.ll_lightvalue);
                        llPickerView.setVisibility(View.GONE);

                        SeekBar sbLight = v.findViewById(R.id.sb_dialoglight);
                        final TextView tvLightValue = v.findViewById(R.id.tv_dialoglightvalue);
                        final ImageView ivKey = v.findViewById(R.id.img_key);
                        final RadioGroup rgPeriod = v.findViewById(R.id.rg_period);
                        RadioButton rbOneTime = v.findViewById(R.id.rb_OneTime);
                        RadioButton rbRepeat = v.findViewById(R.id.rb_repeat);
                        if (pvAlarm.getPeriod()) {
                            rbOneTime.setChecked(true);
                            mContentPeriod = true;
                        } else {
                            rbRepeat.setChecked(true);
                            mContentPeriod = false;
                        }
                        if (pvAlarm.getLedState()) {
                            ivKey.setImageResource(R.mipmap.button_on);
                            ivKey.setTag("key_on");
                            llPickerView.setVisibility(View.VISIBLE);
                            sbLight.setProgress(pvAlarm.getLedLight());
                            tvLightValue.setText(String.format("%d%%", pvAlarm.getLedLight()));
                            mContentState = true;
                            mContentLightvalue = pvAlarm.getLedLight();
                        } else {
                            ivKey.setImageResource(R.mipmap.button_off);
                            ivKey.setTag("key_off");
                            llPickerView.setVisibility(View.GONE);
                            mContentState = false;
                            mContentLightvalue = 0;
                        }

                        sbLight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                tvLightValue.setText(String.format("%d%%", progress).toString());
                                mContentLightvalue = progress;
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });
                        rgPeriod.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup group, int checkedId) {
                                RadioButton radbtn = (RadioButton) findViewById(checkedId);
//                                Toast.makeText(getApplicationContext(),radbtn.getText(),Toast.LENGTH_SHORT).show();
                                if (radbtn.getId() == R.id.rb_OneTime) {
                                    mContentPeriod = true;
                                } else {
                                    mContentPeriod = false;
                                }
                            }
                        });

                        tvSubmit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pvAlarmContentCustomTime.returnData();
                                pvAlarmContentCustomTime.dismiss();
                            }
                        });
                        ivCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pvAlarmContentCustomTime.dismiss();
                            }
                        });
                        ivKey.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (v.getId() == R.id.img_key) {
                                    if (ivKey.getTag().equals("key_off")) {//开灯
                                        ivKey.setTag("key_on");
                                        ivKey.setImageResource(R.mipmap.button_on);
                                        mContentState = true;
                                        llPickerView.setVisibility(View.VISIBLE);
                                    } else {//关灯
                                        ivKey.setTag("key_off");
                                        ivKey.setImageResource(R.mipmap.button_off);
                                        mContentState = false;
                                        llPickerView.setVisibility(View.GONE);
                                    }
                                }
                            }
                        });
                    }
                })
                .isCyclic(true)
                .setContentTextSize(18)
                .setType(new boolean[]{false, false, false, true, true, false})
                .setLabel("年", "月", "日", "时", "分", "秒")
                .setLineSpacingMultiplier(1.5f)
                .setTextXOffset(0, 0, 0, 0, 0, 0)
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setDividerColor(0xFF24AD9D)
                .build();
        pvAlarmContentCustomTime.show();
    }

    private int mAddHour = 0;
    private int mAddMin = 0;
    private boolean mAddPeriod = true;//执行一次
    private boolean mAddState = false;//灯状态关
    private int mAddLightvalue = 0;//执行一次

    private void initAddParam() {
        mAddHour = 0;
        mAddMin = 0;
        mAddPeriod = true;
        mAddState = false;
        mAddLightvalue = 0;
    }

    private void showCustomTimePicker() {
        initAddParam();
        Calendar selectedDate = Calendar.getInstance();//系统当前时间
        selectedDate.set(Calendar.SECOND, 0);
        Calendar startDate = Calendar.getInstance();
        startDate.set(2014, 0, 0);
        Calendar endDate = Calendar.getInstance();
        endDate.set(2027, 11, 30);
        //时间选择器 ，自定义布局
        pvCustomTime = new TimePickerBuilder(this, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View v) {//选中事件回调
                mAddHour = getPickViewHour(date);
                mAddMin = getPickViewMin(date);

                mListData.add(new Alarm(mAddHour, mAddMin, mAddPeriod, mAddState, mAddLightvalue, false));
                mListView.setAdapter(mAlarmAdapter);
            }
        })
                .setDate(selectedDate)
                .setOutSideCancelable(false)
                .setRangDate(startDate, endDate)
                .setLayoutRes(R.layout.pickerview_custom_time, new CustomListener() {
                    @Override
                    public void customLayout(View v) {
                        final TextView tvSubmit = (TextView) v.findViewById(R.id.tv_finish);
                        final TextView ivCancel = v.findViewById(R.id.tv_cancel);
                        final ImageView ivKey = v.findViewById(R.id.img_key);
                        final LinearLayout llPickerView = v.findViewById(R.id.ll_lightvalue);
                        llPickerView.setVisibility(View.GONE);
                        SeekBar sbLight = v.findViewById(R.id.sb_dialoglight);
                        final TextView tvLightValue = v.findViewById(R.id.tv_dialoglightvalue);
                        final RadioGroup rgPeriod = v.findViewById(R.id.rg_period);
                        RadioButton rbOneTime = v.findViewById(R.id.rb_OneTime);
                        RadioButton rbRepeat = v.findViewById(R.id.rb_repeat);
                        rbOneTime.setChecked(true);

                        sbLight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                tvLightValue.setText(String.format("%d%%", progress).toString());
                                mAddLightvalue = progress;
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });
                        rgPeriod.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup group, int checkedId) {
                                RadioButton radbtn = (RadioButton) findViewById(checkedId);
                                if (radbtn.getId() == R.id.rb_OneTime) {
                                    mAddPeriod = true;
                                } else {
                                    mAddPeriod = false;
                                }
                            }
                        });

                        tvSubmit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pvCustomTime.returnData();
                                pvCustomTime.dismiss();
                            }
                        });
                        ivCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pvCustomTime.dismiss();
                            }
                        });
                        ivKey.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (v.getId() == R.id.img_key) {
                                    if (ivKey.getTag().equals("key_off")) {//开灯
                                        ivKey.setTag("key_on");
                                        ivKey.setImageResource(R.mipmap.button_on);
                                        mAddState = true;
                                        llPickerView.setVisibility(View.VISIBLE);//显示亮度设置栏
                                    } else {//关灯
                                        ivKey.setTag("key_off");
                                        ivKey.setImageResource(R.mipmap.button_off);
                                        mAddState = false;
                                        llPickerView.setVisibility(View.GONE);//隐藏亮度设置栏
                                    }
                                }
                            }
                        });
                    }
                })
                .isCyclic(true)
                .setContentTextSize(18)
                .setType(new boolean[]{false, false, false, true, true, false})
                .setLabel("年", "月", "日", "时", "分", "秒")
                .setLineSpacingMultiplier(1.5f)
                .setTextXOffset(0, 0, 0, 0, 0, 0)
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setDividerColor(0xFF24AD9D)
                .build();
        pvCustomTime.show();
    }


    private int getPickViewHour(Date date) {//可根据需要自行截取数据显示
        Log.d("getTime()", "choice date millis: " + date.getTime());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Log.d("format", format.format(date).toString().substring(11, 13));
        return Integer.parseInt(format.format(date).toString().substring(11, 13));
    }

    private int getPickViewMin(Date date) {
        Log.d("getTime()", "choice date millis: " + date.getTime());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Log.d("format", format.format(date).toString().substring(14, 16));
        return Integer.parseInt(format.format(date).toString().substring(14, 16));
    }

    private String jsonStr = null;

    private void SaveListData(LinkedList<Alarm> linkedList) {
        FileOutputStream fos = null;
        Gson gson = new Gson();
        jsonStr = gson.toJson(linkedList);//gson
        try {
            fos = openFileOutput(mListFileName, Context.MODE_PRIVATE);
            fos.write(jsonStr.getBytes());
        } catch (Exception e) {
            Log.i("File", "存储失败");
            e.printStackTrace();
        } finally {
            try {
                fos.close();
                Log.i("File", "存储成功");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String ReadListData() {
        FileInputStream fis = null;
        try {
            fis = openFileInput(mListFileName);
            byte[] outByte = new byte[fis.available()];
            StringBuilder sb = new StringBuilder("");
            int len = 0;
            while ((len = fis.read(outByte)) > 0) {
                sb.append(new String(outByte, 0, len));
            }
//            Log.i("File", sb.toString());
            return sb.toString();//加载数据
        } catch (Exception e) {
            Log.i("File", "读取失败");
//            Log.e("File",Log.getStackTraceString(e));
            e.printStackTrace();
        } finally {
            try {
                Log.i("File", "读取成功");
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void LoadData(String jsonStr) {
        JsonArray jsonArrayRoot = new JsonParser().parse(jsonStr).getAsJsonArray();

        int objSize = jsonArrayRoot.size();
        if (objSize > 0) {
            Log.i("File", String.valueOf(objSize));
            JsonObject[] jsonObjArray = new JsonObject[100];//最大存100个listview的数据，即最多可以设置100个定时事件
            for (int i = 0; i < objSize; i++) {
                jsonObjArray[i] = jsonArrayRoot.get(i).getAsJsonObject();
//            Log.i("File",jsonObjArray[i].toStri/ng());

                boolean State = jsonObjArray[i].get("LedState").getAsBoolean();
                boolean Period = jsonObjArray[i].get("Period").getAsBoolean();
                int LedLight = jsonObjArray[i].get("LedLight").getAsInt();
                int hour = jsonObjArray[i].get("calendar").getAsJsonObject().get("hourOfDay").getAsInt();
                int min = jsonObjArray[i].get("calendar").getAsJsonObject().get("minute").getAsInt();
                boolean AlarmState = jsonObjArray[i].get("AlarmState").getAsBoolean();
                mListData.add(new Alarm(hour, min, Period, State, LedLight, AlarmState));//添加到List中
//                Log.i("File", String.format("LedLight:%d", jsonObjArray[i].get("LedLight").getAsInt()));
//                Log.i("File", String.format("hourOfDay:%d", jsonObjArray[i].get("calendar").getAsJsonObject().get("hourOfDay").getAsInt()));
//                Log.i("File", String.format("minute:%d", jsonObjArray[i].get("calendar").getAsJsonObject().get("minute").getAsInt()));
            }
        }
    }

}

