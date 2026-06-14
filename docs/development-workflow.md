# 개발 워크플로우

PickDay 개발은 기능 단위 브랜치와 PR 기반 병합을 원칙으로 합니다.

## 브랜치 규칙

- 모든 작업은 `dev`에서 시작합니다.
- `main`은 MVP 브랜치이므로 직접 개발 커밋을 넣지 않습니다.
- `dev` 병합은 반드시 Pull Request로 진행합니다.
- 서로 관련 없는 기능을 하나의 브랜치에 섞지 않습니다.

허용 브랜치 prefix:

- `feat/<feature-name>`: 새 기능
- `fix/<bug-or-screen-name>`: 버그 수정
- `refactor/<scope-name>`: 내부 구조 개선
- `docs/<document-name>`: 문서 작업
- `style/<screen-or-system>`: 동작 변화 없는 UI 정리
- `test/<scope-name>`: 테스트와 검증
- `chore/<scope-name>`: 설정과 유지보수

예시:

- `feat/local-room-flow`
- `feat/schedule-calculator`
- `fix/home-room-list-state`
- `refactor/repository-layer`
- `docs/server-ready-roadmap`
- `style/room-detail-cards`

## 커밋 규칙

커밋은 작게 나누고 구현 단위가 드러나게 작성합니다.

Conventional Commit 형태를 사용합니다.

- `feat: add local room repository`
- `fix: preserve selected dates on response edit`
- `refactor: split schedule scoring from activity`
- `docs: add UI icon policy`
- `style: align room detail card spacing`
- `test: cover schedule tie ranking`
- `chore: update gradle config`

동작 변경, 대규모 UI 변경, 문서 정리를 하나의 커밋에 섞지 않습니다. 단, 변경이 분리 불가능할 때만 함께 커밋합니다.

## 문서 업데이트 규칙

모든 작업에서 문서 변경 필요 여부를 확인합니다.

아래 항목이 바뀌면 문서를 함께 수정합니다.

- 사용자 흐름
- 화면 상태
- 데이터 모델
- API 계약
- 디자인 시스템
- 아이콘/에셋 정책
- 알려진 제한사항
- 수동 테스트 체크리스트

문서 변경이 필요 없다면 PR 설명에 그 이유를 적습니다.

## 코드 작성 규칙

- 코드 주석은 한국어로 작성합니다.
- 주석은 복잡한 의도, 예외 처리, 화면/도메인 규칙을 설명해야 할 때만 추가합니다.
- 단순히 코드가 하는 일을 반복하는 주석은 작성하지 않습니다.
- 변수명, 함수명, 클래스명은 기존 Android/Java 관례에 맞춰 영어로 작성합니다.
- 사용자에게 노출되는 문구는 자연스러운 한국어를 우선합니다.

## PR 필수 항목

모든 PR에는 아래 내용을 포함합니다.

- 작업 목적
- 변경된 화면
- 데이터/모델 변경
- 문서 업데이트 여부
- 수동 검증 내용
- UI 변경 시 스크린샷
- 알려진 제한사항

## 병합 전 확인

`dev`로 병합하기 전에 확인합니다.

- 브랜치명이 규칙을 따르는가
- 커밋이 기능 단위로 분리되어 있는가
- UI 변경이 디자인 방향과 맞는가
- 문서가 업데이트되어 있는가
- 빌드가 통과하는가
- 관련 없는 생성 파일이 포함되지 않았는가
