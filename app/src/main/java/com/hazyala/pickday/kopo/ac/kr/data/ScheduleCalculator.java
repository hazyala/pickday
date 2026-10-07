package com.hazyala.pickday.kopo.ac.kr.data;

import com.hazyala.pickday.kopo.ac.kr.model.AvailabilityResponse;
import com.hazyala.pickday.kopo.ac.kr.model.MyMeetupRoom;
import com.hazyala.pickday.kopo.ac.kr.model.TimeSlot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class ScheduleCalculator {
    private ScheduleCalculator() { }

    public static List<DateResult> dates(MyMeetupRoom room,
            List<AvailabilityResponse> responses) {
        List<DateResult> results = new ArrayList<>();
        for (String date : room.candidateDateIsos) {
            int count = 0;
            for (AvailabilityResponse response : responses) {
                if (isAvailable(response, date)) count++;
            }
            results.add(new DateResult(date, count, count > 0 && count == room.participantCount));
        }
        results.sort(Comparator.comparingInt((DateResult result) -> result.availableCount)
                .reversed().thenComparing(result -> result.dateIso));
        return results;
    }

    public static List<TimeResult> times(MyMeetupRoom room,
            List<AvailabilityResponse> responses, String dateIso) {
        List<TimeResult> results = new ArrayList<>();
        for (TimeSlot slot : LocalMeetupRepository.getTimeSlots()) {
            if (!room.allowedTimeSlotCodes.contains(slot.code)) continue;
            int count = 0;
            for (AvailabilityResponse response : responses) {
                if (response.submitted && (dateIso == null || isAvailable(response, dateIso))
                        && (response.selectedTimeSlotCodes.contains(slot.code)
                        || response.selectedTimeSlotCodes.contains("ANYTIME"))) count++;
            }
            results.add(new TimeResult(slot.code, slot.label, slot.timeRange, count));
        }
        // 동점 시간은 getTimeSlots의 이른 시간 순서를 유지합니다.
        results.sort(Comparator.comparingInt((TimeResult result) -> result.count).reversed());
        return results;
    }

    public static List<Option> options(MyMeetupRoom room,
            List<AvailabilityResponse> responses) {
        List<Option> options = new ArrayList<>();
        // 같은 날짜와 시간에 실제로 모일 수 있는 인원을 가장 먼저 비교합니다.
        for (DateResult date : dates(room, responses)) {
            if (date.availableCount == 0) continue;
            for (TimeResult time : times(room, responses, date.dateIso)) {
                LocalDateTime start = LocalDateTime.of(LocalDate.parse(date.dateIso),
                        LocalTime.parse(time.timeRange.substring(0, 5)));
                if (time.count > 0 && start.isAfter(LocalDateTime.now())) {
                    options.add(new Option(date.dateIso, time, date.availableCount));
                }
            }
        }
        options.sort(Comparator.comparingInt((Option option) -> option.time.count).reversed()
                .thenComparing(Comparator.comparingInt((Option option) -> option.dateAvailableCount).reversed())
                .thenComparing(option -> option.dateIso)
                .thenComparing(option -> option.time.timeRange));
        return options;
    }

    private static boolean isAvailable(AvailabilityResponse response, String date) {
        return response.submitted && response.availableDateIsos.contains(date)
                && !response.excludedDateIsos.contains(date);
    }

    public static final class DateResult {
        public final String dateIso;
        public final int availableCount;
        public final boolean fullIntersection;
        DateResult(String dateIso, int availableCount, boolean fullIntersection) {
            this.dateIso = dateIso;
            this.availableCount = availableCount;
            this.fullIntersection = fullIntersection;
        }
    }

    public static final class TimeResult {
        public final String code;
        public final String label;
        public final String timeRange;
        public final int count;
        TimeResult(String code, String label, String timeRange, int count) {
            this.code = code;
            this.label = label;
            this.timeRange = timeRange;
            this.count = count;
        }
    }

    public static final class Option {
        public final String dateIso;
        public final TimeResult time;
        public final int dateAvailableCount;
        Option(String dateIso, TimeResult time, int dateAvailableCount) {
            this.dateIso = dateIso;
            this.time = time;
            this.dateAvailableCount = dateAvailableCount;
        }
    }
}
