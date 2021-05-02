package com.example.loraassistant.Common;

import android.app.Application;
import android.os.Handler;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;

public class MyApp extends Application {
    public static CH34xUARTDriver ch34x_driver;
    public static Handler ch34x_read_handler;
    public static String uart_device_name = null;
    public static boolean isUartEnable = false;
}
