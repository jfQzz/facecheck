/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.meibaa.zab.face.utils;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.baidu.idl.facesdk.FaceInfo;

public class FaceCropper {

    public static void adjustRect(int[] argb, int width, Rect rect) {
        rect.left = Math.max(rect.left ,0);
        rect.right = Math.min(rect.right,width);
        int height = argb.length / width;
        rect.bottom = Math.min(rect.bottom,height);
        rect.sort();
    }

    public static int[] crop(int[] argb, int width, Rect rect) {
        adjustRect(argb, width, rect);
        int[] image = new int[rect.width() * rect.height()];

        for (int i = rect.top; i < rect.bottom; i++) {
            int rowIndex = width * i;
            try {
                System.arraycopy(argb, rowIndex + rect.left, image, rect.width() * (i - rect.top), rect.width());
            } catch (Exception e) {
                e.printStackTrace();
                return argb;
            }
        }
        return image;
    }

    public static Bitmap getFace(int[] argb, FaceInfo faceInfo, int imageWidth) {
        int[] points = new int[8];

        faceInfo.getRectPoints(points);

        int left = points[2];
        int top = points[3];
        int right = points[6];
        int bottom = points[7];

        int width = right - left;
        int height = bottom - top;

        width = width * 3 / 2;
        height = height * 2;
        //
        left = faceInfo.mCenter_x - width / 2;
        top = faceInfo.mCenter_y - height / 2;

        height = height * 4 / 5;
        //
        left = Math.max(left, 0);
        top = Math.max(top, 0);

        Rect region = new Rect(left, top, left + width, top + height);
        FaceCropper.adjustRect(argb, imageWidth, region);
        int offset = region.top * imageWidth + region.left;
        Bitmap face = Bitmap.createBitmap(argb,offset,imageWidth,region.width(),region.height(),
                Bitmap.Config.ARGB_8888);
        return face;
    }
}
