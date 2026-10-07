package com.hazyala.pickday.kopo.ac.kr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.hazyala.pickday.kopo.ac.kr.ui.PickDayActivity;
import androidx.appcompat.widget.AppCompatButton;

public class SignupActivity extends PickDayActivity {

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
        for (int id : new int[]{R.id.etName, R.id.etEmail, R.id.etPassword, R.id.etPasswordConfirm}) {
            findViewById(id).setEnabled(false);
        }

        btnBack.setOnClickListener(v -> finish());

        btnSignup.setOnClickListener(v -> {

            Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        });

        tvLogin.setOnClickListener(v -> finish());
    }
}
