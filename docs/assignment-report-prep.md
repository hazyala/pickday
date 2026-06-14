# 과제 보고서 준비 문서

이 문서는 PickDay 최종 보고서를 PDF로 작성하기 전에 필요한 내용, 캡처 자료, 링크, 구현 근거를 모아두기 위한 작업 문서입니다. 최종 제출물은 이 문서를 그대로 제출하는 것이 아니라, 여기의 내용을 바탕으로 PDF 보고서로 정리합니다.

## 과제 조건

- 주제: 자유 주제
- 제출기한: 6월 25일 목요일 오후 11:59
- 사용언어: Android 프로그래밍
- 필수 구현 조건:
  - Activity 3개 이상 사용
  - Activity 간 데이터 전송
- 제출방법: PDF 보고서
- 표지 필수 항목:
  - 주제 관련 제목
  - 학과명
  - 학년
  - 성명
- 표지 금지 항목:
  - 제출일 표시 금지
- 링크 필수:
  - Figma 주소 public
  - GitHub Repository 주소 public

## 표지 초안

제목:

- PickDay
- 여러 사람의 약속 일정을 쉽고 예쁘게 조율하는 Android 일정 관리 앱

표지 정보:

- 학과명: 작성 필요
- 학년: 작성 필요
- 성명: 김해민

주의:

- 표지에는 제출일을 넣지 않습니다.
- 학번, 과목명, 담당 교수명은 이번 과제 요구사항에 없으므로 교수님 안내가 추가로 없으면 표지에서 제외합니다.

## 보고서 목차 초안

1. 주제
2. 주제 선택 배경
3. 사용된 주요 기술
4. 앱 구조와 Activity 흐름
5. 스토리보드
6. Figma 전체 UI 캡처
7. 완성된 Activity 실행 캡처
8. GitHub/Figma 링크
9. 구현 결과와 향후 개선 방향

## 1. 주제

PickDay는 단체 약속의 날짜, 시간, 장소를 쉽게 조율하기 위한 Android 앱입니다. 사용자는 약속 방을 만들고, 후보 날짜와 시간대를 선택하고, 초대 흐름을 통해 참여자의 응답을 모아 가장 적합한 일정을 확인할 수 있습니다.

보고서에서는 아래 표현을 중심으로 정리합니다.

- 여러 사람이 가능한 날짜와 시간을 한눈에 확인하는 일정 조율 앱
- 메신저 대화에 흩어지는 약속 정보를 앱 화면에서 구조화하는 서비스
- 파스텔 퍼플 기반의 부드러운 카드형 UI를 가진 Android 앱

## 2. 주제 선택 배경

작성 방향:

- 단체 약속은 주로 메신저에서 조율되지만, 가능 날짜와 시간을 정리하기 어렵습니다.
- 참여 인원이 늘어나면 메시지가 누적되고, 누가 언제 가능한지 한눈에 보기 어렵습니다.
- PickDay는 날짜, 시간, 장소 선호도를 앱에서 입력하고 결과를 시각적으로 확인할 수 있게 하기 위해 기획했습니다.
- 중간고사 제출물에서는 UI/UX와 더미 데이터 기반 흐름이 중심이었고, 이번 작업에서는 실제 서비스에 가까운 화면 구조와 데이터 기반 렌더링 방향을 강화합니다.
- 현재 UI는 유지하되, 고정 5월 달력과 임시 통계처럼 실제 사용에서 깨지는 부분을 로컬 데이터와 계산 결과 기반 기능으로 개선하는 것이 목표입니다.

## 3. 사용된 주요 기술

Android:

- Java
- XML Layout
- Activity 기반 화면 전환
- Intent를 이용한 Activity 이동 및 데이터 전달
- Drawable XML 기반 카드, 버튼, 칩 스타일
- ScrollView, LinearLayout, HorizontalScrollView를 활용한 화면 구성

현재 코드 기준 Activity:

- SplashActivity
- LoginActivity
- HomeActivity
- CreateMeetupActivity
- SelectionActivity
- InviteMembersActivity
- RoomDetailActivity
- ResponseSelectionActivity
- NotificationActivity

