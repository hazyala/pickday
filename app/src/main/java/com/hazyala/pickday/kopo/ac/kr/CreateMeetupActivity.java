package com.hazyala.pickday.kopo.ac.kr;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hazyala.pickday.kopo.ac.kr.ui.PickDayActivity;

import com.hazyala.pickday.kopo.ac.kr.data.LocalMeetupRepository;
import com.hazyala.pickday.kopo.ac.kr.ui.PickDayDatePicker;

import java.util.Calendar;
import java.util.Locale;

public class CreateMeetupActivity extends PickDayActivity {

    private TextView tvNameCount;
    private TextView tvDescCount;
    private TextView tvDeadlineDate;
    private TextView tvDeadlineTime;
    private TextView tvPeopleCount;
    private TextView edtMeetupName;

    private String draftRoomId;
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
        if (savedInstanceState != null) {
            draftRoomId = savedInstanceState.getString("draftRoomId");
            peopleCount = savedInstanceState.getInt("peopleCount", 2);
            tvDeadlineTime.setText(savedInstanceState.getString("deadlineTime", getString(R.string.create_meet_up_deadline_time_text)));
            selectedDeadlineDate = PickDayDatePicker.parseIsoDate(savedInstanceState.getString("deadlineDate"));
            updateDeadlineDateText();
            deadlineCalendarController.moveTo(selectedDeadlineDate);
        }
        updatePeopleText();
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        state.putString("draftRoomId", draftRoomId);
        state.putInt("peopleCount", peopleCount);
        state.putString("deadlineTime", tvDeadlineTime.getText().toString());
        state.putString("deadlineDate", PickDayDatePicker.formatIsoDate(selectedDeadlineDate));
        super.onSaveInstanceState(state);
    }

    private void setupTextCounters() {
        TextView edtMeetupDesc = findViewById(R.id.edtMeetupDesc);

        edtMeetupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvNameCount.setText(getString(R.string.create_meetup_tv_name_count_format, s.length()));
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        edtMeetupDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvDescCount.setText(getString(R.string.create_meetup_tv_desc_count_format, s.length()));
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
                Toast.makeText(this, getString(R.string.create_meetup_setup_next_button_text), Toast.LENGTH_SHORT).show();
                return;
            }

            String roomId;
            try {
                roomId = LocalMeetupRepository.saveRoomDraft(draftRoomId, meetupTitle, peopleCount,
                        PickDayDatePicker.formatIsoDate(selectedDeadlineDate), tvDeadlineTime.getText().toString(),
                        ((TextView) findViewById(R.id.edtMeetupDesc)).getText().toString());
                draftRoomId = roomId;
            } catch (IllegalArgumentException error) {
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(CreateMeetupActivity.this, SelectionActivity.class);
            intent.putExtra(SelectionActivity.EXTRA_ROOM_ID, roomId);
            startActivity(intent);
        });
    }

    private void setupTimePicker() {
        findViewById(R.id.btnTimePicker).setOnClickListener(v -> {
            TimePickerDialog dialog = new TimePickerDialog(
                    CreateMeetupActivity.this,
                    (view, hourOfDay, minute) -> {
                        String amPm = hourOfDay < 12 ? getString(R.string.time_morning) : getString(R.string.time_afternoon);
                        int hour = hourOfDay % 12;

                        if (hour == 0) {
                            hour = 12;
                        }

                        tvDeadlineTime.setText(String.format(Locale.KOREAN,
                                getString(R.string.time_clock_format), amPm, hour, minute));
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
                Toast.makeText(this, getString(R.string.create_meetup_setup_people_buttons_text), Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnPlusPeople).setOnClickListener(v -> {
            if (peopleCount < 20) {
                peopleCount++;
                updatePeopleText();
            } else {
                Toast.makeText(this, getString(R.string.create_meetup_setup_people_buttons_text_2), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePeopleText() {
        tvPeopleCount.setText(getString(R.string.room_capacity_count, peopleCount));
    }

    private void setupCalendar() {
        selectedDeadlineDate = PickDayDatePicker.today();
        updateDeadlineDateText();

        findViewById(R.id.btnDatePicker).setOnClickListener(v -> {
            PickDayDatePicker.show(this, selectedDeadlineDate, (selectedDate, displayText, summaryText) -> {
                if (PickDayDatePicker.isBeforeToday(selectedDate)) {
                    Toast.makeText(this, getString(R.string.create_meetup_setup_calendar_text), Toast.LENGTH_SHORT).show();
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

}
