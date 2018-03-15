package com.minimon.diocian.player;

import android.content.ContentValues;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class PolicyActivity extends AppCompatActivity implements MinimonWebView.MinimonWebviewListener{

    WebView mWebView;
    MinimonWebView minimonWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy);
        minimonWebView = new MinimonWebView();
        minimonWebView.setListener(this);
        mWebView = findViewById(R.id.webview_policy);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        ContentValues content = new ContentValues();
        content.put("id", UserInfo.getInstance().getUID());
        content.put("loc", "Android");
        content.put("page", "policy");
        minimonWebView.goToWeb("Contents/view", content);
    }

    @Override
    public void onResponseHtml(String html, String baseUrl) {
        Log.d("PolicyActivity","onResponseHtml");
        mWebView.loadDataWithBaseURL(baseUrl,html,"text/html","utf-8",null);
    }
}
