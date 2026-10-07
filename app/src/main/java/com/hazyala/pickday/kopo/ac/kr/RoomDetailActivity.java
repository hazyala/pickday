package com.hazyala.pickday.kopo.ac.kr;

import com.hazyala.pickday.kopo.ac.kr.model.AvailabilityResponse;
import com.hazyala.pickday.kopo.ac.kr.model.MyMeetupRoom;
import com.hazyala.pickday.kopo.ac.kr.model.RoomAvailabilitySummary;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import com.hazyala.pickday.kopo.ac.kr.ui.PickDayActivity;
import androidx.appcompat.app.AlertDialog;
import android.widget.Toast;
import com.hazyala.pickday.kopo.ac.kr.data.ScheduleCalculator;
import androidx.appcompat.widget.AppCompatButton;

import com.hazyala.pickday.kopo.ac.kr.data.LocalMeetupRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RoomDetailActivity extends PickDayActivity {

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
        if (LocalMeetupRepository.getMeetupRoomById(roomId) == null) {
            Toast.makeText(this, getString(R.string.error_room_not_found), Toast.LENGTH_SHORT).show();
            finish(); return;
        }
        renderRoomData();
        setupBackNavigation();
        findViewById(R.id.btnConfirmSchedule).setOnClickListener(v -> showScheduleConfirmation());

        btnBack.setOnClickListener(v -> goHome());

        findViewById(R.id.btnChangeResponse).setOnClickListener(v -> {
            Intent intent = new Intent(
                    RoomDetailActivity.this,
                    LocalMeetupRepository.getResponseCandidateDates(roomId).isEmpty()
                            ? SelectionActivity.class : ResponseSelectionActivity.class
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

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goHome();
            }
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
            MyMeetupRoom room = LocalMeetupRepository.getMeetupRoomByTitle(roomTitle);
            roomId = room == null ? "" : room.roomId;
        }
    }

    private void renderRoomData() {
        MyMeetupRoom room = LocalMeetupRepository.getMeetupRoomById(roomId);
        List<AvailabilityResponse> responses =
                LocalMeetupRepository.getAvailabilityResponses(room.roomId);
        RoomAvailabilitySummary summary = LocalMeetupRepository.getRoomAvailabilitySummary(roomId);
        int completedCount = summary.completedResponseCount;
        int waitingCount = Math.max(0, room.participantCount - completedCount);
        int responseRate = summary.responseRate;
        int waitingRate = Math.max(0, 100 - responseRate);

        tvRoomStatus.setText(getStatusText(room));
        tvRoomTitle.setText(room.title);
        tvRoomSubtitle.setText(getSubtitleText(room));
        tvDeadline.setText(getString(R.string.room_deadline_datetime, formatDate(room.deadlineDateIso), room.deadlineTimeText));
        tvParticipantSummary.setText(getString(R.string.room_participants_capacity, room.participantCount, room.maxParticipants));
        tvProgressUpdatedAt.setText(completedCount == 0 ? getString(R.string.room_detail_render_room_data_text) : getString(R.string.room_detail_render_room_data_text_2));
        tvParticipantProgress.setText(getString(R.string.room_detail_participant_progress_format, room.participantCount));
        tvResponseCompleted.setText(getString(R.string.room_detail_response_completed_format, completedCount, responseRate));
        tvResponseWaiting.setText(getString(R.string.room_detail_response_waiting_format, waitingCount, waitingRate));
        tvConfirmedSchedule.setText(getString(R.string.room_detail_confirmed_schedule_format, getConfirmedText(room)));
        tvProgressRate.setText(getString(R.string.percent_value, responseRate));
        tvParticipantSectionTitle.setText(getString(R.string.room_detail_participant_section_title_format, room.participantCount));

        TextView responseButton = findViewById(R.id.btnChangeResponse);
        boolean confirmed = !room.confirmedDateIso.isEmpty();
        boolean closed = LocalMeetupRepository.isDeadlinePassed(room);
        responseButton.setEnabled(!confirmed && !closed);
        AvailabilityResponse mine = LocalMeetupRepository.getResponse(roomId, LocalMeetupRepository.getCurrentUser().name);
        responseButton.setText(confirmed ? getString(R.string.room_detail_render_room_data_text_3) : closed ? getString(R.string.room_detail_render_room_data_text_4)
                : room.candidateDateIsos.isEmpty() ? getString(R.string.room_detail_render_room_data_text_5)
                : mine != null && mine.submitted ? getString(R.string.room_detail_render_room_data_text_6) : getString(R.string.room_detail_render_room_data_text_7));
        TextView confirmButton = findViewById(R.id.btnConfirmSchedule);
        confirmButton.setEnabled(!confirmed && room.hostName.equals(LocalMeetupRepository.getCurrentUser().name)
                && !LocalMeetupRepository.getScheduleOptions(roomId).isEmpty());
        confirmButton.setText(confirmed ? getString(R.string.room_detail_render_room_data_text_8) : getString(R.string.room_detail_confirm_schedule_text));
        confirmButton.setAlpha(confirmButton.isEnabled() ? 1f : 0.5f);
        updateProgressBar(responseRate, waitingRate);
        renderCandidateDates(room, responses);
        renderTimePreferences(responses);
        renderParticipants(responses);
    }

    private String getStatusText(MyMeetupRoom room) {
        if (room.confirmedDateIso != null && !room.confirmedDateIso.isEmpty()) {
            return getString(R.string.action_confirm);
        }

        if (LocalMeetupRepository.isDeadlinePassed(room)) return getString(R.string.home_set_main_meetup_data_text);
        if (room.responseRate >= 100) {
            return getString(R.string.response_completed);
        }

        return getString(R.string.room_detail_room_status_text);
    }

    private String getSubtitleText(MyMeetupRoom room) {
        if (room.confirmedDateIso != null && !room.confirmedDateIso.isEmpty()) {
            return getString(R.string.room_detail_get_subtitle_text_text);
        }

        return getString(R.string.room_detail_room_subtitle_text);
    }

    private String getConfirmedText(MyMeetupRoom room) {
        if (room.confirmedDateIso == null || room.confirmedDateIso.isEmpty()) {
            return getString(R.string.schedule_undecided);
        }

        return formatDate(room.confirmedDateIso) + "\n" + room.confirmedTimeText;
    }

    private String formatDate(String dateIso) {
        if (dateIso == null || dateIso.isEmpty()) {
            return getString(R.string.schedule_undecided);
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
            MyMeetupRoom room,
            List<AvailabilityResponse> responses
    ) {
        List<ScheduleCalculator.DateResult> results = ScheduleCalculator.dates(room, responses);
        List<ScheduleCalculator.Option> options = LocalMeetupRepository.getScheduleOptions(roomId);
        String recommendedDate = options.isEmpty() ? "" : options.get(0).dateIso;
        if (results.size() > candidateDateViews.length) {
            LinearLayout container = (LinearLayout) candidateDateViews[0].getParent();
            java.util.ArrayList<TextView> views = new java.util.ArrayList<>(java.util.Arrays.asList(candidateDateViews));
            while (views.size() < results.size()) {
                TextView view = new TextView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(100), LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, dp(12), 0);
                view.setLayoutParams(params);
                view.setGravity(Gravity.CENTER);
                view.setMinHeight(dp(88));
                view.setPadding(dp(8), dp(8), dp(8), dp(8));
                view.setTextSize(12);
                view.setTypeface(null, Typeface.BOLD);
                container.addView(view);
                views.add(view);
            }
            candidateDateViews = views.toArray(new TextView[0]);
        }

        for (int index = 0; index < candidateDateViews.length; index++) {
            TextView view = candidateDateViews[index];

            if (index >= results.size()) {
                view.setVisibility(View.GONE);
                continue;
            }

            ScheduleCalculator.DateResult result = results.get(index);
            boolean recommended = result.dateIso.equals(recommendedDate);
            int rate = getPercent(result.availableCount, Math.max(1, room.participantCount));
            view.setVisibility(View.VISIBLE);
            view.setBackgroundResource(recommended
                    ? R.drawable.pickday_selected
                    : R.drawable.pickday_unselected);
            view.setTextColor(recommended
                    ? Color.parseColor("#5B4CDB")
                    : Color.parseColor("#77748E"));
            view.setText(getString(R.string.room_detail_view_format_2, formatDate(result.dateIso), (result.fullIntersection ? getString(R.string.room_detail_render_candidate_dates_text) : recommended ? getString(R.string.room_detail_render_candidate_dates_text_2) : ""), result.availableCount, rate, getAvailabilityDots(result.availableCount, room.participantCount)));
        }
    }

    private void renderTimePreferences(List<AvailabilityResponse> responses) {
        List<ScheduleCalculator.TimeResult> results = ScheduleCalculator.times(
                LocalMeetupRepository.getMeetupRoomById(roomId), responses, null);

        for (int index = 0; index < timePreferenceViews.length; index++) {
            TextView view = timePreferenceViews[index];

            if (index >= results.size()) {
                view.setVisibility(View.GONE);
                continue;
            }

            ScheduleCalculator.TimeResult result = results.get(index);
            view.setVisibility(View.VISIBLE);
            view.setTextColor(Color.parseColor("#77748E"));
            view.setText(getString(R.string.room_detail_view_format, result.label, result.timeRange, getPreferenceBar(result.count), result.count));
        }
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
        int length = count * 3;
        StringBuilder builder = new StringBuilder();

        for (int index = 0; index < length; index++) {
            builder.append("━");
        }

        return builder.toString();
    }

    private void renderParticipants(List<AvailabilityResponse> responses) {
        layoutParticipantContainer.removeAllViews();

        if (responses.isEmpty()) {
            TextView emptyView = createParticipantView(getString(R.string.room_detail_render_participants_text), getString(R.string.room_detail_render_participants_text_2));
            layoutParticipantContainer.addView(emptyView);
            return;
        }

        for (AvailabilityResponse response : responses) {
            String status = response.submitted ? getString(R.string.response_completed) : getString(R.string.response_pending);
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
        textView.setText(getString(R.string.room_detail_text_view_format, getInitial(name), name, status));
        textView.setTextColor(Color.parseColor("#77748E"));
        textView.setTextSize(14);
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

    @Override
    protected void onResume() {
        super.onResume();
        if (roomId != null && LocalMeetupRepository.getMeetupRoomById(roomId) != null) renderRoomData();
    }

    private void showScheduleConfirmation() {
        List<ScheduleCalculator.Option> options = LocalMeetupRepository.getScheduleOptions(roomId);
        String[] labels = new String[options.size()];
        for (int index = 0; index < options.size(); index++) {
            ScheduleCalculator.Option option = options.get(index);
            labels[index] = getString(R.string.schedule_option_summary,
                    formatDate(option.dateIso), option.time.label, option.time.timeRange, option.time.count);
        }
        new AlertDialog.Builder(this).setTitle(getString(R.string.room_detail_show_schedule_confirmation_text_2))
                .setItems(labels, (dialog, index) -> {
                    ScheduleCalculator.Option option = options.get(index);
                    MyMeetupRoom room = LocalMeetupRepository.getMeetupRoomById(roomId);
                    int waiting = room.participantCount - LocalMeetupRepository.getRoomAvailabilitySummary(roomId).completedResponseCount;
                    String message = getString(R.string.schedule_confirm_message, labels[index],
                            waiting > 0 ? getString(R.string.schedule_unanswered_notice, waiting) : "");
                    new AlertDialog.Builder(this).setTitle(getString(R.string.room_detail_show_schedule_confirmation_text_6)).setMessage(message)
                            .setNegativeButton(getString(R.string.action_cancel), null).setPositiveButton(getString(R.string.action_confirm), (confirm, which) -> {
                                try {
                                    LocalMeetupRepository.confirmSchedule(roomId, option.dateIso, option.time.code,
                                            LocalMeetupRepository.getCurrentUser().name);
                                    renderRoomData();
                                } catch (IllegalArgumentException error) {
                                    Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }).show();
                }).setNegativeButton(getString(R.string.action_cancel), null).show();
    }

    private void goHome() {
        Intent intent = new Intent(RoomDetailActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

}
