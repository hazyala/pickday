package com.hazyala.pickday.kopo.ac.kr.model;

public final class UserRoomStats {
    public final int activeRooms;
    public final int submittedRooms;
    public final int confirmedRooms;
    public final int totalRooms;

    public UserRoomStats(int activeRooms, int submittedRooms, int confirmedRooms, int totalRooms) {
        this.activeRooms = activeRooms;
        this.submittedRooms = submittedRooms;
        this.confirmedRooms = confirmedRooms;
        this.totalRooms = totalRooms;
    }
}
