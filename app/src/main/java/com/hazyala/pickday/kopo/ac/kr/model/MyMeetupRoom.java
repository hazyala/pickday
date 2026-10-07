package com.hazyala.pickday.kopo.ac.kr.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyMeetupRoom {
    public String roomId;
    public String title;
    public int participantCount;
    public String dDay;
    public int responseRate;
    public String iconType;
    public int maxParticipants;
    public String description = "";
    public String hostName = "";
    public boolean notifyOnJoin = true;
    public List<String> allowedTimeSlotCodes = new ArrayList<>(Arrays.asList("MORNING", "AFTERNOON",
            "LATE_AFTERNOON", "EVENING", "LATE_EVENING"));
    public List<String> hostExcludedDateIsos = new ArrayList<>();
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
        this.maxParticipants = participantCount;
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
