# PickDay 개발 방향 기록

기존 README에 작성한 브랜치·커밋 규칙과 UI 방향을 보관한다. 목표·완료 기준을 설명한 문서이며 현재 구현은 [CURRENT_IMPLEMENTATION.md](CURRENT_IMPLEMENTATION.md)를 따른다.

# PickDay

단체 약속의 날짜·시간·장소 선택 화면을 연결한 Android MVP 스냅샷.

## 화면에서 확인할 수 있는 것

PickDay는 단체 약속을 잡을 때 날짜·시간·장소를 함께 정하기 위한 앱이다. 이 README는 `main`의 구현을 설명한다. `main`은 MVP 스냅샷, `dev`는 통합 개발 브랜치이며 서로 같은 기능 상태가 아니다.

- Splash → 로그인 화면 → Home으로 이동한다. 로그인 버튼은 화면 전환이며 인증 서버 호출은 없다.
- Home은 `DummyDataSource`의 사용자·약속·가능 날짜·방 목록을 렌더링한다.
- 방 만들기 → 날짜 선택 → 초대 화면, 방 상세 → 응답 선택 화면을 탐색한다.
- 초대 링크를 복사하거나 Android 공유 chooser로 전달한다. 링크 값은 고정 샘플이다.

현재 백엔드·OAuth·push·실시간 채팅·영구 DB 저장은 없다. [API 계약 초안](api-contract-draft.md)은 서버 구현 문서가 아니라 계획이다. 화면의 생성·응답·확정 동작과 실제 저장 여부는 [현재 구현](CURRENT_IMPLEMENTATION.md)에 구분했다.

## 앱 구성

Java Activity와 XML layout을 사용한다. AndroidX AppCompat·ConstraintLayout·Activity, Material Components가 화면을 구성하며 Compose나 Retrofit은 의존성에 없다.

```text
app/src/main/
├── java/com/hazyala/pickday/kopo/ac/kr/
│   ├── *Activity.java   화면과 이동·입력 처리
│   ├── data/           DummyDataSource와 내부 데이터 객체
│   └── ui/             PickDayDatePicker
└── res/                layout, drawable, 문자열·테마
docs/                   현재 상태, 설계 초안, 테스트·개발 규칙
```

화면 전환은 Intent로 연결한다. `DummyDataSource`는 서버 Repository가 아니라 앱 프로세스 안의 샘플 데이터다. 캐릭터 이미지와 화면 리소스는 `res/`에서 사용하며 실행 스크린샷으로 소개하지 않는다.

## Android Studio에서 실행

저장소 루트를 Gradle 프로젝트로 연다. `app/build.gradle.kts` 기준 minSdk 26, targetSdk 36, compileSdk 36의 minor API 1이며 Android Gradle Plugin은 `gradle/libs.versions.toml`의 9.2.1이다. 해당 SDK와 프로젝트 Gradle JDK가 필요하다. Java 소스 호환성 11과 Gradle 실행 JDK는 별개의 설정이다.

```bash
./gradlew :app:assembleDebug
```

wrapper 실행 권한이 없으면 `bash gradlew :app:assembleDebug`로 실행한다. 연결된 에뮬레이터/기기에 Android Studio Run으로 설치하고 Splash부터 확인한다. 앱 API 키나 `.env`는 필요하지 않다. 이 안내는 설정과 manifest 대조 기준이며 Android SDK 없이 빌드 성공을 검증한 것으로 보지 않는다.

## 개발 기록

[현재 구현](CURRENT_IMPLEMENTATION.md) · [화면 흐름](screen-flow.md) · [데이터 모델 설계](data-model.md) · [수동 테스트](manual-test-cases.md)

[개발 방향과 브랜치·커밋 규칙](project-direction.md) · [워크플로우](development-workflow.md) · [UI 기준](ui-guidelines.md) · [기존 로드맵](roadmap.md)
