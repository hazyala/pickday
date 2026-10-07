package com.hazyala.pickday.kopo.ac.kr.model;


public class ChatRoomStatus {
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
