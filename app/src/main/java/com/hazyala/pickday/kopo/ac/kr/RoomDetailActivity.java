package com.hazyala.pickday.kopo.ac.kr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class RoomDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ROOM_ID = "extra_room_id";
    public static final String EXTRA_ROOM_TITLE = "extra_room_title";

    private AppCompatButton btnBack;
    private String roomId;
    private String roomTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        btnBack = findViewById(R.id.btnBack);
        roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
        roomTitle = getIntent().getStringExtra(EXTRA_ROOM_TITLE);

        // 뒤로가기 → 홈
        btnBack.setOnClickListener(v -> goHome());

        // XML에서 직접 클릭 처리용
        findViewById(R.id.btnChangeResponse).setOnClickListener(v -> {
            Intent intent = new Intent(
                    RoomDetailActivity.this,
                    ResponseSelectionActivity.class
            );
            startActivity(intent);
        });
    }

    private void goHome() {
        Intent intent = new Intent(RoomDetailActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        goHome();
    }
}
