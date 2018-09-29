package com.meibaa.zab.util;

import android.content.Context;

import com.meibaa.zab.ninelock.Contants;


/**
 * Created by leo on 16/4/5.
 */
public class PasswordUtil {

    /**
     * 获取设置过的密码
     */
    public static String getPin(Context context) {
        String password = ConfigUtil.getInstance(context).getString(Contants.PASS_KEY);
        return password;
    }
}
