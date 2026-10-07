package com.hazyala.pickday.kopo.ac.kr;

import com.hazyala.pickday.kopo.ac.kr.model.MyMeetupRoom;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hazyala.pickday.kopo.ac.kr.ui.PickDayActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.hazyala.pickday.kopo.ac.kr.data.LocalMeetupRepository;

import java.util.List;

public class AllMeetupRoomsActivity extends PickDayActivity {

    private AppCompatButton btnBack;
    private TextView btnCreateRoom;
    private TextView tvRoomCount;
    private LinearLayout layoutRoomList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_meetup_rooms);

        initViews();
        renderRooms();
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderRooms();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnCreateRoom = findViewById(R.id.btnCreateRoom);
        tvRoomCount = findViewById(R.id.tvRoomCount);
        layoutRoomList = findViewById(R.id.layoutRoomList);
    }

    private void renderRooms() {
        List<MyMeetupRoom> rooms = LocalMeetupRepository.getMyMeetupRooms();
        tvRoomCount.setText(getString(R.string.room_total_count, rooms.size()));
        layoutRoomList.removeAllViews();

        if (rooms.isEmpty()) {
            layoutRoomList.addView(createEmptyView());
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        for (MyMeetupRoom room : rooms) {
            View item = inflater.inflate(R.layout.item_meetup_room, layoutRoomList, false);
            bindRoomItem(item, room);
            layoutRoomList.addView(item);
        }
    }

    private void bindRoomItem(View item, MyMeetupRoom room) {
        TextView tvRoomIcon = item.findViewById(R.id.tvRoomIcon);
        TextView tvRoomTitle = item.findViewById(R.id.tvRoomTitle);
        TextView tvRoomInfo = item.findViewById(R.id.tvRoomInfo);
        TextView tvRoomRate = item.findViewById(R.id.tvRoomRate);

        tvRoomIcon.setText(getIconText(room.iconType));
        tvRoomTitle.setText(room.title);
        tvRoomInfo.setText(getString(R.string.room_participants_deadline, room.participantCount, room.dDay));
        tvRoomRate.setText(getString(R.string.percent_value, room.responseRate));

        item.setOnClickListener(v -> {
            Intent intent = new Intent(AllMeetupRoomsActivity.this, RoomDetailActivity.class);
            intent.putExtra(RoomDetailActivity.EXTRA_ROOM_ID, room.roomId);
            intent.putExtra(RoomDetailActivity.EXTRA_ROOM_TITLE, room.title);
            startActivity(intent);
        });
    }

    private String getIconText(String iconType) {
        if ("group".equals(iconType)) {
            return getString(R.string.room_badge_team);
        }

        if ("cake".equals(iconType)) {
            return getString(R.string.room_badge_birthday);
        }

        if ("camp".equals(iconType)) {
            return getString(R.string.room_badge_club);
        }

        return getString(R.string.room_badge_default);
    }

    private View createEmptyView() {
        TextView emptyView = new TextView(this);
        emptyView.setText(getString(R.string.all_meetup_rooms_create_empty_view_text));
        emptyView.setTextColor(0xFF8D8AA5);
        emptyView.setTextSize(13);
        emptyView.setGravity(android.view.Gravity.CENTER);
        emptyView.setMinHeight(dp(180));
        emptyView.setBackgroundResource(R.drawable.pickday_card_white);
        return emptyView;
    }

    private void setListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnCreateRoom.setOnClickListener(v ->
                startActivity(new Intent(AllMeetupRoomsActivity.this, CreateMeetupActivity.class))
        );
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
