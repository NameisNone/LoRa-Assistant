package com.example.loraassistant.Adapter;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

public class Alarm {
    private Calendar calendar;//定时时间
    private boolean LedState;//灯状态，true：开 false：关
    private boolean Period;//执行周期,true：执行一次 false：每天执行
    private int LedLight;//灯亮度

    public Alarm(int hour,int min,boolean period,boolean ledState,int ledLight){
        this.calendar = Calendar.getInstance();
        setTime(hour,min);
        this.Period = period;
        this.LedState = ledState;
        this.LedLight = ledLight;
    }

    //设置时间
    public void setTime(int hour, int min) {
        this.calendar.set(Calendar.YEAR,0);
        this.calendar.set(Calendar.MONTH,0);
        this.calendar.set(Calendar.DAY_OF_MONTH,0);
        this.calendar.set(Calendar.HOUR_OF_DAY, hour);
        this.calendar.set(Calendar.MINUTE, min);
        this.calendar.set(Calendar.SECOND,0);
    }

    //设置灯的状态 true：开 false：关
    public void setLedState(boolean state) {
        this.LedState = state;
    }

    //设置执行周期，true：执行一次 false：每天执行
    public void setPeriod(boolean period) {
        this.Period = period;
    }

    //设置灯的亮度
    public void setLightValue(int value) {
        this.LedLight = value;
    }


    public int getHour(){
        return this.calendar.get(Calendar.HOUR_OF_DAY);
    }

    public int getMin(){
        return this.calendar.get(Calendar.MINUTE);
    }
    //获取定时时间
    public String getTimeString(){
        return String.format("%02d:%02d",this.calendar.get(Calendar.HOUR_OF_DAY),this.calendar.get(Calendar.MINUTE));
    }
    //获取执行周期，执行一次/每天执行
    public boolean getPeriod(){
        return this.Period;
    }
    //获取led执行的动作，开或关
    public boolean getLedState(){
        return this.LedState;
    }
    //获取led的当前亮度
    public int getLedLight(){
        return this.LedLight;
    }

}
