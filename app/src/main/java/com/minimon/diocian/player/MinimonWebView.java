package com.minimon.diocian.player;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by GOOD on 2018-03-14.
 */

public class MinimonWebView {
    private final String WEBVIEW_URL = "http://dev.api.minimon.com/";
    private String currentRequest;
    private String baseUrl;

    public interface MinimonWebviewListener{
        void onResponseHtml(String html, String baseUrl);
    }

    private MinimonWebviewListener mListener;

    public void setListener(MinimonWebviewListener listener){ mListener = listener;}

    private void requestFunctionWebView(String current, ContentValues info){
        currentRequest = current;
        baseUrl = WEBVIEW_URL+current;
        WebViewNetworkTask task = new WebViewNetworkTask( WEBVIEW_URL+current,info);
        task.setToken(UserInfo.getInstance().getToken());
        task.execute();
    }

    private void requestPayFunctionWebView(String url, ContentValues info)
    {
        baseUrl = url;
        WebViewNetworkTask task = new WebViewNetworkTask(url, info);
        task.setToken(UserInfo.getInstance().getToken());
        task.execute();
    }
    public class WebViewNetworkTask extends AsyncTask<Void,Void,String>{

        private String url;
        private ContentValues values;
        private String token;

        public WebViewNetworkTask(String url, ContentValues values){
            this.url = url;
            this.values = values;
            this.token = null;
        }

        private void setToken(String token){this.token = token;}

        @Override
        protected String doInBackground(Void... voids) {
            String result;
            RequestHttpURLConnection connection = new RequestHttpURLConnection();
            result = connection.request(url, values, token);
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            responseNetworkTask(s);
        }
    }

    public void responseNetworkTask(String s){
        if(s == null)
            return;
        if(mListener !=null){
            Log.d("responseNetworkTask",s);
            mListener.onResponseHtml(s,baseUrl);
        }
    }

    public void goToWeb(String url, ContentValues info){
        requestFunctionWebView(url, info);
    }

    public void goToPayWeb(String url, ContentValues info){
        requestPayFunctionWebView(url, info);
    }
}
