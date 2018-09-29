/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.meibaa.zab.face;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.baidu.idl.face.platform.FaceConfig;
import com.baidu.idl.face.platform.FaceEnvironment;
import com.baidu.idl.face.platform.FaceSDKManager;
import com.baidu.idl.face.platform.FaceStatusEnum;
import com.baidu.idl.face.platform.LivenessTypeEnum;
import com.baidu.idl.face.platform.ui.FaceLivenessActivity;
import com.baidu.idl.face.platform.utils.Base64Utils;
import com.baidu.idl.facesdk.FaceInfo;
import com.baidu.idl.facesdk.FaceSDK;
import com.baidu.idl.facesdk.FaceTracker;
import com.meibaa.zab.face.utils.FaceCropper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OfflineFaceLivenessActivity extends FaceLivenessActivity {

    private String bestImagePath;
    private AlertDialog.Builder alertDialog;

    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int PERMISSIONS_REQUEST_CAMERA = 800;
    private static final int PERMISSIONS_EXTERNAL_STORAGE = 801;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFaceSDK();

        alertDialog = new AlertDialog.Builder(this);
        ActivityCompat.requestPermissions(OfflineFaceLivenessActivity.this,
                new String[] {Manifest.permission.CAMERA},
                PERMISSIONS_REQUEST_CAMERA);
    }

    /**
     * 初始化SDK
     */
    private void initFaceSDK() {
        // 第一个参数 应用上下文
        // 第二个参数 licenseID license申请界面查看
        // 第三个参数 assets目录下的License文件名
        FaceSDKManager.getInstance().initialize(this, Config.licenseID, Config.licenseFileName);
        setFaceConfig();
    }

    private void setFaceConfig() {
        FaceConfig config = FaceSDKManager.getInstance().getFaceConfig();
        // SDK初始化已经设置完默认参数（推荐参数），您也根据实际需求进行数值调整
        List<LivenessTypeEnum> livenessList = new ArrayList<>();
        livenessList.add(LivenessTypeEnum.Mouth);
        livenessList.add(LivenessTypeEnum.Eye);
        // livenessList.add(LivenessTypeEnum.HeadUp);
        // livenessList.add(LivenessTypeEnum.HeadDown);
        // livenessList.add(LivenessTypeEnum.HeadLeft);
        // livenessList.add(LivenessTypeEnum.HeadRight);
        config.setLivenessTypeList(livenessList);

        // 设置 活体动作是否随机 boolean
        config.setLivenessRandom(true);
        // 模糊度范围 (0-1) 推荐小于0.7
        config.setBlurnessValue(FaceEnvironment.VALUE_BLURNESS);
        // 光照范围 (0-1) 推荐大于40
        config.setBrightnessValue(FaceEnvironment.VALUE_BRIGHTNESS);
        // 裁剪人脸大小
        config.setCropFaceValue(FaceEnvironment.VALUE_CROP_FACE_SIZE);
        // 人脸yaw,pitch,row 角度，范围（-45，45），推荐-15-15
        config.setHeadPitchValue(FaceEnvironment.VALUE_HEAD_PITCH);
        config.setHeadRollValue(FaceEnvironment.VALUE_HEAD_ROLL);
        config.setHeadYawValue(FaceEnvironment.VALUE_HEAD_YAW);
        // 最小检测人脸（在图片人脸能够被检测到最小值）80-200， 越小越耗性能，推荐120-200
        config.setMinFaceSize(FaceEnvironment.VALUE_MIN_FACE_SIZE);
        // 人脸置信度（0-1）推荐大于0.6
        config.setNotFaceValue(FaceEnvironment.VALUE_NOT_FACE_THRESHOLD);
        // 人脸遮挡范围 （0-1） 推荐小于0.5
        config.setOcclusionValue(FaceEnvironment.VALUE_OCCLUSION);
        // 是否进行质量检测
        config.setCheckFaceQuality(true);
        // 人脸检测使用线程数
        // config.setFaceDecodeNumberOfThreads(4);
        // 是否开启提示音
        config.setSound(true);

        FaceSDKManager.getInstance().setFaceConfig(config);
    }

    @Override
    public void onLivenessCompletion(FaceStatusEnum status, String message, HashMap<String, String> base64ImageMap) {
        super.onLivenessCompletion(status, message, base64ImageMap);
        if (status == FaceStatusEnum.OK && mIsCompletion) {
            // Toast.makeText(this, "活体检测成功", Toast.LENGTH_SHORT).show();
            saveImage(base64ImageMap);
            alertText("检测结果", "活体检测成功");
        } else if (status == FaceStatusEnum.Error_DetectTimeout ||
                status == FaceStatusEnum.Error_LivenessTimeout ||
                status == FaceStatusEnum.Error_Timeout) {
            // Toast.makeText(this, "活体检测采集超时", Toast.LENGTH_SHORT).show();
            alertText("检测结果", "活体检测采集超时");
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    private void saveImage(HashMap<String, String> imageMap) {
        // imageMap 里保存了最佳人脸和各个动作的图片，若对安全要求比较高，可以传多张图片进行在线活体，目前只用最佳人脸进行了在线活体检测
        //        Set<Map.Entry<String, String>> sets = imageMap.entrySet();
        //
        //        Bundle bundle = new Bundle();
        //        Bitmap bmp;
        //        List<File> fileList = new ArrayList<>();
        //        for (Map.Entry<String, String> entry : sets) {
        //            bmp = base64ToBitmap(entry.getValue());
        //            ImageView iv = new ImageView(this);
        //            iv.setImageBitmap(bmp);
        //
        //            try {
        //                File file = File.createTempFile(UUID.randomUUID().toString(), ".jpg");
        //                FileOutputStream outputStream = new FileOutputStream(file);
        //                bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        //                outputStream.close();
        //                fileList.add(new File(file.getAbsolutePath()));
        //
        //            } catch (IOException e) {
        //                e.printStackTrace();
        //            }
        //        }
        //        onlineLiveness(fileList);

        String bestimageBase64 = imageMap.get("bestImage0");
        Bitmap bmp = base64ToBitmap(bestimageBase64);

//        Bitmap newBmp = detect(bmp);
//        if (newBmp == null) {
//            newBmp = bmp;
//        }

        // 如果觉的在线校验慢，可以压缩图片的分辨率，目前没有压缩分辨率，压缩质量置80，在neuxs5上大概30k，后面版本我们将截出人脸部分，大小应该小于10k
        try {
            File file = File.createTempFile("face", ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            outputStream.close();

            bestImagePath = file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap detect(Bitmap bitmap) {

        FaceSDKManager.getInstance().getFaceTracker().clearTrackedFaces();

        int[] argb = new int[bitmap.getHeight() * bitmap.getWidth()];
        bitmap.getPixels(argb, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int value = FaceSDKManager.getInstance().getFaceTracker().prepare_data_for_verify(
                argb, bitmap.getHeight(), bitmap.getWidth(), FaceSDK.ImgType.ARGB.ordinal(),
                FaceTracker.ActionType.RECOGNIZE.ordinal());

        FaceInfo[] faces = FaceSDKManager.getInstance().getFaceTracker().get_TrackedFaceInfo();
        Log.i("detect", value + " faces->" + faces);
        if (faces != null ) {

            FaceInfo faceInfo = faces[0];
            int faceWith = faceInfo.mWidth;
            int centerX = faceInfo.mCenter_x ;
            int centerY = faceInfo.mCenter_y;

                int left = centerX - faceWith / 2;
                int top = centerY - faceWith / 2;
            // int left = 0;
            int right = centerX + faceWith / 2;
            // int right = bitmap.getWidth();
            // int top = centerY - faceWith;
            if (left < 0) {
                 left = 0;
            }
            if (right > bitmap.getWidth()) {
                right = bitmap.getWidth();
            }
            if (top < 0) {
                top = 0;
            }
            int bottom = centerY + faceWith / 2;
            if (bottom > bitmap.getHeight()) {
                bottom = bitmap.getHeight();
            }

            Rect cropRect = new Rect(left, top, right, bottom);
            int[] cropArgb = FaceCropper.crop(argb, bitmap.getWidth(), cropRect);
            Bitmap cropBitmap =
                    Bitmap.createBitmap(cropArgb, 0, cropRect.width(), cropRect.width(), cropRect.height(),
                            Bitmap.Config.ARGB_8888);

            FaceSDKManager.getInstance().getFaceTracker().clearTrackedFaces();
            return cropBitmap;
        }

        return bitmap;
    }

        private void alertText(final String title, final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertDialog.setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Intent intent = new Intent();
                                intent.putExtra("bestimage_path", bestImagePath);
                                setResult(Activity.RESULT_OK, intent);
                                finish();
                            }
                        })
                        .show();
            }
        });
    }

    private static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64Utils.decode(base64Data, Base64Utils.NO_WRAP);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

}