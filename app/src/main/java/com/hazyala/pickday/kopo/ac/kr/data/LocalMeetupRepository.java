package com.hazyala.pickday.kopo.ac.kr.data;

import com.hazyala.pickday.kopo.ac.kr.model.AvailabilityResponse;
import com.hazyala.pickday.kopo.ac.kr.model.AvailableDate;
import com.hazyala.pickday.kopo.ac.kr.model.CalendarMeetup;
import com.hazyala.pickday.kopo.ac.kr.model.ChatMessage;
import com.hazyala.pickday.kopo.ac.kr.model.ChatRoomStatus;
import com.hazyala.pickday.kopo.ac.kr.model.HostSelectionDraft;
import com.hazyala.pickday.kopo.ac.kr.model.MainMeetup;
import com.hazyala.pickday.kopo.ac.kr.model.MyMeetupRoom;
import com.hazyala.pickday.kopo.ac.kr.model.Notification;
import com.hazyala.pickday.kopo.ac.kr.model.RoomAvailabilitySummary;
import com.hazyala.pickday.kopo.ac.kr.model.TimeSlot;
import com.hazyala.pickday.kopo.ac.kr.model.User;
import com.hazyala.pickday.kopo.ac.kr.model.UserRoomStats;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;

public final class LocalMeetupRepository {
    private LocalMeetupRepository() { }

    public static final String ROOM_ID_TEAM_MEETING = "room-team-meeting";
    public static final String ROOM_ID_BIRTHDAY_PARTY = "room-birthday-party";
    public static final String ROOM_ID_CAMP_MT = "room-club-mt";
    public static final String DEFAULT_ROOM_ID = ROOM_ID_CAMP_MT;

    private static final MeetupMemoryStore store = new MeetupMemoryStore();

    public static User getCurrentUser() {
        return SampleMeetupData.getCurrentUser();
    }

    public static UserRoomStats getUserRoomStats() {
        int active = 0, submitted = 0, confirmed = 0, total = 0;
        String name = getCurrentUser().name;
        for (MyMeetupRoom room : getMyMeetupRooms()) {
            AvailabilityResponse mine = getResponse(room.roomId, name);
            if (mine == null) continue;
            total++;
            if (mine.submitted) submitted++;
            if (!room.confirmedDateIso.isEmpty()) confirmed++;
            else if (!isDeadlinePassed(room)) active++;
        }
        return new UserRoomStats(active, submitted, confirmed, total);
    }

    public static MainMeetup getMainMeetup() {
        MyMeetupRoom room = store.createdMeetupRooms.isEmpty()
                ? getMeetupRoomById(DEFAULT_ROOM_ID)
                : getMeetupRoomById(store.createdMeetupRooms.get(store.createdMeetupRooms.size() - 1).roomId);
        RoomAvailabilitySummary summary = getRoomAvailabilitySummary(room.roomId);

        return new MainMeetup(
                room.roomId,
                room.confirmedDateIso.isEmpty() ? "진행 중인 약속" : "확정된 약속",
                room.title,
                room.participantCount,
                room.dDay,
                summary.responseRate,
                room.confirmedDateIso.isEmpty() ? summary.bestDateTime : formatConfirmedSchedule(room),
                room.confirmedDateIso.isEmpty() ? summary.bestAvailableCount : getConfirmedAvailableCount(room)
        );
    }

    private static int getConfirmedAvailableCount(MyMeetupRoom room) {
        for (ScheduleCalculator.TimeResult time : ScheduleCalculator.times(room,
                getAvailabilityResponses(room.roomId), room.confirmedDateIso)) {
            if (room.confirmedTimeText.equals(time.label + " " + time.timeRange)) return time.count;
        }
        return 0;
    }

