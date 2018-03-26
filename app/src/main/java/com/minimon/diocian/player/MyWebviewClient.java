package com.minimon.diocian.player;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.UrlQuerySanitizer;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
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
    private boolean isStarted = false;
    private boolean isFinished = false;
    private String pageName;

    public interface  myWebViewClientListener{
        void loadingFinished();
    }

    public void setMyWebViewClientListener(myWebViewClientListener listener){
        mListener = listener;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.d("MyWebviewClient","onPageStarted");
        isStarted = true;
        super.onPageStarted(view, url, favicon);
        bar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        Log.d("MyWebviewClient","onPageFinished : "+url+", pageName : "+pageName);
        isFinished = true;
        super.onPageFinished(view, url);
        if(isStarted && isFinished) {
            bar.setVisibility(View.GONE);
            if (mListener != null) mListener.loadingFinished();
        }else{
            Log.d("finishDrama", "finish");
            if("episode".equals(pageName))
                ((DramaPlayActivity)mContext).finish();
            else
                ((MainActivity)mContext).finish();
        }
        isStarted = false;
        isFinished = false;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d("shouldOverrideUrl",url);
        return false;
    }

    public MyWebviewClient(Context context, ProgressBar progress, String page){
        mContext = context;
        bar = progress;
        pageName = page;
    }
}
