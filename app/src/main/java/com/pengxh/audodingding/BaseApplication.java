package com.pengxh.audodingding;

import android.app.Application;

import com.pengxh.app.multilib.utils.SaveKeyValues;
import com.pengxh.app.multilib.widget.EasyToast;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2019/12/25 13:19
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        EasyToast.init(this);
        SaveKeyValues.initSharedPreferences(this);
    }
}
