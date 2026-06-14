# 데이터 모델

이 문서는 백엔드 연동 전 Android 앱에서 사용할 로컬 모델 방향을 정의합니다.

앱은 Activity 내부에 직접 박힌 더미 문자열에서 벗어나 Repository 기반 데이터 구조로 이동해야 합니다. 첫 구현은 Java model class와 메모리/SharedPreferences/로컬 JSON 저장으로 시작할 수 있고, 이후 Retrofit DTO 매핑으로 교체합니다.

현재 UI는 유지하되 기능 데이터만 실제 구조로 바꿉니다. 예를 들어 달력의 `2025년 5월`, 방 상세의 참여자 수, 응답률, 시간 선호도, Home의 BEST 날짜는 하드코딩하지 않고 모델과 계산 결과에서 렌더링합니다.

## Calendar 기준

공통 달력 include(`view_pickday_calendar.xml`)는 화면 크기와 색상 톤을 유지하고, Java 렌더러에서 실제 월 데이터를 계산합니다.

- 달력은 6주 x 7일, 총 42칸을 기준으로 렌더링합니다.
- 현재 월은 기기 날짜 기준 `Calendar.getInstance()`에서 시작합니다.
- 이전/다음 월 이동은 표시 월을 1개월씩 변경하고 시작 요일과 말일을 다시 계산합니다.
- 선택 날짜는 `yyyy-MM-dd` 저장값과 `yyyy.MM.dd`, `M.d`, 요일 표시값으로 변환할 수 있어야 합니다.
- 비활성 날짜 기준은 화면별로 주입합니다. 마감일/방장 후보 날짜는 오늘 이전을 비활성화하고, 참여자 응답은 방장이 고른 후보 날짜 밖을 비활성화합니다.

## 핵심 모델

### User

- `id`
- `displayName`
- `profileImageUrl`
- `isGuest`

프로필 placeholder는 이모지가 아니라 이니셜 또는 중립 UI를 사용합니다.

### MeetupRoom

- `id`
- `title`
- `description`
- `hostUserId`
- `deadlineDateTime`
- `minParticipants`
- `maxParticipants`
- `visibility`
- `notifyOnJoin`
- `status`
- `inviteCode`
- `createdAt`
- `confirmedScheduleId`

권장 상태:

- `DRAFT`
- `INVITING`
- `COLLECTING_RESPONSES`
- `READY_TO_CONFIRM`
- `CONFIRMED`
- `CLOSED`

### Participant

- `id`
- `roomId`
- `userId`
- `displayName`
- `role`
- `responseStatus`
- `joinedAt`
- `respondedAt`

권장 역할:

- `HOST`
- `MEMBER`

권장 응답 상태:

- `NOT_STARTED`
- `IN_PROGRESS`
- `SUBMITTED`

### CandidateDate

- `id`
- `roomId`
- `date`
- `label`
- `isHostPreferred`

### TimeSlot

- `id`
- `code`
- `label`
- `startTime`
- `endTime`

초기 시간대 code:

- `MORNING`
- `AFTERNOON`
- `LATE_AFTERNOON`
- `EVENING`
- `LATE_EVENING`
- `ANYTIME`

### AvailabilityResponse

- `id`
- `roomId`
- `participantId`
- `availableDateIds`
- `preferredDateIds`
- `selectedTimeSlotCodes`
- `excludedDateIds`
- `submittedAt`

### ScheduleResult

- `roomId`
- `dateCandidates`
- `timeCandidates`
- `computedAt`

### DateCandidateResult

- `date`
- `availableParticipantCount`
- `preferredParticipantCount`
- `score`
- `isFullIntersection`
- `participantIds`

날짜 점수 규칙:

- 가능한 날짜: 1점
- 희망 날짜: 추가 1점
- 전원 교집합: 일반 점수 정렬보다 먼저 우선 후보로 표시

### TimeCandidateResult

- `slotCode`
- `participantCount`
- `score`

### ConfirmedSchedule

- `id`
- `roomId`
- `date`
- `timeSlotCode`
- `confirmedByUserId`
- `confirmedAt`

### NotificationItem

- `id`
- `roomId`
- `type`
- `title`
- `message`
- `isRead`
- `createdAt`

초기 type:

- `ROOM_JOINED`
- `RESPONSE_SUBMITTED`
- `DEADLINE_SOON`
- `SCHEDULE_CONFIRMED`

현재 Android 더미 데이터는 화면 렌더링용으로 `section`, `title`, `roomTitle`, `message`, `time`, `accentColor`만 사용합니다. `id`, `roomId`, `type`, `isRead`, `createdAt`은 Repository/API 연동 시 정식 모델로 확장합니다.

### UserSettings

- `userId`
- `notifyOnJoin`
- `notifyOnDeadline`
- `notifyOnConfirmed`

### CalendarViewState

- `visibleYearMonth`
- `minSelectableDate`
- `maxSelectableDate`
- `candidateDates`
- `selectedDates`
- `excludedDates`

달력 렌더링 규칙:

- 특정 월을 XML에 고정하지 않습니다.
- 현재 월, 사용자가 이동한 월, 방 후보 날짜 범위를 기준으로 날짜 셀을 만듭니다.
- 선택 가능/선택됨/제외/비활성 상태는 `CalendarViewState`에서 계산합니다.

## Repository 방향

실제 API 연동 전 Repository interface를 먼저 정의합니다.

- `UserRepository`
- `MeetupRepository`
- `InviteRepository`
- `ResponseRepository`
- `ScheduleRepository`
- `NotificationRepository`

초기 구현:

- In-memory Repository 또는 로컬 JSON/SharedPreferences

백엔드 구현:

- Retrofit Repository에서 API DTO를 앱 모델로 매핑

## 화면 렌더링 규칙

화면은 하드코딩된 문자열이 아니라 모델 상태를 기반으로 렌더링합니다.

통계 렌더링:

- 참여 완료율 = 제출 완료 참여자 수 / 전체 참여자 수
- 미응답 인원 = 전체 참여자 수 - 제출 완료 참여자 수
- BEST 날짜 = `ScheduleResult.dateCandidates`의 우선순위 1위
- 시간 선호도 = `AvailabilityResponse.selectedTimeSlotCodes` 집계 결과
- 마감 D-day = 현재 날짜/시간과 `MeetupRoom.deadlineDateTime` 차이

허용:

- Repository 계층의 fixture 데이터
- 명확히 demo/fixture로 분리된 샘플 데이터

지양:

- Activity 내부의 임시 더미 문자열
- 모델 값으로 사용하는 이모지
- 장식용 이모지를 선택하기 위한 방 카테고리 문자열
