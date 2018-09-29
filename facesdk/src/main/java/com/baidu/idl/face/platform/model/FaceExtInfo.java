package com.baidu.idl.face.platform.model;

import android.graphics.Point;
import android.graphics.Rect;

import com.baidu.idl.facesdk.FaceInfo;

import java.util.HashMap;

/**
 * 人脸数据对象
 */
public class FaceExtInfo {

    private int mWidth;
    private int mCentery;
    private int mCenterx;
    private float mConf;
    public int[] landmarks;
    private int faceId;
    private float[] headPose;
    private int[] isLive;

    public FaceExtInfo() {
    }

    public void addFaceInfo(FaceInfo info) {
        this.mWidth = info.mWidth;
        this.mCentery = info.mCenter_y;
        this.mCenterx = info.mCenter_x;
        this.mConf = info.mConf;
        this.landmarks = info.landmarks;
        this.faceId = info.face_id;
        this.headPose = info.headPose;
        this.isLive = info.is_live;
    }

    public int getFaceId() {
        return faceId;
    }

    public boolean isLiveEye() {
        return this.isLive != null && this.isLive.length == 11 ? 1 == this.isLive[0] : false;
    }

    public boolean isLiveMouth() {
        return this.isLive != null && this.isLive.length == 11 ? 1 == this.isLive[3] : false;
    }

    public boolean isLiveHeadTurnLeft() {
        return this.isLive != null && this.isLive.length == 11 ? 1 == this.isLive[5] : false;
    }

    public boolean isLiveHeadTurnRight() {
        return this.isLive != null && this.isLive.length == 11 ? 1 == this.isLive[6] : false;
    }

    public boolean isLiveHeadTurnLeftOrRight() {
        return this.isLive != null && this.isLive.length == 11
                ? (1 == this.isLive[5] || 1 == this.isLive[6]) : false;
    }

    public boolean isLiveHeadUp() {
        return this.isLive != null && this.isLive.length == 11 ? 1 == this.isLive[8] : false;
    }

    public boolean isLiveHeadDown() {
        return this.isLive != null && this.isLive.length == 11 ? 1 == this.isLive[9] : false;
    }

    // 人脸区域
    public Rect getFaceRect() {
        Rect rect = new Rect(
                mCenterx - mWidth / 2,
                mCentery - mWidth / 2,
                mWidth,
                mWidth);
        return rect;
    }

    private HashMap<String, Point[]> facePointMap;

    // 人脸宽度
    public int getFaceWidth() {
        return mWidth;
    }

    // 头部姿态
    // pitch 低头仰头角度
    // yaw 侧脸
    // roll平面内旋转

    private float mPitch;
    private float mYaw;
    private float mRoll;

    public float getPitch() {
        mPitch = headPose[0];
        if (headPose != null && headPose.length > 0) {
            mPitch = headPose[0];
        }
        return mPitch;
    }

    public float getYaw() {
        if (headPose != null && headPose.length > 1) {
            mYaw = headPose[1];
        }
        return mYaw;
    }

    public float getRoll() {
        if (headPose != null && headPose.length > 2) {
            mRoll = headPose[2];
        }
        return mRoll;
    }

    // 置信度
    public float getConfidence() {
        return mConf;
    }

    // 取得活体状态
    public int[] getLiveInfo() {
        return isLive;
    }

    // 取得人脸在跟踪框外的关键点数量
    private static int nComponents = 9;
    private static int[] comp1 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    private static int[] comp2 = {13, 14, 15, 16, 17, 18, 19, 20, 13, 21};
    private static int[] comp3 = {22, 23, 24, 25, 26, 27, 28, 29, 22};
    private static int[] comp4 = {30, 31, 32, 33, 34, 35, 36, 37, 30, 38};
    private static int[] comp5 = {39, 40, 41, 42, 43, 44, 45, 46, 39};
    private static int[] comp6 = {47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 47};
    private static int[] comp7 = {51, 57, 52};
    private static int[] comp8 = {58, 59, 60, 61, 62, 63, 64, 65, 58};
    private static int[] comp9 = {58, 66, 67, 68, 62, 69, 70, 71, 58};
    private static int[] nPoints = {13, 10, 9, 10, 9, 11, 3, 9, 9};

    public int getLandmarksOutOfDetectCount(Rect detectRect) {
        float ratioX = 1;
        float ratioY = 1;
        int outCount = 0;
        if (landmarks.length == 144) {
            int[][] idx = {comp1, comp2, comp3, comp4, comp5, comp6, comp7, comp8, comp9};
            float[] positionArr = new float[4];
            for (int i = 0; i < nComponents; ++i) {
                for (int j = 0; j < nPoints[i] - 1; ++j) {
                    positionArr[0] = landmarks[idx[i][j] << 1];
                    positionArr[1] = landmarks[1 + (idx[i][j] << 1)];
                    positionArr[2] = landmarks[idx[i][j + 1] << 1];
                    positionArr[3] = landmarks[1 + (idx[i][j + 1] << 1)];

                    if (!detectRect.contains((int) (positionArr[0] * ratioX), (int) (positionArr[1] * ratioY))) {
                        outCount++;
                    }
                    if (!detectRect.contains((int) (positionArr[2] * ratioX), (int) (positionArr[3] * ratioY))) {
                        outCount++;
                    }
                }
            }
        }
        return outCount;
    }

    public boolean isOutofDetectRect(Rect detectRect) {
        Rect rect = getFaceRect();
        return detectRect.contains(rect);
    }
}
