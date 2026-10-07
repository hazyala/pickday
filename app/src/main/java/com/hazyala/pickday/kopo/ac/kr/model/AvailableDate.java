package com.hazyala.pickday.kopo.ac.kr.model;


public class AvailableDate {
    public String roomId;
    public String label;
    public String date;
    public String dayOfWeek;
    public int availableCount;
    public boolean selected;
    public boolean best;
    public boolean hasMeetupStatus;

    public AvailableDate(
            String roomId,
            String label,
            String date,
            String dayOfWeek,
            int availableCount,
            boolean selected,
            boolean best,
            boolean hasMeetupStatus
    ) {
        this.roomId = roomId;
        this.label = label;
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.availableCount = availableCount;
        this.selected = selected;
        this.best = best;
        this.hasMeetupStatus = hasMeetupStatus;
    }
}
