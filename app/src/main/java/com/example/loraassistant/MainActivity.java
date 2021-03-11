package com.example.loraassistant;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.example.loraassistant.Fragment.Fragment_Home;
import com.example.loraassistant.Fragment.Fragment_Setting;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    private Fragment_Home fg_home;
    private Fragment_Setting fg_setting;

    private FragmentManager fragmentManager;

    private RadioGroup mRg_tab;
    private RadioButton mRb_home;
    private RadioButton mRb_setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getFragmentManager();
        initUI();
    }

    private void initUI() {
        mRg_tab = findViewById(R.id.rg_tab);
        mRg_tab.setOnCheckedChangeListener(this);

        //选中home RadioButton会触发onCheckedChanged事件
        mRb_home = findViewById(R.id.rb_home);
        mRb_home.setChecked(true);
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        hideAllFragment(fragmentTransaction);
        switch (checkedId) {
            case R.id.rb_home:
                if (fg_home == null) {
                    fg_home = new Fragment_Home();
                    fragmentTransaction.add(R.id.FrameLayout_content, fg_home);//2、add
                } else {
                    fragmentTransaction.show(fg_home);
                }
                break;
            case R.id.rb_setting:
                if (fg_setting == null) {
                    fg_setting = new Fragment_Setting();
                    fragmentTransaction.add(R.id.FrameLayout_content, fg_setting);//2、add
                } else {
                    fragmentTransaction.show(fg_setting);
                }
                break;
            default:
                break;
        }
        fragmentTransaction.commitAllowingStateLoss();
    }

    private void hideAllFragment(FragmentTransaction fragmentTransaction) {
        if (fg_home != null) fragmentTransaction.hide(fg_home);
        if (fg_setting != null) fragmentTransaction.hide(fg_setting);
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        // 当按下返回键时所执行的命令
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            // 此处写你按返回键之后要执行的事件的逻辑
//
//            return super.onKeyDown(keyCode, event);
//        }
//        return super.onKeyDown(keyCode, event);
//    }
}
