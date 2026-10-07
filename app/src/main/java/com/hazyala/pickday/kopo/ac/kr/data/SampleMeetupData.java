package com.hazyala.pickday.kopo.ac.kr.data;

import com.hazyala.pickday.kopo.ac.kr.model.AvailabilityResponse;
import com.hazyala.pickday.kopo.ac.kr.model.ChatMessage;
import com.hazyala.pickday.kopo.ac.kr.model.MyMeetupRoom;
import com.hazyala.pickday.kopo.ac.kr.model.User;
import static com.hazyala.pickday.kopo.ac.kr.data.LocalMeetupRepository.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class SampleMeetupData {
    private static final LocalDate fixtureBaseDate = LocalDate.now();
    private SampleMeetupData() { }

    static User getCurrentUser() {
        return new User(
                "김해민",
                "방장",
                "home_profile"
        );
    }

    static List<ChatMessage> getFixtureChatMessages(String roomId) {
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

    static List<AvailabilityResponse> getFixtureAvailabilityResponses(String roomId) {
        List<AvailabilityResponse> responses = new ArrayList<>();

        if (ROOM_ID_TEAM_MEETING.equals(roomId)) {
            responses.add(new AvailabilityResponse(roomId, "김해민", true,
                    listOf(fixtureDate(2), fixtureDate(4)), listOf("AFTERNOON", "EVENING")));
            responses.add(new AvailabilityResponse(roomId, "수용", true,
                    listOf(fixtureDate(4), fixtureDate(7)), listOf("EVENING")));
            responses.add(new AvailabilityResponse(roomId, "지윤", true,
                    listOf(fixtureDate(2), fixtureDate(4)), listOf("AFTERNOON")));
            responses.add(new AvailabilityResponse(roomId, "민재", true,
                    listOf(fixtureDate(4), fixtureDate(7)), listOf("AFTERNOON", "LATE_AFTERNOON")));
            return responses;
        }

        if (ROOM_ID_BIRTHDAY_PARTY.equals(roomId)) {
            responses.add(new AvailabilityResponse(roomId, "김해민", true,
                    listOf(fixtureDate(3), fixtureDate(4)), listOf("EVENING")));
            responses.add(new AvailabilityResponse(roomId, "민재", true,
                    listOf(fixtureDate(4)), listOf("EVENING", "LATE_EVENING")));
            responses.add(new AvailabilityResponse(roomId, "서연", true,
                    listOf(fixtureDate(4), fixtureDate(5)), listOf("AFTERNOON", "EVENING")));
            responses.add(new AvailabilityResponse(roomId, "지윤", true,
                    listOf(fixtureDate(4)), listOf("EVENING")));
            responses.add(new AvailabilityResponse(roomId, "현우", true,
                    listOf(fixtureDate(3), fixtureDate(5)), listOf("AFTERNOON")));
            responses.add(new AvailabilityResponse(roomId, "수빈", false,
                    new ArrayList<>(), new ArrayList<>()));
            return responses;
        }

        if (ROOM_ID_CAMP_MT.equals(roomId)) {
            responses.add(new AvailabilityResponse(roomId, "김해민", true,
                    listOf(fixtureDate(7), fixtureDate(13)), listOf("AFTERNOON", "EVENING")));
            responses.add(new AvailabilityResponse(roomId, "현우", true,
                    listOf(fixtureDate(13), fixtureDate(14)), listOf("AFTERNOON")));
            responses.add(new AvailabilityResponse(roomId, "지윤", true,
                    listOf(fixtureDate(7), fixtureDate(13)), listOf("LATE_AFTERNOON", "EVENING")));
            responses.add(new AvailabilityResponse(roomId, "수빈", true,
                    listOf(fixtureDate(13)), listOf("AFTERNOON", "LATE_AFTERNOON")));
            responses.add(new AvailabilityResponse(roomId, "민재", true,
                    listOf(fixtureDate(7), fixtureDate(14)), listOf("EVENING")));
            responses.add(new AvailabilityResponse(roomId, "서연", false,
                    new ArrayList<>(), new ArrayList<>()));
            responses.add(new AvailabilityResponse(roomId, "도윤", false,
                    new ArrayList<>(), new ArrayList<>()));
        }

        return responses;
    }

    static List<MyMeetupRoom> getDefaultMeetupRooms() {
        List<MyMeetupRoom> rooms = new ArrayList<>();

        List<String> teamDates = new ArrayList<>();
        teamDates.add(fixtureDate(2));
        teamDates.add(fixtureDate(4));
        teamDates.add(fixtureDate(7));
        rooms.add(new MyMeetupRoom(
                ROOM_ID_TEAM_MEETING,
                "팀플 회의 일정",
                4,
                "마감",
                100,
                "group",
                fixtureDate(1),
                "오후 11:59",
                teamDates,
                "",
                "",
                "팀"
        ));

        List<String> birthdayDates = new ArrayList<>();
        birthdayDates.add(fixtureDate(3));
        birthdayDates.add(fixtureDate(4));
        birthdayDates.add(fixtureDate(5));
        rooms.add(new MyMeetupRoom(
                ROOM_ID_BIRTHDAY_PARTY,
                "지윤이 생일 파티",
                6,
                "D-5",
                83,
                "cake",
                fixtureDate(2),
                "오후 11:59",
                birthdayDates,
                fixtureDate(4),
                "저녁 18:00~21:00",
                "생"
        ));

        List<String> campDates = new ArrayList<>();
        campDates.add(fixtureDate(7));
        campDates.add(fixtureDate(13));
        campDates.add(fixtureDate(14));
        rooms.add(new MyMeetupRoom(
                ROOM_ID_CAMP_MT,
                "동아리 MT 일정 정하기",
                7,
                "D-10",
                71,
                "camp",
                fixtureDate(6),
                "오후 11:59",
                campDates,
                fixtureDate(13),
                "오후 12:00~15:00",
                "동"
        ));

        for (MyMeetupRoom room : rooms) {
            room.hostName = getCurrentUser().name;
        }
        return rooms;
    }

    static String fixtureDate(int offset) {
        return fixtureBaseDate.plusDays(offset).toString();
    }
    private static List<String> listOf(String... values) {
        return new ArrayList<>(Arrays.asList(values));
    }
}
