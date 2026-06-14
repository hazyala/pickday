package com.hazyala.pickday.kopo.ac.kr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.hazyala.pickday.kopo.ac.kr.data.DummyDataSource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RoomDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ROOM_ID = "extra_room_id";
    public static final String EXTRA_ROOM_TITLE = "extra_room_title";

    private AppCompatButton btnBack;
    private String roomId;
    private String roomTitle;
    private TextView tvRoomStatus;
    private TextView tvRoomTitle;
    private TextView tvRoomSubtitle;
    private TextView tvDeadline;
    private TextView tvParticipantSummary;
    private TextView tvProgressUpdatedAt;
    private TextView tvParticipantProgress;
    private TextView tvResponseCompleted;
    private TextView tvResponseWaiting;
    private TextView tvConfirmedSchedule;
    private TextView tvProgressRate;
    private TextView tvParticipantSectionTitle;
    private View viewProgressComplete;
    private View viewProgressWaiting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        initViews();
        readRoomData();
        renderRoomData();

        // 뒤로가기 → 홈
        btnBack.setOnClickListener(v -> goHome());

        // XML에서 직접 클릭 처리용
        findViewById(R.id.btnChangeResponse).setOnClickListener(v -> {
            Intent intent = new Intent(
                    RoomDetailActivity.this,
                    ResponseSelectionActivity.class
            );
            startActivity(intent);
        });
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvRoomStatus = findViewById(R.id.tvRoomStatus);
        tvRoomTitle = findViewById(R.id.tvRoomTitle);
        tvRoomSubtitle = findViewById(R.id.tvRoomSubtitle);
        tvDeadline = findViewById(R.id.tvDeadline);
        tvParticipantSummary = findViewById(R.id.tvParticipantSummary);
        tvProgressUpdatedAt = findViewById(R.id.tvProgressUpdatedAt);
        tvParticipantProgress = findViewById(R.id.tvParticipantProgress);
        tvResponseCompleted = findViewById(R.id.tvResponseCompleted);
        tvResponseWaiting = findViewById(R.id.tvResponseWaiting);
        tvConfirmedSchedule = findViewById(R.id.tvConfirmedSchedule);
        tvProgressRate = findViewById(R.id.tvProgressRate);
        tvParticipantSectionTitle = findViewById(R.id.tvParticipantSectionTitle);
        viewProgressComplete = findViewById(R.id.viewProgressComplete);
        viewProgressWaiting = findViewById(R.id.viewProgressWaiting);
    }

    private void readRoomData() {
        roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
        roomTitle = getIntent().getStringExtra(EXTRA_ROOM_TITLE);

        if (roomId == null || roomId.isEmpty()) {
            roomId = DummyDataSource.getMeetupRoomByTitle(roomTitle).roomId;
        }
    }

    private void renderRoomData() {
        DummyDataSource.MyMeetupRoom room = DummyDataSource.getMeetupRoomById(roomId);
        int completedCount = getCompletedResponseCount(room);
        int waitingCount = Math.max(0, room.participantCount - completedCount);
        int waitingRate = Math.max(0, 100 - room.responseRate);

        tvRoomStatus.setText(getStatusText(room));
        tvRoomTitle.setText(room.title);
        tvRoomSubtitle.setText(getSubtitleText(room));
        tvDeadline.setText("마감일 " + formatDate(room.deadlineDateIso) + " " + room.deadlineTimeText);
        tvParticipantSummary.setText("인원 " + room.participantCount + "명");
        tvProgressUpdatedAt.setText("로컬 더미 데이터 기준");
        tvParticipantProgress.setText("참여자\n" + room.participantCount + "명");
        tvResponseCompleted.setText(
                "응답 완료\n" + completedCount + "명 (" + room.responseRate + "%)"
        );
        tvResponseWaiting.setText(
                "응답 대기\n" + waitingCount + "명 (" + waitingRate + "%)"
        );
        tvConfirmedSchedule.setText("최종 확정\n" + getConfirmedText(room));
        tvProgressRate.setText(room.responseRate + "%");
        tvParticipantSectionTitle.setText("참여자 (" + room.participantCount + "명)");

        updateProgressBar(room.responseRate, waitingRate);
    }

    private int getCompletedResponseCount(DummyDataSource.MyMeetupRoom room) {
        return Math.round(room.participantCount * room.responseRate / 100f);
    }

    private String getStatusText(DummyDataSource.MyMeetupRoom room) {
        if (room.confirmedDateIso != null && !room.confirmedDateIso.isEmpty()) {
            return "확정";
        }

        if (room.responseRate >= 100) {
            return "응답 완료";
        }

        return "진행 중";
    }

    private String getSubtitleText(DummyDataSource.MyMeetupRoom room) {
        if (room.confirmedDateIso != null && !room.confirmedDateIso.isEmpty()) {
            return "확정된 약속 일정을 확인해요";
        }

        return "모두가 가능한 최고의 날을 찾아봐요!";
    }

    private String getConfirmedText(DummyDataSource.MyMeetupRoom room) {
        if (room.confirmedDateIso == null || room.confirmedDateIso.isEmpty()) {
            return "미정";
        }

        return formatDate(room.confirmedDateIso) + "\n" + room.confirmedTimeText;
    }

    private String formatDate(String dateIso) {
        if (dateIso == null || dateIso.isEmpty()) {
            return "미정";
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

    private void updateProgressBar(int completedRate, int waitingRate) {
        LinearLayout.LayoutParams completedParams =
                (LinearLayout.LayoutParams) viewProgressComplete.getLayoutParams();
        LinearLayout.LayoutParams waitingParams =
                (LinearLayout.LayoutParams) viewProgressWaiting.getLayoutParams();

        completedParams.weight = Math.max(0, completedRate);
        waitingParams.weight = Math.max(0, waitingRate);
        viewProgressComplete.setLayoutParams(completedParams);
        viewProgressWaiting.setLayoutParams(waitingParams);
    }

    private void goHome() {
        Intent intent = new Intent(RoomDetailActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        goHome();
    }
}
