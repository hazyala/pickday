package com.hazyala.pickday.kopo.ac.kr.data;

import com.hazyala.pickday.kopo.ac.kr.model.AvailabilityResponse;
import com.hazyala.pickday.kopo.ac.kr.model.ChatMessage;
import com.hazyala.pickday.kopo.ac.kr.model.HostSelectionDraft;
import com.hazyala.pickday.kopo.ac.kr.model.MyMeetupRoom;
import com.hazyala.pickday.kopo.ac.kr.model.Notification;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class MeetupMemoryStore {
    final List<MyMeetupRoom> defaultMeetupRooms = SampleMeetupData.getDefaultMeetupRooms();
    final List<MyMeetupRoom> createdMeetupRooms = new ArrayList<>();
    final Map<String, List<String>> draftCandidateDatesByRoomId = new HashMap<>();
    final Map<String, HostSelectionDraft> hostSelectionDrafts = new HashMap<>();
    String currentDraftRoomId = LocalMeetupRepository.DEFAULT_ROOM_ID;
    final Map<String, List<AvailabilityResponse>> responsesByRoomId = new HashMap<>();
    final Map<String, List<ChatMessage>> messagesByRoomId = new HashMap<>();
    final List<Notification> localNotifications = new ArrayList<>();
}
