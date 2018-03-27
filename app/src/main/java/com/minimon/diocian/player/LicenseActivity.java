package com.minimon.diocian.player;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class LicenseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);
        ImageView img_toolbar_frag_go_back2 = findViewById(R.id.img_toolbar_frag_go_back2);
        img_toolbar_frag_go_back2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tv_frag_title = findViewById(R.id.tv_frag_title);
        tv_frag_title.setText("오픈소스 라이센스");
    }
}
