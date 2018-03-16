package com.minimon.diocian.player;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.UrlQuerySanitizer;
import android.util.Log;
import android.view.View;
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
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d("LoadingUrl",url);
        if(url.startsWith("minimon://goToWeb")){
//            view.loadUrl("http://dev.api.minimon.com/Test/view/channel");
//            url = url.substring(18);
//            String params[] = url.split("&");
//            Map<String, String> map = new HashMap<>();
//            for(String param : params){
//                String[] arrParam = param.split("=");
//
//
//                String name = param.split("=")[0];
//                String value = "";
//                if(arrParam.length > 1)
//                    value = param.split("=")[1];
//
//                map.put(name,value);
//            }
//            if("channel".equals(map.get("page"))){ //페이지일 경우
//                view.loadUrl("http://dev.api.minimon.com/Test/view/channel");
////                Log.d("LoadingUrlMap", "url : "+map.get("url")+", page : "+map.get("page")+", key : "+map.get("key")+", value : "+map.get("value"));
////                Log.d("LoadingUrlSend", "window.minimon.goToWeb('"+map.get("url")+"','"+map.get("page")+"','"+map.get("key")+"','"+map.get("value")+"');");
////                view.loadUrl("window.minimon.goToWeb("+map.get("url")+","+map.get("page")+","+map.get("key")+","+map.get("value")+");");
////                window.minimon.goToWeb(url,page,key,value);
//            }else if("episode".equals(map.get("page"))){                                  //에피소드일 경우
//                EpisodeInfo.getInsatnace().setIdx(map.get("value"));
//                Log.d("LoadingUrl_ep_idx","value : "+map.get("value"));
//                Intent intent = new Intent(mContext,DramaPlayActivity.class);
//                mContext.startActivity(intent);
//            }else if("qna.write".equals(map.get("page"))){
//                view.loadUrl("http://dev.api.minimon.com/Test/view/qna.write");
//            }
//            Log.d("LoadingUrl",url);
           return true;
        }else {
            Log.d("LoadingUrl",url);
            return super.shouldOverrideUrlLoading(view, url);
        }
    }



//    @Override
//    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//        try {
//            URL mUrl = new URL(url);
//            HttpURLConnection connection = (HttpURLConnection) mUrl.openConnection();
//            Log.d("InterCeptMethod", connection.getRequestMethod().toString());
//            Log.d("InterCeptProperty", connection.getRequestProperties().toString());
//            connection.setRequestMethod("POST"); // URL 요청에 대한 메소드 설정 : POST.
//            connection.setRequestProperty("Accept-Charset", "UTF-8"); // Accept-Charset 설정.
//            connection.setRequestProperty("Context_Type", "application/x-www-form-urlencoded;charset=UTF-8");
//            connection.setRequestProperty("Authorization", UserInfo.getInstance().getToken());
//
//            Log.d("InterCeptUrlRe",url);
//            String strParams = url.split("\\?")[1];
//            Log.d("InterCeptUrl",strParams);
//
//            OutputStream os = connection.getOutputStream();
//            os.write(strParams.getBytes("UTF-8")); // 출력 스트림에 출력.
//            os.flush(); // 출력 스트림을 플러시(비운다)하고 버퍼링 된 모든 출력 바이트를 강제 실행.
//            os.close(); // 출력 스트림을 닫고 모든 시스템 자원을 해제.
//
////            connection.setDoInput(true);
//            connection.connect();
//
//            InputStream resultInputStream = connection.getInputStream();
//
//            return new WebResourceResponse("text/html","utf-8",resultInputStream);
//        }catch (MalformedURLException e){
//            return null;
//        }catch (IOException e){
//            return null;
//        }
//    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        bar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        bar.setVisibility(View.GONE);
    }

    public MyWebviewClient(Context context, ProgressBar progress){
        mContext = context;
        bar = progress;
    }
}
