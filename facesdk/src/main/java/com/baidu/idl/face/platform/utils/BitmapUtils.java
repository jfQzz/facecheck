/**
 * Copyright (C) 2017 Baidu Inc. All rights reserved.
 */

package com.baidu.idl.face.platform.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 这个类提供一些操作Bitmap的方法
 */
public final class BitmapUtils {
    /**
     * Log TAG
     */
    private static final String TAG = "ImageUtils";
    /**
     * 保存图片的质量：100
     */
    private static final int QUALITY = 100;
    /**
     * 图像的旋转方向是0
     */
    public static final int ROTATE0 = 0;
    /**
     * 图像的旋转方向是90
     */
    public static final int ROTATE90 = 90;
    /**
     * 图像的旋转方向是180
     */
    public static final int ROTATE180 = 180;
    /**
     * 图像的旋转方向是270
     */
    public static final int ROTATE270 = 270;
    /**
     * 图像的旋转方向是360
     */
    public static final int ROTATE360 = 360;
    /**
     * 图片太大内存溢出后压缩的比例
     */
    public static final int PIC_COMPRESS_SIZE = 4;
    /**
     * 图像压缩边界
     */
    public static final int IMAGEBOUND = 128;
    /**
     * 图片显示最大边的像素
     */
    public static final int MAXLENTH = 1024;
    /**
     * 默认的最大尺寸
     */
    private static final int DEFAULT_MAX_SIZE_CELL_NETWORK = 600;
    /**
     * 题编辑wifi环境下压缩的最大尺寸
     */
    private static final int QUESTION_MAX_SIZE_CELL_NETWORK = 1024;
    /**
     * 图片压缩的质量
     */
    private static final int QUESTION_IMAGE_JPG_QUALITY = 75;
    /**
     * 默认的图片压缩的质量
     */
    private static final int DEFAULT_IMAGE_JPG_QUALITY = 50;
    /**
     * 网络请求超时时间
     */
    private static final int CONNECTTIMEOUT = 3000;

    public static final String IMAGE_KEY_SUFFIX = "jpg";
    private static final int DEFAULT_JPEG_QUALITY = 90;

    /**
     * Private constructor to prohibit nonsense instance creation.
     */
    private BitmapUtils() {
    }

    /**
     * 得到要显示的图片数据
     *
     * @param context     Context
     * @param data        拍照保存的图片数据byte[]类型
     * @param orientation 图片方向
     * @return Bitmap 返回显示的图片bitmap
     */
    public static Bitmap createBitmap(Context context, byte[] data, float orientation) {
        Bitmap bitmap = null;
        Bitmap transformed = null;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        try {

            int width = DensityUtils.getDisplayWidth(context);
            int hight = DensityUtils.getDisplayHeight(context);
            int min = Math.min(width, hight);
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, opts);
            opts.inSampleSize =
                    BitmapUtils.computeSampleSize(opts, min, BitmapUtils.MAXLENTH * BitmapUtils.MAXLENTH);
            opts.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
            transformed = BitmapUtils.rotateBitmap(orientation, bitmap);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap = null;
            }
            if (transformed != null && !transformed.isRecycled()) {
                transformed.recycle();
                transformed = null;
            }
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, opts);
            opts.inSampleSize =
                    BitmapUtils.computeSampleSize(opts, -1, opts.outWidth * opts.outHeight
                            / BitmapUtils.PIC_COMPRESS_SIZE);
            opts.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
            transformed = BitmapUtils.rotateBitmap(orientation, bitmap);
        }
        if (transformed != bitmap && bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        return transformed;

    }

    /**
     * 根据从数据中读到的方向旋转图片
     *
     * @param orientation 图片方向
     * @param bitmap      要旋转的bitmap
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmap(float orientation, Bitmap bitmap) {
        Bitmap transformed;
        Matrix m = new Matrix();
        if (orientation == 0) {
            transformed = bitmap;
        } else {
            m.setRotate(orientation);
            transformed = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
        }
        return transformed;
    }

    /**
     * 获取无损压缩图片合适的压缩比例
     *
     * @param options        图片的一些设置项
     * @param minSideLength  最小边长
     * @param maxNumOfPixels 最大的像素数目
     * @return 返回合适的压缩值
     */
    public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = BitmapUtils.computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) { // SUPPRESS CHECKSTYLE
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8; // SUPPRESS CHECKSTYLE
        }
        return roundedSize;
    }

    /**
     * 获取无损压缩图片的压缩比
     *
     * @param options        图片的一些设置项
     * @param minSideLength  最小边长
     * @param maxNumOfPixels 最大的像素数目
     * @return 返回合适的压缩值
     */
    public static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength,
                                               int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound =
                (minSideLength == -1) ? BitmapUtils.IMAGEBOUND : (int) Math.min(
                        Math.floor(w / minSideLength), Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }


    /**
     * 图片转换为base64
     *
     * @param bitmap  图片
     * @param quality 压缩质量
     * @return base64编码的数据
     */
    public static String bitmapToJpegBase64(Bitmap bitmap, int quality) {
        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.JPEG, quality, out);
            byte[] data = out.toByteArray();
            return Base64Utils.encodeToString(data, Base64Utils.NO_WRAP);
        } catch (Exception e) {
            return null;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Bitmap createLivenessBitmap(Context context, int[] argbByte, Rect roundRect) {
        Bitmap transformed = null;
        try {
            transformed = Bitmap.createBitmap(argbByte,
                    roundRect.width(), roundRect.height(),
                    Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            if (transformed != null) {
                transformed.recycle();
            }
        }
        return transformed;
    }
}
