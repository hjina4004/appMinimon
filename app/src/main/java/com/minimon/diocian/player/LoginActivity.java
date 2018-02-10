package com.minimon.diocian.player;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.ApiErrorCode;
import com.kakao.usermgmt.LoginButton;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;
import com.nhn.android.naverlogin.ui.OAuthLoginDialogMng;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private String TAG = "LoginActivity";

    private OAuthLoginDialogMng mDialogMng;

    private MinimonUser minimonUser;
    private NaverLogin naverLogin;

    // for kakao
    private SessionCallback callback;
    private LoginButton btn_kakao_login;

    // for facebook
    private CallbackManager callbackManager;
    com.facebook.login.widget.LoginButton btn_facebook_login;

    // for Google
    private static final int GOOGLE_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mFirebaseAuth;

    private static Context mContext;

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

//        setImageInButton(R.mipmap.a001_social_naver, R.id.btnNaver);
//        setImageInButton(R.mipmap.a001_social_kakao, R.id.btnKakao);
//        setImageInButton(R.mipmap.a001_social_facebook, R.id.btnFacebook);
//        setImageInButton(R.mipmap.a001_social_google, R.id.btnGoogle);

        mDialogMng = new OAuthLoginDialogMng();

        initAutoLogin();

        initMinomon();
        initNaver();
        initKakao();
        initFacebook();
        initGoogle();
    }

    private void initMinomon() {
        minimonUser = new MinimonUser();
        minimonUser.setListener(new MinimonUser.MinimonLoginListener() {
            @Override
            public void onLogined(JSONObject info) {
                Log.d("LoginActivity:", "onLogined: " + info);
            }
        });
    }

    private void initNaver() {
        naverLogin = new NaverLogin(mContext);
        naverLogin.setListener(new NaverLogin.NaverLoginListener() {
            @Override
            public void onLogined(String uid, String email) {
                newMemberSNS("naver", "naver_" + uid, email);
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
        mFirebaseAuth = FirebaseAuth.getInstance();

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

    private void initAutoLogin() {
        CheckBox checkBox = findViewById(R.id.cbAutoLogin);
        checkBox.setChecked(loadAutoLogin());

        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.getId() == R.id.cbAutoLogin) {
                    saveAutoLogin(isChecked);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //finish();
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
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
                mDialogMng.hideProgressDlg();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mDialogMng.hideProgressDlg();

                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    FirebaseUser user = mFirebaseAuth.getCurrentUser();

                    String name = user.getDisplayName();
                    String email = user.getEmail();
                    String uid = user.getUid();

                    Log.d(TAG, "Google account --- uid=" + uid);
                    Log.d(TAG, "Google account --- email=" + email);
                    Log.d(TAG, "Google account --- name=" + name);

                    newMemberSNS("google", "gg_"+uid, email);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                newMemberSNS("kakao", "kakao_" + userProfile.getId(), userProfile.getEmail());
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
        Log.e("onSuccess", "--------" + accessToken);
        Log.e("Token", "--------" + accessToken.getToken());
        Log.e("Permission", "--------" + accessToken.getPermissions());

        GraphRequest graphRequest = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                Log.v(TAG, "GraphRequest result:" + response.toString());
                try {
                    String id = object.getString("id");
                    String email = object.has("email")? object.getString("email"):"";
                    newMemberSNS("facebook", "fb_" + id, email);
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

    public void setImageInButton(int drawableID, int btnID) {
        Drawable drawable = ContextCompat.getDrawable(LoginActivity.this, drawableID);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        Button btn = (Button) findViewById(btnID);
        btn.setCompoundDrawables(drawable, null, null, null);
    }

    public void onClickLogin(View view) {
        Log.d("LoginActivity:", "onClickLogin start");

        ContentValues loginInfo = new ContentValues();
        loginInfo.put("type", "basic");
        loginInfo.put("id", "hjina");
        loginInfo.put("value", "12345678");
        loginInfo.put("device_id", DeviceUuidFactory.getDeviceUuid(this.getApplicationContext()));

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
        startActivity(intent);
    }

    private void saveAutoLogin(boolean isAuto) {
        SharedPreferences prefs = getSharedPreferences("minimon_preference", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("AutoLogin", isAuto? "1":"0");
        editor.commit();
    }

    public boolean loadAutoLogin() {
        SharedPreferences prefs = getSharedPreferences("minimon_preference", MODE_PRIVATE);
        if (prefs.getString("AutoLogin", "0").equals("1"))
            return true;

        return false;
    }
}
