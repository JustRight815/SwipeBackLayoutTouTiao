package com.zh.swipebackdemo.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import com.zh.swipebackdemo.BaseSwipeActivity;
import com.zh.swipebackdemo.R;
import com.zh.swipebacklib.SwipeBackLayout;

/**
 * Created by GongWen on 17/8/24.
 */

public class CommonActivity extends BaseSwipeActivity {
    private int[] mBgColors;
    private static int mBgIndex = 0;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_attach_to_common;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switch (mSwipeBackLayout.getDirectionMode()) {
            case SwipeBackLayout.FROM_LEFT:
                fromLeftRb.setChecked(true);
                break;
            case SwipeBackLayout.FROM_TOP:
                fromTopRb.setChecked(true);
                break;
            case SwipeBackLayout.FROM_RIGHT:
                fromRightRb.setChecked(true);
                break;
            case SwipeBackLayout.FROM_BOTTOM:
                fromBottomRb.setChecked(true);
                break;
            default:
                break;
        }

        changeActionBarColor();
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CommonActivity.this,CommonActivity.class));
            }
        });
    }

    private void changeActionBarColor() {
        setStatusBarColor(getColors()[mBgIndex], 0);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getColors()[mBgIndex]));
        mBgIndex++;
        if (mBgIndex >= getColors().length) {
            mBgIndex = 0;
        }
    }

    private int[] getColors() {
        if (mBgColors == null) {
            Resources resource = getResources();
            mBgColors = new int[] {
                    resource.getColor(R.color.androidColorA),
                    resource.getColor(R.color.androidColorB),
                    resource.getColor(R.color.androidColorC),
                    resource.getColor(R.color.androidColorD),
                    resource.getColor(R.color.androidColorE),
            };
        }
        return mBgColors;
    }
}
