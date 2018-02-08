package com.minimon.diocian.player;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class NewMemberActivity extends AppCompatActivity {
    String strType = "basic";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_member);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.a001_top_back);

        strType = getIntent().getStringExtra("type");
        Log.i("strType", strType);
        if (!strType.equals("basic")) modeSNS();
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
}
