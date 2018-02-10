package com.minimon.diocian.player;


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

public class NewMemberActivity extends AppCompatActivity {
    String strType = "basic";

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
        Log.i("strType", strType);
        if (!strType.equals("basic")) modeSNS();

        initAggre();
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

    private void initAggre() {
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
        }) ;
        cbAgreeTemrsOfUse.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox)v).isChecked()) {
                    // TODO : CheckBox is checked.
                    if (cbAgreePrivacyPolicy.isChecked())  cbAgreeTotal.setChecked(true);
                } else {
                    // TODO : CheckBox is unchecked.
                    cbAgreeTotal.setChecked(false);
                }
            }
        }) ;
        cbAgreePrivacyPolicy.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox)v).isChecked()) {
                    // TODO : CheckBox is checked.
                    if (cbAgreeTemrsOfUse.isChecked())  cbAgreeTotal.setChecked(true);
                } else {
                    // TODO : CheckBox is unchecked.
                    cbAgreeTotal.setChecked(false);
                }
            }
        }) ;
    }
}
