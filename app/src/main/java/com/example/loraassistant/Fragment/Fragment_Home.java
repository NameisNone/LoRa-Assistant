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

import com.example.loraassistant.CtrlOneActivity;
import com.example.loraassistant.R;

public class Fragment_Home extends Fragment implements View.OnClickListener {

    private ImageView mImgLed1, mImgLed2, mImgLed3, mImgLed4;
    private IUpdateViewParam listener;

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

        getActivity().findViewById(R.id.Img_key_on1).setOnClickListener(this);
        getActivity().findViewById(R.id.Img_key_on2).setOnClickListener(this);
        getActivity().findViewById(R.id.Img_key_on3).setOnClickListener(this);
        getActivity().findViewById(R.id.Img_key_on4).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_1:
                Intent intent = new Intent();
                intent.setClass(getActivity(), CtrlOneActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("nodename", "节点x");
                intent.putExtras(bundle);
                startActivity(intent);
//                listener.onClick("节点2","file1","file2");
                break;
            case R.id.ll_2:
                break;
            case R.id.ll_3:
                break;
            case R.id.ll_4:
                break;
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
            default:
                break;
        }
    }

    public interface IUpdateViewParam{
        void onClick(String nodeName,String fileName,String listFileName);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        try {
//            listener = (IUpdateViewParam) context;//强转为IUpdateViewParam类型，检查Activity是否实现了该接口
//        }catch(ClassCastException e) {
//            throw new ClassCastException("Activity必须实现 IUpdateViewParam接口");
//        }
    }
}
