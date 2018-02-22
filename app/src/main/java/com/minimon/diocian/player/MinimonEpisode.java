package com.minimon.diocian.player;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by GOOD on 2018-02-20.
 */

public class MinimonEpisode {
    private final String TAG = "MinimonEpisode";
    private final String API_URL = "http://dev.api.minimon.com/Episode/";
    private String currentRequest;

    public interface MinimonEpisodeListener {
        // These methods are the different events and need to pass relevant arguments with the event
        public void onResponse(JSONObject info);
    }

    private MinimonEpisodeListener listener;

    public MinimonEpisode(){
        this.listener = null;
    }

    public void setListener(MinimonEpisodeListener listener){ this.listener = listener; }

    private void requestFunction(String current, ContentValues info){
        currentRequest = current;
        NetworkTask networkTask = new NetworkTask(API_URL+current, info);
        networkTask.setToken(UserInfo.getInstance().getToken());
        networkTask.execute();
    }
    public class NetworkTask extends AsyncTask<Void,Void,String>{
        private String url;
        private ContentValues values;
        private String token;

        public NetworkTask(String url, ContentValues values) {
            this.url = url;
            this.values = values;
            this.token = null;
        }

        public void setToken(String token) {
            this.token = token;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result;
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, values, token); // 해당 URL로 부터 결과물을 얻어온다.

            Log.e(TAG, "doInBackground: " + result);
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            responseNetworkTask(s);
        }
    }

    public void responseNetworkTask(String s) {
        if (s == null) {
            return;
        }
        try {
            JSONObject objJSON = new JSONObject(s);
            objJSON.put("current_request", currentRequest);
            Log.d(TAG, "responseNetworkTask: " + objJSON);

            if (listener != null)
                listener.onResponse(objJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void info(ContentValues value){
        requestFunction("info", value);
    }
}
