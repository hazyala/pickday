package com.hazyala.pickday.kopo.ac.kr;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.hazyala.pickday.kopo.ac.kr.ui.PickDayActivity;

public class SplashActivity extends PickDayActivity {

    // 스플래시 유지 시간 (2초)
    private static final int SPLASH_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {

            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);

            finish();

        }, SPLASH_TIME);
    }
}