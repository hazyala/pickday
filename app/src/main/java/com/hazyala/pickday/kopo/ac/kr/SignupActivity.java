package com.hazyala.pickday.kopo.ac.kr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class SignupActivity extends AppCompatActivity {

    private AppCompatButton btnBack;
    private AppCompatButton btnSignup;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        btnBack = findViewById(R.id.btnBack);
        btnSignup = findViewById(R.id.btnSignup);
        tvLogin = findViewById(R.id.tvLogin);

        btnBack.setOnClickListener(v -> finish());

        btnSignup.setOnClickListener(v -> {

            Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
            startActivity(intent);

        });

        tvLogin.setOnClickListener(v -> finish());
    }
}
