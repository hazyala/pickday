package com.hazyala.pickday.kopo.ac.kr;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.hazyala.pickday.kopo.ac.kr.data.DummyDataSource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
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
    private TextView btnViewAllParticipants;
    private LinearLayout layoutParticipantContainer;
    private TextView[] candidateDateViews;
    private TextView[] timePreferenceViews;
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
            intent.putExtra(EXTRA_ROOM_ID, roomId);
            startActivity(intent);
        });

        btnViewAllParticipants.setOnClickListener(v -> {
            Intent intent = new Intent(
                    RoomDetailActivity.this,
                    ParticipantListActivity.class
            );
            intent.putExtra(ParticipantListActivity.EXTRA_ROOM_ID, roomId);
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
        btnViewAllParticipants = findViewById(R.id.btnViewAllParticipants);
        layoutParticipantContainer = findViewById(R.id.layoutParticipantContainer);
        candidateDateViews = new TextView[]{
                findViewById(R.id.tvCandidateDate1),
                findViewById(R.id.tvCandidateDate2),
                findViewById(R.id.tvCandidateDate3),
                findViewById(R.id.tvCandidateDate4)
        };
        timePreferenceViews = new TextView[]{
                findViewById(R.id.tvTimePreference1),
                findViewById(R.id.tvTimePreference2),
                findViewById(R.id.tvTimePreference3),
                findViewById(R.id.tvTimePreference4),
                findViewById(R.id.tvTimePreference5)
        };
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
        List<DummyDataSource.AvailabilityResponse> responses =
                DummyDataSource.getAvailabilityResponses(room.roomId);
        int completedCount = getCompletedResponseCount(responses);
        int waitingCount = Math.max(0, room.participantCount - completedCount);
        int responseRate = getResponseRate(room, responses);
        int waitingRate = Math.max(0, 100 - responseRate);

        tvRoomStatus.setText(getStatusText(room));
        tvRoomTitle.setText(room.title);
        tvRoomSubtitle.setText(getSubtitleText(room));
        tvDeadline.setText("마감일 " + formatDate(room.deadlineDateIso) + " " + room.deadlineTimeText);
        tvParticipantSummary.setText("인원 " + room.participantCount + "명");
        tvProgressUpdatedAt.setText("로컬 더미 데이터 기준");
        tvParticipantProgress.setText("참여자\n" + room.participantCount + "명");
        tvResponseCompleted.setText(
                "응답 완료\n" + completedCount + "명 (" + responseRate + "%)"
        );
        tvResponseWaiting.setText(
                "응답 대기\n" + waitingCount + "명 (" + waitingRate + "%)"
        );
        tvConfirmedSchedule.setText("최종 확정\n" + getConfirmedText(room));
        tvProgressRate.setText(responseRate + "%");
        tvParticipantSectionTitle.setText("참여자 (" + room.participantCount + "명)");

        updateProgressBar(responseRate, waitingRate);
        renderCandidateDates(room, responses);
        renderTimePreferences(responses);
        renderParticipants(responses);
    }

    private int getCompletedResponseCount(List<DummyDataSource.AvailabilityResponse> responses) {
        int count = 0;

        for (DummyDataSource.AvailabilityResponse response : responses) {
            if (response.submitted) {
                count++;
            }
        }

        return count;
    }

    private int getResponseRate(
            DummyDataSource.MyMeetupRoom room,
            List<DummyDataSource.AvailabilityResponse> responses
    ) {
        if (room.participantCount <= 0) {
            return 0;
        }

        if (responses.isEmpty()) {
            return room.responseRate;
        }

        return Math.round(getCompletedResponseCount(responses) * 100f / room.participantCount);
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

    private void renderCandidateDates(
            DummyDataSource.MyMeetupRoom room,
            List<DummyDataSource.AvailabilityResponse> responses
    ) {
        List<CandidateDateResult> results = getCandidateDateResults(room, responses);

        for (int index = 0; index < candidateDateViews.length; index++) {
            TextView view = candidateDateViews[index];

            if (index >= results.size()) {
                view.setVisibility(View.GONE);
                continue;
            }

            CandidateDateResult result = results.get(index);
            int rate = getPercent(result.availableCount, Math.max(1, room.participantCount));
            view.setVisibility(View.VISIBLE);
            view.setBackgroundResource(index == 0
                    ? R.drawable.pickday_selected
                    : R.drawable.pickday_unselected);
            view.setTextColor(index == 0
                    ? Color.parseColor("#5B4CDB")
                    : Color.parseColor("#77748E"));
            view.setText(
                    formatDate(result.dateIso) +
                            (index == 0 ? "   1순위" : "") +
                            "\n" +
                            result.availableCount +
                            "명 가능\n" +
                            rate +
                            "%\n" +
                            getAvailabilityDots(result.availableCount, room.participantCount)
            );
        }
    }

    private List<CandidateDateResult> getCandidateDateResults(
            DummyDataSource.MyMeetupRoom room,
            List<DummyDataSource.AvailabilityResponse> responses
    ) {
        List<CandidateDateResult> results = new ArrayList<>();

        for (String dateIso : room.candidateDateIsos) {
            int availableCount = 0;

            for (DummyDataSource.AvailabilityResponse response : responses) {
                if (response.submitted && response.availableDateIsos.contains(dateIso)) {
                    availableCount++;
                }
            }

            results.add(new CandidateDateResult(dateIso, availableCount));
        }

        Collections.sort(results, (first, second) -> {
            int countCompare = Integer.compare(second.availableCount, first.availableCount);

            if (countCompare != 0) {
                return countCompare;
            }

            return first.dateIso.compareTo(second.dateIso);
        });

        return results;
    }

    private void renderTimePreferences(List<DummyDataSource.AvailabilityResponse> responses) {
        List<TimePreferenceResult> results = getTimePreferenceResults(responses);

        for (int index = 0; index < timePreferenceViews.length; index++) {
            TextView view = timePreferenceViews[index];

            if (index >= results.size()) {
                view.setVisibility(View.GONE);
                continue;
            }

            TimePreferenceResult result = results.get(index);
            view.setVisibility(View.VISIBLE);
            view.setTextColor(result.count > 0
                    ? Color.parseColor("#77748E")
                    : Color.parseColor("#C9C6D8"));
            view.setText(
                    result.label +
                            " (" +
                            result.timeRange +
                            ")    " +
                            getPreferenceBar(result.count) +
                            "  " +
                            result.count +
                            "명"
            );
        }
    }

    private List<TimePreferenceResult> getTimePreferenceResults(
            List<DummyDataSource.AvailabilityResponse> responses
    ) {
        List<TimePreferenceResult> results = new ArrayList<>();

        for (DummyDataSource.TimeSlot timeSlot : DummyDataSource.getTimeSlots()) {
            int count = 0;

            for (DummyDataSource.AvailabilityResponse response : responses) {
                if (response.submitted && response.selectedTimeSlotCodes.contains(timeSlot.code)) {
                    count++;
                }
            }

            results.add(new TimePreferenceResult(timeSlot.label, timeSlot.timeRange, count));
        }

        Collections.sort(results, Comparator
                .comparingInt((TimePreferenceResult result) -> result.count)
                .reversed());

        return results;
    }

    private int getPercent(int value, int total) {
        if (total <= 0) {
            return 0;
        }

        return Math.round(value * 100f / total);
    }

    private String getAvailabilityDots(int availableCount, int participantCount) {
        StringBuilder builder = new StringBuilder();

        for (int index = 0; index < participantCount; index++) {
            if (index > 0) {
                builder.append(" ");
            }

            builder.append(index < availableCount ? "●" : "○");
        }

        return builder.toString();
    }

    private String getPreferenceBar(int count) {
        int length = Math.max(1, count * 3);
        StringBuilder builder = new StringBuilder();

        for (int index = 0; index < length; index++) {
            builder.append("━");
        }

        return builder.toString();
    }

    private void renderParticipants(List<DummyDataSource.AvailabilityResponse> responses) {
        layoutParticipantContainer.removeAllViews();

        if (responses.isEmpty()) {
            TextView emptyView = createParticipantView("참여자 정보 없음", "대기");
            layoutParticipantContainer.addView(emptyView);
            return;
        }

        for (DummyDataSource.AvailabilityResponse response : responses) {
            String status = response.submitted ? "응답 완료" : "응답 대기";
            layoutParticipantContainer.addView(
                    createParticipantView(response.participantName, status)
            );
        }
    }

    private TextView createParticipantView(String name, String status) {
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(70), LinearLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(0, 0, dp(4), 0);
        textView.setLayoutParams(params);
        textView.setGravity(Gravity.CENTER);
        textView.setText(getInitial(name) + "\n" + name + "\n" + status);
        textView.setTextColor(Color.parseColor("#77748E"));
        textView.setTextSize(11);
        textView.setTypeface(null, Typeface.BOLD);
        return textView;
    }

    private String getInitial(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }

        return name.substring(0, 1);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class CandidateDateResult {
        String dateIso;
        int availableCount;

        CandidateDateResult(String dateIso, int availableCount) {
            this.dateIso = dateIso;
            this.availableCount = availableCount;
        }
    }

    private static class TimePreferenceResult {
        String label;
        String timeRange;
        int count;

        TimePreferenceResult(String label, String timeRange, int count) {
            this.label = label;
            this.timeRange = timeRange;
            this.count = count;
        }
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
