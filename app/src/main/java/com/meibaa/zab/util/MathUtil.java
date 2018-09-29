package com.meibaa.zab.util;

import android.view.View;

/**
 * 计算
 */
public class MathUtil {
    /**
     * 两点间的距离
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.abs(x1 - x2) * Math.abs(x1 - x2)
                + Math.abs(y1 - y2) * Math.abs(y1 - y2));
    }

    /**
     * 计算点a(x,y)的角度
     *
     * @param x
     * @param y
     * @return
     */
    public static double pointTotoDegrees(double x, double y) {
        return Math.toDegrees(Math.atan2(x, y));
    }


    /**
     * 获取控件尺寸
     */
    public static int measure(int origin) {
        int result = 400;
        int specMode = View.MeasureSpec.getMode(origin);
        int specSize = View.MeasureSpec.getSize(origin);
        if (specMode == View.MeasureSpec.EXACTLY) {//控件设定了固定的高度
            result = specSize;
        } else if (specMode == View.MeasureSpec.AT_MOST) {//控件没有设定固定宽高，wrap_content
            result = Math.min(specSize, 400);
        }
        return result;
    }
}
