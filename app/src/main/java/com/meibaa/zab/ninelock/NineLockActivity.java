package com.meibaa.zab.ninelock;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.meibaa.zab.R;
import com.meibaa.zab.util.PasswordUtil;
import com.meibaa.zab.util.StringUtils;

public class NineLockActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nine_lock2);
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.iv_back:
                finish();
                break;
            case R.id.btn_set_psw:

    actionSecondActivity(LockMode.SETTING_PASSWORD);
                break;
            case R.id.btn_confirm_psw:

    actionSecondActivity(LockMode.VERIFY_PASSWORD);
                break;
            case R.id.btn_modify_psw:

    actionSecondActivity(LockMode.EDIT_PASSWORD);
                break;
            case R.id.btn_clear_psw:

            actionSecondActivity(LockMode.CLEAR_PASSWORD);
                break;
        }
    }

    /**
     * 跳转到密码处理界面
     */
    private void actionSecondActivity(LockMode mode) {
        if (mode != LockMode.SETTING_PASSWORD) {
            if (StringUtils.isEmpty(PasswordUtil.getPin(this))) {
                Toast.makeText(getBaseContext(), "请先设置密码", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Intent intent = new Intent(this, NineLockFunctionActivity.class);
        intent.putExtra(Contants.INTENT_SECONDACTIVITY_KEY, mode);
        startActivity(intent);
    }

}
