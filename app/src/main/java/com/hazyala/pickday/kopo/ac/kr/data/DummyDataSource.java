package com.hazyala.pickday.kopo.ac.kr.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DummyDataSource {

    public static final String ROOM_ID_TEAM_MEETING = "room-team-meeting";
    public static final String ROOM_ID_BIRTHDAY_PARTY = "room-birthday-party";
    public static final String ROOM_ID_CAMP_MT = "room-club-mt";
    public static final String DEFAULT_ROOM_ID = ROOM_ID_CAMP_MT;

    private static final List<MyMeetupRoom> createdMeetupRooms = new ArrayList<>();
    private static final Map<String, List<String>> draftCandidateDatesByRoomId = new HashMap<>();
    private static String currentDraftRoomId = DEFAULT_ROOM_ID;

    public static User getCurrentUser() {
        return new User(
                "김해민",
                "방장",
                "home_profile"
        );
    }

    public static MainMeetup getMainMeetup() {
        MyMeetupRoom room = getMeetupRoomById(DEFAULT_ROOM_ID);
        RoomAvailabilitySummary summary = getRoomAvailabilitySummary(room.roomId);

        return new MainMeetup(
                room.roomId,
                "진행 중인 약속",
                room.title,
                room.participantCount,
                room.dDay,
                summary.responseRate,
                summary.bestDateTime,
                summary.bestAvailableCount
        );
    }

    public static RoomAvailabilitySummary getRoomAvailabilitySummary(String roomId) {
        MyMeetupRoom room = getMeetupRoomById(roomId);
        List<AvailabilityResponse> responses = getAvailabilityResponses(room.roomId);
        int completedCount = getCompletedResponseCount(responses);
        int responseRate = getResponseRate(room, responses);
        List<CandidateDateResult> dateResults = getCandidateDateResults(room, responses);
        TimePreferenceResult timeResult = getBestTimePreference(responses);

        if (dateResults.isEmpty()) {
            return new RoomAvailabilitySummary(
                    room.roomId,
                    completedCount,
                    responseRate,
                    "미정",
                    0
            );
        }

        CandidateDateResult bestDate = dateResults.get(0);

        return new RoomAvailabilitySummary(
                room.roomId,
                completedCount,
                responseRate,
                formatBestDateTime(bestDate.dateIso, timeResult.label),
                bestDate.availableCount
        );
    }

    public static List<AvailableDate> getAvailableDates() {
        List<AvailableDate> dates = new ArrayList<>();
        Calendar today = Calendar.getInstance();
        Calendar weekStart = Calendar.getInstance();
        weekStart.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        List<CalendarMeetup> calendarMeetups = getCalendarMeetups();
        Map<String, Integer> availableCountsByDate = getAvailableCountsByDate();
        String bestDateIso = getBestDateIsoForWeek(
                weekStart,
                availableCountsByDate
        );

        int todayIndex = 0;

        for (int index = 0; index < 7; index++) {
            Calendar date = (Calendar) weekStart.clone();
            date.add(Calendar.DAY_OF_MONTH, index);

            if (isSameDay(date, today)) {
                todayIndex = index;
            }
        }

        for (int index = 0; index < 7; index++) {
            Calendar date = (Calendar) weekStart.clone();
            date.add(Calendar.DAY_OF_MONTH, index);

            String label = "";

            if (isSameDay(date, today)) {
                label = "오늘";
            } else {
                Calendar tomorrow = (Calendar) today.clone();
                tomorrow.add(Calendar.DAY_OF_MONTH, 1);

                if (isSameDay(date, tomorrow)) {
                    label = "내일";
                }
            }

            boolean hasMeetupStatus = false;
            String dateIso = formatIsoDate(date);
            int availableCount = getAvailableCountForDate(
                    dateIso,
                    availableCountsByDate,
                    calendarMeetups
            );

            if (availableCount > 0 || hasCalendarMeetup(dateIso, calendarMeetups)) {
                hasMeetupStatus = true;
            }

            dates.add(new AvailableDate(
                    DEFAULT_ROOM_ID,
                    label,
                    formatMonthDay(date),
                    formatWeekday(date),
                    availableCount,
                    index == todayIndex,
                    dateIso.equals(bestDateIso),
                    hasMeetupStatus
            ));
        }

        return dates;
    }

    private static int getAvailableCountForDate(
            String dateIso,
            Map<String, Integer> availableCountsByDate,
            List<CalendarMeetup> calendarMeetups
    ) {
        int availableCount = 0;

        if (availableCountsByDate.containsKey(dateIso)) {
            availableCount = availableCountsByDate.get(dateIso);
        }

        for (CalendarMeetup meetup : calendarMeetups) {
            if (dateIso.equals(meetup.dateIso)) {
                availableCount = Math.max(availableCount, meetup.participantCount);
            }
        }

        return availableCount;
    }

    private static boolean hasCalendarMeetup(
            String dateIso,
            List<CalendarMeetup> calendarMeetups
    ) {
        for (CalendarMeetup meetup : calendarMeetups) {
            if (dateIso.equals(meetup.dateIso)) {
                return true;
            }
        }

        return false;
    }

    private static Map<String, Integer> getAvailableCountsByDate() {
        Map<String, Integer> countsByDate = new HashMap<>();

        for (MyMeetupRoom room : getMyMeetupRooms()) {
            List<AvailabilityResponse> responses = getAvailabilityResponses(room.roomId);

            for (String dateIso : room.candidateDateIsos) {
                int availableCount = 0;

                for (AvailabilityResponse response : responses) {
                    if (response.submitted && response.availableDateIsos.contains(dateIso)) {
                        availableCount++;
                    }
                }

                int currentCount = countsByDate.containsKey(dateIso)
                        ? countsByDate.get(dateIso)
                        : 0;
                countsByDate.put(dateIso, Math.max(currentCount, availableCount));
            }
        }

        return countsByDate;
    }

    private static String getBestDateIsoForWeek(
            Calendar weekStart,
            Map<String, Integer> availableCountsByDate
    ) {
        String bestDateIso = "";
        int bestCount = 0;

        for (int index = 0; index < 7; index++) {
            Calendar date = (Calendar) weekStart.clone();
            date.add(Calendar.DAY_OF_MONTH, index);
            String dateIso = formatIsoDate(date);
            int availableCount = availableCountsByDate.containsKey(dateIso)
                    ? availableCountsByDate.get(dateIso)
                    : 0;

            if (availableCount > bestCount
                    || (availableCount == bestCount
                    && availableCount > 0
                    && !bestDateIso.isEmpty()
                    && dateIso.compareTo(bestDateIso) < 0)) {
                bestDateIso = dateIso;
                bestCount = availableCount;
            }
        }

        return bestDateIso;
    }

    private static boolean isSameDay(Calendar first, Calendar second) {
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
                && first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR);
    }

    private static String formatMonthDay(Calendar date) {
        SimpleDateFormat sdf = new SimpleDateFormat("M.d", Locale.KOREAN);
        return sdf.format(date.getTime());
    }

    private static String formatWeekday(Calendar date) {
        SimpleDateFormat sdf = new SimpleDateFormat("E", Locale.KOREAN);
        return sdf.format(date.getTime());
    }

    private static String formatIsoDate(Calendar date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN);
        return sdf.format(date.getTime());
    }

    public static List<CalendarMeetup> getCalendarMeetups() {
        List<CalendarMeetup> meetups = new ArrayList<>();

        for (MyMeetupRoom room : getMyMeetupRooms()) {
            if (room.deadlineDateIso != null && !room.deadlineDateIso.trim().isEmpty()) {
                meetups.add(new CalendarMeetup(
                        room.roomId,
                        room.title,
                        room.deadlineDateIso,
                        room.deadlineTimeText,
                        room.participantCount,
                        "응답 마감일",
                        "#FF9338",
                        room.iconText
                ));
            }

            if (room.confirmedDateIso != null && !room.confirmedDateIso.trim().isEmpty()) {
                meetups.add(new CalendarMeetup(
                        room.roomId,
                        room.title,
                        room.confirmedDateIso,
                        room.confirmedTimeText,
                        room.participantCount,
                        "확정된 약속일",
                        "#4EBD73",
                        room.iconText
                ));
            }
        }

        return meetups;
    }

    public static List<MyMeetupRoom> getMyMeetupRooms() {
        List<MyMeetupRoom> rooms = new ArrayList<>();
        rooms.addAll(getDefaultMeetupRooms());
        rooms.addAll(createdMeetupRooms);

        return rooms;
    }

    public static MyMeetupRoom getMeetupRoomById(String roomId) {
        if (roomId != null) {
            for (MyMeetupRoom room : getMyMeetupRooms()) {
                if (room.roomId.equals(roomId)) {
                    return room;
                }
            }
        }

        return getDefaultMeetupRooms().get(2);
    }

    public static MyMeetupRoom getMeetupRoomByTitle(String title) {
        if (title != null) {
            for (MyMeetupRoom room : getMyMeetupRooms()) {
                if (room.title.equals(title)) {
                    return room;
                }
            }
        }

        return getMeetupRoomById(DEFAULT_ROOM_ID);
    }

    public static void setCurrentDraftCandidateDates(List<String> candidateDates) {
        setDraftCandidateDates(currentDraftRoomId, candidateDates);
    }

    public static List<String> getCurrentDraftCandidateDates() {
        return getDraftCandidateDates(currentDraftRoomId);
    }

    public static String getCurrentDraftRoomId() {
        return currentDraftRoomId;
    }

    public static void setDraftCandidateDates(String roomId, List<String> candidateDates) {
        if (roomId == null || roomId.isEmpty()) {
            return;
        }

        if (candidateDates == null || candidateDates.isEmpty()) {
            draftCandidateDatesByRoomId.remove(roomId);
            return;
        }

        draftCandidateDatesByRoomId.put(roomId, new ArrayList<>(candidateDates));
    }

    public static List<String> getDraftCandidateDates(String roomId) {
        if (roomId == null || roomId.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> candidateDates = draftCandidateDatesByRoomId.get(roomId);

        if (candidateDates == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(candidateDates);
    }

    public static List<String> getResponseCandidateDates() {
        return getResponseCandidateDates(DEFAULT_ROOM_ID);
    }

    public static List<String> getResponseCandidateDates(String roomId) {
        MyMeetupRoom room = getMeetupRoomById(roomId);

        if (!room.candidateDateIsos.isEmpty()) {
            return new ArrayList<>(room.candidateDateIsos);
        }

        List<String> draftCandidateDates = getDraftCandidateDates(room.roomId);

        if (!draftCandidateDates.isEmpty()) {
            return draftCandidateDates;
        }

        List<String> fallbackDates = new ArrayList<>();
        fallbackDates.add("2026-06-20");
        fallbackDates.add("2026-06-21");
        fallbackDates.add("2026-06-28");
        fallbackDates.add("2026-07-04");
        return fallbackDates;
    }

    public static String addCreatedMeetupRoom(
            String title,
            int participantCount,
            String dDay,
            String deadlineDateIso,
            String deadlineTimeText
    ) {
        if (title == null || title.trim().isEmpty()) {
            return currentDraftRoomId;
        }

        String normalizedTitle = title.trim();
        String roomId = createCreatedRoomId(normalizedTitle);
        currentDraftRoomId = roomId;
        setDraftCandidateDates(roomId, new ArrayList<>());

        MyMeetupRoom createdRoom = new MyMeetupRoom(
                roomId,
                normalizedTitle,
                participantCount,
                dDay,
                0,
                "default",
                deadlineDateIso,
                deadlineTimeText,
                new ArrayList<>(),
                "",
                "",
                "마"
        );

        for (int index = 0; index < createdMeetupRooms.size(); index++) {
            MyMeetupRoom room = createdMeetupRooms.get(index);

            if (room.roomId.equals(roomId)) {
                createdMeetupRooms.set(index, createdRoom);
                return roomId;
            }
        }

        createdMeetupRooms.add(createdRoom);
        return roomId;
    }

    public static void updateCreatedRoomCandidateDates(
            String roomId,
            List<String> candidateDates
    ) {
        if (roomId == null || candidateDates == null) {
            return;
        }

        for (MyMeetupRoom room : createdMeetupRooms) {
            if (room.roomId.equals(roomId)) {
                room.candidateDateIsos = new ArrayList<>(candidateDates);
                setDraftCandidateDates(roomId, candidateDates);
                return;
            }
        }
    }

    public static String getInviteLink(String roomId) {
        if (roomId == null || roomId.isEmpty()) {
            return "https://pickday.app/room/" + DEFAULT_ROOM_ID;
        }

        return "https://pickday.app/room/" + roomId;
    }

    public static List<Notification> getNotifications() {
        List<Notification> notifications = new ArrayList<>();
        MyMeetupRoom teamRoom = getMeetupRoomById(ROOM_ID_TEAM_MEETING);
        MyMeetupRoom birthdayRoom = getMeetupRoomById(ROOM_ID_BIRTHDAY_PARTY);
        MyMeetupRoom campRoom = getMeetupRoomById(ROOM_ID_CAMP_MT);

        notifications.add(new Notification(
                birthdayRoom.roomId,
                "오늘",
                "민재님이 응답했어요",
                birthdayRoom.title,
                "현재 응답률 " + getRoomAvailabilitySummary(birthdayRoom.roomId).responseRate + "%",
                "방금 전",
                "#5B4CDB"
        ));

        notifications.add(new Notification(
                birthdayRoom.roomId,
                "오늘",
                "일정이 확정되었어요",
                birthdayRoom.title,
                formatConfirmedSchedule(birthdayRoom),
                "10분 전",
                "#61D48A"
        ));

        notifications.add(new Notification(
                ROOM_ID_CAMP_MT,
                "이번 주",
                "마감이 다가오고 있어요",
                campRoom.title,
                "응답하지 않은 2명이 있어요",
                "오늘",
                "#FFC21A"
        ));

        notifications.add(new Notification(
                ROOM_ID_CAMP_MT,
                "이번 주",
                "지윤님이 응답했어요",
                campRoom.title,
                "현재 응답률 " + getRoomAvailabilitySummary(campRoom.roomId).responseRate + "%",
                "어제",
                "#5B4CDB"
        ));

        notifications.add(new Notification(
                ROOM_ID_CAMP_MT,
                "이번 주",
                "일정이 확정되었어요",
                campRoom.title,
                formatConfirmedSchedule(campRoom),
                "2일 전",
                "#61D48A"
        ));

        notifications.add(new Notification(
                ROOM_ID_TEAM_MEETING,
                "이번 주",
                "응답 수집이 마감되었어요",
                teamRoom.title,
                getRoomAvailabilitySummary(teamRoom.roomId).completedResponseCount + "명이 응답했어요",
                "3일 전",
                "#FFC21A"
        ));

        notifications.add(new Notification(
                ROOM_ID_TEAM_MEETING,
                "이전",
                "팀플 회의 후보가 정리되었어요",
                teamRoom.title,
                getRoomAvailabilitySummary(teamRoom.roomId).bestDateTime,
                "5일 전",
                "#61D48A"
        ));

        notifications.add(new Notification(
                ROOM_ID_BIRTHDAY_PARTY,
                "이전",
                "서연님이 응답했어요",
                birthdayRoom.title,
                "가능한 날짜가 추가되었어요",
                "6일 전",
                "#5B4CDB"
        ));

        return notifications;
    }

    private static String formatConfirmedSchedule(MyMeetupRoom room) {
        if (room.confirmedDateIso == null || room.confirmedDateIso.trim().isEmpty()) {
            return "확정 일정을 확인해 주세요";
        }

        String dateText = room.confirmedDateIso;

        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN);
            Date date = parser.parse(room.confirmedDateIso);
            SimpleDateFormat formatter = new SimpleDateFormat("M월 d일 (E)", Locale.KOREAN);
            dateText = formatter.format(date);
        } catch (Exception e) {
            dateText = room.confirmedDateIso;
        }

        if (room.confirmedTimeText == null || room.confirmedTimeText.trim().isEmpty()) {
            return dateText;
        }

        return dateText + " " + room.confirmedTimeText;
    }

    public static List<ChatMessage> getChatMessages(String roomTitle) {
        return getChatMessagesByRoomId(getMeetupRoomByTitle(roomTitle).roomId);
    }

    public static List<ChatMessage> getChatMessagesByRoomId(String roomId) {
        List<ChatMessage> messages = new ArrayList<>();

        if (ROOM_ID_TEAM_MEETING.equals(roomId)) {
            messages.add(new ChatMessage(roomId, "수용", "회의 끝나고 뭐 먹을까요?", "오후 6:12", false, false));
            messages.add(new ChatMessage(roomId, "김해민", "짜장면 괜찮아요. 학교 앞에 새로 생긴 중국집도 있어요.", "오후 6:13", true, false));
            messages.add(new ChatMessage(roomId, "지윤", "거기 탕수육도 괜찮대요.", "오후 6:15", false, false));
            messages.add(new ChatMessage(roomId, "김해민", "그럼 중국집이랑 분식집 두 군데 후보로 적어둘게요.", "오후 6:16", true, false));
            return messages;
        }

        if (ROOM_ID_BIRTHDAY_PARTY.equals(roomId)) {
            messages.add(new ChatMessage(roomId, "민재", "케이크는 초코가 좋을까요?", "오후 3:25", false, false));
            messages.add(new ChatMessage(roomId, "김해민", "초코 좋고, 음식은 파스타나 피자 쪽이 무난할 것 같아요.", "오후 3:27", true, false));
            messages.add(new ChatMessage(roomId, "서연", "맛집 알아요? 너무 시끄럽지 않은 곳이면 좋겠어요.", "오후 3:30", false, false));
            messages.add(new ChatMessage(roomId, "김해민", "조용한 파스타집 하나 찾아보고 후보에 넣어둘게요.", "오후 3:32", true, false));
            return messages;
        }

        if (ROOM_ID_CAMP_MT.equals(roomId)) {
            messages.add(new ChatMessage(roomId, "현우", "MT 가면 저녁은 뭐 먹을까요?", "오후 9:12", false, false));
            messages.add(new ChatMessage(roomId, "김해민", "고기 구워 먹는 것도 좋고, 비 오는 날이면 전골도 괜찮을 것 같아요.", "오후 9:13", true, false));
            messages.add(new ChatMessage(roomId, "지윤", "근처 맛집 알아요?", "오후 9:15", false, false));
            messages.add(new ChatMessage(roomId, "김해민", "숙소 근처 식당 몇 군데 찾아보고 후보로 정리해볼게요.", "오후 9:17", true, false));
        }

        return messages;
    }

    public static ChatRoomStatus getChatRoomStatus(String roomTitle) {
        return getChatRoomStatusByRoomId(getMeetupRoomByTitle(roomTitle).roomId);
    }

    public static ChatRoomStatus getChatRoomStatusByRoomId(String roomId) {
        MyMeetupRoom room = getMeetupRoomById(roomId);

        return new ChatRoomStatus(
                room.roomId,
                room.title,
                room.participantCount,
                room.dDay,
                room.responseRate,
                room.deadlineDateIso,
                room.deadlineTimeText
        );
    }

    public static List<AvailabilityResponse> getAvailabilityResponses(String roomId) {
        List<AvailabilityResponse> responses = new ArrayList<>();

        if (ROOM_ID_TEAM_MEETING.equals(roomId)) {
            responses.add(new AvailabilityResponse(roomId, "김해민", true,
                    listOf("2026-05-22", "2026-05-24"), listOf("AFTERNOON", "EVENING")));
            responses.add(new AvailabilityResponse(roomId, "수용", true,
                    listOf("2026-05-24", "2026-05-27"), listOf("EVENING")));
            responses.add(new AvailabilityResponse(roomId, "지윤", true,
                    listOf("2026-05-22", "2026-05-24"), listOf("AFTERNOON")));
            responses.add(new AvailabilityResponse(roomId, "민재", true,
                    listOf("2026-05-24", "2026-05-27"), listOf("AFTERNOON", "LATE_AFTERNOON")));
            return responses;
        }

        if (ROOM_ID_BIRTHDAY_PARTY.equals(roomId)) {
            responses.add(new AvailabilityResponse(roomId, "김해민", true,
                    listOf("2026-06-19", "2026-06-20"), listOf("EVENING")));
            responses.add(new AvailabilityResponse(roomId, "민재", true,
                    listOf("2026-06-20"), listOf("EVENING", "LATE_EVENING")));
            responses.add(new AvailabilityResponse(roomId, "서연", true,
                    listOf("2026-06-20", "2026-06-21"), listOf("AFTERNOON", "EVENING")));
            responses.add(new AvailabilityResponse(roomId, "지윤", true,
                    listOf("2026-06-20"), listOf("EVENING")));
            responses.add(new AvailabilityResponse(roomId, "현우", true,
                    listOf("2026-06-19", "2026-06-21"), listOf("AFTERNOON")));
            responses.add(new AvailabilityResponse(roomId, "수빈", false,
                    new ArrayList<>(), new ArrayList<>()));
            return responses;
        }

        if (ROOM_ID_CAMP_MT.equals(roomId)) {
            responses.add(new AvailabilityResponse(roomId, "김해민", true,
                    listOf("2026-06-28", "2026-07-04"), listOf("AFTERNOON", "EVENING")));
            responses.add(new AvailabilityResponse(roomId, "현우", true,
                    listOf("2026-07-04", "2026-07-05"), listOf("AFTERNOON")));
            responses.add(new AvailabilityResponse(roomId, "지윤", true,
                    listOf("2026-06-28", "2026-07-04"), listOf("LATE_AFTERNOON", "EVENING")));
            responses.add(new AvailabilityResponse(roomId, "수빈", true,
                    listOf("2026-07-04"), listOf("AFTERNOON", "LATE_AFTERNOON")));
            responses.add(new AvailabilityResponse(roomId, "민재", true,
                    listOf("2026-06-28", "2026-07-05"), listOf("EVENING")));
            responses.add(new AvailabilityResponse(roomId, "서연", false,
                    new ArrayList<>(), new ArrayList<>()));
            responses.add(new AvailabilityResponse(roomId, "도윤", false,
                    new ArrayList<>(), new ArrayList<>()));
        }

        return responses;
    }

    public static List<TimeSlot> getTimeSlots() {
        List<TimeSlot> timeSlots = new ArrayList<>();
        timeSlots.add(new TimeSlot("MORNING", "오전", "09:00~12:00"));
        timeSlots.add(new TimeSlot("AFTERNOON", "오후", "12:00~15:00"));
        timeSlots.add(new TimeSlot("LATE_AFTERNOON", "늦은 오후", "15:00~18:00"));
        timeSlots.add(new TimeSlot("EVENING", "저녁", "18:00~21:00"));
        timeSlots.add(new TimeSlot("LATE_EVENING", "늦은 저녁", "21:00~24:00"));
        return timeSlots;
    }

    private static int getCompletedResponseCount(List<AvailabilityResponse> responses) {
        int count = 0;

        for (AvailabilityResponse response : responses) {
            if (response.submitted) {
                count++;
            }
        }

        return count;
    }

    private static int getResponseRate(
            MyMeetupRoom room,
            List<AvailabilityResponse> responses
    ) {
        if (room.participantCount <= 0) {
            return 0;
        }

        if (responses.isEmpty()) {
            return room.responseRate;
        }

        return Math.round(getCompletedResponseCount(responses) * 100f / room.participantCount);
    }

    private static List<CandidateDateResult> getCandidateDateResults(
            MyMeetupRoom room,
            List<AvailabilityResponse> responses
    ) {
        List<CandidateDateResult> results = new ArrayList<>();

        for (String dateIso : room.candidateDateIsos) {
            int availableCount = 0;

            for (AvailabilityResponse response : responses) {
                if (response.submitted && response.availableDateIsos.contains(dateIso)) {
                    availableCount++;
                }
            }

            results.add(new CandidateDateResult(dateIso, availableCount));
        }

        Collections.sort(results, (first, second) -> {
            int countCompare = Integer.compare(second.availableCount, first.availableCount);

            if (countCompare != 0) {
                return countCompare;
            }

            return first.dateIso.compareTo(second.dateIso);
        });

        return results;
    }

    private static TimePreferenceResult getBestTimePreference(List<AvailabilityResponse> responses) {
        List<TimePreferenceResult> results = new ArrayList<>();

        for (TimeSlot timeSlot : getTimeSlots()) {
            int count = 0;

            for (AvailabilityResponse response : responses) {
                if (response.submitted && response.selectedTimeSlotCodes.contains(timeSlot.code)) {
                    count++;
                }
            }

            results.add(new TimePreferenceResult(timeSlot.label, count));
        }

        Collections.sort(results, Comparator
                .comparingInt((TimePreferenceResult result) -> result.count)
                .reversed());

        return results.isEmpty() ? new TimePreferenceResult("", 0) : results.get(0);
    }

    private static String formatBestDateTime(String dateIso, String timeLabel) {
        String dateText = dateIso;

        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN);
            Date date = parser.parse(dateIso);
            SimpleDateFormat formatter = new SimpleDateFormat("M월 d일 (E)", Locale.KOREAN);
            dateText = formatter.format(date);
        } catch (Exception e) {
            dateText = dateIso;
        }

        if (timeLabel == null || timeLabel.trim().isEmpty()) {
            return dateText;
        }

        return dateText + " " + timeLabel;
    }

    private static List<MyMeetupRoom> getDefaultMeetupRooms() {
        List<MyMeetupRoom> rooms = new ArrayList<>();

        List<String> teamDates = new ArrayList<>();
        teamDates.add("2026-05-22");
        teamDates.add("2026-05-24");
        teamDates.add("2026-05-27");
        rooms.add(new MyMeetupRoom(
                ROOM_ID_TEAM_MEETING,
                "팀플 회의 일정",
                4,
                "마감",
                100,
                "group",
                "2026-05-24",
                "오후 11:59",
                teamDates,
                "",
                "",
                "팀"
        ));

        List<String> birthdayDates = new ArrayList<>();
        birthdayDates.add("2026-06-19");
        birthdayDates.add("2026-06-20");
        birthdayDates.add("2026-06-21");
        rooms.add(new MyMeetupRoom(
                ROOM_ID_BIRTHDAY_PARTY,
                "지윤이 생일 파티",
                6,
                "D-5",
                83,
                "cake",
                "2026-06-18",
                "오후 11:59",
                birthdayDates,
                "2026-06-20",
                "오후 6:00 ~ 9:00",
                "생"
        ));

        List<String> campDates = new ArrayList<>();
        campDates.add("2026-06-28");
        campDates.add("2026-07-04");
        campDates.add("2026-07-05");
        rooms.add(new MyMeetupRoom(
                ROOM_ID_CAMP_MT,
                "동아리 MT 일정 정하기",
                7,
                "D-10",
                71,
                "camp",
                "2026-06-25",
                "오후 11:59",
                campDates,
                "2026-07-04",
                "오후 2:00",
                "동"
        ));

        return rooms;
    }

    private static String createCreatedRoomId(String title) {
        return "room-created-" + Integer.toHexString(title.hashCode());
    }

    private static List<String> listOf(String... values) {
        List<String> list = new ArrayList<>();

        for (String value : values) {
            list.add(value);
        }

        return list;
    }

    public static class User {
        public String name;
        public String role;
        public String profileImageName;

        public User(String name, String role, String profileImageName) {
            this.name = name;
            this.role = role;
            this.profileImageName = profileImageName;
        }
    }

    public static class MainMeetup {
        public String roomId;
        public String statusLabel;
        public String title;
        public int participantCount;
        public String dDay;
        public int responseRate;
        public String bestDateTime;
        public int availableCount;

        public MainMeetup(
                String roomId,
                String statusLabel,
                String title,
                int participantCount,
                String dDay,
                int responseRate,
                String bestDateTime,
                int availableCount
        ) {
            this.roomId = roomId;
            this.statusLabel = statusLabel;
            this.title = title;
            this.participantCount = participantCount;
            this.dDay = dDay;
            this.responseRate = responseRate;
            this.bestDateTime = bestDateTime;
            this.availableCount = availableCount;
        }
    }

    public static class RoomAvailabilitySummary {
        public String roomId;
        public int completedResponseCount;
        public int responseRate;
        public String bestDateTime;
        public int bestAvailableCount;

        public RoomAvailabilitySummary(
                String roomId,
                int completedResponseCount,
                int responseRate,
                String bestDateTime,
                int bestAvailableCount
        ) {
            this.roomId = roomId;
            this.completedResponseCount = completedResponseCount;
            this.responseRate = responseRate;
            this.bestDateTime = bestDateTime;
            this.bestAvailableCount = bestAvailableCount;
        }
    }

    public static class AvailableDate {
        public String roomId;
        public String label;
        public String date;
        public String dayOfWeek;
        public int availableCount;
        public boolean selected;
        public boolean best;
        public boolean hasMeetupStatus;

        public AvailableDate(
                String roomId,
                String label,
                String date,
                String dayOfWeek,
                int availableCount,
                boolean selected,
                boolean best,
                boolean hasMeetupStatus
        ) {
            this.roomId = roomId;
            this.label = label;
            this.date = date;
            this.dayOfWeek = dayOfWeek;
            this.availableCount = availableCount;
            this.selected = selected;
            this.best = best;
            this.hasMeetupStatus = hasMeetupStatus;
        }
    }

    public static class CalendarMeetup {
        public String roomId;
        public String title;
        public String dateIso;
        public String timeText;
        public int participantCount;
        public String statusText;
        public String accentColor;
        public String iconText;

        public CalendarMeetup(
                String roomId,
                String title,
                String dateIso,
                String timeText,
                int participantCount,
                String statusText,
                String accentColor,
                String iconText
        ) {
            this.roomId = roomId;
            this.title = title;
            this.dateIso = dateIso;
            this.timeText = timeText;
            this.participantCount = participantCount;
            this.statusText = statusText;
            this.accentColor = accentColor;
            this.iconText = iconText;
        }
    }

    public static class MyMeetupRoom {
        public String roomId;
        public String title;
        public int participantCount;
        public String dDay;
        public int responseRate;
        public String iconType;
        public String deadlineDateIso;
        public String deadlineTimeText;
        public List<String> candidateDateIsos;
        public String confirmedDateIso;
        public String confirmedTimeText;
        public String iconText;

        public MyMeetupRoom(
                String roomId,
                String title,
                int participantCount,
                String dDay,
                int responseRate,
                String iconType
        ) {
            this(roomId, title, participantCount, dDay, responseRate, iconType, "",
                    "", new ArrayList<>(), "", "", "");
        }

        public MyMeetupRoom(
                String roomId,
                String title,
                int participantCount,
                String dDay,
                int responseRate,
                String iconType,
                String deadlineDateIso,
                String deadlineTimeText,
                List<String> candidateDateIsos,
                String confirmedDateIso,
                String confirmedTimeText,
                String iconText
        ) {
            this.roomId = roomId;
            this.title = title;
            this.participantCount = participantCount;
            this.dDay = dDay;
            this.responseRate = responseRate;
            this.iconType = iconType;
            this.deadlineDateIso = deadlineDateIso;
            this.deadlineTimeText = deadlineTimeText;
            this.candidateDateIsos = new ArrayList<>(candidateDateIsos);
            this.confirmedDateIso = confirmedDateIso;
            this.confirmedTimeText = confirmedTimeText;
            this.iconText = iconText;
        }
    }

    public static class Notification {
        public String roomId;
        public String section;
        public String title;
        public String roomTitle;
        public String message;
        public String time;
        public String accentColor;

        public Notification(
                String roomId,
                String section,
                String title,
                String roomTitle,
                String message,
                String time,
                String accentColor
        ) {
            this.roomId = roomId;
            this.section = section;
            this.title = title;
            this.roomTitle = roomTitle;
            this.message = message;
            this.time = time;
            this.accentColor = accentColor;
        }
    }

    public static class ChatMessage {
        public String roomId;
        public String senderName;
        public String message;
        public String time;
        public boolean mine;
        public boolean notice;

        public ChatMessage(
                String senderName,
                String message,
                String time,
                boolean mine,
                boolean notice
        ) {
            this(DEFAULT_ROOM_ID, senderName, message, time, mine, notice);
        }

        public ChatMessage(
                String roomId,
                String senderName,
                String message,
                String time,
                boolean mine,
                boolean notice
        ) {
            this.roomId = roomId;
            this.senderName = senderName;
            this.message = message;
            this.time = time;
            this.mine = mine;
            this.notice = notice;
        }
    }

    public static class ChatRoomStatus {
        public String roomId;
        public String title;
        public int participantCount;
        public String dDay;
        public int responseRate;
        public String deadlineDateIso;
        public String deadlineTimeText;

        public ChatRoomStatus(
                String roomId,
                String title,
                int participantCount,
                String dDay,
                int responseRate,
                String deadlineDateIso,
                String deadlineTimeText
        ) {
            this.roomId = roomId;
            this.title = title;
            this.participantCount = participantCount;
            this.dDay = dDay;
            this.responseRate = responseRate;
            this.deadlineDateIso = deadlineDateIso;
            this.deadlineTimeText = deadlineTimeText;
        }
    }

    public static class AvailabilityResponse {
        public String roomId;
        public String participantName;
        public boolean submitted;
        public List<String> availableDateIsos;
        public List<String> selectedTimeSlotCodes;

        public AvailabilityResponse(
                String roomId,
                String participantName,
                boolean submitted,
                List<String> availableDateIsos,
                List<String> selectedTimeSlotCodes
        ) {
            this.roomId = roomId;
            this.participantName = participantName;
            this.submitted = submitted;
            this.availableDateIsos = new ArrayList<>(availableDateIsos);
            this.selectedTimeSlotCodes = new ArrayList<>(selectedTimeSlotCodes);
        }
    }

    public static class TimeSlot {
        public String code;
        public String label;
        public String timeRange;

        public TimeSlot(String code, String label, String timeRange) {
            this.code = code;
            this.label = label;
            this.timeRange = timeRange;
        }
    }

    private static class CandidateDateResult {
        String dateIso;
        int availableCount;

        CandidateDateResult(String dateIso, int availableCount) {
            this.dateIso = dateIso;
            this.availableCount = availableCount;
        }
    }

    private static class TimePreferenceResult {
        String label;
        int count;

        TimePreferenceResult(String label, int count) {
            this.label = label;
            this.count = count;
        }
    }
}
