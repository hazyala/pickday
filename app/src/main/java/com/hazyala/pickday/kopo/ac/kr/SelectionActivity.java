package com.hazyala.pickday.kopo.ac.kr;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hazyala.pickday.kopo.ac.kr.ui.PickDayActivity;

import com.hazyala.pickday.kopo.ac.kr.data.LocalMeetupRepository;
import com.hazyala.pickday.kopo.ac.kr.ui.PickDayDatePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SelectionActivity extends PickDayActivity {

    public static final String EXTRA_ROOM_ID = "extra_room_id";

    private View btnBack;
    private TextView btnNext;
    private View selectionCalendar;
    private String roomId;

    private TextView tvDateCount, tvExcludeCount;
    private LinearLayout layoutCandidateDates;
    private PickDayDatePicker.CalendarController selectionCalendarController;

    private final Set<String> selectedDates = new LinkedHashSet<>();
    private final Set<TextView> selectedTimes = new HashSet<>();
    private final Set<String> selectedExcludeDates = new LinkedHashSet<>();

    private static final int MAX_DATE_COUNT = 10;

    private final int PURPLE = Color.parseColor("#6A4DFF");
    private final int DARK_TEXT = Color.parseColor("#59566F");
    private final int WHITE = Color.parseColor("#FFFFFF");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        initViews();
        if (LocalMeetupRepository.getMeetupRoomById(roomId) == null) {
            Toast.makeText(this, getString(R.string.error_room_not_found), Toast.LENGTH_SHORT).show();
            finish(); return;
        }
        selectedExcludeDates.addAll(LocalMeetupRepository.getHostSelectionDraft(roomId).excluded);
        if (savedInstanceState != null) {
            selectedExcludeDates.clear();
            List<String> restored = savedInstanceState.getStringArrayList("excluded");
            if (restored != null) selectedExcludeDates.addAll(restored);
        }
        initButtons();
        initDateChips(savedInstanceState);
        initTimeChips();
        initExcludeDateChips();
        if (savedInstanceState != null) {
            List<String> times = savedInstanceState.getStringArrayList("times");
            selectedTimes.clear();
            int[] ids = {R.id.timeMorning, R.id.timeAfternoon, R.id.timeLateAfternoon,
                    R.id.timeEvening, R.id.timeLateEvening, R.id.timeAny};
            for (int id : ids) {
                TextView view = findViewById(id);
                boolean selected = times != null && times.contains(view.getTag());
                if (selected) selectedTimes.add(view);
                setTimeChipSelected(view, selected);
            }
        }
        updateDateCount();
        updateExcludeCount();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);

        if (roomId == null || roomId.isEmpty()) {
            roomId = LocalMeetupRepository.getCurrentDraftRoomId();
        }

        selectionCalendar = findViewById(R.id.selectionCalendar);
        layoutCandidateDates = findViewById(R.id.layoutCandidateDates);

        tvDateCount = findViewById(R.id.tvDateCount);
        tvExcludeCount = findViewById(R.id.tvExcludeCount);
    }

    private void initButtons() {
        btnBack.setOnClickListener(v -> finish());

        btnNext.setOnClickListener(v -> {
            if (selectedDates.isEmpty()) {
                Toast.makeText(this, getString(R.string.selection_init_buttons_text), Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedTimes.isEmpty()) {
                Toast.makeText(this, getString(R.string.selection_init_buttons_text_2), Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> times = new ArrayList<>();
            for (TextView view : selectedTimes) times.add((String) view.getTag());
            List<String> excluded = new ArrayList<>(selectedExcludeDates);
            try {
                LocalMeetupRepository.saveHostSelection(roomId, getSortedSelectedDates(), times, excluded);
            } catch (IllegalArgumentException error) {
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(SelectionActivity.this, InviteMembersActivity.class);
            intent.putExtra(InviteMembersActivity.EXTRA_ROOM_ID, roomId);
            startActivity(intent);
        });

        selectionCalendar.setVisibility(View.VISIBLE);
    }

    private void initDateChips(Bundle state) {
        List<String> restored = state == null ? null : state.getStringArrayList("dates");
        selectedDates.addAll(restored == null ? LocalMeetupRepository.getHostSelectionDraft(roomId).dates : restored);

        selectionCalendarController = PickDayDatePicker.attachCalendar(
                selectionCalendar,
                selectedDates.isEmpty() ? PickDayDatePicker.today() : PickDayDatePicker.parseIsoDate(getSortedSelectedDates().get(0)),
                new PickDayDatePicker.CalendarDateRule() {
                    @Override
                    public boolean isEnabled(Calendar date) {
                        return !PickDayDatePicker.isBeforeToday(date)
                                && PickDayDatePicker.formatIsoDate(date).compareTo(
                                        LocalMeetupRepository.getMeetupRoomById(roomId).deadlineDateIso) > 0;
                    }

                    @Override
                    public boolean isSelected(Calendar date) {
                        return selectedDates.contains(PickDayDatePicker.formatIsoDate(date));
                    }
                },
                selectedDate -> toggleDate(PickDayDatePicker.formatIsoDate(selectedDate))
        );

        renderSelectedDateChips();
    }

    private void toggleDate(String isoDate) {
        boolean isSelected = selectedDates.contains(isoDate);

        if (isSelected) {
            selectedDates.remove(isoDate);
        } else {
            if (selectedDates.size() >= MAX_DATE_COUNT) {
                Toast.makeText(this, getString(R.string.response_selection_toggle_date_text), Toast.LENGTH_SHORT).show();
                return;
            }

            selectedDates.add(isoDate);
            selectedExcludeDates.remove(isoDate);
            updateExcludeCount();
        }

        renderSelectedDateChips();
        initExcludeDateChips();
        selectionCalendarController.render();
        updateDateCount();
    }

    private void renderSelectedDateChips() {
        layoutCandidateDates.removeAllViews();

        for (String isoDate : getSortedSelectedDates()) {
            layoutCandidateDates.addView(createDateChip(isoDate));
        }
    }

    private View createDateChip(String isoDate) {
        Calendar date = PickDayDatePicker.parseIsoDate(isoDate);
        LinearLayout chip = new LinearLayout(this);
        chip.setOrientation(LinearLayout.VERTICAL);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(0, dp(8), 0, dp(8));
        chip.setBackgroundResource(R.drawable.pickday_selected);
        chip.setClickable(true);
        chip.setFocusable(true);

        LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(dp(72), LinearLayout.LayoutParams.WRAP_CONTENT);
        chipParams.setMargins(0, 0, dp(8), 0);
        chip.setLayoutParams(chipParams);

        TextView topText = createDateChipText(WHITE, "");
        TextView mainText = createDateChipText(PURPLE, PickDayDatePicker.formatChipDate(date));
        mainText.setTextSize(16);
        TextView subText = createDateChipText(PURPLE, PickDayDatePicker.formatWeek(date));
        TextView checkText = createDateChipText(WHITE, "✓");
        checkText.setTextSize(12);
        checkText.setBackgroundResource(R.drawable.pickday_button_primary);

        String relativeLabel = getRelativeDateLabel(date);
        topText.setText(relativeLabel);

        chip.addView(topText);
        chip.addView(mainText);
        chip.addView(subText);
        chip.addView(checkText);
        chip.setOnClickListener(v -> toggleDate(isoDate));

        return chip;
    }

    private TextView createDateChipText(int color, String text) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        textView.setGravity(Gravity.CENTER);
        textView.setIncludeFontPadding(false);
        textView.setText(text);
        textView.setTextColor(color);
        textView.setTextSize(14);
        textView.setTypeface(null, Typeface.BOLD);
        return textView;
    }

    private String getRelativeDateLabel(Calendar date) {
        Calendar today = PickDayDatePicker.today();
        Calendar tomorrow = PickDayDatePicker.today();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        if (PickDayDatePicker.isSameDate(date, today)) {
            return getString(R.string.date_today);
        }

        if (PickDayDatePicker.isSameDate(date, tomorrow)) {
            return getString(R.string.date_tomorrow);
        }

        return "";
    }

    private List<String> getSortedSelectedDates() {
        List<String> dates = new ArrayList<>(selectedDates);
        Collections.sort(dates);
        return dates;
    }

    private void updateDateCount() {
        tvDateCount.setText(getString(R.string.selection_date_count_format, selectedDates.size()));
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        state.putStringArrayList("dates", new ArrayList<>(selectedDates));
        ArrayList<String> times = new ArrayList<>();
        for (TextView view : selectedTimes) times.add((String) view.getTag());
        state.putStringArrayList("times", times);
        state.putStringArrayList("excluded", new ArrayList<>(selectedExcludeDates));
        super.onSaveInstanceState(state);
    }

    @Override
    protected void onStop() {
        super.onStop();
        List<String> times = new ArrayList<>();
        for (TextView view : selectedTimes) times.add((String) view.getTag());
        LocalMeetupRepository.keepHostSelectionDraft(roomId, getSortedSelectedDates(), times,
                new ArrayList<>(selectedExcludeDates));
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void initTimeChips() {
        TextView timeMorning = findViewById(R.id.timeMorning);
        TextView timeAfternoon = findViewById(R.id.timeAfternoon);
        TextView timeLateAfternoon = findViewById(R.id.timeLateAfternoon);
        TextView timeEvening = findViewById(R.id.timeEvening);
        TextView timeLateEvening = findViewById(R.id.timeLateEvening);
        TextView timeAny = findViewById(R.id.timeAny);

        TextView[] timeViews = {
                timeMorning, timeAfternoon, timeLateAfternoon,
                timeEvening, timeLateEvening, timeAny
        };

        String[] codes = {"MORNING", "AFTERNOON", "LATE_AFTERNOON", "EVENING", "LATE_EVENING", "ANYTIME"};
        List<String> draftTimes = LocalMeetupRepository.getHostSelectionDraft(roomId).times;
        for (int index = 0; index < timeViews.length; index++) {
            TextView timeView = timeViews[index];
            timeView.setTag(codes[index]);
            if (draftTimes.contains(codes[index])) {
                selectedTimes.add(timeView);
                setTimeChipSelected(timeView, true);
            }
            timeView.setOnClickListener(v -> toggleTimeChip((TextView) v));
        }
    }

    private void toggleTimeChip(TextView timeView) {
        boolean isSelected = selectedTimes.contains(timeView);

        if (isSelected) {
            selectedTimes.remove(timeView);
            setTimeChipSelected(timeView, false);
        } else {
            for (TextView selected : new ArrayList<>(selectedTimes)) {
                if ("ANYTIME".equals(timeView.getTag()) || "ANYTIME".equals(selected.getTag())) {
                    selectedTimes.remove(selected);
                    setTimeChipSelected(selected, false);
                }
            }
            selectedTimes.add(timeView);
            setTimeChipSelected(timeView, true);
        }
    }

    private void setTimeChipSelected(TextView timeView, boolean selected) {
        String text = timeView.getText().toString();
        text = text.replace("✓", "").replace("○", "").trim();

        if (selected) {
            timeView.setBackgroundResource(R.drawable.pickday_selected);
            timeView.setTextColor(PURPLE);
            timeView.setText(getString(R.string.selection_time_view_format_2, text));
        } else {
            timeView.setBackgroundResource(R.drawable.pickday_unselected);
            timeView.setTextColor(DARK_TEXT);
            timeView.setText(getString(R.string.selection_time_view_format, text));
        }
    }

    private void initExcludeDateChips() {
        LinearLayout container = findViewById(R.id.layoutExcludeDates);
        container.removeAllViews();
        Set<String> dates = new java.util.TreeSet<>(selectedDates);
        dates.addAll(selectedExcludeDates);
        Calendar date = PickDayDatePicker.today();
        for (int index = 0; index < 6; index++) {
            dates.add(PickDayDatePicker.formatIsoDate(date));
            date.add(Calendar.DAY_OF_MONTH, 1);
        }
        for (String isoDate : dates) {
            TextView view = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(64), dp(60));
            params.setMargins(0, 0, dp(8), 0);
            view.setLayoutParams(params);
            view.setGravity(Gravity.CENTER);
            view.setTextSize(12);
            view.setTag(isoDate);
            view.setText(formatExcludeDateLabel(PickDayDatePicker.parseIsoDate(isoDate)));
            view.setContentDescription(getString(R.string.excluded_date_description, isoDate));
            view.setClickable(true);
            view.setFocusable(true);
            setExcludeChipSelected(view, selectedExcludeDates.contains(isoDate));
            view.setOnClickListener(v -> toggleExcludeChip((TextView) v));
            container.addView(view);
        }
    }

    private String formatExcludeDateLabel(Calendar date) {
        return PickDayDatePicker.formatChipDate(date) + "\n" + PickDayDatePicker.formatWeek(date);
    }

    private void toggleExcludeChip(TextView excludeView) {
        String isoDate = (String) excludeView.getTag();
        boolean isSelected = selectedExcludeDates.contains(isoDate);

        if (isSelected) {
            selectedExcludeDates.remove(isoDate);
            setExcludeChipSelected(excludeView, false);
        } else {
            selectedExcludeDates.add(isoDate);
            selectedDates.remove((String) excludeView.getTag());
            renderSelectedDateChips();
            selectionCalendarController.render();
            updateDateCount();
            setExcludeChipSelected(excludeView, true);
        }

        updateExcludeCount();
    }

    private void setExcludeChipSelected(TextView excludeView, boolean selected) {
        if (selected) {
            excludeView.setBackgroundResource(R.drawable.pickday_selected);
            excludeView.setTextColor(PURPLE);
        } else {
            excludeView.setBackgroundResource(R.drawable.pickday_unselected);
            excludeView.setTextColor(DARK_TEXT);
        }
    }

    private void updateExcludeCount() {
        tvExcludeCount.setText(getString(R.string.selection_exclude_count_format, selectedExcludeDates.size()));
    }
}
