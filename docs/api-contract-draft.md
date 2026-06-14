# API 계약 초안

이 문서는 Android 앱이 로컬 동작을 완성한 뒤 백엔드를 붙일 때 사용할 API 초안입니다.

앱은 아래 API가 Local Repository를 대체할 수 있도록 설계합니다.

현재 dev 목표는 UI를 유지하고 기능 데이터만 실제 구조로 교체하는 것입니다. API도 기존 화면의 방 생성, 응답 저장, 달력/통계 계산, 알림/내 정보 표시를 뒷받침하는 범위로 먼저 맞춥니다.

## 인증

인증은 이번 dev 목표의 필수 구현 범위가 아닙니다.

계획:

- Google 로그인
- Kakao 로그인
- JWT access token
- Refresh token 처리

실제 인증 전에는 로컬 게스트 사용자를 사용할 수 있습니다.

## Endpoints

### 약속 방 생성

`POST /api/rooms`

Request:

```json
{
  "title": "동아리 MT",
  "description": "모두가 가능한 최고의 날을 찾아봐요",
  "deadlineDateTime": "2026-05-27T23:59:00+09:00",
  "minParticipants": 2,
  "maxParticipants": 20,
  "visibility": "INVITE_ONLY",
  "notifyOnJoin": true
}
```

Response:

```json
{
  "id": "room_123",
  "inviteCode": "Abc123",
  "inviteUrl": "https://pickday.app/room/Abc123",
  "status": "INVITING"
}
```

### 방 조회

`GET /api/rooms/{roomId}`

응답에 포함할 내용:

- 방 요약
- 방장 정보
- 참여자 요약
- 현재 사용자의 응답 상태
- 확정 일정이 있다면 확정 일정

### 내 방 목록

`GET /api/rooms`

Query 예시:

- `status=active`
- `status=confirmed`

### 초대 코드로 참여

`POST /api/invites/{inviteCode}/join`

응답에 포함할 내용:

- 방 ID
- 참여자 ID
- 현재 응답 상태

### 응답 제출

`PUT /api/rooms/{roomId}/responses/me`

Request:

```json
{
  "availableDateIds": ["date_1", "date_2"],
  "preferredDateIds": ["date_2"],
  "selectedTimeSlotCodes": ["AFTERNOON", "EVENING"],
  "excludedDateIds": ["date_8"]
}
```

### 일정 결과 조회

`GET /api/rooms/{roomId}/result`

응답에 포함할 내용:

- 날짜 후보
- 시간 후보
- 전원 교집합 후보
- 마지막 계산 시각

### 일정 확정

`POST /api/rooms/{roomId}/confirm`

Request:

```json
{
  "date": "2026-05-24",
  "timeSlotCode": "AFTERNOON"
}
```

### 리마인더 발송

`POST /api/rooms/{roomId}/reminders`

실제 push 알림 전까지는 stub으로 둘 수 있습니다.

### 알림 목록

`GET /api/notifications`

응답에 포함할 내용:

- 알림 ID
- 연결된 방 ID
- 알림 종류
- 제목과 보조 문구
- 읽음 여부
- 생성 시각

### 내 정보 조회

`GET /api/me`

응답에 포함할 내용:

- 사용자 ID
- 표시 이름
- 프로필 이미지 URL 또는 placeholder 정보
- 게스트 여부
- 기본 알림 설정

## 오류 형식

오류 응답은 하나의 예측 가능한 형식을 사용합니다.

```json
{
  "code": "ROOM_NOT_FOUND",
  "message": "Room was not found.",
  "details": {}
}
```

## Android 연동 규칙

Android Activity에서 Retrofit을 직접 호출하지 않습니다.

권장 구조:

- Activity/View 계층
- Repository interface
- Local Repository 또는 API Repository 구현체
- 필요 시 Model/DTO Mapper

이 구조를 유지하면 백엔드를 너무 일찍 강제하지 않으면서도 서버-ready 상태로 개발할 수 있습니다.
