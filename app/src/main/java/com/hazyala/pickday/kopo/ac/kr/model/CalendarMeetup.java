package com.hazyala.pickday.kopo.ac.kr.model;


public class CalendarMeetup {
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
