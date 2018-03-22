package com.minimon.diocian.player;

import android.content.ContentValues;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class PolicyActivity extends AppCompatActivity implements MinimonWebView.MinimonWebviewListener{

    WebView mWebView;
    MinimonWebView minimonWebView;
    String policyType = "";
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

        policyType = getIntent().getStringExtra("policyType");

        ContentValues content = new ContentValues();
        content.put("id", UserInfo.getInstance().getUID());
        content.put("loc", "Android");
        content.put("page", "policy");
        content.put("tap",policyType);
        minimonWebView.goToWeb("Contents/view", content);
//        getActionBar().setDisplayHomeAsUpEnabled(true);
//        getActionBar().sethome(R.drawable.ic_back);
        setTitle("이용약관");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResponseHtml(String html, String baseUrl) {
        Log.d("PolicyActivity","onResponseHtml");
        mWebView.loadDataWithBaseURL(baseUrl,html,"text/html","utf-8",null);
    }
}
