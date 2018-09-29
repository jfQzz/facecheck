package com.meibaa.zab.gesture;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.meibaa.zab.R;

import java.io.File;
import java.io.IOException;

/**
 * @author shuang
 * @date 2016/11/3
 */

public class SignatureActivity extends Activity {
    private static final String TAG = SignatureActivity.class.getSimpleName();
    private GestureSignatureView mMSignature;
    public static String signaturePath= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "signature.png";
    private TextView tvPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.active_signature);
        mMSignature = (GestureSignatureView) findViewById(R.id.gsv_signature);
        tvPath = (TextView) findViewById(R.id.tv_path);

        findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMSignature.clear();
            }
        });
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        tvPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
                intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/png");
                startActivity(intentToPickPic);
            }
        });
        findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mMSignature.getTouched()) {
                    Toast.makeText(SignatureActivity.this," 请输入签名",Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    mMSignature.save(signaturePath);
                    tvPath.setVisibility(View.VISIBLE);
                    tvPath.setText("图片保存路径\t"+signaturePath+"\t\t点击查看");
                    Toast.makeText(SignatureActivity.this,"保存成功",Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    tvPath.setVisibility(View.GONE);
                    Toast.makeText(SignatureActivity.this,"保存失败",Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }
}
