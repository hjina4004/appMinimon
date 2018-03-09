package com.minimon.diocian.player;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.kakao.util.helper.Utility.getPackageInfo;


public class GateActivity extends AppCompatActivity {
    private final static String TAG = "GateActivity";
    private final String PREF_NAME = "minimon-preference";

    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    private IntroPagerAdapter introPagerAdapter;
    private ViewPager viewPager;
    private LinearLayout pager_indicator;
    private int dotsCount;
    private ImageView[] dots;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate);

        Log.d(TAG, "Hash Key = " + getKeyHash(this.getApplicationContext()));

        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String autoLogin = pref.getString("AutoLogin","0");
        String token = pref.getString("token","");
        if(token != null && token.length() > 1 && "1".equals(autoLogin))
            gotoLogin();

        Button btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoLogin();
            }
        });

        Button btnSignup = (Button) findViewById(R.id.btn_signup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoSignup();
            }
        });

        initIntro();
    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), R.string.notice_exit_app, Toast.LENGTH_SHORT).show();
        }
    }

    public void gotoLogin() {
        Intent intent = new Intent(GateActivity.this.getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    public void gotoSignup() {
        Intent intent = new Intent(GateActivity.this.getApplicationContext(), NewMemberActivity.class);
        intent.putExtra("type", "basic");
        intent.putExtra("device_id", DeviceUuidFactory.getDeviceUuid(this.getApplicationContext()));
        startActivity(intent);
    }

    public static String getKeyHash(final Context context) {
        PackageInfo packageInfo = getPackageInfo(context, PackageManager.GET_SIGNATURES);
        if (packageInfo == null)
            return null;

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                return Base64.encodeToString(md.digest(), Base64.NO_WRAP);
            } catch (NoSuchAlgorithmException e) {
                Log.w(TAG, "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
        return null;
    }

    private void initIntro() {
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        introPagerAdapter = new IntroPagerAdapter(this);
        viewPager.setAdapter(introPagerAdapter);

        dotsCount = introPagerAdapter.getCount();

        drawPageSelectionIndicators(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.d("###onPageSelected, pos ", String.valueOf(position));
                drawPageSelectionIndicators(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void drawPageSelectionIndicators(int mPosition){
        if(pager_indicator!=null) {
            pager_indicator.removeAllViews();
        }
        pager_indicator = findViewById(R.id.viewPagerCountDots);
        dots = new ImageView[dotsCount];
        for (int i = 0; i < dotsCount; i++) {
            dots[i] = new ImageView(getApplicationContext());
            if(i==mPosition)
                dots[i].setImageDrawable(getResources().getDrawable(R.drawable.selecteditem_dot));
            else
                dots[i].setImageDrawable(getResources().getDrawable(R.drawable.unselecteditem_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(10, 0, 10, 0);
            pager_indicator.addView(dots[i], params);
        }
    }
}
