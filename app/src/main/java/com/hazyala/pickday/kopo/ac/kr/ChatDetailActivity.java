package com.hazyala.pickday.kopo.ac.kr;

import com.hazyala.pickday.kopo.ac.kr.model.ChatMessage;
import com.hazyala.pickday.kopo.ac.kr.model.ChatRoomStatus;
import com.hazyala.pickday.kopo.ac.kr.model.MyMeetupRoom;

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
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import com.hazyala.pickday.kopo.ac.kr.ui.PickDayActivity;

import com.hazyala.pickday.kopo.ac.kr.data.LocalMeetupRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatDetailActivity extends PickDayActivity {

    public static final String EXTRA_ROOM_ID = "extra_room_id";
    public static final String EXTRA_ROOM_TITLE = "extra_room_title";

    private ImageView btnBack;
    private TextView tvChatDetailTitle;
    private TextView tvChatNoticeTitle;
    private TextView tvChatNoticeMeta;
    private TextView tvChatEmptyState;
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
        if (!setRoomData()) return;
        loadMessages();
        setListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvChatDetailTitle = findViewById(R.id.tvChatDetailTitle);
        tvChatNoticeTitle = findViewById(R.id.tvChatNoticeTitle);
        tvChatNoticeMeta = findViewById(R.id.tvChatNoticeMeta);
        tvChatEmptyState = findViewById(R.id.tvChatEmptyState);
        scrollChatMessages = findViewById(R.id.scrollChatMessages);
        layoutChatMessageContainer = findViewById(R.id.layoutChatMessageContainer);
        etMessageInput = findViewById(R.id.etMessageInput);
        btnSendMessage = findViewById(R.id.btnSendMessage);
    }

    private boolean setRoomData() {
        roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
        roomTitle = getIntent().getStringExtra(EXTRA_ROOM_TITLE);

        if (roomId == null || roomId.isEmpty()) {
            MyMeetupRoom selected = LocalMeetupRepository.getMeetupRoomByTitle(roomTitle);
            roomId = selected == null ? "" : selected.roomId;
        }

        MyMeetupRoom room =
                LocalMeetupRepository.getMeetupRoomById(roomId);

        if (room == null) {
            Toast.makeText(this, getString(R.string.error_room_not_found), Toast.LENGTH_SHORT).show();
            finish(); return false;
        }
        if (roomTitle == null || roomTitle.isEmpty()) {
            roomTitle = room.title;
        }

        tvChatDetailTitle.setText(roomTitle);
        return true;
    }

    private void loadMessages() {
        setNoticeStatus();

        List<ChatMessage> messages =
                LocalMeetupRepository.getChatMessagesByRoomId(roomId);

        tvChatEmptyState.setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);

        for (ChatMessage message : messages) {
            addMessageView(message);
        }
    }

    private void addMessageView(ChatMessage message) {
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
        ChatRoomStatus status =
                LocalMeetupRepository.getChatRoomStatusByRoomId(roomId);

        tvChatNoticeTitle.setText(getString(R.string.chat_detail_chat_notice_title_format, status.participantCount, status.responseRate));

        tvChatNoticeMeta.setText(getString(R.string.chat_detail_chat_notice_meta_format, formatDate(status.deadlineDateIso), status.deadlineTimeText));
    }

    private String formatDate(String dateIso) {
        if (dateIso == null || dateIso.isEmpty()) {
            return getString(R.string.schedule_undecided);
        }

        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN);
            Date date = parser.parse(dateIso);
            SimpleDateFormat formatter = new SimpleDateFormat("M.d (E)", Locale.KOREAN);
            return formatter.format(date);
        } catch (ParseException e) {
            return dateIso;
        }
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

        tvChatEmptyState.setVisibility(View.GONE);
        addMessageView(LocalMeetupRepository.sendChatMessage(roomId, message));

        etMessageInput.setText("");
        scrollChatMessages.post(() -> scrollChatMessages.fullScroll(View.FOCUS_DOWN));
    }
}
