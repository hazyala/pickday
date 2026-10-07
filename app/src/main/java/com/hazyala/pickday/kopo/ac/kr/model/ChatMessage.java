package com.hazyala.pickday.kopo.ac.kr.model;


public class ChatMessage {
    public String roomId;
    public String senderName;
    public String message;
    public String time;
    public boolean mine;
    public boolean notice;

    public ChatMessage(
            String roomId,
            String senderName,
            String message,
            String time,
            boolean mine,
            boolean notice
    ) {
        this.roomId = roomId;
        this.senderName = senderName;
        this.message = message;
        this.time = time;
        this.mine = mine;
        this.notice = notice;
    }
}
