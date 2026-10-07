# 코드 구조와 문자열 관리

## 패키지와 책임

기본 패키지는 `com.hazyala.pickday.kopo.ac.kr`입니다.

| 위치 | 책임 |
| --- | --- |
| `*Activity.java` | 입력 처리, 화면 이동, 모델과 리소스 문구 표시 |
| `model/` | 방·응답·일정·메시지·알림 등 독립 모델 13개 |
| `data/LocalMeetupRepository.java` | 조회, 입력 검증, 방·응답·확정·메시지 변경 |
| `data/MeetupMemoryStore.java` | 방, 응답, 작성 중 선택, 메시지, 알림 컬렉션 보관 |
| `data/SampleMeetupData.java` | 실행 날짜 기준 기본 방·사용자·응답·채팅 샘플 생성 |
| `data/ScheduleCalculator.java` | 날짜·시간 집계와 추천 조합 정렬 |
| `ui/PickDayDatePicker.java` | 공통 달력 표시와 날짜 선택 |
| `res/values/strings.xml` | Activity·레이아웃 문구와 동적 표시 서식 |

Activity → `LocalMeetupRepository` → `MeetupMemoryStore` 순으로 상태에 접근합니다. 저장소는 초기화와 첫 조회 시 `SampleMeetupData`에서 샘플을 생성합니다. 추천 계산은 `ScheduleCalculator`에 전달합니다. 모델은 Activity나 Android Context를 참조하지 않습니다.

`MeetupMemoryStore`와 `SampleMeetupData`는 데이터 패키지 안에서만 사용합니다. 화면은 샘플 생성이나 메모리 컬렉션을 직접 호출하지 않습니다.

## 메모리 수명

`LocalMeetupRepository`는 프로세스가 공유하는 정적 메모리 저장소를 사용합니다. 화면 이동과 재진입 시 상태가 유지되며, 프로세스가 종료되면 생성 데이터는 사라집니다. 저장소 인터페이스, 영구 저장과 네트워크 구현체는 없습니다.

공개 모델 필드는 기존 입력·계산 흐름을 유지합니다. 반환 목록은 목록 자체의 복사본이며, 내부 모델까지 모두 불변인 구조는 아닙니다. 네트워크 저장소를 추가할 때는 인스턴스 기반 인터페이스와 모델 변경 경계를 함께 정리합니다.

## 공통 화면과 통계

`ui/PickDayActivity`는 시스템 바·화면 잘림 영역·키보드 여백을 처리합니다. 주요 화면은 이 클래스를 상속합니다. 화면 테마는 라이트 테마로 고정합니다.

`UserRoomStats`는 현재 사용자가 참여한 방을 기준으로 진행 중인 방, 응답한 방, 확정된 방과 전체 방 수를 전달합니다. 진행 중인 방은 미확정·미마감 상태이며, 응답 완료는 본인의 제출 여부로 계산합니다.

## 화면 문자열

- XML의 `android:text`, `android:hint`, `android:contentDescription`은 `@string/...`을 사용합니다.
- Activity의 고정 문구는 `getString(R.string...)`으로 읽습니다.
- 인원·응답률·마감·선택 개수는 인자를 받는 서식 문자열로 표시합니다.
- 정수는 `%1$d`, 문자열은 `%1$s`처럼 순서를 명시합니다. 서식 안의 퍼센트 기호는 `%%`로 표시합니다.
- 공통 취소·추가·확정 동작과 인원 표시 문구는 여러 화면에서 공유합니다.
- 줄바꿈과 선택 기호, 초대 공유 문구와 접근성 설명도 리소스에서 관리합니다.

예시:

```java
tvParticipantCount.setText(getString(
        R.string.room_participants_capacity,
        room.participantCount,
        room.maxParticipants));
```

샘플의 이름·방 제목·대화 내용은 `SampleMeetupData`의 데이터입니다. 저장소가 만드는 검증 오류와 일부 집계 표시값, 시간대 이름은 아직 Java에 있습니다. 리소스 이동은 Activity와 레이아웃의 직접 작성 문구를 대상으로 하며 다국어 지원 전체를 의미하지 않습니다. 다국어 지원 시에는 오류 코드와 화면 표시 변환 계층, 날짜·시간 입력 형식도 함께 정리합니다.

마감 상태 판단은 화면 문구 비교 대신 `LocalMeetupRepository.isDeadlinePassed()`를 사용합니다.

## 검증

기존 단위 테스트는 분리된 저장소와 모델의 방 생성·응답·계산·확정 규칙을 확인합니다. 기기 테스트는 응답 제출, 확정, 회전 복원, 마감 제한과 뒤로가기 흐름을 확인합니다.

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
```

lint의 문자열 하드코딩, 표시 문자열 연결, 서식 인자 불일치를 점검합니다. 화면 문구가 바뀌면 줄바꿈과 동적 값의 실제 표시도 확인합니다.
