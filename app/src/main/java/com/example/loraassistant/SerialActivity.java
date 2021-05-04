package com.example.loraassistant;

import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.loraassistant.Common.MyApp;

public class SerialActivity extends AppCompatActivity {

    private static final String ACTION_USB_PERMISSION = "com.example.loraassistant.USB_PERMISSION";
    private Spinner mspin_baudrate;
    private Spinner mspin_databit;
    private Spinner mspin_stopbit;
    private Spinner mspin_parity;

    private SpinnerOnItemSelectedListener mSpinnerOnItemSelectedListener;

    /*波特率 数据位 停止位 奇偶校验 流控*/
    private static int baudRate=115200;       //波特率
    private static byte dataBit=8;           //数据位
    private static byte stopBit=1;           //停止位
    private static byte parity=0;            //校验
    private static byte flowControl=0;       //流控


    private Button mBtn_send; //发送按钮
    private Button mBtn_clear;//清除按钮
    private OnClick onClick;

    private boolean isOpen = false;
    private EditText mEt_write;
    private TextView mTv_read;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial);

        initUI();
        //启动一个线程读取接收到的数据
        new Thread(){
            @Override
            public void run() {
                super.run();
                while (true){
                    if(!MyApp.isUartReadBuffNull){
                        String RxBuf = MyApp.ReadBuff.toString();
                        if(mTv_read.getText().length() > 0){
                                mTv_read.append("\r\n" + RxBuf);
                            }else{
                                mTv_read.setText(RxBuf);
                        }
                        MyApp.isUartReadBuffNull = true;
                        MyApp.ReadBuff = new String("");
                    }
                }
            }
        }.start();
    }

    private void initUI() {
        mSpinnerOnItemSelectedListener = new SpinnerOnItemSelectedListener();

        mspin_baudrate = findViewById(R.id.spin_baudrate);
        mspin_baudrate.setSelection(6,true);
        mspin_baudrate.setOnItemSelectedListener(mSpinnerOnItemSelectedListener);

        mspin_databit = findViewById(R.id.spin_databit);
        mspin_databit.setSelection(3,true);
        mspin_databit.setOnItemSelectedListener(mSpinnerOnItemSelectedListener);

        mspin_stopbit = findViewById(R.id.spin_stopbit);
        mspin_stopbit.setSelection(0,true);
        mspin_stopbit.setOnItemSelectedListener(mSpinnerOnItemSelectedListener);

        mspin_parity = findViewById(R.id.spin_parity);
        mspin_parity.setSelection(0,true);
        mspin_parity.setOnItemSelectedListener(mSpinnerOnItemSelectedListener);

        mEt_write = findViewById(R.id.et_send);
        mTv_read  = findViewById(R.id.tv_recv);
        mTv_read.setMovementMethod(ScrollingMovementMethod.getInstance());

        onClick = new OnClick();
        mBtn_send = findViewById(R.id.send_btn);
        mBtn_send.setOnClickListener(onClick);

        mBtn_clear = findViewById(R.id.clear_btn);
        mBtn_clear.setOnClickListener(onClick);
    }

    private class OnClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.send_btn:
                    CH34xWriteData();
                    break;
                case R.id.clear_btn:
                    mTv_read.setText("");
                    break;
            }
        }
    }

    //发送数据
    private void CH34xWriteData(){
        if(MyApp.isUartEnable){
            byte[] to_send = toByteArray2(mEt_write.getText().toString());		//以字符串方式发送
//        Toast.makeText(getApplicationContext(), String.format("0x%02x",to_send[0]), Toast.LENGTH_SHORT).show();
            int retval = MyApp.ch34x_driver.WriteData(to_send, to_send.length);//写数据，第一个参数为需要发送的字节数组，第二个参数为需要发送的字节长度，返回实际发送的字节长度
            if (retval < 0) {
                Toast.makeText(getApplicationContext(), "发送失败", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(), "发送成功", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //OnItemSelected监听器
    private class SpinnerOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String selectItem = parent.getItemAtPosition(position).toString();
//            Toast.makeText(getApplicationContext(), selectItem, Toast.LENGTH_LONG).show();
            switch (parent.getId()) {
                case R.id.spin_baudrate:
                    baudRate = Integer.parseInt(selectItem);
//                    Toast.makeText(getApplicationContext(), String.format("波特率:%d", baudRate), Toast.LENGTH_LONG).show();
                    break;
                case R.id.spin_databit:
                    int tDataBit = Integer.parseInt(selectItem);
                    dataBit = (byte) tDataBit;
//                    Toast.makeText(getApplicationContext(), String.format("数据位:%d", dataBit), Toast.LENGTH_LONG).show();
                    break;
                case R.id.spin_stopbit:
                    int tStopBit = Integer.parseInt(selectItem);
                    dataBit = (byte) tStopBit;
//                    Toast.makeText(getApplicationContext(), String.format("停止位:%d", stopBit), Toast.LENGTH_LONG).show();
                    break;
                case R.id.spin_parity:
                    if (selectItem == "无") {
                        parity = 0;
                    } else if (selectItem == "奇校验") {
                        parity = 1;
                    } else if (selectItem == "偶校验") {
                        parity = 2;
                    }
//                    Toast.makeText(getApplicationContext(), String.format("校验位:%d", parity), Toast.LENGTH_LONG).show();
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
//            String selectItem = "以前是我没得选！";
//            Toast.makeText(getApplicationContext(), selectItem, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 将byte[]数组转化为String类型
     * @param arg
     *            需要转换的byte[]数组
     * @param length
     *            需要转换的数组长度
     * @return 转换后的String队形
     */
    private String toHexString(byte[] arg, int length) {
        String result = new String();
        if (arg != null) {
            for (int i = 0; i < length; i++) {
                result = result
                        + (Integer.toHexString(
                        arg[i] < 0 ? arg[i] + 256 : arg[i]).length() == 1 ? "0"
                        + Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])
                        : Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])) + " ";
            }
            return result;
        }
        return "";
    }

    /**
     * 将String转化为byte[]数组
     * @param arg
     *            需要转换的String对象
     * @return 转换后的byte[]数组
     */
    private byte[] toByteArray(String arg) {
        if (arg != null) {
            /* 1.先去除String中的' '，然后将String转换为char数组 */
            char[] NewArray = new char[1000];
            char[] array = arg.toCharArray();
            int length = 0;
            for (int i = 0; i < array.length; i++) {
                if (array[i] != ' ') {
                    NewArray[length] = array[i];
                    length++;
                }
            }
            /* 将char数组中的值转成一个实际的十进制数组 */
            int EvenLength = (length % 2 == 0) ? length : length + 1;
            if (EvenLength != 0) {
                int[] data = new int[EvenLength];
                data[EvenLength - 1] = 0;
                for (int i = 0; i < length; i++) {
                    if (NewArray[i] >= '0' && NewArray[i] <= '9') {
                        data[i] = NewArray[i] - '0';
                    } else if (NewArray[i] >= 'a' && NewArray[i] <= 'f') {
                        data[i] = NewArray[i] - 'a' + 10;
                    } else if (NewArray[i] >= 'A' && NewArray[i] <= 'F') {
                        data[i] = NewArray[i] - 'A' + 10;
                    }
                }
                /* 将 每个char的值每两个组成一个16进制数据 */
                byte[] byteArray = new byte[EvenLength / 2];
                for (int i = 0; i < EvenLength / 2; i++) {
                    byteArray[i] = (byte) (data[i * 2] * 16 + data[i * 2 + 1]);
                }
                return byteArray;
            }
        }
        return new byte[] {};
    }

    /**
     * 将String转化为byte[]数组
     * @param arg
     *            需要转换的String对象
     * @return 转换后的byte[]数组
     */
    private byte[] toByteArray2(String arg) {
        if (arg != null) {
            /* 1.先去除String中的' '，然后将String转换为char数组 */
            char[] NewArray = new char[1000];
            char[] array = arg.toCharArray();
            int length = 0;
            for (int i = 0; i < array.length; i++) {
                if (array[i] != ' ') {
                    NewArray[length] = array[i];
                    length++;
                }
            }

            byte[] byteArray = new byte[length];
            for (int i = 0; i < length; i++) {
                byteArray[i] = (byte)NewArray[i];
            }
            return byteArray;

        }
        return new byte[] {};
    }
}
