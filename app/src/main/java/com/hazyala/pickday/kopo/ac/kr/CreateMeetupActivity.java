package com.hazyala.pickday.kopo.ac.kr;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hazyala.pickday.kopo.ac.kr.data.DummyDataSource;
import com.hazyala.pickday.kopo.ac.kr.ui.PickDayDatePicker;

import java.util.Calendar;

public class CreateMeetupActivity extends AppCompatActivity {

    private TextView tvNameCount;
    private TextView tvDescCount;
    private TextView tvDeadlineDate;
    private TextView tvDeadlineTime;
    private TextView tvPeopleCount;
    private TextView edtMeetupName;

    private int peopleCount = 2;
    private Calendar selectedDeadlineDate;
    private PickDayDatePicker.CalendarController deadlineCalendarController;

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
                    getDeadlineDDay(),
                    PickDayDatePicker.formatIsoDate(selectedDeadlineDate),
                    tvDeadlineTime.getText().toString()
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
        selectedDeadlineDate = PickDayDatePicker.today();
        updateDeadlineDateText();

        findViewById(R.id.btnDatePicker).setOnClickListener(v -> {
            PickDayDatePicker.show(this, selectedDeadlineDate, (selectedDate, displayText, summaryText) -> {
                if (PickDayDatePicker.isBeforeToday(selectedDate)) {
                    Toast.makeText(this, "오늘 이후 날짜를 선택해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                selectedDeadlineDate = selectedDate;
                updateDeadlineDateText();
                deadlineCalendarController.moveTo(selectedDeadlineDate);
            });
        });

        View deadlineCalendar = findViewById(R.id.deadlineCalendar);
        deadlineCalendarController = PickDayDatePicker.attachCalendar(
                deadlineCalendar,
                selectedDeadlineDate,
                new PickDayDatePicker.CalendarDateRule() {
                    @Override
                    public boolean isEnabled(Calendar date) {
                        return !PickDayDatePicker.isBeforeToday(date);
                    }

                    @Override
                    public boolean isSelected(Calendar date) {
                        return PickDayDatePicker.isSameDate(date, selectedDeadlineDate);
                    }
                },
                selectedDate -> {
                    selectedDeadlineDate = selectedDate;
                    updateDeadlineDateText();
                }
        );
    }

    private void updateDeadlineDateText() {
        tvDeadlineDate.setText(PickDayDatePicker.formatDeadlineDate(selectedDeadlineDate));
    }

    private String getDeadlineDDay() {
        Calendar today = PickDayDatePicker.today();
        Calendar deadline = PickDayDatePicker.parseIsoDate(PickDayDatePicker.formatIsoDate(selectedDeadlineDate));
        long diffMillis = deadline.getTimeInMillis() - today.getTimeInMillis();
        long diffDays = diffMillis / (24L * 60L * 60L * 1000L);

        if (diffDays <= 0) {
            return "D-Day";
        }

        return "D-" + diffDays;
    }
}
