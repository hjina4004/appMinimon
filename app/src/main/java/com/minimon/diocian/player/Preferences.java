package com.minimon.diocian.player;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Created by GOOD on 2018-02-19.
 */

public class Preferences extends Activity{
    public static final int LOGIN = 1;
    private static Preferences instance;
    int mode = 0;
    public Preferences(int mMode){
        mode = mMode;
    }
    public static Preferences getInstance(int mode){
        if(instance == null){
            instance = new Preferences(mode);
        }
        return instance;
    }
    public void getPreferences(){
        if(mode == LOGIN){
            SharedPreferences pref = getSharedPreferences("login",MODE_PRIVATE);
            UserInfo.getInstance().setToken(pref.getString("token",""));
            UserInfo.getInstance().setUID(pref.getString("uid",""));

//            UserInfo.getInstance().setNickname(pref.getString("nickname",""));
//            UserInfo.getInstance().setEmail(pref.getString("email",""));
//            UserInfo.getInstance().setState(pref.getString("state",""));
//            UserInfo.getInstance().setAdult(pref.getString("adult",""));
//            UserInfo.getInstance().setCertificate(pref.getString("certificate",""));
            UserInfo.getInstance().setSocial(pref.getString("social",""));
//            UserInfo.getInstance().setLoc(pref.getString("loc",""));
//            UserInfo.getInstance().setPoint(pref.getString("point",""));
//            UserInfo.getInstance().setFixed();
        }
    }

    public void setPreferences(){
        if(mode == LOGIN){
            SharedPreferences pref = getSharedPreferences("login",MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("token",UserInfo.getInstance().getToken());
            editor.putString("uid",UserInfo.getInstance().getUID());
            editor.putString("social",UserInfo.getInstance().getSocial());
            editor.commit();
//            editor.putString("nickname",UserInfo.getInstance().getNickname());
//            editor.putString("email",UserInfo.getInstance().getEmail());
//            editor.putString("state",UserInfo.getInstance().getState());
//            editor.putString("adult",UserInfo.getInstance().getAdult());
//            editor.putString("")
        }
    }
}