Activity 간 데이터 전송 근거:

- HomeActivity에서 RoomDetailActivity로 `roomId` 전달
- InviteMembersActivity에서 Android share Intent로 초대 문구 전달
- 이후 보고서 제출 전 Create Meetup, Selection, Invite 흐름에서도 생성 방 정보 또는 선택 정보를 명시적으로 전달하는 근거를 추가하면 좋습니다.
- 달력, 응답률, BEST 날짜, 시간 선호도는 최종적으로 Activity 하드코딩이 아니라 Repository 데이터와 계산 결과를 표시한다고 설명합니다.

## 4. 앱 구조와 Activity 흐름

기본 흐름:

1. SplashActivity
2. LoginActivity
3. HomeActivity
4. CreateMeetupActivity
5. SelectionActivity
6. InviteMembersActivity
7. RoomDetailActivity
8. ResponseSelectionActivity

보고서 설명 방향:

- Splash/Login은 앱 진입과 시작 화면 역할을 합니다.
- Home은 진행 중인 약속, 이번주 약속 현황, 내 약속 방을 보여주는 메인 허브입니다.
- Create Meetup은 약속 방 생성을 시작하는 화면입니다.
- Selection은 방장이 후보 날짜와 시간을 선택하는 화면입니다.
- Invite Members는 초대 링크 또는 초대 코드를 공유하는 화면입니다.
- Room Detail은 방의 진행 현황과 추천 일정을 확인하는 화면입니다.
- Response Selection은 참여자가 가능한 날짜와 시간을 응답하는 화면입니다.

## 5. 스토리보드 작성 재료

스토리보드는 각 Activity별로 Figma UI 화면 옆에 설명을 적는 방식으로 구성합니다.

| Activity | Figma 화면 | 실행 캡처 | 화면 설명에 넣을 내용 |
| --- | --- | --- | --- |
| SplashActivity | 필요 | 필요 | 앱 시작 시 브랜드 로고와 캐릭터를 보여주는 진입 화면 |
| LoginActivity | 필요 | 필요 | 사용자가 앱을 시작해 Home으로 이동하는 화면 |
| HomeActivity | 필요 | 필요 | 진행 중인 약속, 이번주 약속 현황, 내 약속 방을 보여주는 메인 화면 |
| CreateMeetupActivity | 필요 | 필요 | 방장이 새 약속 방 정보를 입력하는 화면 |
| SelectionActivity | 필요 | 필요 | 후보 날짜와 시간대를 선택하는 화면 |
| InviteMembersActivity | 필요 | 필요 | 초대 링크/코드를 공유하고 다음 흐름으로 이동하는 화면 |
| RoomDetailActivity | 필요 | 필요 | 방별 응답 현황과 추천 일정을 확인하는 상세 화면 |
| ResponseSelectionActivity | 필요 | 필요 | 참여자가 가능한 날짜와 시간대를 응답하는 화면 |
| NotificationActivity | 필요 | 필요 | Home 상단 알림 버튼에서 진입해 최근 로컬 알림 목록을 확인하는 화면 |

## 6. Figma 전체 UI 캡처 준비

필요 자료:

- Figma 전체 프레임이 보이는 캡처 1장
- 각 Activity별 주요 화면 캡처
- public 접근 가능한 Figma 링크

중간고사 보고서에 사용한 Figma 링크:

- https://www.figma.com/design/kdHvPDj4Ac85weMBIb6tyF/%ED%94%BD-%EB%8D%B0%EC%9D%B4--PickDay-?node-id=3-467&t=sBN9DyDpKuoZLOhA-1

제출 전 확인:

- 링크가 public인지 확인합니다.
- 최신 화면이 반영되어 있는지 확인합니다.
- 보고서에는 목업을 그대로 베꼈다는 표현보다 실제 앱 화면으로 정리했다는 방향을 씁니다.

## 7. 완성된 Activity 실행 캡처 준비

필요 캡처:

