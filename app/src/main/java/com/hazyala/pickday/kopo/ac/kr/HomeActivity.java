package com.hazyala.pickday.kopo.ac.kr;

import android.content.res.ColorStateList;
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
    private TextView tvMainStatus;
    private TextView tvMainParticipants;
    private TextView tvMainDday;
    private TextView tvMainResponseRate;
    private TextView tvBestDate;
    private TextView tvBestCount;

    private LinearLayout cardMainMeetup;
    private LinearLayout cardEmptyMeetup;
    private LinearLayout sectionAvailableDates;
    private LinearLayout layoutDateContainer;
    private LinearLayout layoutRoomContainer;
    private TextView tvRoomEmpty;

    private AppCompatButton btnDetail;
    private AppCompatButton btnFab;
    private AppCompatButton btnCreateSmall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();

        renderHome(DummyDataSource.getHomeState());

        setListeners();
    }

    private void initViews() {

        tvGreeting = findViewById(R.id.tvGreeting);

        tvMainStatus = findViewById(R.id.tvMainStatus);
        tvMainTitle = findViewById(R.id.tvMainTitle);
        tvMainParticipants = findViewById(R.id.tvMainParticipants);
        tvMainDday = findViewById(R.id.tvMainDday);
        tvMainResponseRate = findViewById(R.id.tvMainResponseRate);
        tvBestDate = findViewById(R.id.tvBestDate);
        tvBestCount = findViewById(R.id.tvBestCount);

        cardMainMeetup = findViewById(R.id.cardMainMeetup);
        cardEmptyMeetup = findViewById(R.id.cardEmptyMeetup);
        sectionAvailableDates = findViewById(R.id.sectionAvailableDates);
        layoutDateContainer = findViewById(R.id.layoutDateContainer);
        layoutRoomContainer = findViewById(R.id.layoutRoomContainer);
        tvRoomEmpty = findViewById(R.id.tvRoomEmpty);

        btnDetail = findViewById(R.id.btnDetail);
        btnFab = findViewById(R.id.btnFab);
        btnCreateSmall = findViewById(R.id.btnCreateSmall);
    }

    private void renderHome(DummyDataSource.HomeState homeState) {
        renderMainMeetup(homeState.currentUser, homeState.mainMeetup);
        loadAvailableDates(homeState.availableDates);
        loadMeetupRooms(homeState.meetupRooms);
    }

    private void renderMainMeetup(
            DummyDataSource.User user,
            DummyDataSource.MainMeetup meetup
    ) {
        tvGreeting.setText(
                "안녕하세요, " + user.name + "님"
        );

        if (meetup == null) {
            cardMainMeetup.setVisibility(View.GONE);
            cardEmptyMeetup.setVisibility(View.VISIBLE);
            return;
        }

        cardMainMeetup.setVisibility(View.VISIBLE);
        cardEmptyMeetup.setVisibility(View.GONE);

        tvMainStatus.setText(meetup.statusLabel);

        tvMainTitle.setText(meetup.title);

        tvMainParticipants.setText(
                "참여자 " + meetup.participantCount + "명"
        );

        tvMainDday.setText(
                "마감까지 " + meetup.dDay
        );

        tvMainResponseRate.setText(
                meetup.responseRate + "%\n응답 완료"
        );

        if (meetup.bestDateTime == null || meetup.bestDateTime.length() == 0) {
            tvBestDate.setText("아직 충분한 응답이 없어요");
            tvBestCount.setText("응답이 모이면 추천 날짜가 표시됩니다");
        } else {
            tvBestDate.setText(meetup.bestDateTime);
            tvBestCount.setText(meetup.availableCount + "명 가능");
        }
    }

    private void loadAvailableDates(List<DummyDataSource.AvailableDate> dates) {
        LayoutInflater inflater = LayoutInflater.from(this);

        layoutDateContainer.removeAllViews();

        if (dates == null || dates.size() == 0) {
            sectionAvailableDates.setVisibility(View.GONE);
            return;
        }

        sectionAvailableDates.setVisibility(View.VISIBLE);

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
                    date.availableCount + "명"
            );

            if (date.selected) {

                dateRoot.setBackgroundResource(
                        R.drawable.pickday_button_primary
                );

                tvDate.setTextColor(Color.WHITE);

                tvDayOfWeek.setTextColor(Color.WHITE);

                tvDateLabel.setTextColor(Color.WHITE);

                tvAvailableCount.setTextColor(Color.WHITE);
            } else {

                tvAvailableCount.setTextColor(
                        getAvailabilityColor(date.availableCount)
                );
            }

            if (date.best) {

                tvDateBest.setVisibility(View.VISIBLE);
            }

            layoutDateContainer.addView(view);
        }
    }

    private void loadMeetupRooms(List<DummyDataSource.MyMeetupRoom> rooms) {
        LayoutInflater inflater = LayoutInflater.from(this);

        layoutRoomContainer.removeAllViews();

        if (rooms == null || rooms.size() == 0) {
            tvRoomEmpty.setVisibility(View.VISIBLE);
            return;
        }

        tvRoomEmpty.setVisibility(View.GONE);

        for (DummyDataSource.MyMeetupRoom room : rooms) {

            View view = inflater.inflate(
                    R.layout.item_meetup_room,
                    layoutRoomContainer,
                    false
            );

            TextView tvRoomTitle =
                    view.findViewById(R.id.tvRoomTitle);

            TextView tvRoomInfo =
                    view.findViewById(R.id.tvRoomInfo);

            TextView tvRoomRate =
                    view.findViewById(R.id.tvRoomRate);

            TextView tvRoomStatus =
                    view.findViewById(R.id.tvRoomStatus);

            TextView tvRoomInitial =
                    view.findViewById(R.id.tvRoomInitial);

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

            tvRoomStatus.setText(room.statusLabel);

            tvRoomInitial.setText(
                    getRoomInitial(room.title)
            );

            tvRoomInitial.setBackgroundTintList(
                    ColorStateList.valueOf(getRoomTintColor(room.responseRate))
            );

            layoutRoomContainer.addView(view);
        }
    }

    private int getAvailabilityColor(int availableCount) {
        if (availableCount >= 7) {
            return Color.parseColor("#5B2DFF");
        }

        if (availableCount >= 4) {
            return Color.parseColor("#5F9DFF");
        }

        if (availableCount >= 1) {
            return Color.parseColor("#9D97C8");
        }

        return Color.parseColor("#C9C7D8");
    }

    private int getRoomTintColor(int responseRate) {
        if (responseRate >= 90) {
            return Color.parseColor("#EEE8FF");
        }

        if (responseRate >= 80) {
            return Color.parseColor("#F4ECFF");
        }

        return Color.parseColor("#F8F6FF");
    }

    private String getRoomInitial(String title) {
        if (title == null || title.length() == 0) {
            return "픽";
        }

        return title.substring(0, 1);
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
