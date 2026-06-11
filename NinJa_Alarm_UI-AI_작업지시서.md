# 🥷 NinJa Alarm — UI · AI 작업 지시서 (에이전트용)

> **담당:** AI · UI 파트 | **대상:** 코딩 에이전트(Claude Code / Cursor 등)에게 그대로 전달하는 작업 브리프
> **연계 문서:** `NinJa_Alarm_프로젝트_통합문서.md` (전체 기획·ERD·난이도 — **단일 진실 공급원**)

---

## 0. 이 문서 사용법
- 이 문서는 **UI(XML 화면·테마·커스텀 뷰·애니메이션)** 와 **AI(손동작 인식 파이프라인)** 작업만을 지시합니다.
- 에이전트는 아래 **3장(기술 제약)** 과 **10장(작업 방식 규칙)** 을 **항상 준수**해야 합니다.
- 작업은 **7장의 Phase 순서**대로 진행하고, 각 단계가 끝날 때마다 **앱이 빌드·실행되는 상태**를 유지합니다.
- 백엔드(DB·알람)는 다른 팀원(김동환) 담당입니다. **9장의 인터페이스 계약**에 맞춰 **Fake 구현으로 먼저 개발**하고, 실제 구현이 들어오면 교체합니다.

---

## 1. 미션 요약
**목표:** 손으로 인(印)을 맺어야 꺼지는 닌자 알람 앱의 **화면 전체와 손동작 인식 기능**을 Java + XML로 구현한다.

| 구분 | 내용 |
| --- | --- |
| 한 줄 목표 | 알람이 울리면 카메라로 **인 시퀀스(술법)** 를 순서대로 맺어 해제하는 경험을 완성 |
| 담당 (IN) | 모든 화면 UI, 디자인 시스템, 커스텀 뷰, 애니메이션, 손동작 인식 ML 파이프라인 |
| 비담당 (OUT) | Room/SQLite 구현, 알람 스케줄링(AlarmManager), 서버 — **인터페이스로만 연동** |

---

## 2. 프로젝트 컨텍스트

### 2.1 앱 정의
알람 해제 시 카메라 앞에서 **12종 인(印, 12간지에 대응)** 을 정해진 순서(=술법)로 맺어야 알람이 꺼진다. 레벨/경험치/칭호로 더 어려운 술법에 도전하게 만든다.

### 2.2 핵심 해제 흐름 (입력 → 처리 → 출력)
```
[입력]  알람 발생 → 카메라 프레임(손 영상)
[처리]  ① 손 랜드마크 추출  ② 12종 인 분류  ③ step_order 순서 검증  ④ 제한시간 카운트다운
[출력]  ✅ 성공 → 알람 해제 + 경험치/레벨/통계 갱신
        ❌ 타임아웃/오인식 → 재시도 + 실패 기록
```

### 2.3 UI/AI가 알아야 할 데이터 모델 (요약)
> 전체 명세는 통합 문서 5장 참고. 아래는 화면/인식에서 **표시·사용하는 필드**만.

- **Seal(인)**: `seal_id`, `name`(쥐·소·호랑이…), `zodiac`(子丑寅…), `image_uri`(손모양 가이드), `display_order`(1~12)
- **Sequence(술법)**: `sequence_id`, `name`, `difficulty`(하급/중급/상급/최상급), `is_custom`
- **SequenceStep**: `seal_id`, `step_order` ← **인을 맺는 순서. 인식의 핵심**
- **Alarm**: `alarm_time`('HH:mm'), `repeat_days`(7비트), `label`, `time_limit`(초, 설정 시점 스냅샷), `is_enabled`(0/1)
- **User/Level**: `nickname`, `exp`, `current_level`, `title`(하급닌자→중급닌자→상급닌자→카게급)
- **DismissLog**: `is_success`, `duration_sec`, `fail_count` ← 통계 화면 표시

### 2.4 난이도 ↔ 제한시간 (인식 로직에 반영)
| 등급 | 인 개수 | 인당 시간 | 비고 |
| --- | --- | --- | --- |
| 하급 | 3 | 3.0초 | 총 = 인당 × 개수 |
| 중급 | 4~5 | 2.0초 | |
| 상급 | 6~7 | 1.5초 | |
| 최상급 | 8+ | 1.2초 | 커스텀 |
> 총 제한시간은 `alarm.time_limit`(초)로 전달받아 카운트다운에 사용. **새로 계산하지 말 것.**

