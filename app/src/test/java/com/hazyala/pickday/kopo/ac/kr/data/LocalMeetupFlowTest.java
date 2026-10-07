package com.hazyala.pickday.kopo.ac.kr.data;

import com.hazyala.pickday.kopo.ac.kr.model.CalendarMeetup;
import com.hazyala.pickday.kopo.ac.kr.model.HostSelectionDraft;
import com.hazyala.pickday.kopo.ac.kr.model.MyMeetupRoom;

import org.junit.Test;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.*;

public class LocalMeetupFlowTest {
    private final String host = LocalMeetupRepository.getCurrentUser().name;
    private final String firstDate = LocalDate.now().plusDays(3).toString();
    private final String secondDate = LocalDate.now().plusDays(4).toString();

    private String room(int capacity) {
        return LocalMeetupRepository.saveRoomDraft(null, "테스트 방", capacity,
                LocalDate.now().plusDays(1).toString(), "오후 11:59", "설명");
    }

    private void candidates(String id) {
        LocalMeetupRepository.saveHostSelection(id, Arrays.asList(secondDate, firstDate),
                Arrays.asList("AFTERNOON", "EVENING"), Collections.emptyList());
    }

    private void submit(String id, String name, String date, String time) {
        LocalMeetupRepository.saveResponse(id, name, Collections.singletonList(date),
                Collections.singletonList(time), Collections.emptyList());
    }

    private void rejects(Runnable action) {
        try { action.run(); fail("검증 오류가 필요합니다"); }
        catch (IllegalArgumentException expected) { assertNotNull(expected.getMessage()); }
    }

    @Test public void sameTitleCreatesIndependentRooms() {
        String first = room(2), second = room(2);
        candidates(first);
        assertNotEquals(first, second);
        assertTrue(LocalMeetupRepository.getResponseCandidateDates(second).isEmpty());
        assertEquals(2, LocalMeetupRepository.getResponseCandidateDates(first).size());
    }

    @Test public void draftUpdateKeepsRoomIdAndActualParticipantCount() {
        String id = room(4);
        assertEquals(id, LocalMeetupRepository.saveRoomDraft(id, "수정한 방", 3,
                LocalDate.now().plusDays(2).toString(), "오후 11:59", "새 설명"));
        MyMeetupRoom room = LocalMeetupRepository.getMeetupRoomById(id);
        assertEquals(1, room.participantCount);
        assertEquals(3, room.maxParticipants);
        assertEquals("새 설명", room.description);
    }

    @Test public void unknownRoomDoesNotReturnSampleRoom() {
        assertNull(LocalMeetupRepository.getMeetupRoomById("missing"));
        assertTrue(LocalMeetupRepository.getResponseCandidateDates("missing").isEmpty());
        rejects(() -> LocalMeetupRepository.addParticipant("missing", "민재"));
    }

    @Test public void emptyResponsesHaveNoRecommendation() {
        String id = room(2);
        candidates(id);
        assertTrue(LocalMeetupRepository.getScheduleOptions(id).isEmpty());
        assertEquals("집계 대기", LocalMeetupRepository.getRoomAvailabilitySummary(id).bestDateTime);
        assertEquals(0, LocalMeetupRepository.getRoomAvailabilitySummary(id).bestAvailableCount);
    }

    @Test public void participantCapacityAndDuplicateNamesAreValidated() {
        String id = room(2);
        LocalMeetupRepository.addParticipant(id, "민재");
        rejects(() -> LocalMeetupRepository.addParticipant(id, "민재"));
        rejects(() -> LocalMeetupRepository.addParticipant(id, "서연"));
        assertEquals(2, LocalMeetupRepository.getMeetupRoomById(id).participantCount);
    }

