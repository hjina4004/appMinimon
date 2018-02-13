package com.minimon.diocian.player;


import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class NewMemberActivity extends AppCompatActivity {
    private final String TAG = "NewMemberActivity";
    String strType = "basic";
    String strDeviceID = "";

    private MinimonUser minimonUser;

    private CheckBox cbAgreeTotal;
    private CheckBox cbAgreeTemrsOfUse;
    private CheckBox cbAgreePrivacyPolicy;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_member);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.a001_top_back);

        strType = getIntent().getStringExtra("type");
        strDeviceID = getIntent().getStringExtra("device_id");
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void resultMinimonSignup(JSONObject info) {
        try {
            String resCode = info.has("resCode") ? info.getString("resCode") : "";

            if (resCode.equals("0000")) {
                gotoLogin();
            } else if (resCode.equals("0400")) {
                alertNotice(info.getString("msg"));
            } else {
                alertNotice(info.getJSONObject("data").getString("errMsg"));
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

        String strNickname = getInputNickname();
        if (strNickname.length() == 0) return;

        String strPW = getInputPassword();
        if (strPW.length() == 0) return;

        String strEmail = getInputEmail();
        if (strEmail.length() == 0) return;

        if (!cbAgreeTotal.isChecked()) {
            alertNotice(R.string.notice_error_agree);
            return;
        }

        UserInfo.getInstance().setUID(strUID);

        ContentValues info = new ContentValues();
        info.put("type", getType());
        info.put("id", strUID);
        info.put("value", strPW);
        info.put("email", strEmail);
        info.put("nickname", strNickname);
        info.put("device_id", strDeviceID);

        minimonUser.create(info);
    }

    private String getType() {
        if (strType.equals("basic"))
            return strType;

        return "social";
    }

    private String getInputUID() {
        EditText text = findViewById(R.id.editTextID);
        if (text.getText().length() < 1) {
            alertNotice(R.string.notice_none_input_id);
            return "";
        }

        String strInput = text.getText().toString();
        boolean flag = Pattern.matches("^[a-zA-Z\\d]{4,12}$", strInput);
        if (!flag) {
            alertNotice(R.string.notice_error_input_id);
            strInput = "";
        }
        return strInput;
    }

    private String getInputPassword() {
        EditText text = findViewById(R.id.editTextPW);
        if (text.getText().length() < 1) {
            alertNotice(R.string.notice_none_input_pw);
            return "";
        }

        if (strType.equals("basic")) {
            String strInput = text.getText().toString();
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

        return strType;
    }

    private String getInputEmail() {
        EditText text = findViewById(R.id.editTextEmail);
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
        return strPW.equals(text.getText().toString());
    }

    private String getInputNickname() {
        EditText text = findViewById(R.id.editTextNickname);
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
        AlertDialog.Builder dialog = new AlertDialog.Builder(NewMemberActivity.this);
        dialog.setMessage(strID);
        dialog.setPositiveButton(R.string.notice_close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        final AlertDialog alert = dialog.create();
        alert.show();
    }

    private void alertNotice(String str) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(NewMemberActivity.this);
        dialog.setMessage(str);
        dialog.setPositiveButton(R.string.notice_close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                gotoLogin();
            }
        });

        final AlertDialog alert = dialog.create();
        alert.show();
    }

    private void gotoLogin() {
        Intent intent = new Intent(NewMemberActivity.this.getApplicationContext(), LoginActivity.class);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("uid", UserInfo.getInstance().getUID());
        startActivity(intent);

        finish();
    }
}
