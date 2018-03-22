package com.minimon.diocian.player;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.nhn.android.naverlogin.ui.OAuthLoginDialogMng;

import org.json.JSONException;
import org.json.JSONObject;

public class LauncherActivity extends AppCompatActivity {
    private final String PREF_NAME = "minimon-preference";

    private MinimonUser minimonUser;

    private String strUID;
    private String strPwd;
    private String token;
    private String social;

    private long timeout_delay = 3000;
    private boolean enoughWait = false;
    private boolean respondeLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        initMinimon();
        loadLoginInfo();

        tryAutoLogin();
    }

    private void initMinimon() {
        minimonUser = new MinimonUser();
        minimonUser.setListener(new MinimonUser.MinimonUserListener() {
            @Override
            public void onResponse(JSONObject info) {
                try {
                    String currentRequest = info.has("current_request")? info.getString("current_request"):"";
                    if (currentRequest.equals("login"))     resultMinimonLogin(info);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    private void loadLoginInfo(){
        SharedPreferences preferences = getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        strUID = preferences.getString("userUID","");
        strPwd = preferences.getString("userPWD","");
        token = preferences.getString("token","");
        social = preferences.getString("social","");
    }

    private void tryAutoLogin(){
        if(strUID.isEmpty()){
            goToGate();
        }else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            enoughWait = true;
                            gotoMain();
                        }
                    });
                }
            }, timeout_delay);
            loginMinimon();
        }
    }

    private void loginMinimon(){
        String myVersion = Build.VERSION.RELEASE;
        String myDeviceModel = Build.MODEL;
        ContentValues loginInfo = new ContentValues();
        String socialValue = "";
        if("GG".equals(social) || "FB".equals(social) || "NV".equals(social) || "KK".equals(social)) {
            socialValue = "social";
            strUID = strUID.substring(2);
        }
        else
            socialValue = "basic";
        loginInfo.put("type",socialValue);
        loginInfo.put("id",strUID);
        if(socialValue.equals("social"))
            loginInfo.put("value",social);
        else
            loginInfo.put("value",strPwd);
        loginInfo.put("device_id", DeviceUuidFactory.getDeviceUuid(this.getApplicationContext()));
        loginInfo.put("device_token", FirebaseInstanceId.getInstance().getToken());
        loginInfo.put("device_os",myVersion);
        loginInfo.put("device_device",myDeviceModel);
        minimonUser.login(loginInfo);
    }

    /*
    private void loginMinimon(String uid, String password, String type) {
        if("social".equals(type)){
            saveAutoLogin(true);
        }
        mDialogMng.showProgressDlg(mContext,"미니몬 로그인 중입니다",null);
        String myVersion = Build.VERSION.RELEASE;
        String myDeviceModel = Build.MODEL;
        ContentValues loginInfo = new ContentValues();
        loginInfo.put("type", type);
        loginInfo.put("id", uid);
        loginInfo.put("value", password);
        loginInfo.put("device_id", DeviceUuidFactory.getDeviceUuid(this.getApplicationContext()));
        loginInfo.put("device_token", FirebaseInstanceId.getInstance().getToken());
        loginInfo.put("device_os",myVersion);
        loginInfo.put("device_device",myDeviceModel);

        print_error(null);
        minimonUser.login(loginInfo);
    }
     */

    private void resultMinimonLogin(JSONObject info) {
        try {
            UserInfo userInfo = UserInfo.getInstance();
            String resCode = info.has("resCode") ? info.getString("resCode") : "";
            String typeSocial = info.has("current_social") ? info.getString("current_social") : "basic";

            if (resCode.equals("0000")) {
                userInfo.setData(info.getJSONObject("data"));
                userInfo.setPWD(strPwd);
                setSetting();
//                saveLoginInfo();
                respondeLogin = true;
                gotoMain();
            }else{
                Log.d("LauncherLog","not 0000 error");
                goToGate();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setSetting(){
        ConfigInfo configInfo = ConfigInfo.getInstance();
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        configInfo.setBandwidth(pref.getInt("BandWidth",1));
        configInfo.setUseData(pref.getBoolean("useData",true));
        configInfo.setAlertEvent(pref.getBoolean("alertEvent",true));
        configInfo.setAlertNotice(pref.getBoolean("alertNotice",true));
        editor.putInt("BandWidth",configInfo.getBandwidth());
        editor.putBoolean("useData",configInfo.isUseData());
        editor.putBoolean("alertEvent",configInfo.isAlertEvent());
        editor.putBoolean("alertNotice",configInfo.isAlertNotice());
        editor.apply();
    }

    public void gotoMain() {
        if (enoughWait && respondeLogin) {
            Intent intent = new Intent(this.getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void goToGate(){
        Intent intent = new Intent(this.getApplicationContext(), GateActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
