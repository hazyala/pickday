package com.hazyala.pickday.kopo.ac.kr;

import com.hazyala.pickday.kopo.ac.kr.model.AvailableDate;
import com.hazyala.pickday.kopo.ac.kr.model.MainMeetup;
import com.hazyala.pickday.kopo.ac.kr.model.MyMeetupRoom;
import com.hazyala.pickday.kopo.ac.kr.model.User;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.content.Intent;
import androidx.appcompat.widget.AppCompatButton;

import com.hazyala.pickday.kopo.ac.kr.ui.PickDayActivity;
import androidx.core.content.ContextCompat;

import com.hazyala.pickday.kopo.ac.kr.data.LocalMeetupRepository;

import java.util.List;

public class HomeActivity extends PickDayActivity {

    private static final int ROOM_PREVIEW_LIMIT = 5;

    private TextView tvGreeting;
    private TextView tvMainTitle;
    private TextView tvMainParticipants;
    private TextView tvMainDday;
    private TextView tvBestDate;
    private TextView tvBestCount;
    private String mainRoomId = LocalMeetupRepository.DEFAULT_ROOM_ID;

    private LinearLayout layoutDateContainer;
    private LinearLayout layoutRoomContainer;

    private AppCompatButton btnDetail;
    private AppCompatButton btnFab;
    private AppCompatButton btnCreateSmall;
    private AppCompatButton btnViewAllDates;
    private AppCompatButton btnViewAllRooms;
    private ImageView btnNotification;
    private LinearLayout tabCalendar;
    private LinearLayout tabChat;
    private LinearLayout tabMy;

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

    @Override
    protected void onResume() {
        super.onResume();
        layoutDateContainer.removeAllViews();
        layoutRoomContainer.removeAllViews();
        setMainMeetupData();
        loadAvailableDates();
        loadMeetupRooms();
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
        btnViewAllDates = findViewById(R.id.btnViewAllDates);
        btnViewAllRooms = findViewById(R.id.btnViewAllRooms);
        btnNotification = findViewById(R.id.btnNotification);
        tabCalendar = findViewById(R.id.tabCalendar);
        tabChat = findViewById(R.id.tabChat);
        tabMy = findViewById(R.id.tabMy);
    }

    private void setMainMeetupData() {

        User user =
                LocalMeetupRepository.getCurrentUser();

        MainMeetup meetup =
                LocalMeetupRepository.getMainMeetup();

        ((TextView) findViewById(R.id.tvScheduleLabel)).setText(
                LocalMeetupRepository.getMeetupRoomById(meetup.roomId).confirmedDateIso.isEmpty()
                        ? R.string.home_label_text : R.string.schedule_confirmed_label);
        mainRoomId = meetup.roomId;
        ((TextView) findViewById(R.id.tvMainStatus)).setText(meetup.statusLabel);

        tvGreeting.setText(getString(R.string.home_greeting, user.name));

        tvMainTitle.setText(meetup.title);

        tvMainParticipants.setText(getString(R.string.participants_count, meetup.participantCount));
        setStartIcon(
                tvMainParticipants,
                R.drawable.icon_group,
                10,
                tvMainParticipants.getCurrentTextColor()
        );

        tvMainDday.setText(
                LocalMeetupRepository.isDeadlinePassed(LocalMeetupRepository.getMeetupRoomById(meetup.roomId)) ? getString(R.string.home_set_main_meetup_data_text) : getString(R.string.home_deadline_remaining, meetup.dDay)
        );
        setStartIcon(
                tvMainDday,
                R.drawable.icon_calendars,
                10,
                tvMainDday.getCurrentTextColor()
        );

        tvBestDate.setText(meetup.bestDateTime);

        tvBestCount.setText(getString(R.string.available_people_count, meetup.availableCount));
        setStartIcon(
                tvBestCount,
                R.drawable.icon_group,
                10,
                tvBestCount.getCurrentTextColor()
        );
    }