---

## 3. 기술 제약 (필수 준수)
- **언어: Java** (Kotlin 금지)
- **UI: XML 레이아웃** (Jetpack Compose 금지)
- **IDE: Android Studio**, Gradle
- `minSdk = 24`, `targetSdk = 최신 안정 버전`
- **권장 라이브러리** (팀 확인 후 추가):
  - 카메라: **CameraX** (`androidx.camera:*`)
  - 손 인식: **MediaPipe Tasks – Hand Landmarker** (`com.google.mediapipe:tasks-vision`)
  - 분류기(필요 시): **TensorFlow Lite** (`org.tensorflow:tensorflow-lite`)
  - UI 컴포넌트: **Material Components (M3)** (`com.google.android.material`)
  - 이미지: Glide 또는 Coil(자바 호환)
- 무거운 의존성 추가 전 **반드시 팀에 확인** (10장 참고)

---

## 4. 작업 범위 & 경계

### ✅ IN SCOPE (내가/에이전트가 만든다)
- 디자인 시스템(테마, 색·타이포·간격 토큰, 공통 스타일)
- 모든 화면(Activity/Fragment + XML) — 7장 목록
- 커스텀 뷰(인 가이드, 랜드마크 오버레이, 시퀀스 진행, 카운트다운 링, 인장 스탬프)
- 애니메이션/전환
- 손동작 인식 파이프라인(카메라 → 랜드마크 → 인 분류 → 시퀀스 매칭)

### ⛔ OUT OF SCOPE (BE 담당 — 김동환)
- Room Entity/DAO/Migration, SQLite 실제 저장
- AlarmManager 스케줄링, BroadcastReceiver, ForegroundService 알람 발화
- 비밀번호 해시/인증 로직, 서버 동기화

> **연동 방식:** UI/AI는 **Repository 인터페이스**(9장)에만 의존한다. 개발 중에는 `Fake*Repository`(메모리/하드코딩 시드)로 동작시키고, BE 구현이 들어오면 DI 지점만 교체한다. **`data/` 패키지(Room) 파일은 수정하지 않는다.**

---

## 5. 패키지/파일 구조 규칙
```
com.ninja.alarm
├─ ui/
│   ├─ alarm/        # 알람 목록 · 추가/편집
│   ├─ dismiss/      # ⭐ 카메라 인식 해제 화면
│   ├─ sequence/     # 술법 도감 · 커스텀 빌더
│   ├─ tutorial/     # 인 학습
│   ├─ profile/      # 프로필 · 레벨 · 통계
│   ├─ auth/         # 로그인 · 회원가입 · 스플래시
│   └─ common/       # 커스텀 뷰, 어댑터, BaseActivity
├─ ml/               # ⭐ SealRecognizer, HandLandmark, SealClassifier, SequenceMatcher
├─ repository/       # 인터페이스 + Fake 구현 (BE 연동 경계)
├─ model/            # UI 표시용 DTO (DismissPlan, SealUi 등)
└─ util/             # 권한, 시간 포맷, 확장 유틸
```
- 화면 1개 = 1 Activity 또는 Fragment + 대응 XML + (필요 시) Adapter
- 레이아웃 파일명: `activity_*`, `fragment_*`, `item_*`, `view_*`
- 색·치수·문자열은 **하드코딩 금지** → `colors.xml`, `dimens.xml`, `strings.xml`, `themes.xml`

---

## 6. 디자인 시스템 (UI)

### 6.1 컨셉 — "먹과 인장(印)"
브랜드는 **검정 배경 + 오렌지(印 로고)** 로 고정이다. 단, **흔한 "다크+네온 강조색" 느낌으로 가지 말 것.** 닌자/주술의 세계(먹, 화선지, **간지 한자**, 인장)를 끌어와 차별화한다.

- **시그니처 요소(이 앱을 기억하게 만드는 단 하나):** 해제 성공 시 화면에 **인장(印)이 쾅 찍히는** 스탬프 모션 + 먹 번짐. 보너스는 여기에만 몰고, 나머지는 절제한다.
- **구조적 모티프:** 12간지 한자(子·丑·寅…)를 단순 장식이 아니라 **인/술법 식별 요소**로 사용. 시퀀스 단계는 **실제 순서**가 있으므로 번호(①②③) 표기가 정당하다.
- **질감:** 순수 흑백 대비 대신 **화선지(washi) 톤의 웜 오프화이트**를 텍스트/포인트에 섞어 종이+먹 느낌을 준다.

