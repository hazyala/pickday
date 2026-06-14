package com.hazyala.pickday.kopo.ac.kr;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.app.AppCompatActivity;

import com.hazyala.pickday.kopo.ac.kr.data.DummyDataSource;

import java.util.List;

public class ChatDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ROOM_ID = "extra_room_id";
    public static final String EXTRA_ROOM_TITLE = "extra_room_title";

    private ImageView btnBack;
    private TextView tvChatDetailTitle;
    private TextView tvChatNoticeTitle;
    private TextView tvChatNoticeMeta;
    private ScrollView scrollChatMessages;
    private LinearLayout layoutChatMessageContainer;
    private EditText etMessageInput;
    private AppCompatButton btnSendMessage;
    private String roomId;
    private String roomTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        initViews();
        setRoomData();
        loadMessages();
        setListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvChatDetailTitle = findViewById(R.id.tvChatDetailTitle);
        tvChatNoticeTitle = findViewById(R.id.tvChatNoticeTitle);
        tvChatNoticeMeta = findViewById(R.id.tvChatNoticeMeta);
        scrollChatMessages = findViewById(R.id.scrollChatMessages);
        layoutChatMessageContainer = findViewById(R.id.layoutChatMessageContainer);
        etMessageInput = findViewById(R.id.etMessageInput);
        btnSendMessage = findViewById(R.id.btnSendMessage);
    }

    private void setRoomData() {
        roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
        roomTitle = getIntent().getStringExtra(EXTRA_ROOM_TITLE);

        if (roomId == null || roomId.isEmpty()) {
            roomId = DummyDataSource.getMeetupRoomByTitle(roomTitle).roomId;
        }

        DummyDataSource.MyMeetupRoom room =
                DummyDataSource.getMeetupRoomById(roomId);

        if (roomTitle == null || roomTitle.isEmpty()) {
            roomTitle = room.title;
        }

        tvChatDetailTitle.setText(roomTitle);
    }

    private void loadMessages() {
        setNoticeStatus();

        List<DummyDataSource.ChatMessage> messages =
                DummyDataSource.getChatMessagesByRoomId(roomId);

        for (DummyDataSource.ChatMessage message : messages) {
            addMessageView(message);
        }
    }

    private void addMessageView(DummyDataSource.ChatMessage message) {
        View view = LayoutInflater.from(this).inflate(
                R.layout.item_chat_message,
                layoutChatMessageContainer,
                false
        );

        LinearLayout messageRoot =
                view.findViewById(R.id.messageRoot);

        LinearLayout layoutMessageRow =
                view.findViewById(R.id.layoutMessageRow);

        TextView tvLeftAvatar =
                view.findViewById(R.id.tvLeftAvatar);

        TextView tvRightAvatar =
                view.findViewById(R.id.tvRightAvatar);

        TextView tvBubble =
                view.findViewById(R.id.tvBubble);

        TextView tvSenderName =
                view.findViewById(R.id.tvSenderName);

        TextView tvMessageTime =
                view.findViewById(R.id.tvMessageTime);

        tvSenderName.setText(message.senderName);
        tvBubble.setText(message.message);
        tvMessageTime.setText(message.time);

        if (message.mine) {
            messageRoot.setGravity(Gravity.END);
            layoutMessageRow.setGravity(Gravity.END);
            tvLeftAvatar.setVisibility(View.GONE);
            tvRightAvatar.setVisibility(View.VISIBLE);
            tvRightAvatar.setText(getInitial(message.senderName));
            tvBubble.setBackgroundResource(R.drawable.bg_chat_bubble_mine);
            tvBubble.setTextColor(Color.parseColor("#252538"));
            tvSenderName.setTextColor(Color.parseColor("#252538"));
            tvMessageTime.setTextColor(Color.parseColor("#8A88A0"));
        } else {
            messageRoot.setGravity(Gravity.START);
            layoutMessageRow.setGravity(Gravity.START);
            tvLeftAvatar.setVisibility(View.VISIBLE);
            tvRightAvatar.setVisibility(View.GONE);
            tvLeftAvatar.setText(getInitial(message.senderName));
            tvBubble.setBackgroundResource(R.drawable.bg_chat_bubble_other);
            tvBubble.setTextColor(Color.parseColor("#252538"));
            tvSenderName.setTextColor(Color.parseColor("#252538"));
            tvMessageTime.setTextColor(Color.parseColor("#8A88A0"));
        }

        layoutChatMessageContainer.addView(view);
    }

    private void setNoticeStatus() {
        DummyDataSource.ChatRoomStatus status =
                DummyDataSource.getChatRoomStatusByRoomId(roomId);

        tvChatNoticeTitle.setText(
                "현재 방 현황: 참여자 " +
                        status.participantCount +
                        "명 · 응답률 " +
                        status.responseRate +
                        "%"
        );

        tvChatNoticeMeta.setText("마감까지 " + status.dDay);
    }

    private String getInitial(String senderName) {
        if (senderName == null || senderName.isEmpty()) {
            return "";
        }

        return senderName.substring(0, 1);
    }

    private void setListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSendMessage.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String message = etMessageInput.getText().toString().trim();

        if (message.isEmpty()) {
            return;
        }

        addMessageView(new DummyDataSource.ChatMessage(
                roomId,
                "김해민",
                message,
                "방금 전",
                true,
                false
        ));

        etMessageInput.setText("");
        scrollChatMessages.post(() -> scrollChatMessages.fullScroll(View.FOCUS_DOWN));
    }
}
