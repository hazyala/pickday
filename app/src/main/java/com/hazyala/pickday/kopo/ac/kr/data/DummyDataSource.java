package com.hazyala.pickday.kopo.ac.kr.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class DummyDataSource {

    private static final List<MyMeetupRoom> createdMeetupRooms = new ArrayList<>();
    private static final List<String> currentDraftCandidateDates = new ArrayList<>();

    public static User getCurrentUser() {
        return new User(
                "김해민",
                "방장",
                "home_profile"
        );
    }

    public static MainMeetup getMainMeetup() {
        return new MainMeetup(
                "진행 중인 약속",
                "동아리 MT 일정 정하기",
                7,
                "D-2",
                78,
                "5월 25일 (일) 오후 2시",
                5
        );
    }

    public static List<AvailableDate> getAvailableDates() {
        List<AvailableDate> dates = new ArrayList<>();
        Calendar today = Calendar.getInstance();
        Calendar weekStart = Calendar.getInstance();
        weekStart.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

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

            dates.add(new AvailableDate(
                    label,
                    formatMonthDay(date),
                    formatWeekday(date),
                    0,
                    index == todayIndex,
                    false,
                    hasMeetupStatus
            ));
        }

        return dates;
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

    public static List<MyMeetupRoom> getMyMeetupRooms() {
        List<MyMeetupRoom> rooms = new ArrayList<>();

        rooms.add(new MyMeetupRoom(
                "팀플 회의 일정",
                4,
                "D-1",
                100,
                "group"
        ));

        rooms.add(new MyMeetupRoom(
                "지윤이 생일 파티",
                6,
                "D-3",
                83,
                "cake"
        ));

        rooms.add(new MyMeetupRoom(
                "동아리 MT 일정 정하기",
                7,
                "D-2",
                78,
                "camp"
        ));

        rooms.addAll(createdMeetupRooms);

        return rooms;
    }

    public static void setCurrentDraftCandidateDates(List<String> candidateDates) {
        currentDraftCandidateDates.clear();

        if (candidateDates != null) {
            currentDraftCandidateDates.addAll(candidateDates);
        }
    }

    public static List<String> getCurrentDraftCandidateDates() {
        return new ArrayList<>(currentDraftCandidateDates);
    }

    public static List<String> getResponseCandidateDates() {
        if (!currentDraftCandidateDates.isEmpty()) {
            return getCurrentDraftCandidateDates();
        }

        List<String> fallbackDates = new ArrayList<>();
        fallbackDates.add("2026-05-21");
        fallbackDates.add("2026-05-22");
        fallbackDates.add("2026-05-23");
        fallbackDates.add("2026-05-24");
        fallbackDates.add("2026-05-25");
        fallbackDates.add("2026-05-26");
        fallbackDates.add("2026-05-27");
        return fallbackDates;
    }

    public static void addCreatedMeetupRoom(
            String title,
            int participantCount,
            String dDay,
            String deadlineDateIso,
            String deadlineTimeText
    ) {
        if (title == null || title.trim().isEmpty()) {
            return;
        }

        String normalizedTitle = title.trim();

        for (int index = 0; index < createdMeetupRooms.size(); index++) {
            MyMeetupRoom room = createdMeetupRooms.get(index);

            if (room.title.equals(normalizedTitle)) {
                createdMeetupRooms.set(index, new MyMeetupRoom(
                        normalizedTitle,
                        participantCount,
                        dDay,
                        0,
                        "default",
                        deadlineDateIso,
                        deadlineTimeText
                ));
                return;
            }
        }

        createdMeetupRooms.add(new MyMeetupRoom(
                normalizedTitle,
                participantCount,
                dDay,
                0,
                "default",
                deadlineDateIso,
                deadlineTimeText
        ));
    }

    public static List<Notification> getNotifications() {
        List<Notification> notifications = new ArrayList<>();

        notifications.add(new Notification(
                "오늘",
                "현우님이 응답했어요",
                "동아리 MT 일정 정하기",
                "",
                "방금 전",
                "#5B4CDB"
        ));

        notifications.add(new Notification(
                "오늘",
                "지윤님이 응답했어요",
                "동아리 MT 일정 정하기",
                "",
                "10분 전",
                "#5B4CDB"
        ));

        notifications.add(new Notification(
                "이번 주",
                "마감이 1일 남았어요",
                "동아리 MT 일정 정하기",
                "응답하지 않은 2명이 있어요",
                "오늘",
                "#FFC21A"
        ));

        notifications.add(new Notification(
                "이번 주",
                "일정이 확정되었어요!",
                "동아리 MT 일정 정하기",
                "5월 24일 (토) 오후 12:00",
                "어제",
                "#61D48A"
        ));

        notifications.add(new Notification(
                "이번 주",
                "새로운 초대장이 도착했어요",
                "세미콜론 종강 회식",
                "참여 여부를 선택해주세요",
                "2일 전",
                "#FF7DA8"
        ));

        notifications.add(new Notification(
                "이번 주",
                "수용님이 댓글을 남겼어요",
                "팀플 회의 일정",
                "\"저녁 7시 이후는 어떤가요?\"",
                "3일 전",
                "#5B4CDB"
        ));

        notifications.add(new Notification(
                "이전",
                "민재님이 응답했어요",
                "지윤이 생일 파티",
                "",
                "5일 전",
                "#5B4CDB"
        ));

        return notifications;
    }

    public static List<ChatMessage> getChatMessages(String roomTitle) {
        List<ChatMessage> messages = new ArrayList<>();

        if ("팀플 회의 일정".equals(roomTitle)) {
            messages.add(new ChatMessage("수용", "회의 끝나고 뭐 먹을까요?", "오후 6:12", false, false));
            messages.add(new ChatMessage("김해민", "짜장면 괜찮아요. 학교 앞에 새로 생긴 중국집도 있어요.", "오후 6:13", true, false));
            messages.add(new ChatMessage("지윤", "거기 탕수육도 괜찮대요.", "오후 6:15", false, false));
            messages.add(new ChatMessage("김해민", "그럼 중국집이랑 분식집 두 군데 후보로 적어둘게요.", "오후 6:16", true, false));
            return messages;
        }

        if ("지윤이 생일 파티".equals(roomTitle)) {
            messages.add(new ChatMessage("민재", "케이크는 초코가 좋을까요?", "오후 3:25", false, false));
            messages.add(new ChatMessage("김해민", "초코 좋고, 음식은 파스타나 피자 쪽이 무난할 것 같아요.", "오후 3:27", true, false));
            messages.add(new ChatMessage("서연", "맛집 알아요? 너무 시끄럽지 않은 곳이면 좋겠어요.", "오후 3:30", false, false));
            messages.add(new ChatMessage("김해민", "조용한 파스타집 하나 찾아보고 후보에 넣어둘게요.", "오후 3:32", true, false));
            return messages;
        }

        messages.add(new ChatMessage("현우", "MT 가면 저녁은 뭐 먹을까요?", "오후 9:12", false, false));
        messages.add(new ChatMessage("김해민", "고기 구워 먹는 것도 좋고, 비 오는 날이면 전골도 괜찮을 것 같아요.", "오후 9:13", true, false));
        messages.add(new ChatMessage("지윤", "근처 맛집 알아요?", "오후 9:15", false, false));
        messages.add(new ChatMessage("김해민", "숙소 근처 식당 몇 군데 찾아보고 후보로 정리해볼게요.", "오후 9:17", true, false));

        return messages;
    }

    public static ChatRoomStatus getChatRoomStatus(String roomTitle) {
        for (MyMeetupRoom room : getMyMeetupRooms()) {
            if (room.title.equals(roomTitle)) {
                return new ChatRoomStatus(
                        room.title,
                        room.participantCount,
                        room.dDay,
                        room.responseRate
                );
            }
        }

        MainMeetup meetup = getMainMeetup();

        return new ChatRoomStatus(
                meetup.title,
                meetup.participantCount,
                meetup.dDay,
                meetup.responseRate
        );
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
        public String statusLabel;
        public String title;
        public int participantCount;
        public String dDay;
        public int responseRate;
        public String bestDateTime;
        public int availableCount;

        public MainMeetup(
                String statusLabel,
                String title,
                int participantCount,
                String dDay,
                int responseRate,
                String bestDateTime,
                int availableCount
        ) {
            this.statusLabel = statusLabel;
            this.title = title;
            this.participantCount = participantCount;
            this.dDay = dDay;
            this.responseRate = responseRate;
            this.bestDateTime = bestDateTime;
            this.availableCount = availableCount;
        }
    }

    public static class AvailableDate {
        public String label;
        public String date;
        public String dayOfWeek;
        public int availableCount;
        public boolean selected;
        public boolean best;
        public boolean hasMeetupStatus;

        public AvailableDate(
                String label,
                String date,
                String dayOfWeek,
                int availableCount,
                boolean selected,
                boolean best,
                boolean hasMeetupStatus
        ) {
            this.label = label;
            this.date = date;
            this.dayOfWeek = dayOfWeek;
            this.availableCount = availableCount;
            this.selected = selected;
            this.best = best;
            this.hasMeetupStatus = hasMeetupStatus;
        }
    }

    public static class MyMeetupRoom {
        public String title;
        public int participantCount;
        public String dDay;
        public int responseRate;
        public String iconType;
        public String deadlineDateIso;
        public String deadlineTimeText;

        public MyMeetupRoom(
                String title,
                int participantCount,
                String dDay,
                int responseRate,
                String iconType
        ) {
            this(title, participantCount, dDay, responseRate, iconType, "", "");
        }

        public MyMeetupRoom(
                String title,
                int participantCount,
                String dDay,
                int responseRate,
                String iconType,
                String deadlineDateIso,
                String deadlineTimeText
        ) {
            this.title = title;
            this.participantCount = participantCount;
            this.dDay = dDay;
            this.responseRate = responseRate;
            this.iconType = iconType;
            this.deadlineDateIso = deadlineDateIso;
            this.deadlineTimeText = deadlineTimeText;
        }
    }

    public static class Notification {
        public String section;
        public String title;
        public String roomTitle;
        public String message;
        public String time;
        public String accentColor;

        public Notification(
                String section,
                String title,
                String roomTitle,
                String message,
                String time,
                String accentColor
        ) {
            this.section = section;
            this.title = title;
            this.roomTitle = roomTitle;
            this.message = message;
            this.time = time;
            this.accentColor = accentColor;
        }
    }

    public static class ChatMessage {
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
            this.senderName = senderName;
            this.message = message;
            this.time = time;
            this.mine = mine;
            this.notice = notice;
        }
    }

    public static class ChatRoomStatus {
        public String title;
        public int participantCount;
        public String dDay;
        public int responseRate;

        public ChatRoomStatus(
                String title,
                int participantCount,
                String dDay,
                int responseRate
        ) {
            this.title = title;
            this.participantCount = participantCount;
            this.dDay = dDay;
            this.responseRate = responseRate;
        }
    }
}
