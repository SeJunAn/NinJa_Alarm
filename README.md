# 🥷 닌자 알람 (NinJa Alarm) — UI · AI 파트

손으로 인(印)을 맺어야 꺼지는 닌자 알람 앱. 본 저장소는 작업지시서
`NinJa_Alarm_UI-AI_작업지시서.md` 의 **UI·AI 파트**를 구현한다.

> **현재 진행 단계: Phase 0 (뼈대) · Phase 1 (화면) · Phase 2 (손동작 인식) 완료.**

---

## 실행 방법

Android Studio 로 열거나, 커맨드라인에서:

```powershell
# JAVA_HOME 미설정 시 Android Studio 번들 JDK 사용
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug      # 디버그 APK 빌드
.\gradlew.bat installDebug       # 연결된 기기/에뮬레이터에 설치
```

- `minSdk = 24`, 언어 **Java**, UI **XML**(Compose 미사용).
- 손동작 인식 모델 파일은 `app/src/main/assets/` 에 위치해야 한다:
  - `yolox_nano_with_post.onnx`, `labels.csv` (해제 화면에서 사용)

---

## Phase 0 산출물

### 패키지 구조 (`com.ninja.alarm`)
지시서 5장 기준으로 재구성. (기존 `com.example.myapplication` → 변경)

```
com.ninja.alarm
├─ ui/
│   ├─ auth/      SplashActivity, LoginActivity
│   ├─ alarm/     AlarmListFragment, AlarmAdapter, AddAlarmActivity
│   ├─ sequence/  SequenceListFragment, SequenceAdapter, CustomSequenceBuilderActivity
│   ├─ tutorial/  TutorialActivity, SealAdapter
│   ├─ profile/   ProfileFragment, StatsActivity
│   ├─ settings/  SettingsActivity
│   ├─ dismiss/   DismissActivity (카메라 인식 통합)
│   ├─ common/    PlaceholderActivity + 커스텀 뷰(CountdownRing·SequenceProgress·DetectionOverlay)
│   └─ HomeActivity (하단 내비 셸)
├─ ml/            YoloxDetector·Labels(ONNX) + SealRecognizer·SequenceMatcher·SealResult·리스너
├─ repository/    인터페이스 5종 + Repositories(DI 지점)
│   └─ fake/      Fake* 구현 + SealData(12간지)·jutsu 시드
├─ model/         Seal/Sequence/Alarm/UserProfile/Stats/DismissPlan/Difficulty …
└─ util/          DayFormat, DifficultyUi, SealText, AppPrefs
```

### 디자인 시스템 (지시서 6장 — "먹과 인장")
- `res/values/colors.xml` — sumi/kuro/washi/shinobi/hi/kasumi/seikou/shippai 토큰
- `res/values/dimens.xml` — 8dp 그리드 간격, 카드 라운드 토큰
- `res/values/themes.xml` — Material3 다크 고정 테마 + 타이포/버튼/카드 스타일 토큰
- `res/values/strings.xml` — 화면 문구(능동형 카피 원칙 적용)
- 색·치수·문구 **하드코딩 없음** (전부 리소스 참조)

### 내비게이션 골격
`스플래시(1.5초) → 로그인(유효성 검사) → 홈`.
홈은 하단 내비게이션으로 **알람 / 술법 도감 / 프로필** 프래그먼트를 전환하고,
상단 툴바의 설정, 알람 목록의 + FAB, 해제 화면 진입까지 **빈 화면이라도 전부 클릭 이동 가능**.

**수용 기준 충족:** 전체 화면 사이를 이동할 수 있다 / 빌드·실행 가능.

---

## Phase 1 산출물 (UI 화면 — Fake 데이터)

### 데이터 경계 (`model/`, `repository/`)
지시서 9장 계약대로 DTO·인터페이스·Fake 구현을 두고, `Repositories` 서비스 로케이터
한 곳에서만 공급한다 (**BE 연동 교체 지점**).

- **model:** `Seal`/`SealUi`, `Sequence`, `Alarm`, `UserProfile`, `Stats`, `SealProgress`,
  `DismissPlan`, `DismissResult`, `Difficulty`(인 개수→등급, 지시서 2.4)
