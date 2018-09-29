package com.meibaa.zab.face;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.meibaa.zab.R;
import com.meibaa.zab.face.exception.FaceError;
import com.meibaa.zab.face.model.AccessToken;
import com.meibaa.zab.face.model.LivenessVsIdcardResult;
import com.meibaa.zab.face.utils.OnResultListener;

import java.io.File;

public class LiveCheckActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int OFFLINE_FACE_LIVENESS_REQUEST = 100;

    private String username;
    private String idnumber;

    private TextView resultTipTV;
    // private TextView onlineFacelivenessTipTV;
    private TextView scoreTV;
    private ImageView avatarIv;
    private Button retBtn;
    private String filePath;
    private boolean policeVerifyFinish = false;
    private boolean waitAccesstoken = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_check);

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            idnumber = intent.getStringExtra("idnumber");
        }

        resultTipTV = (TextView) findViewById(R.id.result_tip_tv);
        // onlineFacelivenessTipTV = (TextView) findViewById(R.id.online_faceliveness_tip_tv);
        scoreTV = (TextView) findViewById(R.id.score_tv);
        avatarIv = (ImageView) findViewById(R.id.avatar_iv);
        retBtn = (Button) findViewById(R.id.retry_btn);
        retBtn.setOnClickListener(this);


        initAccessToken();
        // 打开离线活体检测
        Intent faceLivenessintent = new Intent(this, OfflineFaceLivenessActivity.class);
        startActivityForResult(faceLivenessintent, OFFLINE_FACE_LIVENESS_REQUEST);
    }

    @Override
    public void onClick(View v) {
        if (v == retBtn) {
            // 打开离线活体检测
            Intent faceLivenessintent = new Intent(this, OfflineFaceLivenessActivity.class);
            startActivityForResult(faceLivenessintent, OFFLINE_FACE_LIVENESS_REQUEST);
        }else finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 检测完成后 开始在线活体检测和公安核实
        if (requestCode == OFFLINE_FACE_LIVENESS_REQUEST && data != null) {
            filePath = data.getStringExtra("bestimage_path");
            if (TextUtils.isEmpty(filePath)) {
                Toast.makeText(this, "离线活体图片没找到", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            avatarIv.setImageBitmap(bitmap);
//            policeVerify(filePath);
        } else {
            finish();
        }
    }

    // 在线活体检测和公安核实需要使用该token，为了防止ak、sk泄露，建议在线活体检测和公安接口在您的服务端请求
    private void initAccessToken() {

        displayTip(resultTipTV, "活体检测成功");
        APIService.getInstance().init(getApplicationContext());
        APIService.getInstance().initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                if (result != null && !TextUtils.isEmpty(result.getAccessToken())) {
                    waitAccesstoken = false;
                    policeVerify(filePath);
                } else if (result != null) {
                    displayTip(resultTipTV, "在线活体token获取失败");
                    retBtn.setVisibility(View.VISIBLE);
                } else {
                    displayTip(resultTipTV, "在线活体token获取失败");
                    retBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(FaceError error) {
                displayTip(resultTipTV, "在线活体token获取失败");
                retBtn.setVisibility(View.VISIBLE);
                error.printStackTrace();

            }

        }, this,Config.apiKey, Config.secretKey);
    }

    /**
     * 公安接口合并在线活体，调用公安验证接口进行最后的核身比对；公安权限需要在官网控制台提交工单开启
     * 接口地址：https://aip.baidubce.com/rest/2.0/face/v2/person/verify
     * 入参为「姓名」「身份证号」「bestimage」
     * ext_fields 扩展功能。如 faceliveness 表示返回活体值, qualities 表示返回质检测结果
     * quality string 判断质 是否达标。“use” 表示做质 控制,质  好的照 会 直接拒绝
     * faceliveness string 判断活体值是否达标。 use 表示做活体控制,低于活体阈值的 照 会直接拒绝
     * quality_conf和faceliveness_conf 用于指定阈值，超过此分数才调用公安验证，
     *
     * @param filePath
     */
    private void policeVerify(String filePath) {
        if (TextUtils.isEmpty(filePath) || waitAccesstoken) {
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }

        displayTip(resultTipTV, "公安身份核实中...");
        APIService.getInstance().policeVerify(username, idnumber, filePath, new
                OnResultListener<LivenessVsIdcardResult>() {
                    @Override
                    public void onResult(LivenessVsIdcardResult result) {
                        if (result != null && result.getScore() >= 80) {
                            delete();
                            displayTip(resultTipTV, "核身成功");
                            displayTip(scoreTV, "公安验证分数：" + result.getScore());
                        } else {
                            displayTip(resultTipTV, "核身失败");
                            displayTip(scoreTV, "公安验证分数过低：" + result.getScore());
                            retBtn.setVisibility(View.VISIBLE);
                        }
                    }


                    @Override
                    public void onError(FaceError error) {
                        delete();
                        // TODO 错误处理
                        // 如返回错误码为：216600，则核身失败，提示信息为：身份证号码错误
                        // 如返回错误码为：216601，则核身失败，提示信息为：身份证号码与姓名不匹配
                        Toast.makeText(LiveCheckActivity.this,
                                "公安身份核实失败:" + error.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                        retBtn.setVisibility(View.VISIBLE);

                    }
                }
        );
    }

    private void delete() {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    private void displayTip(final TextView textView, final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (textView != null) {
                    textView.setText(tip);
                }
            }
        });
    }

}