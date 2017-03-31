package com.anxpp.one.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPrerences工具
 * Created by anxpp.com on 2017/3/22.
 */

public class SharedPrerencesUtil {
    /**
     * 存储或修改值
     *
     * @param context 上下文
     * @param flag    存储标志
     * @param key     存储字段名
     * @param value   要保存的值
     */
    public static void save(Context context, String flag, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(flag, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * 获取值
     *
     * @param context 上下文
     * @param flag    存储标志
     * @param key     要获取的字段名
     * @return 要获取的值
     * //@param default   默认值
     */
    public static String getString(Context context, String flag, String key) {
        SharedPreferences sp = context.getSharedPreferences(flag, Context.MODE_PRIVATE);
        return sp.getString(key, "");
    }

    public static String getString(Context context, String flag, String key, String defaultStr) {
        SharedPreferences sp = context.getSharedPreferences(flag, Context.MODE_PRIVATE);
        return sp.getString(key, defaultStr);
    }

    public static int getInt(Context context, String flag, String key) {
        SharedPreferences sp = context.getSharedPreferences(flag, Context.MODE_PRIVATE);
        return sp.getInt(key, 0);
    }

    public static int getInt(Context context, String flag, String key, int defaultInt) {
        SharedPreferences sp = context.getSharedPreferences(flag, Context.MODE_PRIVATE);
        return sp.getInt(key, defaultInt);
    }
}
