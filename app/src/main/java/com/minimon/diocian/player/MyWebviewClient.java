package com.minimon.diocian.player;

import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by ICARUSUD on 2018. 3. 9..
 */

public class MyWebviewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if(url.startsWith("minimon://goToWeb")){
            view.loadUrl("http://dev.api.minimon.com/Test/view/channel");
           return true;
        }else {
            return super.shouldOverrideUrlLoading(view, url);
        }

    }
}
