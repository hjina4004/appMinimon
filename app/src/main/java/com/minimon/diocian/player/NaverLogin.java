package com.minimon.diocian.player;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NaverLogin {
    private OAuthLogin mOAuthLoginInstance;
    private Context mContext;

    public interface NaverLoginListener {
        // These methods are the different events and need to pass relevant arguments with the event
        void onLogined(String uid, String email);
    }
    private NaverLoginListener listener;

    public NaverLogin(Context mContext) {
        this.listener = null; // set null listener
        this.mContext = mContext;
        initNaverAuthInstance();
    }

    // Assign the listener implementing events interface that will receive the events (passed in by the owner)
    public void setListener(NaverLoginListener listener) {
        this.listener = listener;
    }

    // API 인스턴스를 초기화
    private void initNaverAuthInstance() {
        final String OAUTH_CLIENT_ID = "pNqb86SdiAtNl8LEF07A";
        final String OAUTH_CLIENT_SECRET = "vXiEObrjBd";
        final String OAUTH_CLIENT_NAME = "Minimon";

        mOAuthLoginInstance = OAuthLogin.getInstance();
        mOAuthLoginInstance.init(mContext, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME);
    }

    // 로그인 처리
    public void forceLogin() {
        mOAuthLoginInstance.startOauthLoginActivity((Activity)mContext, mOAuthLoginHandler);
    }

    // 로그아웃 처리(토큰도 함께 삭제)
    public void forceLogout() {
        // 스레드로 돌려야 한다. 안 그러면 로그아웃 처리가 안되고 false를 반환한다.
        new Thread(new Runnable() {
            @Override
            public void run() {
                mOAuthLoginInstance.logoutAndDeleteToken(mContext);
            }
        }).start();
    }

    // 로그인을 처리할 핸들러
    private OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {
            if(success) {
                String accessToken = mOAuthLoginInstance.getAccessToken(mContext);
                String refreshToken = mOAuthLoginInstance.getRefreshToken(mContext);
                String tokenType = mOAuthLoginInstance.getTokenType(mContext);
                long expiresAt = mOAuthLoginInstance.getExpiresAt(mContext);

                new RequestApiTask(accessToken).execute(); // 로그인이 성공하면 네이버의 계정 정보를 가져온다.
            } else {
                // 로그인 실패 처리
                String errorCode = mOAuthLoginInstance.getLastErrorCode(mContext).getCode();
                String errorDesc = mOAuthLoginInstance.getLastErrorDesc(mContext);
                Toast.makeText(mContext, "errorCode:" + errorCode + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show();
            }
        }
    };

    public class RequestApiTask extends AsyncTask<Void, Void, StringBuffer> {
        private String token;

        RequestApiTask(String token) {
            this.token = token;
        }

        @Override
        protected void onPostExecute(StringBuffer result) {
            super.onPostExecute(result);
            // 로그인 처리가 완료되면 수행할 로직 작성
            processAuthResult(result);
        }

        @Override
        protected StringBuffer doInBackground(Void... params) {
            String header = "Bearer " + token;
            try {
                final String apiURL = "https://openapi.naver.com/v1/nid/me";
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Authorization", header);
                int responseCode = con.getResponseCode();

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        responseCode == 200 ? con.getInputStream() : con.getErrorStream()));

                String inputLine;
                StringBuffer response = new StringBuffer();
                while((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }

                br.close();
                return response;

            } catch(Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private void processAuthResult(StringBuffer response) {
        try {
            // response는 json encoded된 상태이기 때문에 json 형식으로 decode 해줘야 한다.
            JSONObject object = new JSONObject(response.toString());
            JSONObject innerJson = new JSONObject(object.get("response").toString());

            // 만약 이메일이 필요한데 사용자가 이메일 제공을 거부하면
            // JSON 데이터에는 email이라는 키가 없고, 이걸로 제공 여부를 판단한다.
            if(!innerJson.has("email")) {
                Log.d("Naver", object.toString());
                Log.d("Naver", innerJson.toString());
                forceLogout();

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                alertDialogBuilder.setTitle("네이버 정보 동의");
                alertDialogBuilder
                        .setMessage("이메일 정보 제공에 동의해주세요.")
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                forceLogin();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                // 다이얼로그 생성
                AlertDialog alertDialog = alertDialogBuilder.create();

                // 다이얼로그 보여주기
                alertDialog.show();
            } else {
                String uid = innerJson.getString("id");
                String account = innerJson.getString("email");
                Log.d("NaverInfo", innerJson.toString());

                // 원하는 모든 과정이 처리가 되면 해당 멤버 데이터를 가지고 다음 로직을 수행한다.
                if (listener != null)
                    listener.onLogined(uid, account);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
