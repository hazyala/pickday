package com.hazyala.pickday.kopo.ac.kr;

import android.content.Intent;
import android.widget.LinearLayout;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.hazyala.pickday.kopo.ac.kr.data.LocalMeetupRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.pressBackUnconditionally;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class LocalMeetupFlowInstrumentedTest {
    private String room(int capacity) {
        return LocalMeetupRepository.saveRoomDraft(null, "화면 검증 방", capacity,
                LocalDate.now().plusDays(1).toString(), "오후 11:59", "");
    }

    private Intent intent(Class<?> activity, String id) {
        return new Intent(ApplicationProvider.getApplicationContext(), activity)
                .putExtra(RoomDetailActivity.EXTRA_ROOM_ID, id);
    }

    @Test public void submitResponseThenConfirmFromRoomDetail() {
        String id = room(2);
        LocalDate date = LocalDate.now().plusDays(3);
        LocalMeetupRepository.saveHostSelection(id, Collections.singletonList(date.toString()),
                Arrays.asList("AFTERNOON", "EVENING"), Collections.emptyList());
        try (ActivityScenario<RoomDetailActivity> scenario = ActivityScenario.launch(intent(RoomDetailActivity.class, id))) {
            onView(withId(R.id.btnChangeResponse)).perform(click());
            onView(allOf(withText(String.valueOf(date.getDayOfMonth())),
                    isDescendantOfA(withId(R.id.responseCalendar)), isEnabled())).perform(scrollTo(), click());
            onView(withId(R.id.timeAny)).perform(scrollTo(), click());
            onView(withId(R.id.btnComplete)).perform(click());
            onView(withId(R.id.tvProgressRate)).check(matches(withText("100%")));
            onView(withId(R.id.btnConfirmSchedule)).perform(scrollTo(), click());
            onData(anything()).inRoot(isDialog()).atPosition(0).perform(click());
            onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());
            onView(withId(R.id.tvRoomStatus)).check(matches(withText("확정")));
            onView(withId(R.id.btnChangeResponse)).check(matches(not(isEnabled())));
            assertEquals(date.toString(), LocalMeetupRepository.getMeetupRoomById(id).confirmedDateIso);
        }
    }

    @Test public void restoredResponseAndAllTenExclusionDatesSurviveRecreation() {
        String id = room(2);
        ArrayList<String> dates = new ArrayList<>();
        for (int index = 0; index < 10; index++) dates.add(LocalDate.now().plusDays(index + 2).toString());
        LocalMeetupRepository.saveHostSelection(id, dates, Collections.singletonList("EVENING"), Collections.emptyList());
        LocalMeetupRepository.saveResponse(id, LocalMeetupRepository.getCurrentUser().name,
                Collections.singletonList(dates.get(0)), Collections.singletonList("EVENING"),
                Collections.singletonList(dates.get(9)));
        try (ActivityScenario<ResponseSelectionActivity> scenario = ActivityScenario.launch(intent(ResponseSelectionActivity.class, id))) {
            scenario.onActivity(activity -> assertEquals(10,
                    ((LinearLayout) activity.findViewById(R.id.layoutExcludeDates)).getChildCount()));
            onView(withId(R.id.tvSelectedDateCount)).check(matches(withText(containsString("1개 선택"))));
            onView(withId(R.id.tvExcludeCount)).check(matches(withText(containsString("1개 선택"))));
            scenario.recreate();
            onView(withId(R.id.timeEvening)).check(matches(withText(startsWith("●"))));
            onView(withId(R.id.tvSelectedDateCount)).check(matches(withText(containsString("1개 선택"))));
            onView(withId(R.id.tvExcludeCount)).check(matches(withText(containsString("1개 선택"))));
        }
    }

    @Test public void participantScreenAddsActualParticipant() {
        String id = room(2);
        try (ActivityScenario<ParticipantListActivity> scenario = ActivityScenario.launch(intent(ParticipantListActivity.class, id))) {
            onView(withId(R.id.btnAddParticipant)).perform(click());
            onView(withHint("참여자 이름")).perform(replaceText("민재"), closeSoftKeyboard());
            onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());
            onView(withId(R.id.tvParticipantCount)).check(matches(withText("참여 2명 / 정원 2명")));
            onView(withId(R.id.btnAddParticipant)).check(matches(not(isEnabled())));
            assertNotNull(LocalMeetupRepository.getResponse(id, "민재"));
        }
    }

    @Test public void participantCanSubmitWhenEveryCandidateIsExcluded() {
        String id = room(2);
        LocalDate first = LocalDate.now().plusDays(3), second = LocalDate.now().plusDays(4);
        LocalMeetupRepository.saveHostSelection(id, Arrays.asList(first.toString(), second.toString()),
                Collections.singletonList("EVENING"), Collections.emptyList());
        try (ActivityScenario<ResponseSelectionActivity> scenario = ActivityScenario.launch(intent(ResponseSelectionActivity.class, id))) {
            for (LocalDate date : Arrays.asList(first, second)) {
                onView(withContentDescription(date.getMonthValue() + "." + date.getDayOfMonth() + " 제외 날짜"))
                        .perform(scrollTo(), click());
            }
            onView(withId(R.id.btnComplete)).perform(click());
            onView(withId(R.id.tvProgressRate)).check(matches(withText("100%")));
            onView(withId(R.id.btnConfirmSchedule)).check(matches(not(isEnabled())));
            assertTrue(LocalMeetupRepository.getScheduleOptions(id).isEmpty());
        }
    }

    @Test public void hostUnsubmittedSelectionSurvivesBackAndReentry() {
        String id = room(2);
        LocalDate date = LocalDate.now().plusDays(3);
        try (ActivityScenario<SelectionActivity> scenario = ActivityScenario.launch(intent(SelectionActivity.class, id))) {
            if (date.getMonth() != LocalDate.now().getMonth()) {
                onView(withId(R.id.btnCalendarNext)).perform(scrollTo(), click());
            }
            onView(allOf(withText(String.valueOf(date.getDayOfMonth())),
                    isDescendantOfA(withId(R.id.selectionCalendar)))).perform(scrollTo(), click());
            onView(withId(R.id.tvDateCount)).check(matches(withText(containsString("(1/10)"))));
            onView(withId(R.id.timeEvening)).perform(scrollTo(), click());
            pressBackUnconditionally();
        }
        assertEquals(Collections.singletonList(date.toString()), LocalMeetupRepository.getHostSelectionDraft(id).dates);
        assertTrue(LocalMeetupRepository.getResponseCandidateDates(id).isEmpty());
        try (ActivityScenario<SelectionActivity> scenario = ActivityScenario.launch(intent(SelectionActivity.class, id))) {
            onView(withId(R.id.tvDateCount)).check(matches(withText(containsString("(1/10)"))));
            onView(withId(R.id.timeEvening)).check(matches(withText(startsWith("✓"))));
            onView(withId(R.id.btnNext)).perform(click());
            assertEquals(Collections.singletonList(date.toString()), LocalMeetupRepository.getResponseCandidateDates(id));
        }
    }

    @Test public void expiredRoomDisablesParticipantAdditionAndResponseRows() {
        String id = room(2);
        LocalMeetupRepository.getMeetupRoomById(id).deadlineDateIso = LocalDate.now().minusDays(1).toString();
        try (ActivityScenario<ParticipantListActivity> scenario = ActivityScenario.launch(intent(ParticipantListActivity.class, id))) {
            onView(withId(R.id.btnAddParticipant)).check(matches(not(isEnabled())));
            onView(withContentDescription(LocalMeetupRepository.getCurrentUser().name + " 로컬 응답 입력"))
                    .check(matches(not(isEnabled())));
        }
    }

    @Test public void creationAndBackNavigationUpdateOneRoomInsteadOfDuplicatingIt() {
        int before = LocalMeetupRepository.getMyMeetupRooms().size();
        String id;
        LocalDate date = LocalDate.now().plusDays(2);
        try (ActivityScenario<CreateMeetupActivity> scenario = ActivityScenario.launch(CreateMeetupActivity.class)) {
            onView(withId(R.id.edtMeetupName)).perform(scrollTo(), replaceText("직접 생성"), closeSoftKeyboard());
            onView(withId(R.id.btnNext)).perform(click());
            id = LocalMeetupRepository.getCurrentDraftRoomId();
            assertEquals(LocalDate.now().toString(), LocalMeetupRepository.getMeetupRoomById(id).deadlineDateIso);
            if (date.getMonth() != LocalDate.now().getMonth()) {
                onView(withId(R.id.btnCalendarNext)).perform(scrollTo(), click());
            }
            onView(allOf(withText(String.valueOf(date.getDayOfMonth())),
                    isDescendantOfA(withId(R.id.selectionCalendar)))).perform(scrollTo(), click());
            onView(withId(R.id.tvDateCount)).check(matches(withText(containsString("(1/10)"))));
            onView(withId(R.id.timeEvening)).perform(scrollTo(), click());
            onView(withId(R.id.tvDateCount)).check(matches(withText(containsString("(1/10)"))));
            onView(withId(R.id.btnNext)).perform(click());
            onView(withId(R.id.tvInviteRoomTitle)).check(matches(withText("직접 생성")));
            onView(withId(R.id.btnBack)).perform(scrollTo(), click());
            onView(withId(R.id.btnBack)).perform(click());
            onView(withId(R.id.edtMeetupName)).perform(scrollTo(), replaceText("이름 수정"), closeSoftKeyboard());
            onView(withId(R.id.btnNext)).perform(click());
            onView(withId(R.id.btnNext)).perform(click());
            onView(withId(R.id.tvInviteRoomTitle)).check(matches(withText("이름 수정")));
            assertEquals(id, LocalMeetupRepository.getCurrentDraftRoomId());
            assertEquals(before + 1, LocalMeetupRepository.getMyMeetupRooms().size());
        }
    }
    @Test public void chatCanSendWhileKeyboardIsOpen() {
        String id = room(2);
        try (ActivityScenario<ChatDetailActivity> scenario = ActivityScenario.launch(intent(ChatDetailActivity.class, id))) {
            onView(withId(R.id.etMessageInput)).perform(click(), replaceText("KeyboardCheck"));
            onView(withId(R.id.btnSendMessage)).check(matches(isDisplayed())).perform(click());
            assertEquals("KeyboardCheck", LocalMeetupRepository.getChatMessagesByRoomId(id).get(0).message);
        }
    }

    @Test public void myPageShowsActualRoomStatistics() {
        String id = room(2);
        LocalMeetupRepository.saveHostSelection(id,
                Collections.singletonList(LocalDate.now().plusDays(3).toString()),
                Collections.singletonList("AFTERNOON"), Collections.emptyList());
        LocalMeetupRepository.saveResponse(id, LocalMeetupRepository.getCurrentUser().name,
                Collections.singletonList(LocalDate.now().plusDays(3).toString()),
                Collections.singletonList("AFTERNOON"), Collections.emptyList());
        LocalMeetupRepository.confirmSchedule(id, LocalDate.now().plusDays(3).toString(),
                "AFTERNOON", LocalMeetupRepository.getCurrentUser().name);
        com.hazyala.pickday.kopo.ac.kr.model.UserRoomStats stats = LocalMeetupRepository.getUserRoomStats();
        try (ActivityScenario<MyPageActivity> scenario = ActivityScenario.launch(MyPageActivity.class)) {
            onView(withId(R.id.tvTotalRoomsCount)).check(matches(withText(stats.totalRooms + "개")));
            onView(withId(R.id.tvConfirmedRoomsCount)).check(matches(withText(stats.confirmedRooms + "개")));
        }
    }
}