- **repository(인터페이스):** `AlarmRepository`, `SequenceRepository`, `DismissRepository`,
  `TutorialRepository`, `UserRepository`
- **repository/fake:** 위 5종의 메모리 구현 + 시드. 프리셋 술법은 `assets/jutsu.csv` 중
  12간지만 쓰는 6종(분신술·수란파술·용화술·변화술·호화구술·봉선화술)을 seal_id 로 옮긴 것.
- **DI 교체:** `Repositories.alarm()/sequence()/...` 가 현재 `Fake*` 를 반환 → BE 구현 완성 시
  이 메서드들만 교체.

### 화면 (Fake 데이터로 클릭 가능)
| # | 화면 | 위치 | 동작 |
| --- | --- | --- | --- |
| 3 | 알람 목록(홈) | `ui/alarm/AlarmListFragment` | 카드(시간·요일·라벨·술법·난이도)·ON/OFF·길게눌러 삭제·빈 상태·+FAB |
| 4 | 알람 추가/편집 | `ui/alarm/AddAlarmActivity` | 타임피커·요일7토글·술법선택(난이도)·라벨·저장 반영·해제 체험 |
| 5 | 해제(인식) | `ui/dismiss/DismissActivity` | 카메라 + 해제 계획(술법·순서·제한시간) 패널. 인식은 Phase 2 |
| 6 | 술법 도감 | `ui/sequence/SequenceListFragment` | 프리셋/커스텀 리스트·난이도 뱃지·필터·상세(맺는 순서) |
| 7 | 커스텀 빌더 | `ui/sequence/CustomSequenceBuilderActivity` | 12인 순서 탭·난이도 자동·저장(Fake) |
| 8 | 튜토리얼 | `ui/tutorial/TutorialActivity` | 12인 학습 체크·진행률 바·진행 저장(Fake) |
| 9 | 프로필/레벨 | `ui/profile/ProfileFragment` | 닉네임·칭호·레벨·경험치 바 |
| 10 | 통계 | `ui/profile/StatsActivity` | 성공률·평균 소요·실패·총 시도 |
| 11 | 설정 | `ui/settings/SettingsActivity` | 알람음·카메라 권한(앱 설정 이동)·계정·다크(고정)·모션 줄이기(저장) |

> 1·2번(스플래시·로그인)은 Phase 0 에서 완료.

---

## Phase 2 산출물 (손동작 인식)

`ml/` 에 인식 파이프라인을 두고 해제 화면에 통합했다 (지시서 결정대로 **YOLOX 유지**).

- **파이프라인:** CameraX → `SealRecognizer`(YOLOX ONNX 래핑, 최상위 검출→`SealResult`)
  → `SequenceMatcher`(디바운스 + step_order 순서 검증) → 성공/타임아웃 콜백
- **SequenceMatcher** (순수 로직, Android 비의존): 동일 인이 신뢰도 임계값으로 연속 5프레임
  유지될 때 확정, 기대 인과 일치하면 다음 단계·불일치는 무시. **단위 테스트 5케이스** 포함
  (`app/src/test/.../SequenceMatcherTest`)
- **커스텀 뷰(`ui/common/`):** `CountdownRingView`(제한시간 링), `SequenceProgressView`(①②③ 진행),
  `DetectionOverlayView`(검출 박스/라벨)
- **해제 흐름:** 해제 계획(`DismissPlan`)의 인을 순서대로 맺으면 성공 → `DismissRepository`에
  결과 기록 후 종료, `time_limit` 초과 시 재시도 카피 + 재시작. 추론은 워커 스레드, UI 갱신은 메인.

> 인식 정확도(2e)는 기존 YOLOX 모델 그대로 사용. 데이터/튜닝은 별도 작업.

---

## 다음 단계 (예정)
- **Phase 3:** 인장(印) 스탬프 성공 모션 + 먹 번짐(시그니처), 단계 확정 펄스·레벨업 모션,
  BE 실연동 교체(DI 지점), 접근성·**모션 줄이기 옵션 존중**(`AppPrefs.isReduceMotion`)

## 경계 (BE 담당 — 김동환)
Room/SQLite, AlarmManager 스케줄링, 인증/서버는 구현하지 않는다.
UI/AI 는 `repository/` 인터페이스에만 의존하며 `data/`(Room) 패키지는 수정하지 않는다.
