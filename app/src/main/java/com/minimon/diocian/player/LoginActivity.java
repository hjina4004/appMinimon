package com.minimon.diocian.player;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Button;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.a001_top_back);

        setImageInButton(R.mipmap.a001_social_naver, R.id.btnNaver);
        setImageInButton(R.mipmap.a001_social_kakao, R.id.btnKakao);
        setImageInButton(R.mipmap.a001_social_facebook, R.id.btnFacebook);
        setImageInButton(R.mipmap.a001_social_google, R.id.btnGoogle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setImageInButton(int drawableID, int btnID) {
        Drawable drawable = ContextCompat.getDrawable(LoginActivity.this, drawableID);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        Button btn = findViewById(btnID);
        btn.setCompoundDrawables(drawable, null, null, null);
    }
}
