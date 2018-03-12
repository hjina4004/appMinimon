package com.minimon.diocian.player;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ServerErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_error);
        setTitle("미니몬 서비스");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
