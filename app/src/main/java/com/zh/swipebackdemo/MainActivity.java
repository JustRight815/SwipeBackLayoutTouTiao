package com.zh.swipebackdemo;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.View;
import android.widget.Button;

import com.zh.swipebackdemo.activity.CommonActivity;
import com.zh.swipebackdemo.activity.HorizontalScrollViewActivity;
import com.zh.swipebackdemo.activity.ListViewActivity;
import com.zh.swipebackdemo.activity.NestedScrollViewActivity;
import com.zh.swipebackdemo.activity.RecyclerViewActivity;
import com.zh.swipebackdemo.activity.ScrollViewActivity;
import com.zh.swipebackdemo.activity.SwipeRefreshLayoutActivity;
import com.zh.swipebackdemo.activity.ViewPagerActivity;
import com.zh.swipebackdemo.activity.WebViewActivity;

public class MainActivity extends BaseSwipeActivity implements View.OnClickListener {
    private Button btnCommon;
    private Button btnScrollView;
    private Button btnHorizontalScrollView;
    private Button btnNestedScrollView;
    private Button btnRecyclerView;
    private Button btnListView;
    private Button btnViewPager;
    private Button btnWebView;
    private Button btnSwipeRefreshLayout;

    private int[] mBgColors;
    private static int mBgIndex = 0;

    @Override
    public boolean isSupportSwipeBack() {
        return false;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isMain = true;
        super.onCreate(savedInstanceState);

        btnCommon = initButton(R.id.btnCommon);
        btnScrollView = initButton(R.id.btnScrollView);
        btnHorizontalScrollView = initButton(R.id.btnHorizontalScrollView);
        btnNestedScrollView = initButton(R.id.btnNestedScrollView);
        btnListView = initButton(R.id.btnListView);
        btnRecyclerView = initButton(R.id.btnRecyclerView);
        btnWebView = initButton(R.id.btnWebView);
        btnViewPager = initButton(R.id.btnViewPager);
        btnSwipeRefreshLayout = initButton(R.id.btnSwipeRefreshLayout);

        changeActionBarColor();
    }

    private Button initButton(@IdRes int id) {
        Button btn = (Button) findViewById(id);
        btn.setOnClickListener(this);
        return btn;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnCommon:
                startActivity(CommonActivity.class);
                break;
            case R.id.btnScrollView:
                startActivity(ScrollViewActivity.class);
                break;
            case R.id.btnHorizontalScrollView:
                startActivity(HorizontalScrollViewActivity.class);
                break;
            case R.id.btnNestedScrollView:
                startActivity(NestedScrollViewActivity.class);
                break;
            case R.id.btnListView:
                startActivity(ListViewActivity.class);
                break;
            case R.id.btnRecyclerView:
                startActivity(RecyclerViewActivity.class);
                break;
            case R.id.btnViewPager:
                startActivity(ViewPagerActivity.class);
                break;
            case R.id.btnWebView:
                startActivity(WebViewActivity.class);
                break;
            case R.id.btnSwipeRefreshLayout:
                startActivity(SwipeRefreshLayoutActivity.class);
                break;
        }
    }

    public void startActivity(Class<?> clazz) {
        startActivity(new Intent(MainActivity.this, clazz));
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
