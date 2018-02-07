package com.minimon.diocian.player;


import android.content.ContentValues;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.a001_top_back);

        setImageInButton(R.mipmap.a001_social_naver, R.id.btnNaver);
        setImageInButton(R.mipmap.a001_social_kakao, R.id.btnKakao);
        setImageInButton(R.mipmap.a001_social_facebook, R.id.btnFacebook);
        setImageInButton(R.mipmap.a001_social_google, R.id.btnGoogle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setImageInButton(int drawableID, int btnID) {
        Drawable drawable = ContextCompat.getDrawable(LoginActivity.this, drawableID);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        Button btn = findViewById(btnID);
        btn.setCompoundDrawables(drawable, null, null, null);
    }

    public void onClickLogin(View view) {
        Log.d("LoginActivity:", "onClickLogin start");
        // URL 설정.
        // http://192.168.10.182:8080/lmvas/api/json/login.php?key=gidwls&uname=jina&psw=1234
        // http://dev.api.minimon.com/User/login?type=basic&id=hjina&value=12345678&device_id=358964070302117
        // {"resCode":"0900","msg":"\ud504\ub85c\uc138\uc2a4\uc624\ub958","data":{"errCode":"0104","errMsg":"\uc544\uc774\ub514 \ub610\ub294 \ube44\ubc00\ubc88\ud638\ub97c \ud655\uc778\ud558\uc138\uc694."}}
        String url = "http://dev.api.minimon.com/User/login";
        ContentValues loginInfo = new ContentValues();
        loginInfo.put("type", "basic");
        loginInfo.put("id", "hjina");
        loginInfo.put("value", "12345678");
        // loginInfo.put("device_id", "358964070302117");

        // AsyncTask를 통해 HttpURLConnection 수행.
        NetworkTask networkTask = new NetworkTask(url, loginInfo);
        networkTask.execute();
    }

    public void responseNetworkTask(String s) {
        try {
            JSONObject objJSON = new JSONObject(s);
            Log.d("LoginActivity:", "responseNetworkTask: " + objJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    public void onClickFindMember(View view) {
        Intent intent = new Intent(LoginActivity.this.getApplicationContext(), FindMemberActivity.class);
        startActivity(intent);
    }

    public void onClickNewMember(View view) {
        Intent intent = new Intent(LoginActivity.this.getApplicationContext(), NewMemberActivity.class);
        startActivity(intent);
    }
}
