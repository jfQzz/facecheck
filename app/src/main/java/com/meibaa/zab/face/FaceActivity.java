package com.meibaa.zab.face;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.meibaa.zab.R;
import com.meibaa.zab.face.utils.PreferencesUtil;

public class FaceActivity extends AppCompatActivity implements View.OnClickListener{
    private Button regBtn;
    private Button login1Btn;
    private Button detectedBtn;
    private Button resetBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face);
        findView();
        addListener();
    }

    private void findView() {
        regBtn = (Button) findViewById(R.id.reg_btn);
        login1Btn = (Button) findViewById(R.id.login1_btn);
        detectedBtn = (Button) findViewById(R.id.detected_btn);
        resetBtn = (Button) findViewById(R.id.reset_btn);
    }

    private void addListener() {
        regBtn.setOnClickListener(this);
        login1Btn.setOnClickListener(this);
        detectedBtn.setOnClickListener(this);
        resetBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
            return;
        }
        if (regBtn == v) {
            Intent intent = new Intent(this, RegActivity.class);
            startActivity(intent);
        } else if (login1Btn == v) {
            // TODO 实时人脸检测
            Intent intent = new Intent(this, DetectLoginActivity.class);
            startActivity(intent);
        } else if (detectedBtn == v) {
            Intent intent = new Intent(this, VerifyLoginActivity.class);
            startActivity(intent);
        } else if (resetBtn == v) {
            PreferencesUtil.initPrefs(getApplicationContext());
            PreferencesUtil.remove("username");
        }else finish();
    }
}