### 6.2 컬러 토큰 (시작값 — `colors.xml`)
| 토큰 | HEX | 용도 |
| --- | --- | --- |
| `sumi` (먹) | `#0B0B0D` | 기본 배경 |
| `kuro` (흑) | `#16151A` | 카드/표면 |
| `washi` (화선지) | `#ECE7DD` | 주요 텍스트·종이 포인트 |
| `shinobi` (주색) | `#FF5A1F` | 강조·CTA (로고 오렌지) |
| `hi` (잉걸) | `#B3360E` | 깊은 보조 오렌지(그라데이션·프레스) |
| `kasumi` (안개) | `#8A8790` | 보조 텍스트·비활성 |
| `seikou` (성공) | `#3FA7A0` | 해제 성공 |
| `shippai` (실패) | `#E5484D` | 타임아웃·오류 |

### 6.3 타이포그래피 (Korean+한자 글리프 필수)
- **Display / 술법명:** 강한 개성의 서체(브러시·슬랩 느낌). 절제해서 큰 제목·술법명에만.
- **간지 한자(子丑寅…):** 명조 계열(예: Noto Serif KR)로 전통 먹 느낌.
- **Body / UI:** Pretendard 등 **깔끔한 한글 산세리프**.
- 타입 스케일·굵기를 명확히 정의(`textAppearance`로 토큰화). 폰트는 **라이선스 확인 후** `res/font/`에 포함.

### 6.4 간격·형태
- 간격: **8dp 그리드** (4/8/12/16/24/32)
- 라운드: 카드 8~12dp(과한 라운드·완전 직각 둘 다 지양)
- 컴포넌트: **Material 3** (`MaterialButton`, `MaterialCardView`, `Slider`, `Chip` 등)

### 6.5 카피(문구) 원칙
- 사용자 입장의 능동형. 시스템 용어 금지.
- 실패/빈 화면은 **방향을 제시**(사과·모호함 금지).
  - 해제 실패 → "인을 순서대로 다시 맺으세요" (X "인식 실패")
  - 빈 알람 목록 → "아직 알람이 없어요. 첫 술법을 골라 알람을 만들어 보세요."
- 같은 동작은 흐름 내내 같은 단어로(버튼 "저장" → 토스트 "저장됨").

---

## 7. 작업 목록 — 단계별 (가장 중요)

> 각 Phase 종료 시 **빌드·실행 가능**해야 한다. 화면은 **Fake 데이터로 클릭 가능한 상태**부터 만든 뒤 실제 연동/인식으로 채운다.

### Phase 0 — 셋업 & 뼈대
- [ ] Gradle 설정, 의존성(6장/3장), 패키지 구조(5장) 생성
- [ ] `themes.xml`/`colors.xml`/`dimens.xml`/`strings.xml` 토큰 정의 (6장)
- [ ] 내비게이션 골격(스플래시 → 로그인 → 홈), 하단/상단 내비
- **수용 기준:** 빈 화면이라도 전체 화면 사이를 이동할 수 있다.

### Phase 1 — UI 화면 (Fake 데이터)
각 화면: **목적 / 주요 요소 / 입출력 / 수용 기준** 순으로 구현.

| # | 화면 | 주요 요소 | 수용 기준 |
| --- | --- | --- | --- |
| 1 | 스플래시 | 印 로고, 짧은 진입 모션 | 1.5초 후 자동 전환 |
| 2 | 로그인/회원가입 | 이메일·닉네임 입력, 유효성 표시 | 입력 검증·에러 카피 동작 |
| 3 | **알람 목록(홈)** | 알람 카드(시간·요일·술법명·난이도), ON/OFF 스위치, + FAB | 토글·삭제·빈 상태 카피 |
| 4 | **알람 추가/편집** | 타임피커, 요일 7토글, **술법 선택(난이도 표시)**, 라벨, 사운드 | 저장 시 목록 반영(Fake) |
| 5 | **해제 화면(인식)** ⭐ | 카메라 프리뷰 자리, **인 가이드 뷰**, **시퀀스 진행 뷰**, **카운트다운 링** | Phase 2 전까지 스텁으로 흐름 시연 |
| 6 | 술법 도감 | 프리셋 11종 + 커스텀 리스트, 난이도 뱃지, 시퀀스 미리보기 | 상세 진입·필터 |
| 7 | 커스텀 술법 빌더 | 12종 인 선택, **순서(step_order) 지정**, 난이도 자동 표시 | 시퀀스 구성·저장(Fake) |
| 8 | 튜토리얼(인 학습) | 인별 가이드 이미지/설명, 완료 체크, 진행률 | 완료 토글·진행 저장(Fake) |
| 9 | 프로필/레벨 | 닉네임, **경험치 바**, 칭호, 레벨업 표시 | 경험치/레벨 표시 |
| 10 | 통계 | 성공률, 평균 소요, 실패 횟수(차트/요약) | Fake 데이터 시각화 |
| 11 | 설정 | 알람음, 카메라 권한, 계정, 다크/모션 옵션 | 항목 진입 |

