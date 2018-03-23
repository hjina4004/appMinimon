package com.minimon.diocian.player;

import android.content.Context;
import android.os.Handler;
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
        void goToPg(String url, String item, String how, String title);
        void goToSearch();
        void changePlayer(String idx);
        void closeWebView();
        void setTitle(String title);
        void closeGoToNative(String page);
    }
    private JavascriptInterfaceListener mListener;
    public void setListener(JavascriptInterfaceListener listener){mListener = listener;}

    public JavascriptInterface(Context context, WebView webView){
        mContext = context;
        mWebView = webView;
    }

    @android.webkit.JavascriptInterface
    public void goToWeb(final String url, final String page, final String key, final String value){
        Log.d("JavascriptInterface", url+","+page+","+key+","+value);
//        new Handler().post(new Runnable() {
//            @Override
//            public void run() {
//                ((MainActivity)mContext).runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
                        if(mListener!=null)
                            mListener.onGoToWeb(url,page,key,value);
//                    }
//                });
//            }
//        });

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
        Log.d("JavascriptInterface", "closeDepthRefreshWeb"+","+depth);
        if(mListener!=null){
            mListener.closeDepthRefreshWeb(depth);
        }
    }

    @android.webkit.JavascriptInterface
    public void goToPG(String url, String item, String how, String title){
        Log.d("JavascriptInterface", "goToPG");
        if(mListener != null){
            mListener.goToPg(url,item,how, title);
        }
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
        if(mListener!=null)
            mListener.closeGoToNative(page);
    }

    @android.webkit.JavascriptInterface
    public void changePlayer(String idx){
        Log.d("JavascriptInterface", idx);
        if(mListener!=null)
            mListener.changePlayer(idx);
    }

    @android.webkit.JavascriptInterface
    public void closeWebview(){
        Log.d("JavascriptInterface", "closeWebview");
        if(mListener!=null)
            mListener.closeWebView();
    }

    @android.webkit.JavascriptInterface
    public void setTitle(String title){
        Log.d("JavascriptInterface", "setTitle");
        if(mListener!=null)
            mListener.setTitle(title);
    }
}
