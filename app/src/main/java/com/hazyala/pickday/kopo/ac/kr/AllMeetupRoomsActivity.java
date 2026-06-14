package com.hazyala.pickday.kopo.ac.kr;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.hazyala.pickday.kopo.ac.kr.data.DummyDataSource;

import java.util.List;

public class AllMeetupRoomsActivity extends AppCompatActivity {

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

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnCreateRoom = findViewById(R.id.btnCreateRoom);
        tvRoomCount = findViewById(R.id.tvRoomCount);
        layoutRoomList = findViewById(R.id.layoutRoomList);
    }

    private void renderRooms() {
        List<DummyDataSource.MyMeetupRoom> rooms = DummyDataSource.getMyMeetupRooms();
        tvRoomCount.setText("전체 " + rooms.size() + "개");
        layoutRoomList.removeAllViews();

        if (rooms.isEmpty()) {
            layoutRoomList.addView(createEmptyView());
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        for (DummyDataSource.MyMeetupRoom room : rooms) {
            View item = inflater.inflate(R.layout.item_meetup_room, layoutRoomList, false);
            bindRoomItem(item, room);
            layoutRoomList.addView(item);
        }
    }

    private void bindRoomItem(View item, DummyDataSource.MyMeetupRoom room) {
        TextView tvRoomIcon = item.findViewById(R.id.tvRoomIcon);
        TextView tvRoomTitle = item.findViewById(R.id.tvRoomTitle);
        TextView tvRoomInfo = item.findViewById(R.id.tvRoomInfo);
        TextView tvRoomRate = item.findViewById(R.id.tvRoomRate);

        tvRoomIcon.setText(getIconText(room.iconType));
        tvRoomTitle.setText(room.title);
        tvRoomInfo.setText("참여자 " + room.participantCount + "명 · 마감까지 " + room.dDay);
        tvRoomRate.setText(room.responseRate + "%");

        item.setOnClickListener(v -> {
            Intent intent = new Intent(AllMeetupRoomsActivity.this, RoomDetailActivity.class);
            intent.putExtra(RoomDetailActivity.EXTRA_ROOM_ID, room.roomId);
            intent.putExtra(RoomDetailActivity.EXTRA_ROOM_TITLE, room.title);
            startActivity(intent);
        });
    }

    private String getIconText(String iconType) {
        if ("group".equals(iconType)) {
            return "팀";
        }

        if ("cake".equals(iconType)) {
            return "생";
        }

        if ("camp".equals(iconType)) {
            return "동";
        }

        return "일";
    }

    private View createEmptyView() {
        TextView emptyView = new TextView(this);
        emptyView.setText("아직 만들어진 약속 방이 없어요");
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
