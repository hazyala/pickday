package com.hazyala.pickday.kopo.ac.kr;

import com.hazyala.pickday.kopo.ac.kr.model.User;
import com.hazyala.pickday.kopo.ac.kr.model.UserRoomStats;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hazyala.pickday.kopo.ac.kr.ui.PickDayActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.hazyala.pickday.kopo.ac.kr.data.LocalMeetupRepository;

public class MyPageActivity extends PickDayActivity {

    private TextView tvUserName;
    private TextView tvUserSummary;

    private AppCompatButton btnProfileEdit;
    private AppCompatButton btnSettings;
    private AppCompatButton btnFab;

    private LinearLayout rowCreatedRooms;
    private LinearLayout rowJoinedRooms;
    private LinearLayout rowConfirmedSchedules;
    private LinearLayout rowNotificationSettings;
    private LinearLayout rowThemeSettings;
    private LinearLayout rowServiceInfo;
    private LinearLayout rowLogout;
    private LinearLayout tabHome;
    private LinearLayout tabCalendar;
    private LinearLayout tabChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        initViews();
        bindUser();
        bindRows();
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindUser();
    }

    private void initViews() {

        tvUserName = findViewById(R.id.tvUserName);
        tvUserSummary = findViewById(R.id.tvUserSummary);

        btnProfileEdit = findViewById(R.id.btnProfileEdit);
        btnSettings = findViewById(R.id.btnSettings);
        btnFab = findViewById(R.id.btnFab);

        rowCreatedRooms = findViewById(R.id.rowCreatedRooms);
        rowJoinedRooms = findViewById(R.id.rowJoinedRooms);
        rowConfirmedSchedules = findViewById(R.id.rowConfirmedSchedules);
        rowNotificationSettings = findViewById(R.id.rowNotificationSettings);
        rowThemeSettings = findViewById(R.id.rowThemeSettings);
        rowServiceInfo = findViewById(R.id.rowServiceInfo);
        rowLogout = findViewById(R.id.rowLogout);

        tabHome = findViewById(R.id.tabHome);
        tabCalendar = findViewById(R.id.tabCalendar);
        tabChat = findViewById(R.id.tabChat);
    }

    private void bindUser() {

        User user =
                LocalMeetupRepository.getCurrentUser();

        UserRoomStats stats = LocalMeetupRepository.getUserRoomStats();
        ((TextView) findViewById(R.id.tvActiveRoomsCount)).setText(getString(R.string.rooms_count, stats.activeRooms));
        ((TextView) findViewById(R.id.tvSubmittedRoomsCount)).setText(getString(R.string.rooms_count, stats.submittedRooms));
        ((TextView) findViewById(R.id.tvConfirmedRoomsCount)).setText(getString(R.string.rooms_count, stats.confirmedRooms));
        ((TextView) findViewById(R.id.tvTotalRoomsCount)).setText(getString(R.string.rooms_count, stats.totalRooms));
        tvUserName.setText(getString(R.string.my_page_user_name_format, user.name));
        tvUserSummary.setText(getString(R.string.my_page_user_summary_format, stats.totalRooms));
    }

    private void bindRows() {

        configureRow(
                rowCreatedRooms,
                getString(R.string.my_page_bind_rows_text),
                getString(R.string.my_page_bind_rows_text_2)
        );

        configureRow(
                rowJoinedRooms,
                getString(R.string.my_page_bind_rows_text_3),
                getString(R.string.my_page_bind_rows_text_4)
        );

        configureRow(
                rowConfirmedSchedules,
                getString(R.string.my_page_label_text_5),
                getString(R.string.my_page_bind_rows_text_5)
        );

        configureRow(
                rowNotificationSettings,
                getString(R.string.my_page_bind_rows_text_6),
                getString(R.string.my_page_bind_rows_text_7)
        );

        configureRow(
                rowThemeSettings,
                getString(R.string.my_page_bind_rows_text_8),
                getString(R.string.my_page_bind_rows_text_9)
        );

        configureRow(
                rowServiceInfo,
                getString(R.string.my_page_bind_rows_text_10),
                getString(R.string.my_page_bind_rows_text_11)
        );

        configureRow(
                rowLogout,
                getString(R.string.my_page_bind_rows_text_12),
                getString(R.string.my_page_bind_rows_text_13)
        );
    }

    private void configureRow(
            LinearLayout row,
            String title,
            String subtitle
    ) {

        TextView tvRowTitle = row.findViewById(R.id.tvRowTitle);
        TextView tvRowSubtitle = row.findViewById(R.id.tvRowSubtitle);

        tvRowTitle.setText(title);
        tvRowSubtitle.setText(subtitle);
    }

    private void setListeners() {

        btnProfileEdit.setOnClickListener(v -> openActionPage(
                getString(R.string.my_page_profile_edit_text),
                getString(R.string.my_page_set_listeners_text),
                getString(R.string.my_page_set_listeners_text_2)
        ));

        btnSettings.setOnClickListener(v -> openActionPage(
                getString(R.string.my_page_label_text_12),
                getString(R.string.my_page_set_listeners_text_3),
                getString(R.string.my_page_set_listeners_text_4)
        ));

        rowCreatedRooms.setOnClickListener(v -> startActivity(new Intent(this, AllMeetupRoomsActivity.class)));

        rowJoinedRooms.setOnClickListener(v -> openActionPage(
                getString(R.string.my_page_bind_rows_text_3),
                getString(R.string.my_page_set_listeners_text_5),
                getString(R.string.my_page_set_listeners_text_6)
        ));

        rowConfirmedSchedules.setOnClickListener(v -> startActivity(new Intent(this, CalendarActivity.class)));

        rowNotificationSettings.setOnClickListener(v -> openActionPage(
                getString(R.string.my_page_bind_rows_text_6),
                getString(R.string.my_page_set_listeners_text_7),
                getString(R.string.my_page_set_listeners_text_8)
        ));

        rowThemeSettings.setOnClickListener(v -> openActionPage(
                getString(R.string.my_page_bind_rows_text_8),
                getString(R.string.my_page_set_listeners_text_9),
                getString(R.string.my_page_set_listeners_text_10)
        ));

        rowServiceInfo.setOnClickListener(v -> openActionPage(
                getString(R.string.my_page_bind_rows_text_10),
                getString(R.string.my_page_set_listeners_text_11),
                getString(R.string.my_page_set_listeners_text_12)
        ));

        rowLogout.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MyPageActivity.this,
                            LoginActivity.class
                    );

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnFab.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MyPageActivity.this,
                            CreateMeetupActivity.class
                    );

            startActivity(intent);
        });

        tabHome.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MyPageActivity.this,
                            HomeActivity.class
                    );

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        tabCalendar.setOnClickListener(v -> {
            Intent intent =
                    new Intent(
                            MyPageActivity.this,
                            CalendarActivity.class
                    );

            startActivity(intent);
        });

        tabChat.setOnClickListener(v -> startActivity(new Intent(this, ChatActivity.class)));
    }

    private void openActionPage(String title, String subtitle, String body) {

        Intent intent =
                new Intent(
                        MyPageActivity.this,
                        MyPageActionActivity.class
                );

        intent.putExtra(MyPageActionActivity.EXTRA_TITLE, title);
        intent.putExtra(MyPageActionActivity.EXTRA_SUBTITLE, subtitle);
        intent.putExtra(MyPageActionActivity.EXTRA_BODY,
                title.equals(getString(R.string.my_page_bind_rows_text_10)) ? body : getString(R.string.feature_unavailable_body, body));

        startActivity(intent);
    }
}
