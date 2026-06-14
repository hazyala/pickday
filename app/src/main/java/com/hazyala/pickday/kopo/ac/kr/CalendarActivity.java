package com.hazyala.pickday.kopo.ac.kr;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.hazyala.pickday.kopo.ac.kr.data.DummyDataSource;
import com.hazyala.pickday.kopo.ac.kr.ui.PickDayDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity {

    private TextView tvMonthTitle;
    private TextView tvScheduleTitle;
    private GridLayout monthGrid;
    private LinearLayout layoutScheduleContainer;

    private LinearLayout tabHome;
    private LinearLayout tabChat;
    private LinearLayout tabMy;
    private AppCompatButton btnFab;

    private final Calendar visibleMonth = Calendar.getInstance();
    private final List<DummyDataSource.CalendarMeetup> allMeetups = new ArrayList<>();

    private final int DARK_TEXT = Color.parseColor("#20213B");
    private final int MUTED_TEXT = Color.parseColor("#A9A8C2");
    private final int PURPLE = Color.parseColor("#5B2DFF");
    private final int PINK = Color.parseColor("#FF4F91");
    private final int WHITE = Color.parseColor("#FFFFFF");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        visibleMonth.set(Calendar.DAY_OF_MONTH, 1);
        allMeetups.addAll(DummyDataSource.getCalendarMeetups());

        initViews();
        setListeners();
        renderCalendar();
    }

    private void initViews() {
        tvMonthTitle = findViewById(R.id.tvMonthTitle);
        tvScheduleTitle = findViewById(R.id.tvScheduleTitle);
        monthGrid = findViewById(R.id.monthGrid);
        layoutScheduleContainer = findViewById(R.id.layoutScheduleContainer);

        tabHome = findViewById(R.id.tabHome);
        tabChat = findViewById(R.id.tabChat);
        tabMy = findViewById(R.id.tabMy);
        btnFab = findViewById(R.id.btnFab);
    }

    private void setListeners() {
        findViewById(R.id.btnPrevMonth).setOnClickListener(v -> {
            visibleMonth.add(Calendar.MONTH, -1);
            renderCalendar();
        });

        findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            visibleMonth.add(Calendar.MONTH, 1);
            renderCalendar();
        });

        tabHome.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnFab.setOnClickListener(v -> startActivity(new Intent(CalendarActivity.this, CreateMeetupActivity.class)));
        tabChat.setOnClickListener(v -> startActivity(new Intent(CalendarActivity.this, ChatActivity.class)));
        tabMy.setOnClickListener(v -> startActivity(new Intent(CalendarActivity.this, MyPageActivity.class)));
    }

    private void renderCalendar() {
        tvMonthTitle.setText(formatMonthTitle(visibleMonth));
        monthGrid.removeAllViews();

        Map<String, List<DummyDataSource.CalendarMeetup>> meetupsByDate = getMeetupsByDate();
        Calendar cursor = (Calendar) visibleMonth.clone();
        int firstWeekday = cursor.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
        cursor.add(Calendar.DAY_OF_MONTH, -firstWeekday);

        for (int index = 0; index < 42; index++) {
            Calendar cellDate = (Calendar) cursor.clone();
            monthGrid.addView(createDayCell(cellDate, meetupsByDate));
            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }

        renderMonthSchedules();
    }

    private View createDayCell(
            Calendar cellDate,
            Map<String, List<DummyDataSource.CalendarMeetup>> meetupsByDate
    ) {
        LinearLayout cell = new LinearLayout(this);
        cell.setGravity(Gravity.CENTER);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setPadding(0, dp(2), 0, dp(2));

        GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, 1f),
                GridLayout.spec(GridLayout.UNDEFINED, 1f)
        );
        params.width = 0;
        params.height = 0;
        params.setMargins(dp(1), dp(1), dp(1), dp(1));
        cell.setLayoutParams(params);

        boolean inMonth = cellDate.get(Calendar.MONTH) == visibleMonth.get(Calendar.MONTH);
        boolean isToday = PickDayDatePicker.isSameDate(cellDate, Calendar.getInstance());

        TextView dayText = new TextView(this);
        LinearLayout.LayoutParams dayParams = new LinearLayout.LayoutParams(dp(34), dp(34));
        dayText.setLayoutParams(dayParams);
        dayText.setGravity(Gravity.CENTER);
        dayText.setIncludeFontPadding(false);
        dayText.setText(String.valueOf(cellDate.get(Calendar.DAY_OF_MONTH)));
        dayText.setTextSize(15);
        dayText.setTypeface(null, Typeface.BOLD);

        if (!inMonth) {
            dayText.setTextColor(MUTED_TEXT);
        } else if (isToday) {
            dayText.setTextColor(WHITE);
            dayText.setBackgroundResource(R.drawable.bg_calendar_day_selected);
        } else if (cellDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            dayText.setTextColor(PINK);
        } else if (cellDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            dayText.setTextColor(PURPLE);
        } else {
            dayText.setTextColor(DARK_TEXT);
        }

        LinearLayout dots = new LinearLayout(this);
        dots.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(8)
        ));
        dots.setGravity(Gravity.CENTER);
        dots.setOrientation(LinearLayout.HORIZONTAL);

        List<DummyDataSource.CalendarMeetup> meetups = meetupsByDate.get(PickDayDatePicker.formatIsoDate(cellDate));

        if (inMonth && meetups != null) {
            int maxDotCount = Math.min(meetups.size(), 3);

            for (int index = 0; index < maxDotCount; index++) {
                TextView dot = new TextView(this);
                dot.setLayoutParams(new LinearLayout.LayoutParams(dp(8), dp(8)));
                dot.setGravity(Gravity.CENTER);
                dot.setIncludeFontPadding(false);
                dot.setText("•");
                dot.setTextColor(Color.parseColor(meetups.get(index).accentColor));
                dot.setTextSize(14);
                dots.addView(dot);
            }
        }

        cell.addView(dayText);
        cell.addView(dots);

        return cell;
    }

    private void renderMonthSchedules() {
        layoutScheduleContainer.removeAllViews();
        List<DummyDataSource.CalendarMeetup> monthMeetups = getVisibleMonthMeetups();
        tvScheduleTitle.setText(formatScheduleTitle(monthMeetups.size()));

        if (monthMeetups.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(80)
            ));
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setText("이번 달에 등록된 약속이 없어요");
            emptyText.setTextColor(MUTED_TEXT);
            emptyText.setTextSize(13);
            emptyText.setTypeface(null, Typeface.BOLD);
            emptyText.setBackgroundResource(R.drawable.pickday_card_white);
            layoutScheduleContainer.addView(emptyText);
            return;
        }

        for (DummyDataSource.CalendarMeetup meetup : monthMeetups) {
            layoutScheduleContainer.addView(createScheduleItem(meetup));
        }
    }

    private View createScheduleItem(DummyDataSource.CalendarMeetup meetup) {
        LinearLayout item = new LinearLayout(this);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setPadding(dp(14), dp(12), dp(12), dp(12));
        item.setBackgroundResource(R.drawable.pickday_card_white);

        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(86)
        );
        itemParams.setMargins(0, 0, 0, dp(10));
        item.setLayoutParams(itemParams);

        TextView icon = new TextView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(50), dp(50));
        icon.setLayoutParams(iconParams);
        icon.setGravity(Gravity.CENTER);
        icon.setText(meetup.iconText);
        icon.setTextColor(Color.parseColor(meetup.accentColor));
        icon.setTextSize(17);
        icon.setTypeface(null, Typeface.BOLD);
        icon.setBackgroundResource(R.drawable.pickday_card_soft);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        contentParams.setMargins(dp(14), 0, dp(10), 0);
        content.setLayoutParams(contentParams);

        TextView title = new TextView(this);
        title.setText(meetup.title);
        title.setTextColor(DARK_TEXT);
        title.setTextSize(17);
        title.setTypeface(null, Typeface.BOLD);
        title.setIncludeFontPadding(false);

        TextView time = new TextView(this);
        time.setText(formatScheduleDate(meetup.dateIso) + " " + meetup.timeText);
        time.setTextColor(Color.parseColor(meetup.accentColor));
        time.setTextSize(13);
        time.setTypeface(null, Typeface.BOLD);
        time.setIncludeFontPadding(false);
        time.setPadding(0, dp(7), 0, 0);

        TextView participants = new TextView(this);
        participants.setText("참여자 " + meetup.participantCount + "명");
        participants.setTextColor(Color.parseColor("#6E6C86"));
        participants.setTextSize(12);
        participants.setIncludeFontPadding(false);
        participants.setPadding(0, dp(6), 0, 0);

        content.addView(title);
        content.addView(time);
        content.addView(participants);

        TextView status = new TextView(this);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(dp(66), dp(28));
        status.setLayoutParams(statusParams);
        status.setGravity(Gravity.CENTER);
        status.setText(meetup.statusText);
        status.setTextColor(Color.parseColor(meetup.accentColor));
        status.setTextSize(12);
        status.setTypeface(null, Typeface.BOLD);
        status.setBackgroundResource(R.drawable.bg_calendar_pill);

        TextView arrow = new TextView(this);
        arrow.setLayoutParams(new LinearLayout.LayoutParams(dp(24), LinearLayout.LayoutParams.MATCH_PARENT));
        arrow.setGravity(Gravity.CENTER);
        arrow.setText("›");
        arrow.setTextColor(DARK_TEXT);
        arrow.setTextSize(28);
        arrow.setTypeface(null, Typeface.BOLD);

        item.addView(icon);
        item.addView(content);
        item.addView(status);
        item.addView(arrow);

        return item;
    }

    private Map<String, List<DummyDataSource.CalendarMeetup>> getMeetupsByDate() {
        Map<String, List<DummyDataSource.CalendarMeetup>> meetupsByDate = new HashMap<>();

        for (DummyDataSource.CalendarMeetup meetup : allMeetups) {
            if (!meetupsByDate.containsKey(meetup.dateIso)) {
                meetupsByDate.put(meetup.dateIso, new ArrayList<>());
            }

            meetupsByDate.get(meetup.dateIso).add(meetup);
        }

        return meetupsByDate;
    }

    private List<DummyDataSource.CalendarMeetup> getVisibleMonthMeetups() {
        List<DummyDataSource.CalendarMeetup> monthMeetups = new ArrayList<>();

        for (DummyDataSource.CalendarMeetup meetup : allMeetups) {
            Calendar date = PickDayDatePicker.parseIsoDate(meetup.dateIso);

            if (date.get(Calendar.YEAR) == visibleMonth.get(Calendar.YEAR)
                    && date.get(Calendar.MONTH) == visibleMonth.get(Calendar.MONTH)) {
                monthMeetups.add(meetup);
            }
        }

        return monthMeetups;
    }

    private String formatMonthTitle(Calendar month) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월", Locale.KOREAN);
        return sdf.format(month.getTime());
    }

    private String formatScheduleTitle(int count) {
        SimpleDateFormat sdf = new SimpleDateFormat("M월 일정", Locale.KOREAN);
        return sdf.format(visibleMonth.getTime()) + " (" + count + ")";
    }

    private String formatScheduleDate(String isoDate) {
        Calendar date = PickDayDatePicker.parseIsoDate(isoDate);
        SimpleDateFormat sdf = new SimpleDateFormat("M월 d일 (E)", Locale.KOREAN);
        return sdf.format(date.getTime());
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
