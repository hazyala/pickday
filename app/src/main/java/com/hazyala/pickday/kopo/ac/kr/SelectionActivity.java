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

import androidx.appcompat.app.AppCompatActivity;

import com.hazyala.pickday.kopo.ac.kr.data.DummyDataSource;
import com.hazyala.pickday.kopo.ac.kr.ui.PickDayDatePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SelectionActivity extends AppCompatActivity {

    private View btnBack;
    private TextView btnNext, btnCalendarToggle;
    private View selectionCalendar;

    private TextView tvDateCount, tvExcludeCount;
    private LinearLayout layoutCandidateDates;
    private PickDayDatePicker.CalendarController selectionCalendarController;

    private final Set<String> selectedDates = new LinkedHashSet<>();
    private final Set<TextView> selectedTimes = new HashSet<>();
    private final Set<TextView> selectedExcludeDates = new HashSet<>();

    private static final int MAX_DATE_COUNT = 10;

    private final int PURPLE = Color.parseColor("#6A4DFF");
    private final int DARK_TEXT = Color.parseColor("#59566F");
    private final int WHITE = Color.parseColor("#FFFFFF");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        initViews();
        initButtons();
        initDateChips();
        initTimeChips();
        initExcludeDateChips();
        updateDateCount();
        updateExcludeCount();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        btnCalendarToggle = findViewById(R.id.btnCalendarToggle);

        selectionCalendar = findViewById(R.id.selectionCalendar);
        layoutCandidateDates = findViewById(R.id.layoutCandidateDates);

        tvDateCount = findViewById(R.id.tvDateCount);
        tvExcludeCount = findViewById(R.id.tvExcludeCount);
    }

    private void initButtons() {
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(SelectionActivity.this, CreateMeetupActivity.class);
            startActivity(intent);
            finish();
        });

        btnNext.setOnClickListener(v -> {
            if (selectedDates.isEmpty()) {
                Toast.makeText(this, "후보 날짜를 1개 이상 선택해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedTimes.isEmpty()) {
                Toast.makeText(this, "시간대를 1개 이상 선택해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            DummyDataSource.setCurrentDraftCandidateDates(getSortedSelectedDates());

            Intent intent = new Intent(SelectionActivity.this, InviteMembersActivity.class);
            startActivity(intent);
        });

        btnCalendarToggle.setOnClickListener(v -> {
            if (selectionCalendar.getVisibility() == View.VISIBLE) {
                selectionCalendar.setVisibility(View.GONE);
                btnCalendarToggle.setText("▣  달력 보기");
            } else {
                selectionCalendar.setVisibility(View.VISIBLE);
                btnCalendarToggle.setText("▣  닫기");
            }
        });
    }

    private void initDateChips() {
        selectedDates.addAll(DummyDataSource.getCurrentDraftCandidateDates());

        selectionCalendarController = PickDayDatePicker.attachCalendar(
                selectionCalendar,
                selectedDates.isEmpty() ? PickDayDatePicker.today() : PickDayDatePicker.parseIsoDate(getSortedSelectedDates().get(0)),
                new PickDayDatePicker.CalendarDateRule() {
                    @Override
                    public boolean isEnabled(Calendar date) {
                        return !PickDayDatePicker.isBeforeToday(date);
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
                Toast.makeText(this, "최대 10개까지 선택할 수 있어요", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedDates.add(isoDate);
        }

        renderSelectedDateChips();
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

        LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(dp(58), dp(76));
        chipParams.setMargins(0, 0, dp(8), 0);
        chip.setLayoutParams(chipParams);

        TextView topText = createDateChipText(8, WHITE, "");
        TextView mainText = createDateChipText(22, PURPLE, PickDayDatePicker.formatChipDate(date));
        mainText.setTextSize(16);
        TextView subText = createDateChipText(16, PURPLE, PickDayDatePicker.formatWeek(date));
        TextView checkText = createDateChipText(16, WHITE, "✓");
        checkText.setTextSize(10);
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

    private TextView createDateChipText(int heightDp, int color, String text) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(heightDp)
        ));
        textView.setGravity(Gravity.CENTER);
        textView.setIncludeFontPadding(false);
        textView.setText(text);
        textView.setTextColor(color);
        textView.setTextSize(11);
        textView.setTypeface(null, Typeface.BOLD);
        return textView;
    }

    private String getRelativeDateLabel(Calendar date) {
        Calendar today = PickDayDatePicker.today();
        Calendar tomorrow = PickDayDatePicker.today();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        if (PickDayDatePicker.isSameDate(date, today)) {
            return "오늘";
        }

        if (PickDayDatePicker.isSameDate(date, tomorrow)) {
            return "내일";
        }

        return "";
    }

    private List<String> getSortedSelectedDates() {
        List<String> dates = new ArrayList<>(selectedDates);
        Collections.sort(dates);
        return dates;
    }

    private void updateDateCount() {
        tvDateCount.setText("✓ 최대 10개까지 선택할 수 있어요 (" + selectedDates.size() + "/10)");
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

        for (TextView timeView : timeViews) {
            timeView.setOnClickListener(v -> toggleTimeChip((TextView) v));
        }
    }

    private void toggleTimeChip(TextView timeView) {
        boolean isSelected = selectedTimes.contains(timeView);

        if (isSelected) {
            selectedTimes.remove(timeView);
            setTimeChipSelected(timeView, false);
        } else {
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
            timeView.setText("✓  " + text);
        } else {
            timeView.setBackgroundResource(R.drawable.pickday_unselected);
            timeView.setTextColor(DARK_TEXT);
            timeView.setText("○  " + text);
        }
    }

    private void initExcludeDateChips() {
        TextView exclude0529 = findViewById(R.id.exclude0529);
        TextView exclude0530 = findViewById(R.id.exclude0530);
        TextView exclude0531 = findViewById(R.id.exclude0531);
        TextView exclude0601 = findViewById(R.id.exclude0601);
        TextView exclude0605 = findViewById(R.id.exclude0605);
        TextView exclude0606 = findViewById(R.id.exclude0606);

        TextView[] excludeViews = {
                exclude0529, exclude0530, exclude0531,
                exclude0601, exclude0605, exclude0606
        };

        for (TextView excludeView : excludeViews) {
            excludeView.setOnClickListener(v -> toggleExcludeChip((TextView) v));
        }
    }

    private void toggleExcludeChip(TextView excludeView) {
        boolean isSelected = selectedExcludeDates.contains(excludeView);

        if (isSelected) {
            selectedExcludeDates.remove(excludeView);
            setExcludeChipSelected(excludeView, false);
        } else {
            selectedExcludeDates.add(excludeView);
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
        tvExcludeCount.setText(selectedExcludeDates.size() + "개 선택  ⌄");
    }
}
