package com.hazyala.pickday.kopo.ac.kr;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.content.Intent;
import androidx.appcompat.widget.AppCompatButton;

import androidx.appcompat.app.AppCompatActivity;

import com.hazyala.pickday.kopo.ac.kr.data.DummyDataSource;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private TextView tvMainTitle;
    private TextView tvMainParticipants;
    private TextView tvMainDday;
    private TextView tvBestDate;
    private TextView tvBestCount;

    private LinearLayout layoutDateContainer;
    private LinearLayout layoutRoomContainer;

    private AppCompatButton btnDetail;
    private AppCompatButton btnFab;
    private AppCompatButton btnCreateSmall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();

        setMainMeetupData();

        loadAvailableDates();

        loadMeetupRooms();

        setListeners();
    }

    private void initViews() {

        tvGreeting = findViewById(R.id.tvGreeting);

        tvMainTitle = findViewById(R.id.tvMainTitle);
        tvMainParticipants = findViewById(R.id.tvMainParticipants);
        tvMainDday = findViewById(R.id.tvMainDday);
        tvBestDate = findViewById(R.id.tvBestDate);
        tvBestCount = findViewById(R.id.tvBestCount);

        layoutDateContainer = findViewById(R.id.layoutDateContainer);
        layoutRoomContainer = findViewById(R.id.layoutRoomContainer);

        btnDetail = findViewById(R.id.btnDetail);
        btnFab = findViewById(R.id.btnFab);
        btnCreateSmall = findViewById(R.id.btnCreateSmall);
    }

    private void setMainMeetupData() {

        DummyDataSource.User user =
                DummyDataSource.getCurrentUser();

        DummyDataSource.MainMeetup meetup =
                DummyDataSource.getMainMeetup();

        tvGreeting.setText(
                "안녕하세요, " + user.name + "님! 👋"
        );

        tvMainTitle.setText(meetup.title);

        tvMainParticipants.setText(
                "👥 참여자 " + meetup.participantCount + "명"
        );

        tvMainDday.setText(
                "🕘 마감까지 " + meetup.dDay
        );

        tvBestDate.setText(meetup.bestDateTime);

        tvBestCount.setText(
                "👥 " + meetup.availableCount + "명 가능"
        );
    }

    private void loadAvailableDates() {

        List<DummyDataSource.AvailableDate> dates =
                DummyDataSource.getAvailableDates();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (DummyDataSource.AvailableDate date : dates) {

            View view = inflater.inflate(
                    R.layout.item_available_date,
                    layoutDateContainer,
                    false
            );

            FrameLayout dateRoot =
                    view.findViewById(R.id.dateRoot);

            TextView tvDateBest =
                    view.findViewById(R.id.tvDateBest);

            TextView tvDateLabel =
                    view.findViewById(R.id.tvDateLabel);

            TextView tvDate =
                    view.findViewById(R.id.tvDate);

            TextView tvDayOfWeek =
                    view.findViewById(R.id.tvDayOfWeek);

            TextView tvAvailableCount =
                    view.findViewById(R.id.tvAvailableCount);

            tvDateLabel.setText(date.label);

            tvDate.setText(date.date);

            tvDayOfWeek.setText("(" + date.dayOfWeek + ")");

            tvAvailableCount.setText(
                    "👥 " + date.availableCount + "명"
            );

            if (date.selected) {

                dateRoot.setBackgroundResource(
                        R.drawable.pickday_button_primary
                );

                tvDate.setTextColor(Color.WHITE);

                tvDayOfWeek.setTextColor(Color.WHITE);

                tvDateLabel.setTextColor(Color.WHITE);

                tvAvailableCount.setTextColor(Color.WHITE);
            }

            if (date.best) {

                tvDateBest.setVisibility(View.VISIBLE);
            }

            layoutDateContainer.addView(view);
        }
    }

    private void loadMeetupRooms() {

        List<DummyDataSource.MyMeetupRoom> rooms =
                DummyDataSource.getMyMeetupRooms();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (DummyDataSource.MyMeetupRoom room : rooms) {

            View view = inflater.inflate(
                    R.layout.item_meetup_room,
                    layoutRoomContainer,
                    false
            );

            TextView tvRoomIcon =
                    view.findViewById(R.id.tvRoomIcon);

            TextView tvRoomTitle =
                    view.findViewById(R.id.tvRoomTitle);

            TextView tvRoomInfo =
                    view.findViewById(R.id.tvRoomInfo);

            TextView tvRoomRate =
                    view.findViewById(R.id.tvRoomRate);

            tvRoomTitle.setText(room.title);

            tvRoomInfo.setText(
                    "참여자 " +
                            room.participantCount +
                            "명 · 마감까지 " +
                            room.dDay
            );

            tvRoomRate.setText(
                    room.responseRate + "%"
            );

            switch (room.iconType) {

                case "group":
                    tvRoomIcon.setText("👥");
                    break;

                case "cake":
                    tvRoomIcon.setText("🎂");
                    break;

                case "camp":
                    tvRoomIcon.setText("⛺");
                    break;

                default:
                    tvRoomIcon.setText("📅");
                    break;
            }

            layoutRoomContainer.addView(view);
        }
    }

    private void setListeners() {

        btnDetail.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            RoomDetailActivity.class
                    );

            startActivity(intent);
        });

        btnFab.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            CreateMeetupActivity.class
                    );

            startActivity(intent);
        });

        btnCreateSmall.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            CreateMeetupActivity.class
                    );

            startActivity(intent);
        });
    }
}