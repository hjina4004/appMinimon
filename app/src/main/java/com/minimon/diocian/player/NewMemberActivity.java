package com.minimon.diocian.player;


import android.content.ContentValues;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import org.json.JSONObject;

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
            }
        });
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
        ContentValues info = new ContentValues();
//        type			    String	O	회원타입	basic: 기본회원 , social: 소셜회원
//        id			    String	O	아이디
//        value			    String	O	패스워도 혹은 소셜구분값	"type이 basic일때 패스워드 type이 social일때 소셜타입값 FB:facebook, GG:google, NV:naver, KK:kakao"
//        email			    String	O	이메일
//        nickname			String	O	닉네임
        String strUID = getInputUID();
        String strPW = getInputPassword();
        String strEmail = getInputEmail();
        String strNickname = getInputNickname();

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
        return text.getText().toString();
    }

    private String getInputPassword() {
        EditText text = findViewById(R.id.editTextPW);
        if (strType.equals("basic")) {
            return text.getText().toString();
        }

        return strType;
    }

    private String getInputEmail() {
        EditText text = findViewById(R.id.editTextEmail);
        return text.getText().toString();
    }

    private String getInputNickname() {
        EditText text = findViewById(R.id.editTextNickname);
        return text.getText().toString();
    }
}
