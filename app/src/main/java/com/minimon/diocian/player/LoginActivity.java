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

        setImageInButton(R.drawable.ico_intro_minimon, R.id.btnNaver, 40);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setImageInButton(int drawableID, int btnID, int height) {
        Drawable drawable = ContextCompat.getDrawable(LoginActivity.this, drawableID);
        int right = (int) (drawable.getIntrinsicWidth() * ((float)height / drawable.getIntrinsicHeight()));
        drawable.setBounds(0, 0, right, height);

        Button btn = findViewById(btnID);
        btn.setCompoundDrawables(drawable, null, null, null);
    }
}
