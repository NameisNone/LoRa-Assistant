package com.example.loraassistant.Common;

import android.app.Application;
import android.os.Handler;
import android.util.Log;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;

public class MyApp extends Application {

    public enum LedState {LedOn, LedOff};

    public static CH34xUARTDriver ch34x_driver;
    public static Handler ch34x_read_handler;
    public static String uart_device_name = null;
    public static boolean isUartEnable = false;
    public static boolean isUartReadBuffNull = true;
    public static String ReadBuff;
    public static boolean isBleEnable = false;
    public static boolean isMqttEnable = false;

    /***************灯光控制函数1************/
    /*
    * 参数说明:
    * nodeNum: 节点号 1-4
    * ledState: 灯光状态，打开/关闭
    * brightness: 灯光亮度 0-100
    */
    public static void CtrlLed(int nodeNum, LedState ledState, int brightness){

       if (isUartEnable){
           byte node_num = (byte) nodeNum;
           byte led_state = (byte) ((ledState==LedState.LedOn) ? 0x01 : 0x00);
           byte led_brightness = (byte) brightness;
           if(led_brightness < 0) led_brightness = 0;
           if(led_brightness > 100) led_brightness = 100;
           //封装数据，后面的取反是为了接收端校验确保通信的可靠性
           byte[] TxData = {node_num, 0x01, led_state, led_brightness, (byte) ~led_brightness, (byte) ~led_state, (byte)~0x01, (byte) ~node_num};
           for (int i=0;i<TxData.length;i++) {
               Log.i("TxData",String.format("TxData[%d] = 0x%02x",i,TxData[i]));
           }
           //通过串口通信发送出去
           MyApp.ch34x_driver.WriteData(TxData,TxData.length);
       }else if (isBleEnable){

       }else if (isMqttEnable){

       }
    }
    /***************灯光控制函数2************/
    /*
     * 参数说明:
     * nodeNum: 节点号 1 ~ 4
     * ledState: 灯光状态，打开/关闭
     */
    public static void CtrlLed(int nodeNum, LedState ledState){

        if (isUartEnable){
            byte node_num = (byte) nodeNum;
            byte led_state = (byte) ((ledState==LedState.LedOn) ? 0x01 : 0x00);
            //封装数据，后面的取反是为了接收端校验确保通信的可靠性
            byte[] TxData = {node_num, 0x02, led_state, (byte) ~led_state, (byte)~0x02, (byte) ~node_num};
            for (byte a:TxData) {
                Log.i("TxData",String.format("0x%02x",a));
            }
            //通过串口通信发送出去
            MyApp.ch34x_driver.WriteData(TxData,TxData.length);
        }else if (isBleEnable){

        }else if (isMqttEnable){

        }
    }
    /***************灯光控制函数3************/
    /*
     * 参数说明:
     * nodeNum: 节点号 1 ~ 4
     * brightness: 灯光状态，打开/关闭
     */
    public static void CtrlLed(int nodeNum, int brightness){
        if (isUartEnable){
            byte node_num = (byte) nodeNum;
            byte led_brightness = (byte) brightness;
            //封装数据，后面的取反是为了接收端校验确保通信的可靠性
            byte[] TxData = {node_num, 0x03, led_brightness, (byte) ~led_brightness, (byte)~0x03, (byte) ~node_num};
            for (byte a:TxData) {
                Log.i("TxData",String.format("0x%02x",a));
            }
            //通过串口通信发送出去
            MyApp.ch34x_driver.WriteData(TxData, TxData.length);
        }else if (isBleEnable){

        }else if (isMqttEnable){

        }
    }
}
