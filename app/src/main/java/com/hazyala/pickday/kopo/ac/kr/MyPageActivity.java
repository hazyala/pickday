package com.hazyala.pickday.kopo.ac.kr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.hazyala.pickday.kopo.ac.kr.data.DummyDataSource;

public class MyPageActivity extends AppCompatActivity {

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
    private LinearLayout tabAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        initViews();
        bindUser();
        bindRows();
        setListeners();
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
        tabAlarm = findViewById(R.id.tabAlarm);
    }

    private void bindUser() {

        DummyDataSource.User user =
                DummyDataSource.getCurrentUser();

        tvUserName.setText(user.name + "님");
        tvUserSummary.setText("PickDay로 약속 3개를 조율 중이에요!");
    }

    private void bindRows() {

        configureRow(
                rowCreatedRooms,
                "내가 만든 방",
                "내가 만든 약속 방 목록을 확인해요"
        );

        configureRow(
                rowJoinedRooms,
                "참여한 방",
                "내가 참여 중인 약속 방 목록이에요"
        );

        configureRow(
                rowConfirmedSchedules,
                "확정된 일정",
                "최종 확정된 일정들을 모아봤어요"
        );

        configureRow(
                rowNotificationSettings,
                "알림 설정",
                "푸시 알림을 설정할 수 있어요"
        );

        configureRow(
                rowThemeSettings,
                "테마 설정",
                "앱 테마와 색상을 변경할 수 있어요"
        );

        configureRow(
                rowServiceInfo,
                "서비스 소개",
                "픽데이에 대해 더 알아봐요"
        );

        configureRow(
                rowLogout,
                "로그아웃",
                "계정에서 로그아웃해요"
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
                "프로필 편집",
                "이름과 프로필 이미지를 수정해요.",
                "프로필 사진, 표시 이름, 소개 문구를 변경하는 화면입니다."
        ));

        btnSettings.setOnClickListener(v -> openActionPage(
                "앱 설정",
                "PickDay 사용 환경을 관리해요.",
                "알림, 테마, 계정 설정을 한 곳에서 조정할 수 있어요."
        ));

        rowCreatedRooms.setOnClickListener(v -> openActionPage(
                "내가 만든 방",
                "내가 만든 약속 방 목록을 확인해요.",
                "동아리 MT 일정 정하기, 팀플 회의 일정 등 직접 만든 방을 모아 보여줍니다."
        ));

        rowJoinedRooms.setOnClickListener(v -> openActionPage(
                "참여한 방",
                "내가 참여 중인 약속 방 목록이에요.",
                "초대받아 참여한 약속과 아직 응답하지 않은 방을 확인할 수 있어요."
        ));

        rowConfirmedSchedules.setOnClickListener(v -> openActionPage(
                "확정된 일정",
                "최종 확정된 일정들을 모아봤어요.",
                "확정된 약속 시간, 장소, 참여 멤버를 빠르게 확인하는 화면입니다."
        ));

        rowNotificationSettings.setOnClickListener(v -> openActionPage(
                "알림 설정",
                "푸시 알림을 설정할 수 있어요.",
                "초대, 마감 임박, 일정 확정 알림을 켜고 끄는 화면입니다."
        ));

        rowThemeSettings.setOnClickListener(v -> openActionPage(
                "테마 설정",
                "앱 테마와 색상을 변경할 수 있어요.",
                "PickDay의 기본 보라색 테마와 밝은 화면 설정을 관리합니다."
        ));

        rowServiceInfo.setOnClickListener(v -> openActionPage(
                "서비스 소개",
                "PickDay에 대해 더 알아봐요.",
                "여러 사람의 가능한 시간을 모아 가장 좋은 약속 시간을 고르는 서비스입니다."
        ));

        rowLogout.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MyPageActivity.this,
                            LoginActivity.class
                    );

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
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

        tabAlarm.setOnClickListener(v -> openActionPage(
                "알림",
                "초대, 응답 완료, 일정 확정 알림을 확인해요.",
                "새 알림이 있으면 이 화면에서 가장 먼저 보여줄게요."
        ));
    }

    private void openActionPage(String title, String subtitle, String body) {

        Intent intent =
                new Intent(
                        MyPageActivity.this,
                        MyPageActionActivity.class
                );

        intent.putExtra(MyPageActionActivity.EXTRA_TITLE, title);
        intent.putExtra(MyPageActionActivity.EXTRA_SUBTITLE, subtitle);
        intent.putExtra(MyPageActionActivity.EXTRA_BODY, body);

        startActivity(intent);
    }
}
