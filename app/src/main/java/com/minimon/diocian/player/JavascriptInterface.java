package com.minimon.diocian.player;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

/**
 * Created by GOOD on 2018-03-12.
 */

public class JavascriptInterface {
    public Context mContext;
    private WebView mWebView;

//    public interface JavascriptInterface

    public JavascriptInterface(Context context, WebView webView){
        mContext = context;
        mWebView = webView;
    }

    @android.webkit.JavascriptInterface
    public void goToWeb(String url, String page, String key, String value){
        Log.d("JavscriptInter", url+" ,"+page+" ,"+key+" ,"+value);
        mWebView.loadUrl("window.minimon.goToWeb('"+url+"','"+page+"','"+key+"','"+value+"'");
    }
}