### Phase 2 — AI 손동작 인식 (8장 상세)
- [ ] **2a** CameraX 프리뷰 + ImageAnalysis 연결, **CAMERA 권한** 런타임 처리
- [ ] **2b** MediaPipe HandLandmarker 통합 → **랜드마크 오버레이 뷰**로 손 스켈레톤 표시
- [ ] **2c** `SealClassifier` — 우선 **스텁**(손이 잡히면 "현재 기대 인"을 반환)으로 해제 흐름 완성
- [ ] **2d** `SequenceMatcher` 상태머신 — 순서 검증·디바운스·제한시간(`time_limit`) 연동
- [ ] **2e** 실제 분류기(템플릿/KNN 또는 TFLite) 도입 + 정확도 튜닝
- **수용 기준:** 손 인식 후 시퀀스를 순서대로 맺으면 성공 콜백, 제한시간 초과 시 실패 콜백.

### Phase 3 — 커스텀 뷰 · 애니메이션 · 통합 · 폴리시
- [ ] 시그니처 **인장(印) 스탬프 성공 모션** + 먹 번짐
- [ ] 단계 확정 펄스, 레벨업 모션, 카운트다운 링 채움
- [ ] 9장 인터페이스를 실제 BE 구현으로 교체(DI 지점)
- [ ] 접근성(콘텐츠 설명, 포커스), **모션 줄이기 옵션 존중**, 다양한 화면 대응
- **수용 기준:** Fake → 실 BE 전환 후에도 전 화면 정상, 모션 옵션 OFF 시 과한 모션 비활성.

---

## 8. AI 인식 상세 (손동작 인식)

### 8.1 권장 파이프라인
```
CameraX(ImageAnalysis)
   → MediaPipe HandLandmarker (손 21개 랜드마크 x,y,z)
   → 정규화 (손목 원점 이동 + 손 크기로 스케일)
   → SealClassifier (인 12종 분류 → seal_id + confidence)
   → SequenceMatcher (디바운스 + step_order 순서 검증 + time_limit 카운트다운)
   → onSuccess / onTimeout 콜백
```

### 8.2 분류기 전략 (스텁 → 실모델)
1. **스텁 분류기**: 손이 안정적으로 잡히면 "현재 기대 인"을 반환 → **UI/시퀀스 로직을 먼저 완성**.
2. **템플릿/KNN**: 12종 인의 정규화 랜드마크 기준 샘플과 거리 비교 → 학습 인프라 없이 프로토타입 가능.
3. **TFLite MLP**: 랜드마크 벡터(또는 이미지)로 학습한 소형 분류기. 정확도 부족 시 도입.

### 8.3 안정화(디바운스) 규칙
- 동일 인이 **confidence ≥ 임계값**으로 **연속 N프레임**(예: 5프레임 ≈ 0.3초) 유지될 때만 "확정".
- 확정된 인이 기대 인과 일치하면 다음 단계로 진행, 아니면 무시(또는 정책에 따라 리셋).
- 추론은 **백그라운드 Executor**에서, UI 갱신은 메인 스레드.

### 8.4 데이터 & 리스크 (팀 공유 필수)
- 12종 인 분류 모델은 **라벨링된 손 모양 데이터**가 필요 → 데이터 수집/전처리는 별도 작업.
- **개인정보:** 카메라 영상은 **온디바이스 추론**, 서버 미전송 원칙.
- **마감 리스크 대비:** 12종 정밀 인식이 어려우면 데모용으로 ① 구분 잘 되는 인만 선별, ② 인 개수 축소, ③ 스텁/반자동 모드 — 중 하나로 **폴백** 가능하도록 인식부와 UI를 **느슨히 결합**(인터페이스 분리)해 둔다.

