# main 구현 상태

Home은 `DummyDataSource`의 고정 데이터를 읽는다. Login은 Home으로 Intent를 보내고 사용자 인증을 하지 않는다. 방 생성·응답 선택·상세 화면의 UI는 연결되어 있지만 이를 영구 저장하는 Repository나 서버가 없다. InviteMembers의 링크는 `https://pickday.app/room/Abc123` 고정 문자열이다. 실제 서비스 주소나 동작하는 초대 endpoint로 보지 않는다.

`dev`의 달력·채팅·알림·roomId 연결은 이 브랜치에 구현된 것으로 소개하지 않는다.

## 설계 문서와 코드의 관계

`api-contract-draft.md`의 OAuth/JWT/rooms API, `data-model.md`의 Repository interface·Retrofit·저장 모델은 목표 또는 계약 초안이다. 현재 Java 구현은 `data/DummyDataSource.java`와 각 Activity를 기준으로 읽는다. 계획 문서의 통계·달력 완료 기준도 구현 보증이 아니다.

| 항목 | 현재 경계 |
|---|---|
| 로그인 | 로컬 화면 흐름, 서버 인증 없음 |
| 데이터 | 더미 객체 및 UI 상태, 영구 DB 없음 |
| 초대 | 복사·공유 화면, 서버 참여 처리 없음 |
| 응답 | 선택 UI와 화면 이동, 백엔드 제출 없음 |
| 통신 | REST/WebSocket client와 server 없음 |

## 확인할 파일

[DummyDataSource](../app/src/main/java/com/hazyala/pickday/kopo/ac/kr/data/DummyDataSource.java) · [Manifest](../app/src/main/AndroidManifest.xml) · [Gradle 설정](../app/build.gradle.kts)
