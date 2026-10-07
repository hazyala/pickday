# API 계약 초안

백엔드 연결을 위한 설계안입니다. 아래 API와 네트워크 저장소는 앱에 구현되어 있지 않습니다. 현재 앱은 `LocalMeetupRepository`를 통해 메모리 데이터를 사용합니다.

## 공통 규칙

- 서버는 사용자·방·참여자에 안정적인 ID를 부여합니다.
- 날짜는 `yyyy-MM-dd`, 시각은 시간대가 포함된 ISO 8601 형식을 사용합니다.
- 인증 방식과 토큰 갱신 정책은 서버 설계 시 확정합니다.
- 상태 변경 권한, 정원, 마감과 후보 유효성은 서버에서도 검사합니다.
- 동일 참여자의 응답 제출은 기존 응답을 교체합니다.

## 엔드포인트

| 메서드와 경로 | 용도 | 주요 데이터 |
| --- | --- | --- |
| `POST /api/rooms` | 방 생성 | 제목, 설명, 정원, 응답 마감, 참여 알림 설정 |
| `GET /api/rooms` | 내 방 목록 | 상태 필터, 방 요약 |
| `GET /api/rooms/{roomId}` | 방 조회 | 방장, 후보, 참여자, 내 응답, 확정 일정 |
| `PUT /api/rooms/{roomId}/candidates` | 방장 후보 설정 | 후보 날짜, 허용 시간 코드, 제외 날짜 |
| `POST /api/invites/{inviteCode}/join` | 초대 참여 | 방 ID, 참여자 ID, 응답 상태 |
| `PUT /api/rooms/{roomId}/responses/me` | 응답 제출·수정 | 가능 날짜, 시간 코드, 제외 날짜 |
| `GET /api/rooms/{roomId}/result` | 계산 결과 | 날짜·시간 조합별 인원과 순위, 계산 시각 |
| `POST /api/rooms/{roomId}/confirm` | 일정 확정 | 날짜, 시간 코드 |
| `POST /api/rooms/{roomId}/reminders` | 응답 리마인더 | 대상과 발송 결과 |
| `GET /api/notifications` | 알림 목록 | 방 ID, 종류, 문구, 읽음 여부, 생성 시각 |
| `GET /api/me` | 사용자 정보 | 사용자 ID, 표시 이름, 프로필, 설정 |

리마인더, 알림 읽음 처리, 사용자 설정은 로컬 앱에서도 미지원 기능입니다. API의 권한과 상세 응답 형식은 서버 구현 전에 정의합니다.

## 방 생성 예시

```json
{
  "title": "동아리 모임",
  "description": "모두 가능한 날짜를 골라주세요",
  "deadlineDateTime": "2026-11-20T23:59:00+09:00",
  "maxParticipants": 20,
  "notifyOnJoin": true
}
```

응답 예시:

```json
{
  "id": "room_123",
  "inviteCode": "Abc123",
  "inviteUrl": "https://pickday.app/room/Abc123",
  "status": "DRAFT"
}
```

초대 URL은 설계 예시입니다. 현재 앱에는 해당 URL을 통한 참여 서비스가 없습니다.

## 후보 설정 예시

```json
{
  "candidateDates": ["2026-11-21", "2026-11-22"],
  "allowedTimeSlotCodes": ["AFTERNOON", "EVENING"],
  "excludedDates": []
}
```

후보 날짜는 응답 마감일 다음날부터 1~10개로 제한합니다. 제출된 응답이 있으면 후보 변경을 제한합니다.

## 응답 제출 예시

```json
{
  "availableDates": ["2026-11-21"],
  "selectedTimeSlotCodes": ["AFTERNOON"],
  "excludedDates": ["2026-11-22"]
}
```

`ANYTIME`은 허용된 모든 시간대를 뜻하며 개별 코드와 함께 사용하지 않습니다. 모든 후보를 제외한 경우 가능 날짜와 시간 목록은 비어 있을 수 있습니다. 희망 날짜 가산점은 현재 계산 규칙에 없습니다.

## 일정 결과와 확정

계산 결과는 같은 날짜·시간의 가능 인원, 날짜 가능 인원, 이른 날짜·시간 순으로 정렬합니다. 과거이거나 시작 시간이 지난 조합은 확정 후보에서 제외합니다. 날짜별 가능 인원과 조합별 가능 인원을 응답 필드에서 구분합니다.

확정 요청 예시:

```json
{
  "date": "2026-11-21",
  "timeSlotCode": "AFTERNOON"
}
```

서버는 방장 권한과 실제 가능 응답을 검사합니다. 확정 후에는 참여자 추가와 응답 수정을 차단합니다. 미응답자가 있는 확정의 확인 절차는 앱과 서버에서 같은 규칙을 사용합니다.

## 오류 형식

```json
{
  "code": "ROOM_NOT_FOUND",
  "message": "약속 방을 찾을 수 없습니다.",
  "details": {}
}
```

권한 부족, 정원 초과, 응답 마감, 후보 변경 제한, 이미 확정된 방을 구분할 오류 코드를 정의합니다.

## Android 연결 구조

Activity → 저장소 인터페이스 → 메모리 또는 API 구현체 순으로 데이터를 조회·변경합니다. API 요청·응답 모델은 화면 모델과 분리하고 변환 계층을 둡니다. 네트워크 대기·실패·재시도와 인증 만료 상태를 화면에 전달합니다.

현재 Activity는 `LocalMeetupRepository`를 호출합니다. 저장소 인터페이스 분리와 Retrofit 연결은 [로드맵](roadmap.md)의 후속 작업입니다.
