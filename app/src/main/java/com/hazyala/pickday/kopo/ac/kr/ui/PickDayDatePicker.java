package com.hazyala.pickday.kopo.ac.kr.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import com.hazyala.pickday.kopo.ac.kr.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PickDayDatePicker {

    private static final int DARK_TEXT = Color.parseColor("#252538");
    private static final int PURPLE = Color.parseColor("#6A4DFF");
    private static final int DISABLED_TEXT = Color.parseColor("#B8B8C8");
    private static final int WHITE = Color.parseColor("#FFFFFF");
    private static final String ISO_PATTERN = "yyyy-MM-dd";

    public interface OnDateSelectedListener {
        void onDateSelected(Calendar selectedDate, String displayText, String summaryText);
    }

    public interface CalendarDateRule {
        boolean isEnabled(Calendar date);

        boolean isSelected(Calendar date);

        default boolean isHighlighted(Calendar date) {
            return false;
        }

        default boolean usesFilledSelection(Calendar date) {
            return false;
        }
    }

    public interface OnCalendarDateClickListener {
        void onDateClick(Calendar selectedDate);
    }

    public static class CalendarController {
        private final View calendarView;
        private final TextView titleView;
        private final TextView prevView;
        private final TextView nextView;
        private final GridLayout calendarGrid;
        private final Calendar visibleMonth;
        private final CalendarDateRule dateRule;
        private final OnCalendarDateClickListener clickListener;

        private CalendarController(
                View calendarView,
                Calendar initialMonth,
                CalendarDateRule dateRule,
                OnCalendarDateClickListener clickListener
        ) {
            this.calendarView = calendarView;
            this.titleView = calendarView.findViewById(R.id.tvCalendarTitle);
            this.prevView = calendarView.findViewById(R.id.btnCalendarPrev);
            this.nextView = calendarView.findViewById(R.id.btnCalendarNext);
            this.calendarGrid = calendarView.findViewById(R.id.calendarGrid);
            this.visibleMonth = normalizeMonth(initialMonth == null ? Calendar.getInstance() : initialMonth);
            this.dateRule = dateRule;
            this.clickListener = clickListener;

            setupNavigation();
            render();
        }

        public void render() {
            titleView.setText(formatMonthTitle(visibleMonth));

            List<Calendar> monthCells = getMonthCells(visibleMonth);

            for (int index = 0; index < calendarGrid.getChildCount(); index++) {
                View child = calendarGrid.getChildAt(index);

                if (!(child instanceof TextView)) {
                    continue;
                }

                TextView dayView = (TextView) child;
                Calendar cellDate = index < monthCells.size() ? monthCells.get(index) : null;

                resetDayView(dayView);

                if (cellDate == null || cellDate.get(Calendar.MONTH) != visibleMonth.get(Calendar.MONTH)) {
                    dayView.setText("");
                    dayView.setClickable(false);
                    dayView.setFocusable(false);
                    continue;
                }

                dayView.setText(String.valueOf(cellDate.get(Calendar.DAY_OF_MONTH)));

                boolean enabled = dateRule == null || dateRule.isEnabled(cloneCalendar(cellDate));
                boolean selected = dateRule != null && dateRule.isSelected(cloneCalendar(cellDate));
                boolean highlighted = dateRule != null && dateRule.isHighlighted(cloneCalendar(cellDate));
                boolean filledSelection = dateRule != null && dateRule.usesFilledSelection(cloneCalendar(cellDate));

                if (!enabled) {
                    dayView.setTextColor(DISABLED_TEXT);
                    dayView.setClickable(false);
                    dayView.setFocusable(false);
                    continue;
                }

                if (selected && filledSelection) {
                    dayView.setTextColor(WHITE);
                    dayView.setBackgroundResource(R.drawable.pickday_button_primary);
                } else if (selected) {
                    dayView.setTextColor(PURPLE);
                    dayView.setBackgroundResource(R.drawable.pickday_selected);
                } else if (highlighted) {
                    dayView.setTextColor(PURPLE);
                    dayView.setBackgroundResource(R.drawable.pickday_card_soft);
                } else {
                    dayView.setTextColor(DARK_TEXT);
                    dayView.setBackgroundResource(0);
                }

                dayView.setClickable(true);
                dayView.setFocusable(true);
                dayView.setOnClickListener(v -> {
                    if (clickListener != null) {
                        clickListener.onDateClick(cloneCalendar(cellDate));
                    }
                    render();
                });
            }
        }

        public Calendar getVisibleMonth() {
            return cloneCalendar(visibleMonth);
        }

        public void moveTo(Calendar month) {
            Calendar normalized = normalizeMonth(month);
            visibleMonth.setTimeInMillis(normalized.getTimeInMillis());
            render();
        }

        private void setupNavigation() {
            prevView.setOnClickListener(v -> {
                visibleMonth.add(Calendar.MONTH, -1);
                render();
            });

            nextView.setOnClickListener(v -> {
                visibleMonth.add(Calendar.MONTH, 1);
                render();
            });
        }
    }

    public static void show(
            Context context,
            Calendar currentDate,
            OnDateSelectedListener listener
    ) {
        Calendar baseDate = currentDate == null ? Calendar.getInstance() : currentDate;

        DatePickerDialog dialog = new DatePickerDialog(
                context,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(Calendar.YEAR, year);
                    selected.set(Calendar.MONTH, month);
                    selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    String displayText = formatDisplayDate(selected);
                    String summaryText = formatSummaryDate(selected);

                    if (listener != null) {
                        listener.onDateSelected(selected, displayText, summaryText);
                    }
                },
                baseDate.get(Calendar.YEAR),
                baseDate.get(Calendar.MONTH),
                baseDate.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    public static CalendarController attachCalendar(
            View calendarView,
            Calendar initialMonth,
            CalendarDateRule dateRule,
            OnCalendarDateClickListener clickListener
    ) {
        return new CalendarController(calendarView, initialMonth, dateRule, clickListener);
    }

    public static Calendar today() {
        return normalizeDate(Calendar.getInstance());
    }

    public static Calendar parseIsoDate(String isoDate) {
        SimpleDateFormat sdf = new SimpleDateFormat(ISO_PATTERN, Locale.KOREAN);
        Calendar calendar = Calendar.getInstance();

        try {
            calendar.setTime(sdf.parse(isoDate));
            return normalizeDate(calendar);
        } catch (ParseException | NullPointerException e) {
            return today();
        }
    }

    public static String formatIsoDate(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat(ISO_PATTERN, Locale.KOREAN);
        return sdf.format(calendar.getTime());
    }

    public static String formatDisplayDate(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd (E)", Locale.KOREAN);
        return sdf.format(calendar.getTime());
    }

    public static String formatDeadlineDate(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN);
        return sdf.format(calendar.getTime());
    }

    public static String formatSummaryDate(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("M.dd E", Locale.KOREAN);
        return sdf.format(calendar.getTime());
    }

    public static String formatChipDate(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("M.d", Locale.KOREAN);
        return sdf.format(calendar.getTime());
    }

    public static String formatWeek(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("E", Locale.KOREAN);
        return sdf.format(calendar.getTime());
    }

    public static boolean isSameDate(Calendar first, Calendar second) {
        return first != null
                && second != null
                && first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
                && first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isBeforeToday(Calendar date) {
        return normalizeDate(date).before(today());
    }

    private static List<Calendar> getMonthCells(Calendar visibleMonth) {
        List<Calendar> cells = new ArrayList<>();
        Calendar firstDay = normalizeMonth(visibleMonth);
        int firstWeekday = firstDay.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;

        Calendar cursor = cloneCalendar(firstDay);
        cursor.add(Calendar.DAY_OF_MONTH, -firstWeekday);

        for (int index = 0; index < 42; index++) {
            cells.add(cloneCalendar(cursor));
            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }

        return cells;
    }

    private static void resetDayView(TextView dayView) {
        dayView.setBackgroundColor(Color.TRANSPARENT);
        dayView.setTextColor(DARK_TEXT);
        dayView.setTypeface(null, Typeface.BOLD);
        dayView.setTextSize(12);
        dayView.setGravity(android.view.Gravity.CENTER);
        dayView.setIncludeFontPadding(false);
        dayView.setOnClickListener(null);
    }

    private static String formatMonthTitle(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월", Locale.KOREAN);
        return sdf.format(calendar.getTime());
    }

    private static Calendar normalizeMonth(Calendar calendar) {
        Calendar normalized = cloneCalendar(calendar);
        normalized.set(Calendar.DAY_OF_MONTH, 1);
        normalized.set(Calendar.HOUR_OF_DAY, 0);
        normalized.set(Calendar.MINUTE, 0);
        normalized.set(Calendar.SECOND, 0);
        normalized.set(Calendar.MILLISECOND, 0);
        return normalized;
    }

    private static Calendar normalizeDate(Calendar calendar) {
        Calendar normalized = cloneCalendar(calendar);
        normalized.set(Calendar.HOUR_OF_DAY, 0);
        normalized.set(Calendar.MINUTE, 0);
        normalized.set(Calendar.SECOND, 0);
        normalized.set(Calendar.MILLISECOND, 0);
        return normalized;
    }

    private static Calendar cloneCalendar(Calendar calendar) {
        Calendar cloned = Calendar.getInstance();
        cloned.setTimeInMillis(calendar.getTimeInMillis());
        return cloned;
    }
}
