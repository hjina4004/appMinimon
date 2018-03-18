package com.minimon.diocian.player;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.UrlQuerySanitizer;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ICARUSUD on 2018. 3. 9..
 */

public class MyWebviewClient extends WebViewClient {

    private Context mContext;
    private ProgressBar bar;
    private myWebViewClientListener mListener;

    public interface  myWebViewClientListener{
        void loadingFinished();
    }

    public void setMyWebViewClientListener(myWebViewClientListener listener){
        mListener = listener;
    }
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d("MyWebviewClient","shouldOverrideUrlLoading1");
        return true;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        Log.d("MyWebviewClient","shouldInterceptRequest");
        return super.shouldInterceptRequest(view, request);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        Log.d("MyWebviewClient","shouldInterceptRequest2");
        return super.shouldInterceptRequest(view, url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        Log.d("MyWebviewClient","shouldOverrideUrlLoading2");
        return super.shouldOverrideUrlLoading(view, request);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.d("MyWebviewClient","onPageStarted");
        super.onPageStarted(view, url, favicon);
        bar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        Log.d("MyWebviewClient","onPageFinished");
        super.onPageFinished(view, url);
        bar.setVisibility(View.GONE);
        if(mListener!=null) mListener.loadingFinished();
    }


    public MyWebviewClient(Context context, ProgressBar progress){
        mContext = context;
        bar = progress;
    }
}
