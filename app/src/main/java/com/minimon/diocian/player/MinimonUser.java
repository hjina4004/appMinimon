package com.minimon.diocian.player;


import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class MinimonUser {
    // http://192.168.10.182:8080/lmvas/api/json/login.php?key=gidwls&uname=jina&psw=1234
    // http://dev.api.minimon.com/User/login?type=basic&id=hjina&value=12345678&device_id=358964070302117
    // {"resCode":"0900","msg":"\ud504\ub85c\uc138\uc2a4\uc624\ub958","data":{"errCode":"0104","errMsg":"\uc544\uc774\ub514 \ub610\ub294 \ube44\ubc00\ubc88\ud638\ub97c \ud655\uc778\ud558\uc138\uc694."}}
    private final String TAG = "MinimonUser";
    private final String API_URL = "http://dev.api.minimon.com/User/login";

    public interface MinimonLoginListener {
        // These methods are the different events and need to pass relevant arguments with the event
        public void onLogined(JSONObject info);
    }
    private MinimonLoginListener listener;

    public MinimonUser() {
        this.listener = null; // set null listener

        init();
    }

    // Assign the listener implementing events interface that will receive the events (passed in by the owner)
    public void setListener(MinimonLoginListener listener) {
        this.listener = listener;
    }

    // API 인스턴스를 초기화
    private void init() {
    }

    // 로그인 처리
    public void login(ContentValues loginInfo) {
        Log.d(TAG, "loginInfo: " + loginInfo);

        NetworkTask networkTask = new NetworkTask(API_URL, loginInfo);
        networkTask.execute();
    }

    // 로그아웃 처리(토큰도 함께 삭제)
    public void logout() {
    }

    public class NetworkTask extends AsyncTask<Void, Void, String> {
        private String url;
        private ContentValues values;

        public NetworkTask(String url, ContentValues values) {
            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result; // 요청 결과를 저장할 변수.
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, values); // 해당 URL로 부터 결과물을 얻어온다.

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
            responseNetworkTask(s);
        }
    }

    public void responseNetworkTask(String s) {
        try {
            JSONObject objJSON = new JSONObject(s);
            Log.d(TAG, "responseNetworkTask: " + objJSON);

            if (listener != null)
                listener.onLogined(objJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