- Splash 실행 화면
- Login 실행 화면
- Home 실행 화면
- Create Meetup 실행 화면
- Selection 실행 화면
- Invite Members 실행 화면
- Room Detail 실행 화면
- Response Selection 실행 화면

캡처 기준:

- Android Studio 에뮬레이터 또는 실제 기기에서 실행한 화면을 사용합니다.
- 상태바/내비게이션바가 보여도 괜찮지만, 화면 내용이 잘리지 않아야 합니다.
- Figma 화면과 실행 화면을 나란히 배치하면 스토리보드 설명이 쉬워집니다.

## 8. GitHub 링크 준비

필수 조건:

- GitHub Repository public
- README와 docs가 최신 구현 방향을 설명해야 함
- 최종 제출 전 빌드 가능 상태 확인

현재 확인할 항목:

- Repository public 여부
- 최종 제출 브랜치 또는 PR 상태
- README의 프로젝트 설명과 실제 앱 기능 일치 여부

## 9. 제출 전 구현 체크리스트

필수 조건:

- [ ] Activity 3개 이상 사용 근거 정리
- [ ] Activity 간 데이터 전송 코드 근거 정리
- [ ] Figma public 링크 확인
- [ ] GitHub Repository public 확인
- [ ] 전체 Activity 실행 캡처 준비
- [ ] Figma 전체 UI 캡처 준비
- [ ] 표지에 제출일이 없는지 확인
- [ ] PDF로 변환 후 레이아웃 깨짐 확인

앱 완성도 체크:

- [ ] Home이 신규/빈 데이터 상태에서 기존 방, BEST, 응답률을 임의 표시하지 않음
- [ ] 방 목록은 장식 이모지 없이 제목, 상태, 참여자 수, 마감, 응답률 중심으로 표시
- [ ] 달력이 2025년 5월 고정이 아니라 현재 월/선택 월/방 후보 날짜 기준으로 동작함
- [ ] Room Detail 통계가 실제 참여자 응답 데이터로 계산됨
- [ ] Create Meetup, Invite, Room Detail, Response Selection의 화면 흐름이 끊기지 않음
- [ ] Home 상단 알림 버튼에서 알림 목록 화면으로 이동함
- [ ] 하단 내비게이션의 캘린더 탭은 월간 일정 화면으로 이동함
- [ ] 하단 내비게이션의 방 채팅 탭은 알림 화면과 분리된 채팅 방 목록으로 이동함
- [ ] 주요 화면이 현재 UI 가이드라인과 일관됨
- [ ] `./gradlew assembleDebug` 통과

보고서 작성 체크:

- [ ] 주제 선택 배경이 문제 상황과 앱 해결 방향을 함께 설명함
- [ ] 사용 기술에 Java, XML, Activity, Intent, Drawable XML을 포함함
- [ ] 스토리보드는 Activity별 Figma 화면과 실행 화면을 나란히 배치함
- [ ] 미구현 또는 추후 개선 기능을 구현된 것처럼 과장하지 않음
- [ ] 더미 데이터는 개발 확인용이고 실제 서비스에서는 Repository/API 데이터 기준으로 렌더링한다고 설명함

## 향후 개선 방향 문장 후보

보고서 마지막에는 아래 방향을 짧게 정리할 수 있습니다.

- 현재는 Android Activity와 로컬 데이터 기반으로 핵심 화면 흐름을 구현했습니다.
- 다음 단계에서는 현재 UI를 유지하면서 달력, 방 데이터, 응답 저장, 일정 계산을 실제 로컬 Repository 기반 기능으로 교체할 예정입니다.
- 이후에는 Repository 계층과 Retrofit API를 연결하여 실제 사용자별 방 생성, 응답 저장, 일정 계산을 서버와 동기화할 예정입니다.
- 알림 목록은 Home 상단 알림 버튼에서 진입하는 로컬 데이터 기반 화면으로 제공하고, 하단 내비게이션의 방 채팅 탭은 채팅 방 목록 진입점으로 사용합니다. Google/Kakao 로그인, 푸시 알림, 캘린더 Provider 연동은 실제 서비스 확장 단계에서 붙일 기능입니다.
