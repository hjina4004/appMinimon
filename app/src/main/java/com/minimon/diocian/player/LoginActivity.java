package com.minimon.diocian.player;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.ApiErrorCode;
import com.kakao.usermgmt.LoginButton;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.User;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;
import com.nhn.android.naverlogin.ui.OAuthLoginDialogMng;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private final String TAG = "LoginActivity";
    private final String PREF_NAME = "minimon-preference";
    private String strUID = "";

    private OAuthLoginDialogMng mDialogMng;

    private MinimonUser minimonUser;
    private NaverLogin naverLogin;

    // for kakao
    private SessionCallback callback;
    private LoginButton btn_kakao_login;

    // for facebook
    private CallbackManager callbackManager;
    private com.facebook.login.widget.LoginButton btn_facebook_login;

    // for Google
    private static final int GOOGLE_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;

    private Context mContext;

    String token, social;

    boolean isAutoLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mContext = this;

        init();
    }

    public void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.a001_top_back);

        strUID = getIntent().getStringExtra("uid");
        if (strUID != null && strUID.length() > 0) {
            EditText text = findViewById(R.id.inUserID);
            text.setText(strUID);
        }

        mDialogMng = new OAuthLoginDialogMng();

        initMinimon();
        initNaver();
        initKakao();
        initFacebook();
        initGoogle();
        tryAutoLogin();
    }

    private void initMinimon() {
        minimonUser = new MinimonUser();
        minimonUser.setListener(new MinimonUser.MinimonUserListener() {
            @Override
            public void onResponse(JSONObject info) {
                Log.d(TAG, "MinimonUserListener - onResponse: " + info);
                try {
                    String currentRequest = info.has("current_request")? info.getString("current_request"):"";
                    if (currentRequest.equals("login"))     resultMinimonLogin(info);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    private void resultMinimonLogin(JSONObject info) {
        mDialogMng.hideProgressDlg();
        try {
            UserInfo userInfo = UserInfo.getInstance();
            String resCode = info.has("resCode") ? info.getString("resCode") : "";
            String typeSocial = info.has("current_social") ? info.getString("current_social") : "basic";

            if (resCode.equals("0000")) {
                userInfo.setData(info.getJSONObject("data"));
                userInfo.setPWD(getInputPassword());
                setSetting();
                saveLoginInfo();
                gotoMain();
            } else if (resCode.equals("0402") && !typeSocial.equals("basic")) {
                newMemberSNS(typeSocial, userInfo.getUID(), userInfo.getEmail());
            } else {
                print_error(R.string.notice_error_login);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initNaver() {
        naverLogin = new NaverLogin(mContext);
        naverLogin.setListener(new NaverLogin.NaverLoginListener() {
            @Override
            public void onLogined(String uid, String email) {
                UserInfo userInfo = UserInfo.getInstance();
                userInfo.setUID(uid);
                userInfo.setEmail(email);
                loginMinimon(uid, "NV", "social");
            }

            @Override
            public void onLogout() {
            }
        });

        Button loginButton = findViewById(R.id.btnNaver);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                naverLogin.forceLogin();
            }
        });
    }

    private void initKakao() {
        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);

        btn_kakao_login = findViewById(R.id.login_button_activity);
        Button loginButton = findViewById(R.id.btnKakao);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogMng.showProgressDlg(mContext, "카카오 계정으로 로그인 중입니다.", null);
                if (!Session.getCurrentSession().checkAndImplicitOpen()) {
                    btn_kakao_login.performClick();
                } else {}
            }
        });
    }

    private void initFacebook() {
        callbackManager = CallbackManager.Factory.create();

        btn_facebook_login = findViewById(R.id.login_button_facebook);
        btn_facebook_login.setReadPermissions("public_profile", "email");
        Button loginButton = findViewById(R.id.btnFacebook);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogMng.showProgressDlg(mContext, "페이스북 계정으로 로그인 중입니다.", null);
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                if (accessToken == null) {
                    btn_facebook_login.performClick();
                } else {
                    requestFacebookUser(accessToken);
                }
            }
        });

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mDialogMng.hideProgressDlg();
                requestFacebookUser(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG,"FacebookCallback onCancel");
                mDialogMng.hideProgressDlg();
            }

            @Override
            public void onError(FacebookException exception) {
                Log.d(TAG,"FacebookCallback onError");
                mDialogMng.hideProgressDlg();
            }
        });
    }

    private void initGoogle() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        Button loginButton = findViewById(R.id.btnGoogle);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogMng.showProgressDlg(mContext, "구글 계정으로 로그인 중입니다.", null);
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(LoginActivity.this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }

        // for facebook
        callbackManager.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            mDialogMng.hideProgressDlg();
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
//                mDialogMng.hideProgressDlg();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId() + ", " + acct.getEmail());
        String uid = acct.getId();
        String email = acct.getEmail();

        UserInfo userInfo = UserInfo.getInstance();
        userInfo.setUID(uid);
        userInfo.setEmail(email);
        loginMinimon(uid, "GG", "social");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Session.getCurrentSession().removeCallback(callback);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {
            mDialogMng.hideProgressDlg();
            requestKakaoUser();
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            mDialogMng.hideProgressDlg();
            if(exception != null) {
                Logger.e(exception);
            }
        }
    }

    /**
     * 사용자의 상태를 알아 보기 위해 me API 호출을 한다.
     */
    protected void requestKakaoUser() {
        UserManagement.getInstance().requestMe(new MeResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                String message = "failed to get user info. msg=" + errorResult;
                Log.d(TAG,message);

                int result = errorResult.getErrorCode();
                if (result == ApiErrorCode.CLIENT_ERROR_CODE) {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_message_for_service_unavailable), Toast.LENGTH_SHORT).show();
                } else {
                    redirectLoginActivity();
                }
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                redirectLoginActivity();
            }

            @Override
            public void onSuccess(UserProfile userProfile) {
                Log.d(TAG,"UserProfile : " + userProfile);
                String uid = String.valueOf(userProfile.getId());
                String email = userProfile.getEmail();

                UserInfo userInfo = UserInfo.getInstance();
                userInfo.setUID(uid);
                userInfo.setEmail(email);
                loginMinimon(uid, "KK", "social");
            }

            @Override
            public void onNotSignedUp() {
                showSignup();
            }
        });
    }

    private void redirectLoginActivity() {
        Log.d(TAG,"redirectLoginActivity");
    }
    private void showSignup() {
        Log.d(TAG,"showSignup");
    }

    private void requestFacebookUser(AccessToken accessToken) {
        GraphRequest graphRequest = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                Log.v(TAG, "GraphRequest result:" + response.toString());
                try {
                    String uid = object.getString("id");
                    String email = object.has("email")? object.getString("email"):"";

                    UserInfo userInfo = UserInfo.getInstance();
                    userInfo.setUID(uid);
                    userInfo.setEmail(email);
                    loginMinimon(uid, "FB", "social");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender");
        graphRequest.setParameters(parameters);
        graphRequest.executeAsync();
    }

    public void onClickLogin(View view) {
        Log.d("LoginActivity:", "onClickLogin start");
//        mDialogMng.showProgressDlg(mContext,"미니몬 로그인 중입니다",null);
        loginMinimon(getInputUID(), getInputPassword(), "basic");
    }

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

    public void onClickFindMember(View view) {
        Intent intent = new Intent(LoginActivity.this.getApplicationContext(), FindMemberActivity.class);
        startActivity(intent);
    }

    public void onClickNewMember(View view) {
        Intent intent = new Intent(LoginActivity.this.getApplicationContext(), NewMemberActivity.class);
        intent.putExtra("type", "basic");
        startActivity(intent);
    }

    private void newMemberSNS(String nameSNS, String uid, String email) {
        mDialogMng.hideProgressDlg();

        Intent intent = new Intent(LoginActivity.this.getApplicationContext(), NewMemberActivity.class);
        intent.putExtra("type", nameSNS);
        intent.putExtra("uid", uid);
        intent.putExtra("email", email);
        intent.putExtra("device_id", DeviceUuidFactory.getDeviceUuid(this.getApplicationContext()));
        startActivity(intent);
    }

    private void saveAutoLogin(boolean isAuto) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("AutoLogin", isAuto? "1":"0");
        editor.apply();

        isAutoLogin = isAuto;
    }

    public boolean loadAutoLogin() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return prefs.getString("AutoLogin", "0").equals("1");
    }

    private void saveLoginData(){
        if(!loadAutoLogin())
            return;
        SharedPreferences prefs = getSharedPreferences("minimon-preference",MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("AutoLogin", "1");
        editor.putString("token", UserInfo.getInstance().getToken());
        editor.putString("social", UserInfo.getInstance().getSocial());
        if("basic".equals(UserInfo.getInstance().getSocial())){
            editor.putString("userUID", getInputUID());
            editor.putString("userPWD", getInputPassword());
        }
        editor.apply();
    }

    public void tryAutoLogin(){
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String token = prefs.getString("token","");
        String social = prefs.getString("social","");
        if("".equals(token) || token == null || token.isEmpty()){ // 아이디가 이미 저장되어 있으면 토큰을 저장하도록 하므로 토근이 없으면 로그인시도
        }else{ // 토큰이 있는경우 자동로그인 처리
            if("KK".equals(social)){
                Button loginButton = findViewById(R.id.btnKakao);
                loginButton.performClick();
            }else if("FB".equals(social)){
                Button loginButton = findViewById(R.id.btnFacebook);
                loginButton.performClick();
            }else if("NV".equals(social)){
                Button loginButton = findViewById(R.id.btnNaver);
                loginButton.performClick();
            }else if("GG".equals(social)){
                Button loginButton = findViewById(R.id.btnGoogle);
                loginButton.performClick();
            }else{
                String userUID = prefs.getString("userUID","");
                String userPWD = prefs.getString("userPWD","");

                ((EditText) findViewById(R.id.inUserID)).setText(userUID);
                ((EditText) findViewById(R.id.inUserPW)).setText(userPWD);
                loginMinimon(userUID, userPWD,"basic");
            }
        }
    }

    private String getInputUID() {
        EditText text = findViewById(R.id.inUserID);
        return text.getText().toString();
    }

    private String getInputPassword() {
        EditText text = findViewById(R.id.inUserPW);
        return text.getText().toString();
    }

    public void gotoMain() {
        Intent intent = new Intent(LoginActivity.this.getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void print_error(Integer strID) {
        TextView view = findViewById(R.id.textViewError);
        if (strID == null) {
            view.setText("");
        } else {
            view.setText(strID);
        }
    }

    private void saveLoginInfo() {
        UserInfo userInfo = UserInfo.getInstance();
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("token",userInfo.getToken());
        editor.putString("social", userInfo.getSocial());
        editor.putString("userUID",userInfo.getUID());
        editor.putString("userPWD",userInfo.getPWD());
        editor.apply();
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
}
