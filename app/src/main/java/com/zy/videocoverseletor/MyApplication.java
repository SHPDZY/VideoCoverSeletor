package com.zy.videocoverseletor;

import android.app.Application;

import com.zy.videocoverselector.utils.ScreenUtils;

public class MyApplication extends Application {
    public static MyApplication myApplictaion;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplictaion = this;
        ScreenUtils.init(this);
    }
}
