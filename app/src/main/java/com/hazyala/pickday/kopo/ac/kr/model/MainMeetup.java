package com.hazyala.pickday.kopo.ac.kr.model;


public class MainMeetup {
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
