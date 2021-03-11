package com.example.loraassistant.Fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.loraassistant.CtrlActivity;
import com.example.loraassistant.R;

public class Fragment_Home extends Fragment implements View.OnClickListener {

    private ImageView mImgLed1, mImgLed2, mImgLed3, mImgLed4;

    private boolean led_state = false;


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

        OnClick onClick = new OnClick();
        getActivity().findViewById(R.id.Img_key_on1).setOnClickListener(onClick);
        getActivity().findViewById(R.id.Img_key_on2).setOnClickListener(onClick);
        getActivity().findViewById(R.id.Img_key_on3).setOnClickListener(onClick);
        getActivity().findViewById(R.id.Img_key_on4).setOnClickListener(onClick);
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

    private class OnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Img_key_on1:
                    if (led_state)
                        mImgLed1.setImageResource(R.mipmap.led_off);
                    else
                        mImgLed1.setImageResource(R.mipmap.led_on);
                    led_state = !led_state;
                    break;
                case R.id.Img_key_on2:
                    break;
                case R.id.Img_key_on3:
                    break;
                case R.id.Img_key_on4:
                    break;
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
}
