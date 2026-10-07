package com.hazyala.pickday.kopo.ac.kr;

import com.hazyala.pickday.kopo.ac.kr.model.ChatMessage;
import com.hazyala.pickday.kopo.ac.kr.model.MyMeetupRoom;

import android.graphics.Color;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hazyala.pickday.kopo.ac.kr.ui.PickDayActivity;

import com.hazyala.pickday.kopo.ac.kr.data.LocalMeetupRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends PickDayActivity {

    private boolean activeTabSelected = true;
    private ImageView btnBack;
    private LinearLayout layoutChatRoomContainer;
    private LinearLayout layoutClosedRoomEmpty;
    private TextView tvActiveRoomTab;
    private TextView tvClosedRoomTab;
    private TextView tvChatRoomSectionTitle;
    private TextView tvChatRoomCount;
    private LinearLayout tabHome;
    private LinearLayout tabCalendar;
    private LinearLayout tabMy;
    private TextView btnFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
        loadChatRooms();
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (activeTabSelected) loadChatRooms();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        layoutChatRoomContainer = findViewById(R.id.layoutChatRoomContainer);
        layoutClosedRoomEmpty = findViewById(R.id.layoutClosedRoomEmpty);
        tvActiveRoomTab = findViewById(R.id.tvActiveRoomTab);
        tvClosedRoomTab = findViewById(R.id.tvClosedRoomTab);
        tvChatRoomSectionTitle = findViewById(R.id.tvChatRoomSectionTitle);
        tvChatRoomCount = findViewById(R.id.tvChatRoomCount);
        tabHome = findViewById(R.id.tabHome);
        tabCalendar = findViewById(R.id.tabCalendar);
        tabMy = findViewById(R.id.tabMy);
        btnFab = findViewById(R.id.btnFab);
    }

    private void loadChatRooms() {
        List<MyMeetupRoom> rooms =
                LocalMeetupRepository.getMyMeetupRooms();

        showActiveRooms(rooms);
    }

    private void showActiveRooms(List<MyMeetupRoom> rooms) {
        setSelectedTab(true);

        layoutClosedRoomEmpty.setVisibility(View.GONE);
        layoutChatRoomContainer.setVisibility(View.VISIBLE);
        layoutChatRoomContainer.removeAllViews();

        tvChatRoomSectionTitle.setText(getString(R.string.chat_active_room_tab_text));
        tvChatRoomCount.setText(String.valueOf(rooms.size()));

        LayoutInflater inflater = LayoutInflater.from(this);

        for (MyMeetupRoom room : rooms) {
            View view = inflater.inflate(
                    R.layout.item_chat_room,
                    layoutChatRoomContainer,
                    false
            );

            TextView tvChatRoomIcon =
                    view.findViewById(R.id.tvChatRoomIcon);

            TextView tvChatRoomTitle =
                    view.findViewById(R.id.tvChatRoomTitle);

            TextView tvChatRoomMessage =
                    view.findViewById(R.id.tvChatRoomMessage);

            TextView tvChatRoomInfo =
                    view.findViewById(R.id.tvChatRoomInfo);

            TextView tvChatRoomRate =
                    view.findViewById(R.id.tvChatRoomRate);

            tvChatRoomTitle.setText(room.title);
            tvChatRoomMessage.setText(getLastPreviewMessage(room.roomId));
            tvChatRoomInfo.setText(getString(R.string.chat_chat_room_info_format, room.participantCount, formatDate(room.deadlineDateIso), room.deadlineTimeText));
            tvChatRoomRate.setText(getString(R.string.percent_value, room.responseRate));

            switch (room.iconType) {
                case "group":
                    tvChatRoomIcon.setText(getString(R.string.room_badge_team));
                    break;

                case "cake":
                    tvChatRoomIcon.setText(getString(R.string.room_badge_birthday));
                    break;

                case "camp":
                    tvChatRoomIcon.setText(getString(R.string.room_badge_club));
                    break;

                default:
                    tvChatRoomIcon.setText(getString(R.string.room_badge_default));
                    break;
            }

            view.setOnClickListener(v -> {
                Intent intent =
                        new Intent(
                                ChatActivity.this,
                                ChatDetailActivity.class
                        );

                intent.putExtra(ChatDetailActivity.EXTRA_ROOM_ID, room.roomId);
                intent.putExtra(ChatDetailActivity.EXTRA_ROOM_TITLE, room.title);
                startActivity(intent);
            });

            layoutChatRoomContainer.addView(view);
        }
    }

    private String getLastPreviewMessage(String roomId) {
        List<ChatMessage> messages =
                LocalMeetupRepository.getChatMessagesByRoomId(roomId);

        for (int index = messages.size() - 1; index >= 0; index--) {
            ChatMessage message = messages.get(index);

            if (!message.notice) {
                return message.message;
            }
        }

        return getString(R.string.chat_detail_chat_empty_state_text);
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

    private void showClosedRooms() {
        setSelectedTab(false);

        layoutChatRoomContainer.removeAllViews();
        layoutChatRoomContainer.setVisibility(View.GONE);
        layoutClosedRoomEmpty.setVisibility(View.VISIBLE);

        tvChatRoomSectionTitle.setText(getString(R.string.chat_closed_room_tab_text));
        tvChatRoomCount.setText("0");
    }

    private void setSelectedTab(boolean activeSelected) {
        activeTabSelected = activeSelected;
        if (activeSelected) {
            tvActiveRoomTab.setBackgroundResource(R.drawable.pickday_card_glass);
            tvActiveRoomTab.setTextColor(Color.parseColor("#5B4CDB"));
            tvClosedRoomTab.setBackgroundResource(0);
            tvClosedRoomTab.setTextColor(Color.parseColor("#8A88A0"));
            return;
        }

        tvActiveRoomTab.setBackgroundResource(0);
        tvActiveRoomTab.setTextColor(Color.parseColor("#8A88A0"));
        tvClosedRoomTab.setBackgroundResource(R.drawable.pickday_card_glass);
        tvClosedRoomTab.setTextColor(Color.parseColor("#5B4CDB"));
    }

    private void setListeners() {
        btnBack.setOnClickListener(v -> finish());

        tvActiveRoomTab.setOnClickListener(v -> showActiveRooms(
                LocalMeetupRepository.getMyMeetupRooms()
        ));

        tvClosedRoomTab.setOnClickListener(v -> showClosedRooms());

        tabHome.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        tabCalendar.setOnClickListener(v -> startActivity(new Intent(ChatActivity.this, CalendarActivity.class)));
        tabMy.setOnClickListener(v -> startActivity(new Intent(ChatActivity.this, MyPageActivity.class)));
        btnFab.setOnClickListener(v -> startActivity(new Intent(ChatActivity.this, CreateMeetupActivity.class)));
    }
}
