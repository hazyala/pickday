package com.hazyala.pickday.kopo.ac.kr;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hazyala.pickday.kopo.ac.kr.data.DummyDataSource;

public class CreateMeetupActivity extends AppCompatActivity {

    private TextView tvNameCount;
    private TextView tvDescCount;
    private TextView tvDeadlineDate;
    private TextView tvDeadlineTime;
    private TextView tvPeopleCount;
    private TextView edtMeetupName;

    private int peopleCount = 2;
    private TextView selectedDayView = null;

    private final int[] dayIds = {
            R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5,
            R.id.day6, R.id.day7, R.id.day8, R.id.day9, R.id.day10,
            R.id.day11, R.id.day12, R.id.day13, R.id.day14, R.id.day15,
            R.id.day16, R.id.day17, R.id.day18, R.id.day19, R.id.day20,
            R.id.day21, R.id.day22, R.id.day23, R.id.day24, R.id.day25,
            R.id.day26, R.id.day27, R.id.day28, R.id.day29, R.id.day30,
            R.id.day31
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_meet_up);

        tvNameCount = findViewById(R.id.tvNameCount);
        tvDescCount = findViewById(R.id.tvDescCount);
        tvDeadlineDate = findViewById(R.id.tvDeadlineDate);
        tvDeadlineTime = findViewById(R.id.tvDeadlineTime);
        tvPeopleCount = findViewById(R.id.tvPeopleCount);
        edtMeetupName = findViewById(R.id.edtMeetupName);

        setupTextCounters();
        setupBackButton();
        setupNextButton();
        setupTimePicker();
        setupPeopleButtons();
        setupCalendar();
    }

    private void setupTextCounters() {
        TextView edtMeetupDesc = findViewById(R.id.edtMeetupDesc);

        edtMeetupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvNameCount.setText(s.length() + "/30");
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        edtMeetupDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvDescCount.setText(s.length() + "/100");
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void setupBackButton() {
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            Intent intent = new Intent(CreateMeetupActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupNextButton() {
        findViewById(R.id.btnNext).setOnClickListener(v -> {
            String meetupTitle = edtMeetupName.getText().toString().trim();

            if (meetupTitle.isEmpty()) {
                Toast.makeText(this, "모임명을 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            DummyDataSource.addCreatedMeetupRoom(
                    meetupTitle,
                    peopleCount,
                    "D-1"
            );

            Intent intent = new Intent(CreateMeetupActivity.this, SelectionActivity.class);
            startActivity(intent);
        });
    }

    private void setupTimePicker() {
        findViewById(R.id.btnTimePicker).setOnClickListener(v -> {
            TimePickerDialog dialog = new TimePickerDialog(
                    CreateMeetupActivity.this,
                    (view, hourOfDay, minute) -> {
                        String amPm = hourOfDay < 12 ? "오전" : "오후";
                        int hour = hourOfDay % 12;

                        if (hour == 0) {
                            hour = 12;
                        }

                        tvDeadlineTime.setText(String.format("%s %d:%02d", amPm, hour, minute));
                    },
                    23,
                    59,
                    false
            );

            dialog.show();
        });
    }

    private void setupPeopleButtons() {
        findViewById(R.id.btnMinusPeople).setOnClickListener(v -> {
            if (peopleCount > 2) {
                peopleCount--;
                updatePeopleText();
            } else {
                Toast.makeText(this, "최소 인원은 2명입니다", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnPlusPeople).setOnClickListener(v -> {
            if (peopleCount < 20) {
                peopleCount++;
                updatePeopleText();
            } else {
                Toast.makeText(this, "최대 인원은 20명입니다", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePeopleText() {
        tvPeopleCount.setText(peopleCount + "명");
    }

    private void setupCalendar() {
        for (int i = 0; i < dayIds.length; i++) {
            final int day = i + 1;
            TextView dayView = findViewById(dayIds[i]);

            dayView.setOnClickListener(v -> {
                tvDeadlineDate.setText(String.format("2025.05.%02d", day));
                updateSelectedDay(dayView);
            });

            if (day == 27) {
                tvDeadlineDate.setText("2025.05.27");
                updateSelectedDay(dayView);
            }
        }

        findViewById(R.id.btnDatePicker).setOnClickListener(v -> {
            Toast.makeText(this, "아래 달력에서 날짜를 선택해 주세요", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateSelectedDay(TextView newSelectedDayView) {
        if (selectedDayView != null) {
            selectedDayView.setBackgroundColor(Color.TRANSPARENT);
            selectedDayView.setTextColor(Color.parseColor("#252538"));
        }

        selectedDayView = newSelectedDayView;
        selectedDayView.setBackgroundResource(R.drawable.pickday_selected);
        selectedDayView.setTextColor(Color.parseColor("#6A4DFF"));
    }
}
