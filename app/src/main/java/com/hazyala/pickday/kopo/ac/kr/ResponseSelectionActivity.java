package com.hazyala.pickday.kopo.ac.kr;

import com.hazyala.pickday.kopo.ac.kr.model.AvailabilityResponse;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hazyala.pickday.kopo.ac.kr.ui.PickDayActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.hazyala.pickday.kopo.ac.kr.data.LocalMeetupRepository;
import com.hazyala.pickday.kopo.ac.kr.ui.PickDayDatePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResponseSelectionActivity extends PickDayActivity {

    public static final String EXTRA_PARTICIPANT_NAME = "extra_participant_name";
    public static final String EXTRA_ROOM_ID = RoomDetailActivity.EXTRA_ROOM_ID;

    private AppCompatButton btnBack;
    private TextView btnComplete;
    private String roomId;
    private String participantName;

    private TextView tvDateCount;
    private TextView tvSelectedDateCount;
    private TextView tvExcludeCount;

    private LinearLayout layoutSelectedDates;
    private LinearLayout layoutExcludeDates;

    private LinearLayout responseCalendar;
    private PickDayDatePicker.CalendarController responseCalendarController;

    private final int MAX_SELECT_COUNT = 10;

    private final List<DateItem> candidateDates = new ArrayList<>();
    private final Set<String> selectedDates = new HashSet<>();
    private final Set<String> selectedTimes = new HashSet<>();
    private final Set<String> excludedDates = new HashSet<>();

    private final List<TextView> timeViews = new ArrayList<>();
    private final List<TextView> excludeViews = new ArrayList<>();

    private final int PURPLE = Color.parseColor("#5B4CDB");
    private final int DARK_TEXT = Color.parseColor("#232336");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response_selection);

        readRoomData();
        initViews();
        if (LocalMeetupRepository.getMeetupRoomById(roomId) == null) {
            Toast.makeText(this, getString(R.string.error_room_not_found), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        initCandidateDates();
        participantName = getIntent().getStringExtra(EXTRA_PARTICIPANT_NAME);
        if (participantName == null) participantName = LocalMeetupRepository.getCurrentUser().name;
        AvailabilityResponse response = LocalMeetupRepository.getResponse(roomId, participantName);
        if (response != null) {
            selectedDates.addAll(response.availableDateIsos);
            selectedTimes.addAll(response.selectedTimeSlotCodes);
            excludedDates.addAll(response.excludedDateIsos);
        }
        if (savedInstanceState != null) {
            selectedDates.clear(); selectedTimes.clear(); excludedDates.clear();
            selectedDates.addAll(savedInstanceState.getStringArrayList("dates"));
            selectedTimes.addAll(savedInstanceState.getStringArrayList("times"));
            excludedDates.addAll(savedInstanceState.getStringArrayList("excluded"));
        }
        ((TextView) findViewById(R.id.btnComplete)).setText(getString(R.string.response_submit_named, participantName));

        setupCalendar();
        setupTimeOptions();
        updateTimeState();
        setupExcludeOptions();
        updateSelectedDateArea();

        setListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnComplete = findViewById(R.id.btnComplete);

        tvDateCount = findViewById(R.id.tvDateCount);
        tvSelectedDateCount = findViewById(R.id.tvSelectedDateCount);
        tvExcludeCount = findViewById(R.id.tvExcludeCount);

        layoutSelectedDates = findViewById(R.id.layoutSelectedDates);
        layoutExcludeDates = findViewById(R.id.layoutExcludeDates);

        responseCalendar = findViewById(R.id.responseCalendar);
    }

    private void readRoomData() {
        roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);

        if (roomId == null || roomId.isEmpty()) {
            roomId = LocalMeetupRepository.getCurrentDraftRoomId();
        }
    }

    private void initCandidateDates() {
        List<String> candidateDateValues = LocalMeetupRepository.getResponseCandidateDates(roomId);
        Collections.sort(candidateDateValues);

        for (String isoDate : candidateDateValues) {
            Calendar date = PickDayDatePicker.parseIsoDate(isoDate);
            candidateDates.add(new DateItem(
                    isoDate,
                    String.valueOf(date.get(Calendar.DAY_OF_MONTH)),
                    PickDayDatePicker.formatChipDate(date),
                    PickDayDatePicker.formatWeek(date)
            ));
        }
    }

    private void setupCalendar() {
        Calendar initialMonth = candidateDates.isEmpty()
                ? PickDayDatePicker.today()
                : PickDayDatePicker.parseIsoDate(candidateDates.get(0).isoDate);

        responseCalendarController = PickDayDatePicker.attachCalendar(
                responseCalendar,
                initialMonth,
                new PickDayDatePicker.CalendarDateRule() {
                    @Override
                    public boolean isEnabled(Calendar date) {
                        return findCandidateDate(date) != null;
                    }

                    @Override
                    public boolean isSelected(Calendar date) {
                        return selectedDates.contains(PickDayDatePicker.formatIsoDate(date));
                    }

                    @Override
                    public boolean isHighlighted(Calendar date) {
                        return findCandidateDate(date) != null;
                    }

                    @Override
                    public boolean usesFilledSelection(Calendar date) {
                        return true;
                    }
                },
                selectedDate -> {
                    DateItem item = findCandidateDate(selectedDate);

                    if (item != null) {
                        toggleDate(item);
                    }
                }
        );
    }

    private void toggleDate(DateItem item) {
        if (selectedDates.contains(item.isoDate)) {
            selectedDates.remove(item.isoDate);
        } else {
            if (selectedDates.size() >= MAX_SELECT_COUNT) {
                Toast.makeText(this, getString(R.string.response_selection_toggle_date_text), Toast.LENGTH_SHORT).show();
                return;
            }
            selectedDates.add(item.isoDate);
            excludedDates.remove(item.isoDate);
            updateExcludeState();
        }

        updateCalendarState();
        updateSelectedDateArea();
    }

    private DateItem findCandidateDate(Calendar date) {
        String isoDate = PickDayDatePicker.formatIsoDate(date);

        for (DateItem item : candidateDates) {
            if (item.isoDate.equals(isoDate)) {
                return item;
            }
        }

        return null;
    }

    private void updateCalendarState() {
        responseCalendarController.render();
    }

    private void updateSelectedDateArea() {
        layoutSelectedDates.removeAllViews();
        ((View) layoutSelectedDates.getParent()).setVisibility(selectedDates.isEmpty() ? View.GONE : View.VISIBLE);

        for (DateItem item : candidateDates) {
            if (selectedDates.contains(item.isoDate)) {
                TextView chip = createSelectedDateChip(item);
                layoutSelectedDates.addView(chip);
            }
        }

        tvDateCount.setText(getString(R.string.response_selection_date_count_format, selectedDates.size()));
        tvSelectedDateCount.setText(getString(R.string.response_selection_selected_date_count_format, selectedDates.size()));
    }

    private TextView createSelectedDateChip(DateItem item) {
        TextView chip = new TextView(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(72), LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, dp(8), 0);
        chip.setLayoutParams(params);

        chip.setGravity(android.view.Gravity.CENTER);
        chip.setText(getString(R.string.response_selection_chip_format, item.label, item.week));
        chip.setTextSize(14);
        chip.setTextColor(PURPLE);
        chip.setTypeface(null, android.graphics.Typeface.BOLD);
        chip.setBackgroundResource(R.drawable.pickday_unselected);
        chip.setIncludeFontPadding(false);
        chip.setClickable(true);
        chip.setFocusable(true);

        chip.setOnClickListener(v -> {
            selectedDates.remove(item.isoDate);
            updateCalendarState();
            updateSelectedDateArea();
        });

        return chip;
    }

    private void setupTimeOptions() {
        addTimeView(R.id.timeMorning, "MORNING");
        addTimeView(R.id.timeAfternoon, "AFTERNOON");
        addTimeView(R.id.timeLateAfternoon, "LATE_AFTERNOON");
        addTimeView(R.id.timeEvening, "EVENING");
        addTimeView(R.id.timeLateEvening, "LATE_EVENING");
        addTimeView(R.id.timeAny, "ANYTIME");
        LinearLayout container = findViewById(R.id.layoutTimeOptions);
        container.removeAllViews();
        LinearLayout row = null;
        int visibleIndex = 0;
        for (TextView time : timeViews) {
            if (time.getVisibility() == View.GONE) continue;
            if (visibleIndex % 2 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setBaselineAligned(false);
                row.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                container.addView(row);
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            params.setMargins(dp(4), dp(4), dp(4), dp(4));
            time.setLayoutParams(params);
            time.setMinHeight(dp(72));
            time.setTextSize(14);
            row.addView(time);
            visibleIndex++;
        }
    }

    private void addTimeView(int id, String key) {
        TextView view = findViewById(id);
        timeViews.add(view);
        if (!key.equals("ANYTIME") && !LocalMeetupRepository.getMeetupRoomById(roomId).allowedTimeSlotCodes.contains(key)) {
            view.setVisibility(View.GONE);
        }

        view.setOnClickListener(v -> {
            if (selectedTimes.contains(key)) {
                selectedTimes.remove(key);
            } else {
                if (key.equals("ANYTIME")) selectedTimes.clear();
                else selectedTimes.remove("ANYTIME");
                selectedTimes.add(key);
            }

            updateTimeState();
        });
    }

    private void updateTimeState() {
        updateSingleTimeState(R.id.timeMorning, "MORNING", getString(R.string.response_selection_update_time_state_text));
        updateSingleTimeState(R.id.timeAfternoon, "AFTERNOON", getString(R.string.response_selection_update_time_state_text_2));
        updateSingleTimeState(R.id.timeLateAfternoon, "LATE_AFTERNOON", getString(R.string.response_selection_update_time_state_text_3));
        updateSingleTimeState(R.id.timeEvening, "EVENING", getString(R.string.response_selection_update_time_state_text_4));
        updateSingleTimeState(R.id.timeLateEvening, "LATE_EVENING", getString(R.string.response_selection_update_time_state_text_5));
        updateSingleTimeState(R.id.timeAny, "ANYTIME", getString(R.string.response_selection_update_time_state_text_6));
    }

    private void updateSingleTimeState(int id, String key, String label) {
        TextView view = null;
        for (TextView time : timeViews) {
            if (time.getId() == id) { view = time; break; }
        }
        if (view == null) return;

        if (selectedTimes.contains(key)) {
            view.setText(getString(R.string.response_selection_view_format_3, label));
            view.setTextColor(PURPLE);
            view.setBackgroundResource(R.drawable.pickday_selected);
        } else {
            view.setText(getString(R.string.response_selection_view_format_2, label));
            view.setTextColor(Color.parseColor("#59566F"));
            view.setBackgroundResource(R.drawable.pickday_unselected);
        }
    }

    private void setupExcludeOptions() {
        layoutExcludeDates.removeAllViews();
        for (DateItem item : candidateDates) {
            TextView view = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(64), dp(56));
            params.setMargins(0, 0, dp(8), 0);
            view.setLayoutParams(params);
            view.setGravity(android.view.Gravity.CENTER);
            view.setTextSize(12);
            view.setClickable(true);
            view.setFocusable(true);
            view.setContentDescription(getString(R.string.excluded_date_description, item.label));
            layoutExcludeDates.addView(view);
            addExcludeView(view, item.isoDate);
        }
        updateExcludeState();
    }

    private void addExcludeView(TextView view, String key) {
        excludeViews.add(view);

        view.setOnClickListener(v -> {
            if (excludedDates.contains(key)) {
                excludedDates.remove(key);
            } else {
                excludedDates.add(key);
                selectedDates.remove(key);
                updateSelectedDateArea();
                updateCalendarState();
            }

            updateExcludeState();
        });
    }

    private void updateExcludeState() {
        for (int index = 0; index < excludeViews.size(); index++) {
            DateItem item = candidateDates.get(index);
            updateSingleExcludeState(excludeViews.get(index), item.isoDate, item.label + "\n" + item.week);
        }

        tvExcludeCount.setText(getString(R.string.response_selection_exclude_count_format, excludedDates.size()));
    }

    private void updateSingleExcludeState(TextView view, String key, String label) {
        if (excludedDates.contains(key)) {
            view.setText(getString(R.string.response_selection_view_format, label));
            view.setTextColor(PURPLE);
            view.setBackgroundResource(R.drawable.pickday_selected);
        } else {
            view.setText(label);
            view.setTextColor(DARK_TEXT);
            view.setBackgroundResource(R.drawable.pickday_unselected);
        }
    }

    private void setListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnComplete.setOnClickListener(v -> {
            boolean allExcluded = !candidateDates.isEmpty() && excludedDates.size() == candidateDates.size();
            if (selectedDates.isEmpty() && !allExcluded) {
                Toast.makeText(this, getString(R.string.response_selection_set_listeners_text), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!selectedDates.isEmpty() && selectedTimes.isEmpty()) {
                Toast.makeText(this, getString(R.string.response_selection_set_listeners_text_2), Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                LocalMeetupRepository.saveResponse(roomId, participantName, new ArrayList<>(selectedDates),
                        new ArrayList<>(selectedTimes), new ArrayList<>(excludedDates));
            } catch (IllegalArgumentException error) {
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, getString(R.string.response_selection_set_listeners_text_3), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ResponseSelectionActivity.this, RoomDetailActivity.class);
            intent.putExtra(RoomDetailActivity.EXTRA_ROOM_ID, roomId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        state.putStringArrayList("dates", new ArrayList<>(selectedDates));
        state.putStringArrayList("times", new ArrayList<>(selectedTimes));
        state.putStringArrayList("excluded", new ArrayList<>(excludedDates));
        super.onSaveInstanceState(state);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class DateItem {
        String isoDate;
        String day;
        String label;
        String week;

        DateItem(String isoDate, String day, String label, String week) {
            this.isoDate = isoDate;
            this.day = day;
            this.label = label;
            this.week = week;
        }
    }
}
