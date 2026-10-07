package com.hazyala.pickday.kopo.ac.kr.model;


public class RoomAvailabilitySummary {
    public String roomId;
    public int completedResponseCount;
    public int responseRate;
    public String bestDateTime;
    public int bestAvailableCount;

    public RoomAvailabilitySummary(
            String roomId,
            int completedResponseCount,
            int responseRate,
            String bestDateTime,
            int bestAvailableCount
    ) {
        this.roomId = roomId;
        this.completedResponseCount = completedResponseCount;
        this.responseRate = responseRate;
        this.bestDateTime = bestDateTime;
        this.bestAvailableCount = bestAvailableCount;
    }
}
