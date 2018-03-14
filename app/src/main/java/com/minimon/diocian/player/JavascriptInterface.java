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

    public interface JavascriptInterfaceListener{
        void onGoToWeb(String url, String page, String key, String value);
        void closeRefreshWeb(String url, String page, String key, String value);
        void closeDepthRefreshWeb(String depth);
        void goToPg(String url, String item, String how);
        void goToSearch();
    }
    private JavascriptInterfaceListener mListener;
    public void setListener(JavascriptInterfaceListener listener){mListener = listener;}

    public JavascriptInterface(Context context, WebView webView){
        mContext = context;
        mWebView = webView;
    }

    @android.webkit.JavascriptInterface
    public void goToWeb(String url, String page, String key, String value){
        Log.d("JavascriptInterface", value);
        if(mListener!=null)
            mListener.onGoToWeb(url,page,key,value);
//        mWebView.loadUrl("window.minimon.goToWeb('"+url+"','"+page+"','"+key+"','"+value+"'");
    }

    @android.webkit.JavascriptInterface
    public void closeRefreshWeb(String url, String page, String key, String value){
        Log.d("JavascriptInterface", "closeRefreshWeb");
        if(mListener!=null)
            mListener.closeRefreshWeb(url,page,key,value);
    }

    @android.webkit.JavascriptInterface
    public void closeDepthRefreshWeb(String depth){
        Log.d("JavascriptInterface", "closeDepthRefreshWeb");
        if(mListener!=null){
            mListener.closeDepthRefreshWeb(depth);
        }
    }

    @android.webkit.JavascriptInterface
    public void goToPG(String url, String item, String how){
        Log.d("JavascriptInterface", "goToPG");
    }

    @android.webkit.JavascriptInterface
    public void goToSearch(){
        Log.d("JavascriptInterface", "goToSearch");
        if(mListener!=null)
            mListener.goToSearch();
    }

    @android.webkit.JavascriptInterface
    public void closeGoToNative(String page){
        Log.d("JavascriptInterface", "closeGoToNative");
    }

    @android.webkit.JavascriptInterface
    public void changePlayer(String idx){
        Log.d("JavascriptInterface", "changePlayer");
    }
}
