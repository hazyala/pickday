package com.hazyala.pickday.kopo.ac.kr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.hazyala.pickday.kopo.ac.kr.ui.PickDayActivity;
import androidx.appcompat.widget.AppCompatButton;

public class LoginActivity extends PickDayActivity {

    private AppCompatButton btnLogin;
    private TextView tvSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);
        findViewById(R.id.etEmail).setEnabled(false);
        findViewById(R.id.etPassword).setEnabled(false);

        btnLogin.setOnClickListener(v -> {

            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        });

        tvSignup.setOnClickListener(v -> {

            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);

        });
    }
}
