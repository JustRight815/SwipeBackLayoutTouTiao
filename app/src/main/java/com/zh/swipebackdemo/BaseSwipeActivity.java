package com.zh.swipebackdemo;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.jaeger.library.StatusBarUtil;
import com.zh.swipebacklib.BaseSwipeBackActivity;
import com.zh.swipebacklib.SwipeBackLayout;

/**
 * Created by GongWen on 17/8/25.
 */

public abstract class BaseSwipeActivity extends BaseSwipeBackActivity implements CompoundButton.OnCheckedChangeListener {
    protected final String TAG = getClass().getSimpleName();
    protected Toolbar toolbar;

    protected SwipeBackLayout mSwipeBackLayout;
    protected RadioButton fromLeftRb;
    protected RadioButton fromTopRb;
    protected RadioButton fromRightRb;
    protected RadioButton fromBottomRb;

    public boolean isMain = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        setTitle(TAG);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSwipeBackLayout = getSwipeBackLayout();
        if(isMain){
            return;
        }

        fromLeftRb = (RadioButton) findViewById(R.id.fromLeftRb);
        fromLeftRb.setOnCheckedChangeListener(this);
        fromTopRb = (RadioButton) findViewById(R.id.fromTopRb);
        fromTopRb.setOnCheckedChangeListener(this);
        fromRightRb = (RadioButton) findViewById(R.id.fromRightRb);
        fromRightRb.setOnCheckedChangeListener(this);
        fromBottomRb = (RadioButton) findViewById(R.id.fromBottomRb);
        fromBottomRb.setOnCheckedChangeListener(this);
    }

    protected abstract int getLayoutId();

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            switch (buttonView.getId()) {
                case R.id.fromLeftRb:
                    mSwipeBackLayout.setDirectionMode(SwipeBackLayout.FROM_LEFT);
                    break;
                case R.id.fromTopRb:
                    mSwipeBackLayout.setDirectionMode(SwipeBackLayout.FROM_TOP);
                    break;
                case R.id.fromRightRb:
                    mSwipeBackLayout.setDirectionMode(SwipeBackLayout.FROM_RIGHT);
                    break;
                case R.id.fromBottomRb:
                    mSwipeBackLayout.setDirectionMode(SwipeBackLayout.FROM_BOTTOM);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 设置状态栏颜色
     * @param color
     */
    protected void setStatusBarColor(@ColorInt int color) {
        StatusBarUtil.setColor(this, color);
    }


    /**
     * 设置状态栏颜色
     * @param color
     * @param statusBarAlpha 透明度
     */
    public void setStatusBarColor(@ColorInt int color, @IntRange(from = 0, to = 255) int statusBarAlpha) {
        StatusBarUtil.setColorForSwipeBack(this, color, statusBarAlpha);
    }
}

