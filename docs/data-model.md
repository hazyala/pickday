# 데이터 모델

이 문서는 백엔드 연동 전 Android 앱에서 사용할 로컬 모델 방향을 정의합니다.

앱은 Activity 내부에 직접 박힌 더미 문자열에서 벗어나 Repository 기반 데이터 구조로 이동해야 합니다. 첫 구현은 Java model class와 메모리/SharedPreferences/로컬 JSON 저장으로 시작할 수 있고, 이후 Retrofit DTO 매핑으로 교체합니다.

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
- `placeVotes`
- `submittedAt`

### PlaceVote

- `id`
- `roomId`
- `participantId`
- `name`
- `type`
- `normalizedName`

권장 type:

- `POSSIBLE`
- `PREFERRED`

### ScheduleResult

- `roomId`
- `dateCandidates`
- `timeCandidates`
- `placeCandidates`
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

### PlaceCandidateResult

- `name`
- `normalizedName`
- `possibleCount`
- `preferredCount`
- `score`

장소 점수 규칙:

- 가능한 장소: 1점
- 희망 장소: 2점
- 유사한 장소명은 추후 정규화 또는 병합합니다.

### ConfirmedSchedule

- `id`
- `roomId`
- `date`
- `timeSlotCode`
- `placeName`
- `confirmedByUserId`
- `confirmedAt`

## Repository 방향

실제 API 연동 전 Repository interface를 먼저 정의합니다.

- `UserRepository`
- `MeetupRepository`
- `InviteRepository`
- `ResponseRepository`
- `ScheduleRepository`

초기 구현:

- In-memory Repository 또는 로컬 JSON/SharedPreferences

백엔드 구현:

- Retrofit Repository에서 API DTO를 앱 모델로 매핑

## 화면 렌더링 규칙

화면은 하드코딩된 문자열이 아니라 모델 상태를 기반으로 렌더링합니다.

허용:

- Repository 계층의 fixture 데이터
- 명확히 demo/fixture로 분리된 샘플 데이터

지양:

- Activity 내부의 임시 더미 문자열
- 모델 값으로 사용하는 이모지
- 장식용 이모지를 선택하기 위한 방 카테고리 문자열
