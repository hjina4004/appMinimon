package com.minimon.diocian.player;


import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class NewMemberActivity extends AppCompatActivity {
    private final String TAG = "NewMemberActivity";
    private String strType = "basic";
    private String strDeviceID;

    private MinimonUser minimonUser;

    private CheckBox cbAgreeTotal;
    private CheckBox cbAgreeTemrsOfUse;
    private CheckBox cbAgreePrivacyPolicy;

    private Button btnTermOfUse;
    private Button btnPrivacyPolicy;
    private ContentValues infoNewMember;

//    private boolean

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_member);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.a001_top_back);

        btnTermOfUse = findViewById(R.id.btnTermOfUse);
        btnTermOfUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewMemberActivity.this, PolicyActivity.class);
                startActivity(intent);
            }
        });
        btnPrivacyPolicy = findViewById(R.id.btnPrivacyPolicy);
        btnPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewMemberActivity.this, PolicyActivity.class);
                startActivity(intent);
            }
        });
        strType = getIntent().getStringExtra("type");
        strDeviceID = DeviceUuidFactory.getDeviceUuid(this.getApplicationContext());
        Log.i("strType", strType);
        if (!strType.equals("basic")) modeSNS();

        initMinomon();
        initAgree();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //finish();
            NavUtils.navigateUpFromSameTask(NewMemberActivity.this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void modeSNS() {
        EditText editText = findViewById(R.id.editTextID);
        editText.setText(getIntent().getStringExtra("uid"));
        View view = findViewById(R.id.layoutUserID);
        enableViews(view, false);

        view = findViewById(R.id.layoutUserPW);
        enableViews(view, false);

        view = findViewById(R.id.layoutUserPWConfirm);
        enableViews(view, false);

        editText = findViewById(R.id.editTextEmail);
        editText.setText(getIntent().getStringExtra("email"));
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        view = findViewById(R.id.layoutUserEmail);
        enableViews(view, false);
    }

    private void enableViews(View v, boolean enabled) {
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0;i<vg.getChildCount();i++) {
                enableViews(vg.getChildAt(i), enabled);
            }
        }
        v.setEnabled(enabled);
        v.setAlpha(0.5F);
        v.setFocusable(false);
    }

    private void initMinomon() {
        minimonUser = new MinimonUser();
        minimonUser.setListener(new MinimonUser.MinimonUserListener() {
            @Override
            public void onResponse(JSONObject info) {
                Log.d(TAG, "MinimonUserListener - onResponse: " + info);
                try {
                    String currentRequest = info.has("current_request")? info.getString("current_request"):"";
                    if (currentRequest.equals("create"))     resultMinimonSignup(info);
                    else if (currentRequest.equals("login")) resultMinimonLogin(info);
                    else if (currentRequest.equals("checked")){
                        resultOverlap(info);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    private void checkOverlap(String field, String value){
        ContentValues overlap = new ContentValues();
        Log.d("checkoverlapField",field);
        Log.d("checkoverlapValue",value);
        overlap.put("field",field);
        overlap.put("value",value);
        minimonUser.checked(overlap);
    }

    private void resultOverlap(JSONObject info){
        try {
            String resCode = info.has("resCode") ? info.getString("resCode") : "";
            String msg = info.has("msg")? info.getString("msg") : "";
            String field = info.getString("valid_field");

            if(!resCode.equals("0000")){
                Log.d("overlapRescode",resCode);
                Log.d("overlapInfo",info.toString());
                String errMsg = "";
                if("0900".equals(resCode))
                    errMsg = info.getJSONObject("data").getString("errMsg");
                else
                    errMsg = msg;
                if("id".equals(field))
                    findViewById(R.id.editTextID).requestFocus();
                else if("email".equals(field))
                    findViewById(R.id.editTextEmail).requestFocus();
                else if("nickname".equals(field))
                    findViewById(R.id.editTextNickname).requestFocus();
                alertNotice(errMsg);
                return;
            }else{
                if("id".equals(field)){
                    checkOverlap("nickname",getInputNickname());
                }else if("nickname".equals(field))
                    checkOverlap("email",getInputEmail());
                else if("email".equals(field)) {
                    minimonUser.create(infoNewMember);
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void resultMinimonSignup(JSONObject info) {
        try {
            String resCode = info.has("resCode") ? info.getString("resCode") : "";

            if (resCode.equals("0000")) {
                alertNoticeSignup();
            } else if (resCode.equals("0400")) {
                alertNotice(info.getString("msg"));
            } else {
                alertNotice(info.getJSONObject("data").getString("errMsg"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void resultMinimonLogin(JSONObject info) {
        try {
            UserInfo userInfo = UserInfo.getInstance();
            String resCode = info.has("resCode") ? info.getString("resCode") : "";
            if (resCode.equals("0000")) {
                userInfo.setData(info.getJSONObject("data"));
                gotoMain();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initAgree() {
        cbAgreeTotal = findViewById(R.id.cbAgreeTotal);
        cbAgreeTemrsOfUse = findViewById(R.id.cbAgreeTemrsOfUse) ;
        cbAgreePrivacyPolicy = findViewById(R.id.cbAgreePrivacyPolicy) ;

        cbAgreeTotal.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = ((CheckBox)v).isChecked();
                cbAgreeTemrsOfUse.setChecked(isChecked);
                cbAgreePrivacyPolicy.setChecked(isChecked);
            }
        });
        cbAgreeTemrsOfUse.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox)v).isChecked()) {
                    if (cbAgreePrivacyPolicy.isChecked())  cbAgreeTotal.setChecked(true);
                } else {
                    cbAgreeTotal.setChecked(false);
                }
            }
        });
        cbAgreePrivacyPolicy.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox)v).isChecked()) {
                    if (cbAgreeTemrsOfUse.isChecked())  cbAgreeTotal.setChecked(true);
                } else {
                    cbAgreeTotal.setChecked(false);
                }
            }
        });
    }

    public void onClickSignup (View view) {
//        type			    String	O	회원타입	basic: 기본회원 , social: 소셜회원
//        id			    String	O	아이디
//        value			    String	O	패스워도 혹은 소셜구분값	"type이 basic일때 패스워드 type이 social일때 소셜타입값 FB:facebook, GG:google, NV:naver, KK:kakao"
//        email			    String	O	이메일
//        nickname			String	O	닉네임
        String strUID = getInputUID();
        if (strUID.length() == 0) return;
//        checkOverlap("id",strUID);

        String strNickname = getInputNickname();
        if (strNickname.length() == 0) return;
//        checkOverlap("nickname",strNickname);

        String strEmail = getInputEmail();
        if (strEmail.length() == 0) return;
//        checkOverlap("email",strEmail);

        String strPW = getInputPassword();
        if (strPW.length() == 0) return;


        if (!cbAgreeTotal.isChecked()) {
            alertNotice(R.string.notice_error_agree);
            return;
        }

        UserInfo.getInstance().setUID(strUID);

        infoNewMember = new ContentValues();
        infoNewMember.put("type", getType());
        infoNewMember.put("id", strUID);
        infoNewMember.put("value", strPW);
        infoNewMember.put("email", strEmail);
        infoNewMember.put("nickname", strNickname);
        infoNewMember.put("device_id", strDeviceID);
        createNewMinimonMember();
//        minimonUser.create(info);
    }

    private void createNewMinimonMember(){
        checkOverlap("id",getInputUID());
    }

    private String getType() {
        if (strType.equals("basic"))
            return strType;

        return "social";
    }

    private String getInputUID() {
        EditText text = findViewById(R.id.editTextID);
        text.setImeOptions(EditorInfo.IME_ACTION_DONE);
        if (text.getText().length() < 1) {
            alertNotice(R.string.notice_none_input_id);
            return "";
        }

        String strInput = text.getText().toString();
        if (!strType.equals("basic"))
            return strInput;

        boolean flag = Pattern.matches("^[a-zA-Z\\d]{4,12}$", strInput);
        if (!flag) {
            alertNotice(R.string.notice_error_input_id);
            strInput = "";
        }
        return strInput;
    }

    private String getInputPassword() {
        EditText text = findViewById(R.id.editTextPW);
        text.setImeOptions(EditorInfo.IME_ACTION_DONE);
        if (!strType.equals("basic"))
            return strType;

        String strInput = text.getText().toString();
        if (strInput.length() < 1) {
            alertNotice(R.string.notice_none_input_pw);
            return "";
        }

        boolean flag = Pattern.matches("^[a-zA-Z\\d]{6,12}$", strInput);
        if (!flag) {
            alertNotice(R.string.notice_error_input_pw);
            strInput = "";
        } else if (!confirmPassword(strInput)) {
            alertNotice(R.string.notice_error_input_pwc);
            strInput = "";
        }
        return strInput;
    }

    private String getInputEmail() {
        EditText text = findViewById(R.id.editTextEmail);
        text.setImeOptions(EditorInfo.IME_ACTION_DONE);
        if (text.getText().length() < 1) {
            alertNotice(R.string.notice_none_input_email);
            return "";
        }

        String strInput = text.getText().toString();
        boolean flag = Pattern.matches("^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$", strInput);
        if (!flag) {
            alertNotice(R.string.notice_error_input_email);
            strInput = "";
        }
        return strInput;
    }

    private boolean confirmPassword(String strPW) {
        EditText text = findViewById(R.id.editTextPWC);
        text.setImeOptions(EditorInfo.IME_ACTION_DONE);
        return strPW.equals(text.getText().toString());
    }

    private String getInputNickname() {
        EditText text = findViewById(R.id.editTextNickname);
        text.setImeOptions(EditorInfo.IME_ACTION_DONE);
        if (text.getText().length() < 1) {
            alertNotice(R.string.notice_none_input_nickname);
            return "";
        }

        String strInput = text.getText().toString();
        boolean flag = Pattern.matches("^[a-zA-Z\\d]{2,12}$", strInput);
        if (!flag) {
            alertNotice(R.string.notice_error_input_nickname);
            strInput = "";
        }
        return strInput;
    }

    private void alertNotice(int strID) {
        alertNotice(getResources().getString(strID));
    }

    private void alertNotice(String str) {
        JUtil util = new JUtil();
        util.alertNotice(NewMemberActivity.this, str, null);
    }

    private void alertNoticeSignup() {
        JUtil util = new JUtil();
        util.alertNotice(NewMemberActivity.this, getResources().getString(R.string.notice_success_signup), new JUtil.JUtilListener() {
            @Override
            public void callback(int id) {
                String uid = getInputUID();
                String password = getInputPassword();
                String type = getType();

                loginMinimon(uid, password, type);
            }
        });
    }



    private void loginMinimon(String uid, String password, String type) {
        ContentValues loginInfo = new ContentValues();
        loginInfo.put("type", type);
        loginInfo.put("id", uid);
        loginInfo.put("value", password);
        loginInfo.put("device_id", strDeviceID);

        minimonUser.login(loginInfo);
    }

    private void gotoMain() {
        Intent intent = new Intent(NewMemberActivity.this.getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
