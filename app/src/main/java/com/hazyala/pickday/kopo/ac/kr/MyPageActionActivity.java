package com.hazyala.pickday.kopo.ac.kr;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class MyPageActionActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_SUBTITLE = "extra_subtitle";
    public static final String EXTRA_BODY = "extra_body";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page_action);

        AppCompatButton btnBack = findViewById(R.id.btnBack);
        TextView tvActionTitle = findViewById(R.id.tvActionTitle);
        TextView tvActionSubtitle = findViewById(R.id.tvActionSubtitle);
        TextView tvActionBody = findViewById(R.id.tvActionBody);

        tvActionTitle.setText(getIntent().getStringExtra(EXTRA_TITLE));
        tvActionSubtitle.setText(getIntent().getStringExtra(EXTRA_SUBTITLE));
        tvActionBody.setText(getIntent().getStringExtra(EXTRA_BODY));

        btnBack.setOnClickListener(v -> finish());
    }
}