    @Test public void responseUpdateReplacesPreviousSubmissionAndRefreshesRate() {
        String id = room(2);
        candidates(id);
        LocalMeetupRepository.addParticipant(id, "민재");
        submit(id, host, firstDate, "AFTERNOON");
        assertEquals(50, LocalMeetupRepository.getRoomAvailabilitySummary(id).responseRate);
        submit(id, host, secondDate, "EVENING");
        assertEquals(Collections.singletonList(secondDate), LocalMeetupRepository.getResponse(id, host).availableDateIsos);
        assertEquals(1, LocalMeetupRepository.getRoomAvailabilitySummary(id).completedResponseCount);
        submit(id, "민재", secondDate, "EVENING");
        assertEquals(100, LocalMeetupRepository.getRoomAvailabilitySummary(id).responseRate);
        assertEquals(2, LocalMeetupRepository.getRoomAvailabilitySummary(id).bestAvailableCount);
    }

    @Test public void anytimeVotesOnlyForHostAllowedSlots() {
        String id = room(2);
        candidates(id);
        submit(id, host, firstDate, "ANYTIME");
        List<ScheduleCalculator.Option> options = LocalMeetupRepository.getScheduleOptions(id);
        assertEquals(2, options.size());
        assertEquals("AFTERNOON", options.get(0).time.code);
        assertEquals("EVENING", options.get(1).time.code);
        assertEquals(1, options.get(0).time.count);
    }

    @Test public void recommendationUsesTimesOfParticipantsAvailableOnThatDate() {
        String id = room(3);
        candidates(id);
        LocalMeetupRepository.addParticipant(id, "민재");
        LocalMeetupRepository.addParticipant(id, "서연");
        submit(id, host, firstDate, "AFTERNOON");
        submit(id, "민재", firstDate, "EVENING");
        submit(id, "서연", secondDate, "EVENING");
        ScheduleCalculator.Option best = LocalMeetupRepository.getScheduleOptions(id).get(0);
        assertEquals(firstDate, best.dateIso);
        assertEquals("AFTERNOON", best.time.code);
        assertEquals(1, best.time.count);
    }

    @Test public void fullIntersectionAndDateTieAreDeterministic() {
        String id = room(2);
        candidates(id);
        LocalMeetupRepository.addParticipant(id, "민재");
        for (String name : Arrays.asList(host, "민재")) {
            LocalMeetupRepository.saveResponse(id, name, Arrays.asList(secondDate, firstDate),
                    Collections.singletonList("ANYTIME"), Collections.emptyList());
        }
        List<ScheduleCalculator.DateResult> dates = ScheduleCalculator.dates(
                LocalMeetupRepository.getMeetupRoomById(id), LocalMeetupRepository.getAvailabilityResponses(id));
        assertEquals(firstDate, dates.get(0).dateIso);
        assertTrue(dates.get(0).fullIntersection);
    }

    @Test public void invalidResponseDoesNotMutateExistingSubmission() {
        String id = room(2);
        candidates(id);
        submit(id, host, firstDate, "EVENING");
        rejects(() -> LocalMeetupRepository.saveResponse(id, host, Collections.singletonList(firstDate),
                Collections.singletonList("EVENING"), Collections.singletonList(firstDate)));
        rejects(() -> submit(id, host, firstDate, "MORNING"));
        rejects(() -> submit(id, host, LocalDate.now().plusDays(10).toString(), "EVENING"));
        assertEquals(Collections.singletonList(firstDate), LocalMeetupRepository.getResponse(id, host).availableDateIsos);
    }

    @Test public void excludedDatesAreRestoredAndDoNotCountAsAvailable() {
        String id = room(2);
        candidates(id);
        LocalMeetupRepository.saveResponse(id, host, Collections.singletonList(firstDate),
                Collections.singletonList("EVENING"), Collections.singletonList(secondDate));
        assertEquals(Collections.singletonList(secondDate), LocalMeetupRepository.getResponse(id, host).excludedDateIsos);
        assertEquals(1, LocalMeetupRepository.getScheduleOptions(id).size());
    }

    @Test public void participantCanSubmitUnavailableForEveryCandidate() {
        String id = room(2);
        candidates(id);
        LocalMeetupRepository.saveResponse(id, host, Collections.emptyList(), Collections.emptyList(),
                Arrays.asList(firstDate, secondDate));
        assertEquals(100, LocalMeetupRepository.getRoomAvailabilitySummary(id).responseRate);
        assertTrue(LocalMeetupRepository.getScheduleOptions(id).isEmpty());
        assertTrue(LocalMeetupRepository.getResponse(id, host).submitted);
        rejects(() -> LocalMeetupRepository.saveResponse(id, host, Collections.emptyList(),
                Collections.emptyList(), Collections.singletonList(firstDate)));
    }