    public static RoomAvailabilitySummary getRoomAvailabilitySummary(String roomId) {
        MyMeetupRoom room = requireRoom(roomId);
        List<AvailabilityResponse> responses = getAvailabilityResponses(room.roomId);
        int completedCount = getCompletedResponseCount(responses);
        int responseRate = getResponseRate(room, responses);
        List<ScheduleCalculator.Option> options = ScheduleCalculator.options(room, responses);
        if (options.isEmpty()) {
            return new RoomAvailabilitySummary(room.roomId, completedCount, responseRate, completedCount == 0 ? "집계 대기" : "가능한 일정 없음", 0);
        }
        ScheduleCalculator.Option best = options.get(0);
        return new RoomAvailabilitySummary(room.roomId, completedCount, responseRate,
                formatBestDateTime(best.dateIso, best.time.label), best.time.count);
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
            if (dateIso.equals(meetup.dateIso) && meetup.statusText.equals("확정된 약속일")) {
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
            if (!room.confirmedDateIso.isEmpty()) {
                countsByDate.merge(room.confirmedDateIso, getConfirmedAvailableCount(room), Math::max);
                continue;
            }
            for (ScheduleCalculator.Option option : getScheduleOptions(room.roomId)) {
                countsByDate.merge(option.dateIso, option.time.count, Math::max);
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
                        getConfirmedAvailableCount(room),
                        "확정된 약속일",
                        "#4EBD73",
                        room.iconText
                ));
            }
        }

        meetups.sort(java.util.Comparator.comparing((CalendarMeetup meetup) -> meetup.dateIso)
                .thenComparing(meetup -> meetup.title));
        return meetups;
    }

    public static List<MyMeetupRoom> getMyMeetupRooms() {
        List<MyMeetupRoom> rooms = new ArrayList<>();
        rooms.addAll(store.defaultMeetupRooms);
        rooms.addAll(store.createdMeetupRooms);

        for (MyMeetupRoom room : rooms) {
            refreshRoom(room);
        }
        return rooms;
    }

    public static boolean isDeadlinePassed(MyMeetupRoom room) {
        return !LocalDateTime.of(LocalDate.parse(room.deadlineDateIso),
                parseTime(room.deadlineTimeText)).isAfter(LocalDateTime.now());
    }

    private static void refreshRoom(MyMeetupRoom room) {
        List<AvailabilityResponse> responses = getAvailabilityResponses(room.roomId);
        room.participantCount = responses.size();
        room.responseRate = getResponseRate(room, responses);
        long days = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(room.deadlineDateIso));
        boolean closed = isDeadlinePassed(room);
        room.dDay = closed ? "마감" : days == 0 ? "D-Day" : "D-" + days;
    }

    public static MyMeetupRoom getMeetupRoomById(String roomId) {
        if (roomId != null) {
            for (MyMeetupRoom room : getMyMeetupRooms()) {
                if (room.roomId.equals(roomId)) {
                    return room;
                }
            }
        }

        return null;
    }

    public static MyMeetupRoom getMeetupRoomByTitle(String title) {
        if (title != null) {
            for (MyMeetupRoom room : getMyMeetupRooms()) {
                if (room.title.equals(title)) {
                    return room;
                }
            }
        }

        return title == null || title.isEmpty() ? getMeetupRoomById(DEFAULT_ROOM_ID) : null;
    }

    public static void setCurrentDraftCandidateDates(List<String> candidateDates) {
        setDraftCandidateDates(store.currentDraftRoomId, candidateDates);
    }

    public static List<String> getCurrentDraftCandidateDates() {
        return getDraftCandidateDates(store.currentDraftRoomId);
    }

    public static String getCurrentDraftRoomId() {
        return store.currentDraftRoomId;
    }

    public static void setDraftCandidateDates(String roomId, List<String> candidateDates) {
        if (roomId == null || roomId.isEmpty()) {
            return;
        }

        if (candidateDates == null || candidateDates.isEmpty()) {
            store.draftCandidateDatesByRoomId.remove(roomId);
            return;
        }

        store.draftCandidateDatesByRoomId.put(roomId, new ArrayList<>(candidateDates));
    }

    public static List<String> getDraftCandidateDates(String roomId) {
        if (roomId == null || roomId.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> candidateDates = store.draftCandidateDatesByRoomId.get(roomId);

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

        if (room == null) return new ArrayList<>();
        if (!room.candidateDateIsos.isEmpty()) {
            return new ArrayList<>(room.candidateDateIsos);
        }

        List<String> draftCandidateDates = getDraftCandidateDates(room.roomId);

        if (!draftCandidateDates.isEmpty()) {
            return draftCandidateDates;
        }

        return new ArrayList<>();
    }

    public static String addCreatedMeetupRoom(
            String title,
            int participantCount,
            String dDay,
            String deadlineDateIso,
            String deadlineTimeText
    ) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("모임명을 입력해주세요");
        }
        if (participantCount < 2 || participantCount > 20) {
            throw new IllegalArgumentException("정원은 2~20명이어야 해요");
        }
        if (!LocalDateTime.of(LocalDate.parse(deadlineDateIso), parseTime(deadlineTimeText))
                .isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("마감 시간을 현재 시간 이후로 선택해주세요");
        }

        String normalizedTitle = title.trim();
        String roomId = "room-created-" + UUID.randomUUID();
        store.currentDraftRoomId = roomId;
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

        createdRoom.hostName = getCurrentUser().name;
        createdRoom.maxParticipants = participantCount;
        createdRoom.participantCount = 1;
        store.responsesByRoomId.put(roomId, new ArrayList<>(Collections.singletonList(
                new AvailabilityResponse(roomId, getCurrentUser().name, false,
                        new ArrayList<>(), new ArrayList<>()))));
        store.createdMeetupRooms.add(createdRoom);
        return roomId;
    }

    public static String saveRoomDraft(String roomId, String title, int capacity,
            String deadlineDate, String deadlineTime, String description) {
        if (title == null || title.trim().isEmpty() || title.trim().length() > 30) {
            throw new IllegalArgumentException("모임명을 1~30자로 입력해주세요");
        }
        if (description == null || description.length() > 100) {
            throw new IllegalArgumentException("설명은 100자 이내로 입력해주세요");
        }
        if (capacity < 2 || capacity > 20) throw new IllegalArgumentException("정원은 2~20명이어야 해요");
        if (!LocalDateTime.of(LocalDate.parse(deadlineDate), parseTime(deadlineTime))
                .isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("마감 시간을 현재 시간 이후로 선택해주세요");
        }
        if (roomId == null) {
            roomId = addCreatedMeetupRoom(title, capacity, "", deadlineDate, deadlineTime);
        } else {
            MyMeetupRoom existing = requireRoom(roomId);
            if (!existing.confirmedDateIso.isEmpty()) throw new IllegalArgumentException("확정된 방은 수정할 수 없어요");
            if (getCompletedResponseCount(getAvailabilityResponses(roomId)) > 0) {
                throw new IllegalArgumentException("응답 제출 후에는 방 정보를 변경할 수 없어요");
            }
            if (existing.participantCount > capacity) throw new IllegalArgumentException("참여자 수보다 정원을 줄일 수 없어요");
            existing.title = title.trim();
            existing.maxParticipants = capacity;
            existing.deadlineDateIso = deadlineDate;
            existing.deadlineTimeText = deadlineTime;
            if (existing.candidateDateIsos.stream().anyMatch(date -> date.compareTo(deadlineDate) <= 0)) {
                existing.candidateDateIsos.clear();
                setDraftCandidateDates(roomId, new ArrayList<>());
            }
            HostSelectionDraft draft = store.hostSelectionDrafts.get(roomId);
            if (draft != null) draft.dates.removeIf(date -> date.compareTo(deadlineDate) <= 0);
        }
        requireRoom(roomId).description = description;
        return roomId;
    }

    public static String getInviteLink(String roomId) {
        if (roomId == null || roomId.isEmpty()) {
            return "https://pickday.app/room/" + DEFAULT_ROOM_ID;
        }

        return "https://pickday.app/room/" + roomId;
    }

    public static AvailabilityResponse getResponse(String roomId, String participantName) {
        for (AvailabilityResponse response : getAvailabilityResponses(roomId)) {
            if (response.participantName.equals(participantName)) return response;
        }
        return null;
    }

    public static HostSelectionDraft getHostSelectionDraft(String roomId) {
        MyMeetupRoom room = requireRoom(roomId);
        HostSelectionDraft draft = store.hostSelectionDrafts.get(roomId);
        return draft == null ? new HostSelectionDraft(room.candidateDateIsos,
                room.candidateDateIsos.isEmpty() ? new ArrayList<>() : room.allowedTimeSlotCodes,
                room.hostExcludedDateIsos) : new HostSelectionDraft(draft.dates, draft.times, draft.excluded);
    }

    public static void keepHostSelectionDraft(String roomId, List<String> dates,
            List<String> times, List<String> excluded) {
        if (getMeetupRoomById(roomId) != null) {
            store.hostSelectionDrafts.put(roomId, new HostSelectionDraft(dates, times, excluded));
        }
    }

    public static void addParticipant(String roomId, String name) {
        MyMeetupRoom room = requireRoom(roomId);
        requireOpen(room);
        if (name == null || name.trim().isEmpty() || name.trim().length() > 20) {
            throw new IllegalArgumentException("이름을 1~20자로 입력해주세요");
        }
        String normalized = name.trim();
        List<AvailabilityResponse> responses = store.responsesByRoomId.get(roomId);
        if (getResponse(roomId, normalized) != null) throw new IllegalArgumentException("같은 이름의 참여자가 있어요");
        if (responses.size() >= room.maxParticipants) throw new IllegalArgumentException("방 정원이 가득 찼어요");
        responses.add(new AvailabilityResponse(roomId, normalized, false, new ArrayList<>(), new ArrayList<>()));
        refreshRoom(room);
        if (room.notifyOnJoin) addNotification(room, normalized + "님이 참여했어요", "새 참여자의 응답을 기다리고 있어요");
    }

    public static void saveResponse(String roomId, String participantName, List<String> dates,
            List<String> times, List<String> excluded) {
        MyMeetupRoom room = requireRoom(roomId);
        requireOpen(room);
        AvailabilityResponse response = getResponse(roomId, participantName);
        if (response == null) throw new IllegalArgumentException("참여자 정보를 찾을 수 없어요");
        List<String> available = unique(dates);
        List<String> unavailable = unique(excluded);
        List<String> selectedTimes = unique(times);
        boolean unavailableForAll = available.isEmpty() && !room.candidateDateIsos.isEmpty()
                && unavailable.containsAll(room.candidateDateIsos);
        if (available.isEmpty() && !unavailableForAll) throw new IllegalArgumentException("가능한 날짜를 선택하거나 모든 후보를 제외해주세요");
        if (!available.isEmpty() && selectedTimes.isEmpty()) throw new IllegalArgumentException("시간대를 선택해주세요");
        if (!room.candidateDateIsos.containsAll(available) || !room.candidateDateIsos.containsAll(unavailable)) {
            throw new IllegalArgumentException("방의 후보 날짜 안에서 선택해주세요");
        }
        for (String date : unavailable) {
            if (available.contains(date)) throw new IllegalArgumentException("가능한 날짜와 제외 날짜가 겹쳐요");
        }
        for (String time : selectedTimes) {
            if (!time.equals("ANYTIME") && !room.allowedTimeSlotCodes.contains(time)) {
                throw new IllegalArgumentException("방장이 선택한 시간대 안에서 선택해주세요");
            }
        }
        if (selectedTimes.contains("ANYTIME")) selectedTimes = listOf("ANYTIME");
        response.availableDateIsos = available;
        response.selectedTimeSlotCodes = unavailableForAll ? new ArrayList<>() : selectedTimes;
        response.excludedDateIsos = unavailable;
        response.submitted = true;
        refreshRoom(room);
        addNotification(room, participantName + "님이 응답했어요", "현재 응답률 " + room.responseRate + "%");
    }

    public static void saveHostSelection(String roomId, List<String> dates, List<String> times,
            List<String> excluded) {
        MyMeetupRoom room = requireRoom(roomId);
        requireOpen(room);
        List<String> candidates = unique(dates);
        List<String> unavailable = unique(excluded);
        candidates.removeAll(unavailable);
        if (candidates.isEmpty() || candidates.size() > 10) throw new IllegalArgumentException("후보 날짜를 1~10개 선택해주세요");
        for (String date : candidates) {
            if (LocalDate.parse(date).isBefore(LocalDate.now())
                    || !LocalDate.parse(date).isAfter(LocalDate.parse(room.deadlineDateIso))) {
                throw new IllegalArgumentException("후보 날짜는 응답 마감일 다음날부터 선택해주세요");
            }
        }
        List<String> allowed = unique(times);
        if (allowed.contains("ANYTIME")) {
            allowed = new ArrayList<>();
            for (TimeSlot slot : getTimeSlots()) allowed.add(slot.code);
        }
        if (allowed.isEmpty()) throw new IllegalArgumentException("시간대를 선택해주세요");
        for (String code : allowed) {
            boolean valid = false;
            for (TimeSlot slot : getTimeSlots()) if (slot.code.equals(code)) valid = true;
            if (!valid) throw new IllegalArgumentException("시간대 정보를 확인해주세요");
        }
        // 제출된 응답의 유효성을 유지하기 위해 후보 날짜와 허용 시간 변경을 제한합니다.
        if (getCompletedResponseCount(getAvailabilityResponses(roomId)) > 0
                && (!new java.util.HashSet<>(room.candidateDateIsos).equals(new java.util.HashSet<>(candidates))
                || !new java.util.HashSet<>(room.allowedTimeSlotCodes).equals(new java.util.HashSet<>(allowed)))) {
            throw new IllegalArgumentException("응답 제출 후에는 후보를 변경할 수 없어요");
        }
        Collections.sort(candidates);
        room.candidateDateIsos = candidates;
        room.allowedTimeSlotCodes = allowed;
        room.hostExcludedDateIsos = unavailable;
        setDraftCandidateDates(roomId, candidates);
    }

    public static List<ScheduleCalculator.Option> getScheduleOptions(String roomId) {
        MyMeetupRoom room = requireRoom(roomId);
        return ScheduleCalculator.options(room, getAvailabilityResponses(roomId));
    }

    public static void confirmSchedule(String roomId, String dateIso, String timeCode, String actorName) {
        MyMeetupRoom room = requireRoom(roomId);
        if (!room.hostName.equals(actorName)) throw new IllegalArgumentException("방장만 일정을 확정할 수 있어요");
        if (!room.confirmedDateIso.isEmpty()) throw new IllegalArgumentException("이미 확정된 일정이에요");
        for (ScheduleCalculator.Option option : getScheduleOptions(roomId)) {
            if (option.dateIso.equals(dateIso) && option.time.code.equals(timeCode)) {
                room.confirmedDateIso = dateIso;
                room.confirmedTimeText = option.time.label + " " + option.time.timeRange;
                addNotification(room, "일정이 확정되었어요", formatConfirmedSchedule(room));
                return;
            }
        }
        throw new IllegalArgumentException("가능한 응답이 있는 날짜와 시간을 선택해주세요");
    }

    private static List<String> unique(List<String> values) {
        if (values == null) throw new IllegalArgumentException("선택 정보를 확인해주세요");
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("선택 정보를 확인해주세요");
        }
        return new ArrayList<>(new LinkedHashSet<>(values));
    }

    private static MyMeetupRoom requireRoom(String roomId) {
        MyMeetupRoom room = getMeetupRoomById(roomId);
        if (room == null) throw new IllegalArgumentException("방을 찾을 수 없어요");
        return room;
    }

    private static void requireOpen(MyMeetupRoom room) {
        if (!room.confirmedDateIso.isEmpty()) throw new IllegalArgumentException("확정된 방에서는 응답을 변경할 수 없어요");
        if (!LocalDateTime.of(LocalDate.parse(room.deadlineDateIso), parseTime(room.deadlineTimeText))
                .isAfter(LocalDateTime.now())) throw new IllegalArgumentException("응답 수집이 마감되었어요");
    }

    private static LocalTime parseTime(String text) {
        try {
            if (text != null && text.matches("\\d{1,2}:\\d{2}")) {
                String[] clock = text.split(":");
                return LocalTime.of(Integer.parseInt(clock[0]), Integer.parseInt(clock[1]));
            }
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(
                    "(오전|오후) ([1-9]|1[0-2]):([0-5][0-9])").matcher(text == null ? "" : text.trim());
            if (!matcher.matches()) throw new IllegalArgumentException("마감 시간 형식을 확인해주세요");
            int hour = Integer.parseInt(matcher.group(2)) % 12;
            if (matcher.group(1).equals("오후")) hour += 12;
            return LocalTime.of(hour, Integer.parseInt(matcher.group(3)));
        } catch (java.time.DateTimeException error) {
            throw new IllegalArgumentException("마감 시간 형식을 확인해주세요", error);
        }
    }

    private static void addNotification(MyMeetupRoom room, String title, String message) {
        store.localNotifications.add(0, new Notification(room.roomId, "오늘", title, room.title,
                message, "방금 전", "#5B4CDB"));
    }

    public static List<Notification> getNotifications() {
        List<Notification> notifications = new ArrayList<>(store.localNotifications);
        for (MyMeetupRoom room : store.defaultMeetupRooms) {
            refreshRoom(room);
            String title;
            String message;
            if (!room.confirmedDateIso.isEmpty()) {
                title = "확정된 샘플 일정";
                message = formatConfirmedSchedule(room);
            } else {
                title = isDeadlinePassed(room) ? "샘플 방 응답 수집 마감" : "샘플 방 응답 현황";
                message = "응답 완료 " + getRoomAvailabilitySummary(room.roomId).completedResponseCount
                        + "명 / 참여 " + room.participantCount + "명";
            }
            notifications.add(new Notification(room.roomId, "샘플", title, room.title,
                    message, "데모 데이터", "#5B4CDB"));
        }

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
        MyMeetupRoom room = getMeetupRoomByTitle(roomTitle);
        return room == null ? new ArrayList<>() : getChatMessagesByRoomId(room.roomId);
    }

    public static List<ChatMessage> getChatMessagesByRoomId(String roomId) {
        if (!store.messagesByRoomId.containsKey(roomId)) {
            store.messagesByRoomId.put(roomId, SampleMeetupData.getFixtureChatMessages(roomId));
        }
        return new ArrayList<>(store.messagesByRoomId.get(roomId));
    }

    public static ChatMessage sendChatMessage(String roomId, String text) {
        if (getMeetupRoomById(roomId) == null || text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("메시지와 방 정보를 확인해주세요");
        }
        getChatMessagesByRoomId(roomId);
        ChatMessage message = new ChatMessage(roomId, getCurrentUser().name, text.trim(),
                new SimpleDateFormat("a h:mm", Locale.KOREAN).format(new Date()), true, false);
        store.messagesByRoomId.get(roomId).add(message);
        return message;
    }

    public static ChatRoomStatus getChatRoomStatus(String roomTitle) {
        MyMeetupRoom room = getMeetupRoomByTitle(roomTitle);
        return room == null ? null : getChatRoomStatusByRoomId(room.roomId);
    }

    public static ChatRoomStatus getChatRoomStatusByRoomId(String roomId) {
        MyMeetupRoom room = getMeetupRoomById(roomId);

        if (room == null) return null;
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
        if (!store.responsesByRoomId.containsKey(roomId)) {
            store.responsesByRoomId.put(roomId, SampleMeetupData.getFixtureAvailabilityResponses(roomId));
        }
        return new ArrayList<>(store.responsesByRoomId.get(roomId));
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
            return 0;
        }

        return Math.round(getCompletedResponseCount(responses) * 100f / room.participantCount);
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

    private static List<String> listOf(String... values) {
        List<String> list = new ArrayList<>();

        for (String value : values) {
            list.add(value);
        }

        return list;
    }

}
