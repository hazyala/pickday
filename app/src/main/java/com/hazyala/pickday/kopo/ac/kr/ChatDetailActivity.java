package com.hazyala.pickday.kopo.ac.kr;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ChatDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ROOM_TITLE = "extra_room_title";

    private ImageView btnBack;
    private TextView tvChatDetailTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        initViews();
        setRoomTitle();
        setListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvChatDetailTitle = findViewById(R.id.tvChatDetailTitle);
    }

    private void setRoomTitle() {
        String roomTitle = getIntent().getStringExtra(EXTRA_ROOM_TITLE);

        if (roomTitle == null || roomTitle.isEmpty()) {
            tvChatDetailTitle.setText("채팅");
            return;
        }

        tvChatDetailTitle.setText(roomTitle);
    }

    private void setListeners() {
        btnBack.setOnClickListener(v -> finish());
    }
}
