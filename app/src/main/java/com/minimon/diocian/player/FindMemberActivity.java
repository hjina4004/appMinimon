package com.minimon.diocian.player;


import android.content.ContentValues;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FindMemberActivity extends AppCompatActivity {
    private final String TAG = "FindMemberActivity";
    private MinimonUser minimonUser;

    private JUtil jUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_member);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.a001_top_back);

        initMinimon();

        jUtil = new JUtil();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(FindMemberActivity.this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initMinimon() {
        minimonUser = new MinimonUser();
        minimonUser.setListener(new MinimonUser.MinimonUserListener() {
            @Override
            public void onResponse(JSONObject info) {
                Log.d(TAG, "MinimonUserListener - onResponse: " + info);
                try {
                    String currentRequest = info.has("current_request")? info.getString("current_request"):"";
                    if (currentRequest.equals("find"))                  resultMinimonFind(info);
                    else if (currentRequest.equals("resetPassword"))    resultMinimonResetPassword(info);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    private void resultMinimonFind(JSONObject info) {
        try {
            String resCode = info.has("resCode") ? info.getString("resCode") : "";

            if (resCode.equals("0000")) {
                JSONArray jsonArray = info.getJSONObject("data").getJSONArray("list");
                String foundUIDs = "";
                for (int i = 0; i < 1/*jsonArray.length()*/; i ++) {
                    String uid = jsonArray.getJSONObject(i).getString("id");
                    String text = "\n" + String.format(getResources().getString(R.string.notice_find_id), uid);
                    foundUIDs = foundUIDs.concat(text);
                }
                foundUIDs = foundUIDs.replaceFirst("\n", "");
                alertNotice(foundUIDs);
            } else {
                alertNotice(getResources().getString(R.string.notice_not_found_id));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void resultMinimonResetPassword(JSONObject info) {
        try {
            String resCode = info.has("resCode") ? info.getString("resCode") : "";

            if (resCode.equals("0000")) {
                alertNotice(info.getString("msg"));
            } else {
                alertNotice("실패 : " + info.getString("msg"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onClickFindMember(View view) {
        EditText text = findViewById(R.id.editTextEmail);
        String strEmail[] = text.getText().toString().split("[@]");

        ContentValues info = new ContentValues();
        info.put("email_id", strEmail[0]);
        info.put("email_domain", strEmail[1]);

        minimonUser.find(info);
    }

    public void onClickResetPassword(View view) {
        EditText textUID = findViewById(R.id.editTextUserID);
        EditText textEmail = findViewById(R.id.editTextUserEmail);
        String strEmail[] = textEmail.getText().toString().split("[@]");

        ContentValues info = new ContentValues();
        info.put("email_id", strEmail[0]);
        info.put("email_domain", strEmail[1]);
        info.put("id", textUID.getText().toString());

        minimonUser.find(info);
    }

    private void alertNotice(String str) {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        jUtil.alertNotice(FindMemberActivity.this, str, null);
    }
}
