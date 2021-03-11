package com.example.loraassistant.Fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.loraassistant.MainActivity;
import com.example.loraassistant.R;
import com.example.loraassistant.SerialActivity;

import org.w3c.dom.Text;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class Fragment_Setting extends Fragment implements View.OnClickListener {

    private RelativeLayout mRl_wifi;
    private RelativeLayout mRl_serial;
    private RelativeLayout mRl_ble;

    private Context mContext;
    private AlertDialog alert;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initUI();
    }

    private void initUI() {
        mRl_wifi = getActivity().findViewById(R.id.rl_wifi);
        mRl_ble = getActivity().findViewById(R.id.rl_ble);
        mRl_serial = getActivity().findViewById(R.id.rl_serial);

        mRl_wifi.setOnClickListener(this);
        mRl_ble.setOnClickListener(this);
        mRl_serial.setOnClickListener(this);

        //加粗
        TextView mTv1 = getActivity().findViewById(R.id.tv_1);
        TextPaint paint = mTv1.getPaint();
        paint.setFakeBoldText(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_wifi:
                showWiFiDialog();
//                showAddDialog();
                break;
            case R.id.rl_ble:
                break;
            case R.id.rl_serial:
                Intent intent = new Intent();
                intent.setClass(getActivity(), SerialActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    protected void showAddDialog() {
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View textEntryView = factory.inflate(R.layout.dialog_wifi, null);
        final EditText editTextName = (EditText) textEntryView
                .findViewById(R.id.et_name);
        final EditText editTextPswd = (EditText) textEntryView
                .findViewById(R.id.et_pswd);
        AlertDialog.Builder ad1 = new AlertDialog.Builder(getActivity());
        ad1.setTitle("需要连接的WiFi");
        ad1.setView(textEntryView);
        ad1.setPositiveButton("连接", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                Toast.makeText(getActivity(), "连接", Toast.LENGTH_SHORT).show();
            }
        });
        ad1.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                Toast.makeText(getActivity(), "取消", Toast.LENGTH_SHORT).show();
            }
        });
        ad1.show();// 显示对话框
    }

    private void showWiFiDialog() {
        final SweetAlertDialog wifiDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE);
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View textEntryView = factory.inflate(R.layout.dialog_wifi, null);
        final EditText editTextName = (EditText) textEntryView
                .findViewById(R.id.et_name);
        final EditText editTextPswd = (EditText) textEntryView
                .findViewById(R.id.et_pswd);
        wifiDialog.setCustomView(textEntryView);
        wifiDialog.setCancelable(false);
        wifiDialog.setCancelText("取消")
                .setConfirmText("连接")
                .setTitleText("连接WiFi");
        wifiDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                Toast.makeText(getActivity(), "连接", Toast.LENGTH_SHORT).show();
            }
        }).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                Toast.makeText(getActivity(), "取消", Toast.LENGTH_SHORT).show();
                wifiDialog.cancel();
            }
        });
        wifiDialog.show();
    }
}
