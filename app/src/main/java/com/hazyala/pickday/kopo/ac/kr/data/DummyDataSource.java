package com.hazyala.pickday.kopo.ac.kr.data;

import java.util.ArrayList;
import java.util.List;

public class DummyDataSource {

    public static HomeState getHomeState() {
        return new HomeState(
                getCurrentUser(),
                getMainMeetup(),
                getAvailableDates(),
                getMyMeetupRooms()
        );
    }

    public static User getCurrentUser() {
        return new User(
                "김해민",
                "방장"
        );
    }

    public static MainMeetup getMainMeetup() {
        return new MainMeetup(
                "room-mt-2026",
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
                "room-team-meeting",
                "팀플 회의 일정",
                4,
                "D-1",
                100,
                "응답 완료"
        ));

        rooms.add(new MyMeetupRoom(
                "room-birthday",
                "지윤이 생일 파티",
                6,
                "D-3",
                83,
                "응답 대기"
        ));

        rooms.add(new MyMeetupRoom(
                "room-mt-2026",
                "동아리 MT 일정 정하기",
                7,
                "D-2",
                78,
                "진행 중"
        ));

        return rooms;
    }

    public static class HomeState {
        public User currentUser;
        public MainMeetup mainMeetup;
        public List<AvailableDate> availableDates;
        public List<MyMeetupRoom> meetupRooms;

        public HomeState(
                User currentUser,
                MainMeetup mainMeetup,
                List<AvailableDate> availableDates,
                List<MyMeetupRoom> meetupRooms
        ) {
            this.currentUser = currentUser;
            this.mainMeetup = mainMeetup;
            this.availableDates = availableDates;
            this.meetupRooms = meetupRooms;
        }
    }

    public static class User {
        public String name;
        public String role;

        public User(String name, String role) {
            this.name = name;
            this.role = role;
        }
    }

    public static class MainMeetup {
        public String id;
        public String statusLabel;
        public String title;
        public int participantCount;
        public String dDay;
        public int responseRate;
        public String bestDateTime;
        public int availableCount;

        public MainMeetup(
                String id,
                String statusLabel,
                String title,
                int participantCount,
                String dDay,
                int responseRate,
                String bestDateTime,
                int availableCount
        ) {
            this.id = id;
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
        public String id;
        public String title;
        public int participantCount;
        public String dDay;
        public int responseRate;
        public String statusLabel;

        public MyMeetupRoom(
                String id,
                String title,
                int participantCount,
                String dDay,
                int responseRate,
                String statusLabel
        ) {
            this.id = id;
            this.title = title;
            this.participantCount = participantCount;
            this.dDay = dDay;
            this.responseRate = responseRate;
            this.statusLabel = statusLabel;
        }
    }
}
