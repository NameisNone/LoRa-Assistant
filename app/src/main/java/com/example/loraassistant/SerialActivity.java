package com.example.loraassistant;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

public class SerialActivity extends AppCompatActivity {

    private Spinner mspin_baudrate;
    private Spinner mspin_databit;
    private Spinner mspin_stopbit;
    private Spinner mspin_parity;

    private SpinnerOnItemSelectedListener mSpinnerOnItemSelectedListener;

    /*波特率 数据位 停止位 奇偶校验 流控*/
    private static int baudRate = 0;       //波特率
    private static byte dataBit = 0;           //数据位
    private static byte stopBit = 0;           //停止位
    private static byte parity = 0;            //校验
    private static byte flowControl = 0;       //流控

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial);

        initUI();
    }

    private void initUI() {
        mSpinnerOnItemSelectedListener = new SpinnerOnItemSelectedListener();

        mspin_baudrate = findViewById(R.id.spin_baudrate);
        mspin_baudrate.setOnItemSelectedListener(mSpinnerOnItemSelectedListener);

        mspin_databit = findViewById(R.id.spin_databit);
        mspin_databit.setOnItemSelectedListener(mSpinnerOnItemSelectedListener);

        mspin_stopbit = findViewById(R.id.spin_stopbit);
        mspin_stopbit.setOnItemSelectedListener(mSpinnerOnItemSelectedListener);

        mspin_parity = findViewById(R.id.spin_parity);
        mspin_parity.setOnItemSelectedListener(mSpinnerOnItemSelectedListener);
    }

    //    OnItemSelected监听器
    private class SpinnerOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String selectItem = parent.getItemAtPosition(position).toString();
//            Toast.makeText(getApplicationContext(), selectItem, Toast.LENGTH_LONG).show();
            switch (parent.getId()) {
                case R.id.spin_baudrate:
                    baudRate = Integer.parseInt(selectItem);
                    Toast.makeText(getApplicationContext(), String.format("波特率:%d", baudRate), Toast.LENGTH_LONG).show();
                    break;
                case R.id.spin_databit:
                    int tDataBit = Integer.parseInt(selectItem);
                    dataBit = (byte) tDataBit;
                    Toast.makeText(getApplicationContext(), String.format("数据位:%d", dataBit), Toast.LENGTH_LONG).show();
                    break;
                case R.id.spin_stopbit:
                    int tStopBit = Integer.parseInt(selectItem);
                    dataBit = (byte) tStopBit;
                    Toast.makeText(getApplicationContext(), String.format("停止位:%d", stopBit), Toast.LENGTH_LONG).show();
                    break;
                case R.id.spin_parity:
                    if (selectItem == "无") {
                        parity = 0;
                    } else if (selectItem == "奇校验") {
                        parity = 1;
                    } else if (selectItem == "偶校验") {
                        parity = 2;
                    }
                    Toast.makeText(getApplicationContext(), String.format("校验位:%d", parity), Toast.LENGTH_LONG).show();
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            String selectItem = "以前是我没得选！";
            Toast.makeText(getApplicationContext(), selectItem, Toast.LENGTH_LONG).show();
        }
    }
    //OnItemSelected监听器
//    private class  ProvOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
//        @Override
//        public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
//
//            //获取选择的项的值
//            String sInfo = adapter.getItemAtPosition(position).toString();
//            Toast.makeText(getApplicationContext(), sInfo, Toast.LENGTH_LONG).show();
//        }
//
//        @Override
//        public void onNothingSelected(AdapterView<?> arg0) {
//            String sInfo = "什么也没选！";
//            Toast.makeText(getApplicationContext(), sInfo, Toast.LENGTH_LONG).show();
//
//        }
//    }
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
//            Toast.makeText(this, "按下了back键   onKeyDown()", Toast.LENGTH_SHORT).show();
//            return false;
//        }else {
//            return super.onKeyDown(keyCode, event);
//        }
//    }
}
