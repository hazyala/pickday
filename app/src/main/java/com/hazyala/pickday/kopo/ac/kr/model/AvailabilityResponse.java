package com.hazyala.pickday.kopo.ac.kr.model;

import java.util.ArrayList;
import java.util.List;

public class AvailabilityResponse {
    public String roomId;
    public String participantName;
    public boolean submitted;
    public List<String> availableDateIsos;
    public List<String> selectedTimeSlotCodes;
    public List<String> excludedDateIsos = new ArrayList<>();

    public AvailabilityResponse(
            String roomId,
            String participantName,
            boolean submitted,
            List<String> availableDateIsos,
            List<String> selectedTimeSlotCodes
    ) {
        this.roomId = roomId;
        this.participantName = participantName;
        this.submitted = submitted;
        this.availableDateIsos = new ArrayList<>(availableDateIsos);
        this.selectedTimeSlotCodes = new ArrayList<>(selectedTimeSlotCodes);
    }
}
