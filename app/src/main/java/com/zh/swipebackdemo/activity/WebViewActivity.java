package com.zh.swipebackdemo.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.zh.swipebackdemo.BaseSwipeActivity;
import com.zh.swipebackdemo.R;

/**
 * Created by GongWen on 17/8/24.
 */

public class WebViewActivity extends BaseSwipeActivity {
    private WebView mWebView;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_webview;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        mWebView.loadUrl("https://wap.baidu.com");
    }
}
