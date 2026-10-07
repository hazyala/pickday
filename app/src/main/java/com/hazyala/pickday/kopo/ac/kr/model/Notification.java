package com.hazyala.pickday.kopo.ac.kr.model;


public class Notification {
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
