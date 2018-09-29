package com.meibaa.zab;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.meibaa.zab.face.FaceActivity;
import com.meibaa.zab.face.LiveCheckActivity;
import com.meibaa.zab.gesture.SignatureActivity;
import com.meibaa.zab.ninelock.NineLockActivity;
import com.meibaa.zab.speech.ActivityUiDialog;
import com.meibaa.zab.vr.ThreeDActivity;
import com.meibaa.zab.vr.VRActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_startSignature:
                startActivityForResult(new Intent(MainActivity.this, SignatureActivity.class), 1);
                break;
            case R.id.btn_nine_lock:
                startActivity(new Intent(this, NineLockActivity.class));
                break;
            case R.id.btn_face_check:
//                Intent intent = getPackageManager().getLaunchIntentForPackage("com.meibaa.face.facedemo");
                startActivity(new Intent(this, FaceActivity.class));
                break;
            case R.id.btn_live_check:
                startActivity(new Intent(this, LiveCheckActivity.class));
                break;
            case R.id.btn_speech_check:
                startActivity(new Intent(this, ActivityUiDialog.class));
                break;
            case R.id.btn_hot_fix:

                break;
            case R.id.btn_3d:
                startActivity(new Intent(this, ThreeDActivity.class));
                break;
            case R.id.btn_vr:
                startActivity(new Intent(this, VRActivity.class));
                break;
        }
    }
}