    @Test public void hostExcludedDateIsRemovedFromCandidates() {
        String id = room(2);
        LocalMeetupRepository.saveHostSelection(id, Arrays.asList(firstDate, secondDate),
                Collections.singletonList("ANYTIME"), Collections.singletonList(firstDate));
        assertEquals(Collections.singletonList(secondDate), LocalMeetupRepository.getResponseCandidateDates(id));
        assertEquals(5, LocalMeetupRepository.getMeetupRoomById(id).allowedTimeSlotCodes.size());
    }

    @Test public void confirmationUpdatesCalendarAndLocksResponses() {
        String id = room(2);
        candidates(id);
        submit(id, host, firstDate, "EVENING");
        rejects(() -> LocalMeetupRepository.confirmSchedule(id, firstDate, "EVENING", "다른 사람"));
        rejects(() -> LocalMeetupRepository.confirmSchedule(id, secondDate, "EVENING", host));
        LocalMeetupRepository.confirmSchedule(id, firstDate, "EVENING", host);
        assertEquals(firstDate, LocalMeetupRepository.getMeetupRoomById(id).confirmedDateIso);
        assertTrue(LocalMeetupRepository.getCalendarMeetups().stream().anyMatch(
                event -> event.roomId.equals(id) && event.statusText.equals("확정된 약속일")
                        && event.dateIso.equals(firstDate)));
        rejects(() -> submit(id, host, secondDate, "EVENING"));
        rejects(() -> LocalMeetupRepository.addParticipant(id, "민재"));
        rejects(() -> LocalMeetupRepository.confirmSchedule(id, firstDate, "EVENING", host));
        assertTrue(LocalMeetupRepository.getNotifications().stream().anyMatch(
                notification -> notification.roomId.equals(id) && notification.title.equals("일정이 확정되었어요")));
    }

    @Test public void submittedResponsesPreventChangingCandidates() {
        String id = room(2);
        candidates(id);
        submit(id, host, firstDate, "EVENING");
        rejects(() -> LocalMeetupRepository.saveHostSelection(id, Collections.singletonList(secondDate),
                Collections.singletonList("EVENING"), Collections.emptyList()));
        assertEquals(2, LocalMeetupRepository.getResponseCandidateDates(id).size());
    }

    @Test public void pastDeadlineAndPrematureCandidateAreRejected() {
        rejects(() -> LocalMeetupRepository.saveRoomDraft(null, "방", 2,
                LocalDate.now().minusDays(1).toString(), "오후 11:59", ""));
        String id = room(2);
        rejects(() -> LocalMeetupRepository.saveHostSelection(id, Collections.singletonList(LocalDate.now().toString()),
                Collections.singletonList("EVENING"), Collections.emptyList()));
        LocalMeetupRepository.getMeetupRoomById(id).deadlineDateIso = LocalDate.now().minusDays(1).toString();
        rejects(() -> LocalMeetupRepository.addParticipant(id, "민재"));
    }

    @Test public void chatMessagesSurviveReopeningWithinProcessAndStayRoomScoped() {
        String first = room(2), second = room(2);
        LocalMeetupRepository.sendChatMessage(first, "  안녕하세요  ");
        assertEquals("안녕하세요", LocalMeetupRepository.getChatMessagesByRoomId(first).get(0).message);
        assertTrue(LocalMeetupRepository.getChatMessagesByRoomId(second).isEmpty());
        rejects(() -> LocalMeetupRepository.sendChatMessage(first, " "));
    }

    @Test public void bestScheduleMaximizesPeopleWhoCanMeetAtTheSameTime() {
        String id = room(7);
        candidates(id);
        for (String name : Arrays.asList("민재", "서연", "지윤", "현우", "수빈", "도윤")) {
            LocalMeetupRepository.addParticipant(id, name);
        }
        for (String name : Arrays.asList(host, "민재")) submit(id, name, firstDate, "AFTERNOON");
        for (String name : Arrays.asList("서연", "지윤")) submit(id, name, firstDate, "EVENING");
        for (String name : Arrays.asList("현우", "수빈", "도윤")) submit(id, name, secondDate, "AFTERNOON");
        ScheduleCalculator.Option best = LocalMeetupRepository.getScheduleOptions(id).get(0);
        assertEquals(secondDate, best.dateIso);
        assertEquals(3, best.time.count);
    }

