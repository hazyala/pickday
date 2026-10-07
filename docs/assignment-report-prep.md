# 과제 보고서 준비

## 과제 조건

- 주제: 자유 주제
- 제출기한: 6월 25일 목요일 오후 11:59
- 사용언어: Android 프로그래밍
- 필수 구현: Activity 3개 이상, Activity 간 데이터 전송
- 제출 형식: PDF 보고서
- 표지 필수 항목: 주제 관련 제목, 학과명, 학년, 성명
- 표지 제외 항목: 제출일
- 필수 링크: 공개 Figma 파일, 공개 GitHub 저장소

학과명과 학년은 작성 시 채웁니다. 성명은 김해민입니다. 제출 조건과 기한은 과제 안내를 기준으로 확인합니다.

## 제목과 목차

제목: **PickDay — 여러 사람의 약속 일정을 조율하는 Android 앱**

1. 프로젝트 주제와 기획 배경
2. 사용 기술
3. 데이터 구조와 일정 계산
4. Activity 흐름과 데이터 전달
5. 스토리보드와 Figma UI
6. 앱 실행 화면
7. 테스트 결과
8. GitHub·Figma 링크
9. 지원 범위와 개선 계획

과제 안내의 제출기한은 6월 25일 오후 11:59, 제출 형식은 PDF였습니다. 표지는 제목·학과·학년·성명을 포함하고 제출일은 표시하지 않는 조건입니다.

## 프로젝트 설명

단체 약속을 메신저에서 정하면 참여자의 가능 날짜와 시간이 여러 메시지에 흩어집니다. PickDay는 방별로 응답을 모아 같은 날짜와 시간에 모일 수 있는 인원을 계산하고, 방장이 일정을 확정할 수 있도록 만든 앱입니다.

한 기기에서 방장과 참여자의 응답을 입력하는 로컬 데모입니다. 날짜·시간 조율, 일정 확정, 캘린더 표시를 지원합니다. 장소 추천과 실제 초대 참여는 지원하지 않습니다.

## 사용 기술과 구현 근거

| 기술 | 구현 근거 |
| --- | --- |
| Java·XML | Activity와 레이아웃 구현 |
| AndroidX·Material Components | 화면 전환, 컨트롤과 카드 구성 |
| Intent | 방 ID와 참여자 이름 전달, 초대 문구 공유 |
| 공통 달력 | `PickDayDatePicker`의 월 이동과 날짜 선택 |
| 메모리 데이터 | `LocalMeetupRepository`의 조회·변경과 `MeetupMemoryStore`의 상태 보관 |
| 책임 분리 | `model`의 독립 모델과 `SampleMeetupData`의 샘플 생성 |
| 문자열 리소스 | `strings.xml`의 고정 문구와 인원·마감 서식 |
| 일정 계산 | `ScheduleCalculator`의 날짜·시간별 집계와 추천 정렬 |
| JUnit·Espresso | 데이터 규칙과 화면 흐름 테스트 |

Repository 인터페이스, 서버 API, 데이터베이스는 구현되어 있지 않습니다. 보고서의 구조 설명에는 현재 코드의 책임 구분을 사용합니다.

## Activity 간 데이터 전달

- Home → Room Detail: `RoomDetailActivity.EXTRA_ROOM_ID`
- Create Meetup → Selection → Invite Members: 생성한 방의 `roomId`
- Participant List → Response Selection: 방 ID와 참여자 이름
- Chat → Chat Detail: `ChatDetailActivity.EXTRA_ROOM_ID`
- Invite Members → Android 공유 시트: `ACTION_SEND`의 초대 문구

코드 캡처에는 `putExtra`를 호출하는 부분과 전달값을 읽는 부분을 함께 넣습니다.

## 실행 캡처와 스토리보드

| 화면 | 설명할 내용 |
| --- | --- |
| Splash·Login·Signup | 브랜드 진입, 게스트 시작, 회원가입 미리보기 |
| Home·All Meetup Rooms | 대표 일정, 주간 현황, 전체 방 |
| Create Meetup | 제목·정원·마감 입력과 검증 |
| Selection | 후보 날짜·시간과 제외 날짜 |
| Invite Members | 문구 복사·공유, 참여·응답 현황 |
| Room Detail | 응답 집계, 추천 후보, 일정 확정 |
| Participant List·Response Selection | 참여자 추가, 응답 제출과 수정 |
| Calendar | 마감일과 확정 약속일 |
| Chat·Chat Detail | 방 목록과 메모리 메시지 전송 |
| Notifications·My Page | 로컬 이벤트, 샘플 요약, 게스트 현황 |

각 화면의 Figma 이미지와 앱 실행 캡처를 나란히 배치합니다. 응답 전·후와 확정 전·후를 함께 보여주면 상태 변화를 설명하기 쉽습니다. Figma 전체 프레임 캡처도 준비합니다.

## 링크

- [Figma 디자인](https://www.figma.com/design/kdHvPDj4Ac85weMBIb6tyF/%ED%94%BD-%EB%8D%B0%EC%9D%B4--PickDay-?node-id=3-467)
- [GitHub 저장소](https://github.com/hazyala/pickday): 제출할 브랜치·커밋 명시

제출 전 두 링크의 공개 접근 여부와 화면 최신 상태를 확인합니다.

## 테스트 자료

[수동 테스트 케이스](manual-test-cases.md)에 따라 핵심 흐름과 오류 사례를 확인합니다. 자동 테스트 결과는 제출한 코드에서 생성한 보고서를 첨부합니다.

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
```

단위 테스트는 데이터 검증·응답 교체·추천·확정 규칙을, 기기 테스트는 실제 화면 입력·이동·복원을 확인합니다. 기기명, Android 버전, 테스트 결과와 남은 lint 경고를 기록합니다.

## 지원 범위와 개선 계획

방·응답·채팅 데이터는 프로세스 실행 중에만 유지됩니다. 실제 인증, 다른 기기의 초대 참여, 네트워크 채팅과 푸시는 없습니다. 내 정보의 일부 메뉴는 준비 중 안내를 표시합니다.

개선 항목은 터치 영역과 글자 크기, 여러 화면 크기 검증, 저장소 인터페이스 분리, 인증·초대·동기화 연결입니다. 상세 계획은 [로드맵](roadmap.md)에 정리합니다.

## 제출 체크리스트

- [ ] Activity 3개 이상과 데이터 전달 코드 근거 포함
- [ ] 학과명·학년·성명 기입, 표지에 제출일 제외
- [ ] Figma 전체 화면과 주요 앱 실행 캡처 준비
- [ ] 공개 Figma·GitHub 링크 확인
- [ ] 제출 코드의 빌드와 테스트 결과 포함
- [ ] 샘플 데이터와 로컬 입력 데이터 구분
- [ ] 지원 기능과 향후 계획 구분
- [ ] PDF 변환 후 글꼴·이미지·표·링크 확인