### 8.5 인식부 인터페이스(예시)
```java
interface SealClassifier {
    // 정규화된 랜드마크 → 분류 결과
    SealResult classify(float[] normalizedLandmarks);
}
// SealResult { int sealId; float confidence; }

interface SealRecognitionListener {
    void onStepConfirmed(int stepIndex, int sealId); // 단계 확정
    void onSequenceSuccess(long durationMs);         // 전체 성공
    void onTimeout(int failCount);                   // 제한시간 초과
}
```

---

## 9. 백엔드 의존 인터페이스 (계약)
> UI/AI는 아래 인터페이스에만 의존한다. **개발 중 `Fake*Repository`로 동작** → BE 완성 시 교체. 시그니처/명칭은 BE(김동환)와 합의해 확정.

```java
// 해제 화면이 알람 1건의 해제 계획을 받아온다
interface SequenceRepository {
    DismissPlan getDismissPlan(long alarmId);          // 술법명·난이도·time_limit·순서대로의 인 목록
    List<Sequence> getSequences(boolean includeCustom);
    void saveCustomSequence(Sequence seq, List<Integer> orderedSealIds);
}

// 해제 결과 기록(통계·경험치 반영은 BE가 처리)
interface DismissRepository {
    void recordResult(DismissResult result);           // is_success, durationSec, failCount
    Stats getStats(long userId);
}

interface AlarmRepository {
    List<Alarm> getAlarms();
    void upsert(Alarm alarm);
    void setEnabled(long alarmId, boolean enabled);
    void delete(long alarmId);
}

interface TutorialRepository {
    List<SealProgress> getProgress(long userId);
    void markCompleted(long userId, long sealId);
}

interface UserRepository {
    UserProfile getProfile(long userId);               // 닉네임·exp·현재 레벨·칭호
}
```
**UI 표시용 DTO(예):**
```java
// model 패키지
class DismissPlan { String sequenceName; String difficulty; int timeLimitSec; List<SealUi> orderedSeals; }
class SealUi { int sealId; String name; String zodiac; String imageUri; }
```

---

## 10. 작업 방식 (에이전트 행동 규칙)
1. **항상 빌드 가능 상태 유지** — Phase/화면 단위로 작게 완성하고 실행 확인.
2. **`data/`(Room) 패키지 수정 금지** — BE 경계는 `repository/` 인터페이스로만.
3. **Fake 우선** — BE 미완 기능은 `Fake*Repository`(하드코딩 시드)로 동작시키고 TODO 주석으로 표시.
4. **하드코딩 금지** — 색/치수/문자열은 리소스로.
5. **권한 처리** — CAMERA(필수, 런타임), 필요 시 POST_NOTIFICATIONS·오디오. 거부 시 안내 화면.
6. **접근성/모션** — 콘텐츠 설명, 키보드/포커스, **모션 줄이기 옵션 존중**.
7. **다음은 진행 전 반드시 질문** — ① 데이터 모델 변경 ② 무거운 의존성 추가 ③ ML 모델 최종 방식 ④ Compose/Kotlin 사용.
8. **결과 보고** — 작업 후 무엇을 만들었고 실행 방법은 무엇인지 짧은 메모(README) 제공.
9. **커밋 단위** — 화면/기능 1개 = 1커밋, 메시지에 무엇을 했는지 명확히.

---

## 11. 산출물 체크리스트 (UI · AI 파트)
- [ ] 디자인 토큰(`themes/colors/dimens/strings.xml`) 정의 완료
- [ ] 11개 화면 XML + 화면 전환 동작 (Fake 데이터)
- [ ] 커스텀 뷰 5종(인 가이드·랜드마크 오버레이·시퀀스 진행·카운트다운 링·인장 스탬프)
- [ ] 손동작 인식 파이프라인(스텁 → 실모델) + 시퀀스 매칭 동작
- [ ] 시그니처 성공 모션 + 주요 애니메이션, 모션 줄이기 대응
- [ ] `repository/` 인터페이스 + Fake 구현, BE 연동 교체 지점 명시
- [ ] 실행 방법·구현 요약 README
```