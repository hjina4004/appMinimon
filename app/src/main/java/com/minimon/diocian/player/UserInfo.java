package com.minimon.diocian.player;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class UserInfo {
    private static String TAG = "UserInfo";
    private static volatile UserInfo sSoleInstance;

    private JSONObject data;
    private String mToken;
    private String mUID;
    private String mNickname;
    private String mEmail;
    private String mState;
    private String mAdult;
    private String mCertificate;
    private String mSocial;
    private String mLoc;
    private String mPoint;
    private String mFixed;

    //private constructor.
    private UserInfo(){
        //Prevent form the reflection api.
        if (sSoleInstance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static UserInfo getInstance() {
        //Double check locking pattern
        if (sSoleInstance == null) { //Check for the first time

            synchronized (UserInfo.class) {   //Check for the second time.
                //if there is no instance available... create new one
                if (sSoleInstance == null) sSoleInstance = new UserInfo();
            }
        }
        return sSoleInstance;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        // {"apiToken":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJ3d3cubWluaW1vbi5jb20iLCJpYXQiOjE1MTg0MTYwNzgsImV4cCI6MjM4MjQxNjA3OCwiZGF0YSI6eyJpZCI6ImhqaW5hIiwia2V5IjozNjE3NDk4NzQwNjYzNDU1MDY0fX0.cMknL1dCx1NADoQN-l9zhulyw0QbE0ptLjQRdbEJTWwvL5t_-TtOdV4YG0-XlrO6FmRFSm6Dl-BkC5vtCku9ng",
        // "userInfo":{"idx":"3974","id":"hjina","name":null,"nickname":"hjina","email":"jina@lmfriends.com","state":"0","is_adult":"0","is_certificate":"0","is_social":"0","loc":null,"point":"0","fixed":"0"}}

        Log.w(TAG, "setData: "+data);
        this.data = data;
        try {
            this.setToken(data.getString("apiToken"));

            JSONObject userInfo = data.getJSONObject("userInfo");
            this.setUID(userInfo.getString("id"));
            this.setNickname(userInfo.getString("nickname"));
            this.setEmail(userInfo.getString("email"));
            this.setState(userInfo.getString("state"));
            this.setAdult(userInfo.getString("is_adult"));
            this.setCertificate(userInfo.getString("is_certificate"));
            this.setSocial(userInfo.getString("is_social"));
            this.setLoc(userInfo.getString("loc"));
            this.setPoint(userInfo.getString("point"));
            this.setFixed(userInfo.getString("fixed"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getToken() {
        return mToken;
    }
    public void setToken(String token) {
        this.mToken = token;
    }

    public String getUID() {
        return mUID;
    }
    public void setUID(String uid) {
        this.mUID = uid;
    }

    public String getNickname() {
        return mNickname;
    }
    public void setNickname(String nickname) {
        this.mNickname = nickname;
    }

    public String getEmail() {
        return mEmail;
    }
    public void setEmail(String value) {
        this.mEmail = value;
    }

    public String getState() {
        return mState;
    }
    public void setState(String value) {
        this.mState = value;
    }

    public String getAdult() {
        return mAdult;
    }
    public void setAdult(String value) {
        this.mAdult = value;
    }

    public String getCertificate() {
        return mCertificate;
    }
    public void setCertificate(String value) {
        this.mCertificate = value;
    }

    public String getSocial() {
        return mSocial;
    }
    public void setSocial(String value) {
        this.mSocial = value;
    }

    public String getLoc() {
        return mLoc;
    }
    public void setLoc(String value) {
        this.mLoc = value;
    }

    public String getPoint() {
        return mPoint;
    }
    public void setPoint(String value) {
        this.mPoint = value;
    }

    public String getFixed() {
        return mFixed;
    }
    public void setFixed(String value) {
        this.mFixed = value;
    }
}
