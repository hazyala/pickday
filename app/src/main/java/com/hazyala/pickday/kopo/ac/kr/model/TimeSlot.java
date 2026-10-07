package com.hazyala.pickday.kopo.ac.kr.model;


public class TimeSlot {
    public String code;
    public String label;
    public String timeRange;

    public TimeSlot(String code, String label, String timeRange) {
        this.code = code;
        this.label = label;
        this.timeRange = timeRange;
    }
}
