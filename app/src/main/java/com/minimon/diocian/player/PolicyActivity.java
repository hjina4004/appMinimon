package com.minimon.diocian.player;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class PolicyActivity extends AppCompatActivity {

    WebView mWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy);
        mWebView = findViewById(R.id.webview_policy);
        mWebView.loadUrl("http://dev.api.minimon.com/Test/view/policy");
    }
}
