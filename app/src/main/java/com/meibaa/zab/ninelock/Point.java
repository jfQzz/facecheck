package com.meibaa.zab.ninelock;

/**
 * 点位置
 */
public class Point {
    public static int STATE_NORMAL = 0; // 未选中
    public static int STATE_CHECK = 1; // 选中
    public static int STATE_CHECK_ERROR = 2; // 输入错误

    public float x;
    public float y;
    public int state = 0;
    public int index = 0;// 下标

    public Point() {}

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
