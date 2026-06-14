package com.hazyala.pickday.kopo.ac.kr;

import android.graphics.Color;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hazyala.pickday.kopo.ac.kr.data.DummyDataSource;

import java.util.List;

public class ChatActivity extends AppCompatActivity {

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
        List<DummyDataSource.MyMeetupRoom> rooms =
                DummyDataSource.getMyMeetupRooms();

        showActiveRooms(rooms);
    }

    private void showActiveRooms(List<DummyDataSource.MyMeetupRoom> rooms) {
        setSelectedTab(true);

        layoutClosedRoomEmpty.setVisibility(View.GONE);
        layoutChatRoomContainer.setVisibility(View.VISIBLE);
        layoutChatRoomContainer.removeAllViews();

        tvChatRoomSectionTitle.setText("참여 중인 방");
        tvChatRoomCount.setText(String.valueOf(rooms.size()));

        LayoutInflater inflater = LayoutInflater.from(this);

        for (DummyDataSource.MyMeetupRoom room : rooms) {
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
            tvChatRoomInfo.setText(
                    "참여자 " +
                            room.participantCount +
                            "명  |  마감까지 " +
                            room.dDay
            );
            tvChatRoomRate.setText(room.responseRate + "%");

            switch (room.iconType) {
                case "group":
                    tvChatRoomIcon.setText("팀");
                    break;

                case "cake":
                    tvChatRoomIcon.setText("생");
                    break;

                case "camp":
                    tvChatRoomIcon.setText("동");
                    break;

                default:
                    tvChatRoomIcon.setText("일");
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
        List<DummyDataSource.ChatMessage> messages =
                DummyDataSource.getChatMessagesByRoomId(roomId);

        for (int index = messages.size() - 1; index >= 0; index--) {
            DummyDataSource.ChatMessage message = messages.get(index);

            if (!message.notice) {
                return message.message;
            }
        }

        return "아직 채팅이 없습니다";
    }

    private void showClosedRooms() {
        setSelectedTab(false);

        layoutChatRoomContainer.removeAllViews();
        layoutChatRoomContainer.setVisibility(View.GONE);
        layoutClosedRoomEmpty.setVisibility(View.VISIBLE);

        tvChatRoomSectionTitle.setText("종료된 방");
        tvChatRoomCount.setText("0");
    }

    private void setSelectedTab(boolean activeSelected) {
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
                DummyDataSource.getMyMeetupRooms()
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
