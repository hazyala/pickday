package com.hazyala.pickday.kopo.ac.kr.data;

import java.util.ArrayList;
import java.util.List;

public class DummyDataSource {

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

        dates.add(new AvailableDate("오늘", "5.21", "수", 3, false, false));
        dates.add(new AvailableDate("내일", "5.22", "목", 4, false, false));
        dates.add(new AvailableDate("", "5.23", "금", 6, false, false));
        dates.add(new AvailableDate("", "5.24", "토", 7, true, false));
        dates.add(new AvailableDate("", "5.25", "일", 5, false, true));
        dates.add(new AvailableDate("", "5.26", "월", 2, false, false));
        dates.add(new AvailableDate("", "5.27", "화", 3, false, false));

        return dates;
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

        return rooms;
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

        public AvailableDate(
                String label,
                String date,
                String dayOfWeek,
                int availableCount,
                boolean selected,
                boolean best
        ) {
            this.label = label;
            this.date = date;
            this.dayOfWeek = dayOfWeek;
            this.availableCount = availableCount;
            this.selected = selected;
            this.best = best;
        }
    }

    public static class MyMeetupRoom {
        public String title;
        public int participantCount;
        public String dDay;
        public int responseRate;
        public String iconType;

        public MyMeetupRoom(
                String title,
                int participantCount,
                String dDay,
                int responseRate,
                String iconType
        ) {
            this.title = title;
            this.participantCount = participantCount;
            this.dDay = dDay;
            this.responseRate = responseRate;
            this.iconType = iconType;
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
}