    private void loadAvailableDates() {

        List<AvailableDate> dates =
                LocalMeetupRepository.getAvailableDates();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (AvailableDate date : dates) {

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

            tvDayOfWeek.setText(getString(R.string.home_tv_day_of_week_format, date.dayOfWeek));

            if (date.hasMeetupStatus) {
                tvAvailableCount.setText(getString(R.string.people_count, date.availableCount));
            } else {
                tvAvailableCount.setVisibility(View.INVISIBLE);
            }

            if (date.selected) {

                dateRoot.setBackgroundResource(
                        R.drawable.pickday_button_primary
                );

                tvDate.setTextColor(Color.WHITE);

                tvDayOfWeek.setTextColor(Color.WHITE);

                tvDateLabel.setTextColor(Color.WHITE);

                if (date.hasMeetupStatus) {
                    tvAvailableCount.setTextColor(Color.WHITE);
                }
            }

            if (date.hasMeetupStatus) {
                setStartIcon(
                        tvAvailableCount,
                        R.drawable.icon_group,
                        9,
                        tvAvailableCount.getCurrentTextColor()
                );
            }

            if (date.hasMeetupStatus && date.best) {

                tvDateBest.setVisibility(View.VISIBLE);
            }

            layoutDateContainer.addView(view);
        }
    }

    private void loadMeetupRooms() {

        List<MyMeetupRoom> rooms =
                LocalMeetupRepository.getMyMeetupRooms();

        LayoutInflater inflater = LayoutInflater.from(this);

        int visibleRoomCount = Math.min(ROOM_PREVIEW_LIMIT, rooms.size());

        for (int index = 0; index < visibleRoomCount; index++) {
            MyMeetupRoom room = rooms.get(index);

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

            tvRoomInfo.setText(getString(R.string.room_participants_deadline, room.participantCount, room.dDay));

            tvRoomRate.setText(
                    getString(R.string.percent_value, room.responseRate)
            );

            switch (room.iconType) {

                case "group":
                    tvRoomIcon.setText(getString(R.string.room_badge_team));
                    break;

                case "cake":
                    tvRoomIcon.setText(getString(R.string.room_badge_birthday));
                    break;

                case "camp":
                    tvRoomIcon.setText(getString(R.string.room_badge_club));
                    break;

                default:
                    tvRoomIcon.setText(getString(R.string.room_badge_default));
                    break;
            }

            view.setOnClickListener(v -> {
                Intent intent = new Intent(
                        HomeActivity.this,
                        RoomDetailActivity.class
                );
                intent.putExtra(RoomDetailActivity.EXTRA_ROOM_ID, room.roomId);
                intent.putExtra(RoomDetailActivity.EXTRA_ROOM_TITLE, room.title);
                startActivity(intent);
            });

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

            intent.putExtra(RoomDetailActivity.EXTRA_ROOM_ID, mainRoomId);
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

        btnNotification.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            NotificationActivity.class
                    );

            startActivity(intent);
        });

        btnViewAllDates.setOnClickListener(v -> {
            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            CalendarActivity.class
                    );

            startActivity(intent);
        });

        btnViewAllRooms.setOnClickListener(v -> {
            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            AllMeetupRoomsActivity.class
                    );

            startActivity(intent);
        });

        tabCalendar.setOnClickListener(v -> {
            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            CalendarActivity.class
                    );

            startActivity(intent);
        });

        tabChat.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            ChatActivity.class
                    );

            startActivity(intent);
        });

        tabMy.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            MyPageActivity.class
                    );

            startActivity(intent);
        });
    }

    private void setStartIcon(
            TextView textView,
            int drawableRes,
            int sizeDp,
            int tintColor
    ) {
        Drawable icon = ContextCompat.getDrawable(this, drawableRes);

        if (icon == null) {
            return;
        }

        icon = icon.mutate();
        icon.setTint(tintColor);

        int sizePx = (int) (sizeDp * getResources().getDisplayMetrics().density);
        icon.setBounds(0, 0, sizePx, sizePx);
        textView.setCompoundDrawables(icon, null, null, null);
        textView.setCompoundDrawablePadding((int) (3 * getResources().getDisplayMetrics().density));
    }
}
