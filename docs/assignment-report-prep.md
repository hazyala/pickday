# PickDay 과제 구현 정리 — dev

단체 약속의 날짜·시간·장소 조율을 주제로 Java Activity와 XML layout을 작성했다. 앱 진입, 방 만들기, 후보 선택, 초대, 방 상세와 참여자 응답 화면을 구성했다.

## 화면과 데이터 전달

`CreateMeetupActivity`가 입력한 방을 `DummyDataSource`의 static collection에 추가하고 생성된 `roomId`를 `SelectionActivity`에 넘긴다. 후보 날짜를 선택하면 해당 방의 날짜 목록을 갱신하고 `InviteMembersActivity`에 같은 ID를 전달한다. Home·Calendar·RoomDetail·참여자 목록·채팅 상세가 이 ID로 같은 방을 조회한다.

`RoomDetailActivity`는 `AvailabilityResponse` 샘플 응답을 집계해 통계를 표시한다. `ResponseSelectionActivity`는 후보 안에서 날짜·시간을 선택하고 상세로 돌아가며, 입력을 응답 저장소에 제출하는 경로는 없다. 채팅·알림은 샘플 목록이고 새로 만든 방과 날짜는 앱 process 종료 시 사라진다.

Activity 간 이동은 Intent를 사용한다. Android 공유는 `ACTION_SEND`로 초대 문구를 전달한다. 화면 layout·버튼·카드·칩은 `res/layout`과 drawable XML에 있고 달력 입력은 `PickDayDatePicker`가 담당한다.

## 과제 요구와 구현 근거

| 요구 | 소스에서 볼 부분 |
|---|---|
| Activity 3개 이상 | Splash, Login, Home, CreateMeetup, Selection, InviteMembers, RoomDetail, ResponseSelection |
| Activity 간 데이터 전송 | Home → RoomDetail의 Intent extras, InviteMembers의 공유 Intent |
| 화면 구성 설명 | [화면 흐름](screen-flow.md), `app/src/main/res/layout/` |
| 코드와 설계 링크 | [GitHub](https://github.com/hazyala/pickday), [Figma 디자인](https://www.figma.com/design/kdHvPDj4Ac85weMBIb6tyF/%ED%94%BD-%EB%8D%B0%EC%9D%B4--PickDay-?node-id=3-467&t=sBN9DyDpKuoZLOhA-1) |

## 제출 자료 구성

과제 안내의 제출기한은 6월 25일 오후 11:59, 형식은 PDF였다. 표지는 제목·학과·학년·성명을 포함하고 제출일은 표시하지 않는 조건이었다. 보고서는 주제·배경, 기술, Activity 흐름, Figma storyboard, 실행 화면, 코드 링크 순서로 구성한다. 실행 캡처는 에뮬레이터 또는 실제 기기 화면을 사용하고 Figma 프레임과 구분한다.

서버 API·OAuth·push는 현재 앱에 연결되어 있지 않다. 화면별 데이터와 저장 범위는 [현재 구현](CURRENT_IMPLEMENTATION.md), 실행은 [README](../README.md)에 정리했다.
