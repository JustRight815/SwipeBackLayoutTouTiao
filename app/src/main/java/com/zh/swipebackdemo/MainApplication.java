package com.zh.swipebackdemo;

import android.app.Application;
import com.zh.swipebacklib.SlideFinishManager;

/**
 * Created by zh on 2018/10/21.
 */

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SlideFinishManager.getInstance().init(this);
    }
}