    @Test public void movingDeadlineOntoCandidateClearsInvalidCandidates() {
        String id = room(2);
        candidates(id);
        LocalMeetupRepository.saveRoomDraft(id, "수정", 2, firstDate, "오후 11:59", "");
        assertTrue(LocalMeetupRepository.getResponseCandidateDates(id).isEmpty());
    }

    @Test public void expiredDraftCanBeRepairedBeforeAnyResponseIsSubmitted() {
        String id = room(2);
        LocalMeetupRepository.getMeetupRoomById(id).deadlineDateIso = LocalDate.now().minusDays(1).toString();
        assertEquals(id, LocalMeetupRepository.saveRoomDraft(id, "새 마감", 2,
                LocalDate.now().plusDays(2).toString(), "오후 11:59", ""));
    }

    @Test public void pastCandidateCannotBeConfirmed() {
        String id = room(2);
        candidates(id);
        submit(id, host, firstDate, "EVENING");
        String past = LocalDate.now().minusDays(1).toString();
        LocalMeetupRepository.getMeetupRoomById(id).candidateDateIsos = Collections.singletonList(past);
        LocalMeetupRepository.getResponse(id, host).availableDateIsos = Collections.singletonList(past);
        rejects(() -> LocalMeetupRepository.confirmSchedule(id, past, "EVENING", host));
    }

    @Test public void hostDraftIsSeparateFromPublishedCandidatesAndReturnedAsACopy() {
        String id = room(2);
        LocalMeetupRepository.keepHostSelectionDraft(id, Collections.singletonList(firstDate),
                Collections.singletonList("EVENING"), Collections.emptyList());
        assertTrue(LocalMeetupRepository.getResponseCandidateDates(id).isEmpty());
        HostSelectionDraft copy = LocalMeetupRepository.getHostSelectionDraft(id);
        copy.dates.clear();
        assertEquals(Collections.singletonList(firstDate), LocalMeetupRepository.getHostSelectionDraft(id).dates);
    }

    @Test public void confirmedCalendarAndHomeUseActualAvailableCount() {
        String id = room(2);
        candidates(id);
        LocalMeetupRepository.addParticipant(id, "민재");
        submit(id, host, firstDate, "EVENING");
        LocalMeetupRepository.confirmSchedule(id, firstDate, "EVENING", host);
        CalendarMeetup event = LocalMeetupRepository.getCalendarMeetups().stream()
                .filter(item -> item.roomId.equals(id) && item.statusText.equals("확정된 약속일"))
                .findFirst().get();
        assertEquals(1, event.participantCount);
        assertEquals(1, LocalMeetupRepository.getMainMeetup().availableCount);
    }
    @Test public void userStatisticsFollowCreationResponseAndConfirmation() {
        com.hazyala.pickday.kopo.ac.kr.model.UserRoomStats before = LocalMeetupRepository.getUserRoomStats();
        String id = room(2);
        candidates(id);
        com.hazyala.pickday.kopo.ac.kr.model.UserRoomStats created = LocalMeetupRepository.getUserRoomStats();
        assertEquals(before.totalRooms + 1, created.totalRooms);
        assertEquals(before.activeRooms + 1, created.activeRooms);
        submit(id, host, firstDate, "AFTERNOON");
        assertEquals(before.submittedRooms + 1, LocalMeetupRepository.getUserRoomStats().submittedRooms);
        LocalMeetupRepository.confirmSchedule(id, firstDate, "AFTERNOON", host);
        com.hazyala.pickday.kopo.ac.kr.model.UserRoomStats confirmed = LocalMeetupRepository.getUserRoomStats();
        assertEquals(before.confirmedRooms + 1, confirmed.confirmedRooms);
        assertEquals(before.activeRooms, confirmed.activeRooms);
    }
}
