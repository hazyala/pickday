# PickDay 개발 방향

단체 약속의 후보 날짜·시간·장소를 한 방에서 정리하는 앱을 만든다. Java Activity와 XML 화면을 유지하면서, 고정 달력과 화면 내부 샘플 값을 방 데이터·참여 응답에 연결하는 것이 개발 방향이다.

## 화면과 데이터 기준

- Home, Splash/Login, 방 만들기와 초대에는 PickDay 캐릭터 asset을 사용한다.
- 카드·버튼·칩은 파스텔 퍼플, 공통 곡률과 글꼴을 사용한다. 세부 기준은 [UI 가이드](ui-guidelines.md)에 있다.
- 방 이름·참여자·후보 날짜·마감일은 방 데이터를 통해 전달한다. 빈 목록은 빈 상태와 방 만들기 동작으로 표시하는 방향이다.
- 응답률·BEST 날짜·시간 선호도는 참여 응답에서 계산하는 것이 목표다. 현재 샘플 데이터와 저장 범위는 [현재 구현](CURRENT_IMPLEMENTATION.md)에 정리했다.
- 서버 API, Google/Kakao 로그인, push, 캘린더 Provider는 [API 계약 초안](api-contract-draft.md)과 [로드맵](roadmap.md)의 후속 작업이다.

## 브랜치·커밋 규칙

`main`은 MVP 스냅샷, `dev`는 통합 개발 브랜치다. 기능 브랜치는 `dev`에서 분기하고 PR로 `dev`에 병합한다. 브랜치 접두사는 `feat/`, `fix/`, `refactor/`, `docs/`, `style/`, `test/`, `chore/`를 사용한다.

커밋은 `type: 간단한 한국어 요약`으로 작성하며 동작·화면·데이터가 바뀌면 관련 문서를 함께 갱신한다. 주석은 한국어로 의도와 예외를 설명하고, 클래스·변수·함수 이름은 Java 관례를 따른다.

[개발 워크플로우](development-workflow.md) · [화면 흐름](screen-flow.md) · [과제 구현 정리](assignment-report-prep.md)
