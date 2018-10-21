package com.zh.swipebacklib;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.Stack;

/**
 * Created by zh on 2018/10/21.
 * 描述：滑动finish管理
 */

public class SlideFinishManager implements Application.ActivityLifecycleCallbacks {
    private static SlideFinishManager mSlideFinishManager;
    public Stack<Activity> mActivityStack = new Stack<>();

    private SlideFinishManager() {
    }

    public static SlideFinishManager getInstance() {
        if (mSlideFinishManager == null) {
            synchronized (SlideFinishManager.class) {
                if (mSlideFinishManager == null) {
                    mSlideFinishManager = new SlideFinishManager();
                }
            }
        }
        return mSlideFinishManager;
    }

    public void init(Application mApplication) {
        mApplication.registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        mActivityStack.add(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        mActivityStack.remove(activity);
    }

    /**
     * 获取倒数第二个Activity
     *
     * @return
     */
    public Activity getPenultimateActivity() {
        return mActivityStack.size() >= 2 ? mActivityStack.get(mActivityStack.size() - 2) : null;
    }


    /**
     * 获取倒数第二个 Activity
     *
     * @return
     */
    @Nullable
    public Activity getPenultimateActivity(Activity currentActivity) {
        Activity activity = null;
        try {
            if (mActivityStack.size() > 1) {
                activity = mActivityStack.get(mActivityStack.size() - 2);

                if (currentActivity.equals(activity)) {
                    int index = mActivityStack.indexOf(currentActivity);
                    if (index > 0) {
                        // 处理内存泄漏或最后一个 Activity 正在 finishing 的情况
                        activity = mActivityStack.get(index - 1);
                    } else if (mActivityStack.size() == 2) {
                        // 处理屏幕旋转后 mActivityStack 中顺序错乱
                        activity = mActivityStack.lastElement();
                    }
                }
            }
        } catch (Exception e) {
        }
        return activity;
    }
}
