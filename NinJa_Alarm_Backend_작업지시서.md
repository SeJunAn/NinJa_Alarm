# 🥷 NinJa Alarm — Backend(데이터·알람·인증) 작업 지시서 (에이전트용)

> **담당:** 백엔드 파트(김동환) | **대상:** 코딩 에이전트(Claude Code / Cursor 등)에게 그대로 전달하는 작업 브리프
> **연계 문서:**
> - `NinJa_Alarm_UI-AI_작업지시서.md` — UI·AI 파트(상대 팀, IN/OUT 경계의 반대편)
> - `NinJa_Alarm_프로젝트_통합문서.md` — 전체 기획·ERD·난이도 (**단일 진실 공급원**)
> - 본 저장소의 실제 코드: `app/src/main/java/com/ninja/alarm/repository/`, `.../model/` (**계약의 실체**)

---

## 0. 이 문서 사용법
- 이 문서는 **데이터(Room/SQLite)·알람 스케줄링·인증·서버 동기화·경험치/레벨 로직** 작업만을 지시합니다.
- UI(화면·커스텀 뷰)와 AI(손동작 인식)는 **다른 팀이 이미 구현**했습니다. BE는 그쪽이 의존하는 **인터페이스(8장)** 를 실제 구현으로 채우는 일이 핵심입니다.
- 작업은 **7장의 Phase 순서**대로 진행하고, 각 단계가 끝날 때마다 **앱이 빌드·실행되는 상태**를 유지합니다.
- **8장 인터페이스 계약은 절대 임의로 바꾸지 않습니다.** 시그니처/DTO 변경이 필요하면 UI 담당과 합의 후 양쪽을 동시에 수정합니다.

---

## 1. 미션 요약
**목표:** 손으로 인(印)을 맺어야 꺼지는 닌자 알람 앱의 **데이터 영속화·알람 발화·인증·성장 로직**을 Java로 구현하고, UI/AI가 의존하는 `repository` 인터페이스를 **실제 구현(Room 기반)** 으로 교체한다.

| 구분 | 내용 |
| --- | --- |
| 한 줄 목표 | 알람을 정확히 울리고, 해제 결과를 저장해 경험치·레벨·통계로 환산하며, 모든 데이터를 온디바이스에 영속화 |
| 담당 (IN) | Room Entity/DAO/Migration, AlarmManager 스케줄링, BroadcastReceiver, ForegroundService 알람 발화, 인증(해시), 경험치/레벨 계산, (선택) 서버 동기화 |
| 비담당 (OUT) | 모든 화면 UI, 커스텀 뷰, 애니메이션, 손동작 인식 ML — **인터페이스로만 연동** |

---

## 2. 프로젝트 컨텍스트

### 2.1 앱 정의
알람 해제 시 카메라 앞에서 **12종 인(印, 12간지에 대응)** 을 정해진 순서(=술법)로 맺어야 알람이 꺼진다. 레벨/경험치/칭호로 더 어려운 술법에 도전하게 만든다.

### 2.2 핵심 해제 흐름에서 BE의 위치
```
[알람 발화]  AlarmManager → BroadcastReceiver → ForegroundService(알람음/풀스크린)  ← BE
[해제 화면]  Service 가 DismissActivity 실행(EXTRA_ALARM_ID 전달)                  ← BE가 실행, UI가 화면
[인식/판정]  카메라 → 인 시퀀스 순서 검증 → 성공/타임아웃                            ← UI·AI
[결과 반영]  DismissActivity 가 DismissRepository.recordResult(...) 호출            ← UI가 호출, BE가 저장
[성장]      성공/실패에 따라 exp·level·통계·DismissLog 갱신                         ← BE
```

### 2.3 UI/AI 측이 **이미 구현해 둔 것** (BE가 채워야 할 빈칸의 형태)
- `com.ninja.alarm.model.*` — UI 표시용 DTO (8장 목록). **BE가 Room Entity ↔ 이 DTO 매핑**.
- `com.ninja.alarm.repository.*` — 인터페이스 5종 + `Fake*Repository`(메모리 시드) + `Repositories` 서비스 로케이터.
- `com.ninja.alarm.repository.Repositories` — **유일한 DI 교체 지점**. 현재 `Fake*` 반환 → BE 구현으로 교체.
- `DismissActivity` 는 `EXTRA_ALARM_ID`(long) 를 받으면 `SequenceRepository.getDismissPlan(alarmId)` 로 해제 계획을 가져온다. 해제 결과는 `DismissRepository.recordResult(...)` 로 보고한다.

> **BE의 1차 목표는 "Fake* 를 Room 기반 실제 구현으로 교체"** 다. 인터페이스/DTO는 그대로 둔다.

---

## 3. 기술 제약 (필수 준수)
- **언어: Java** (Kotlin 금지) · **IDE: Android Studio**, Gradle (Kotlin DSL)
- `minSdk = 24`, `targetSdk = 36`, `namespace/applicationId = com.ninja.alarm`
- **권장 라이브러리** (추가 전 팀 확인 — 11장):
  - DB: **Room** (`androidx.room:room-runtime`, `room-compiler`(annotationProcessor))
  - 알람: **AlarmManager**(`setAlarmClock`/`setExactAndAllowWhileIdle`), `BroadcastReceiver`, **ForegroundService**
  - 비밀번호 해시: **AndroidX Security** 또는 표준 `javax.crypto`(PBKDF2)/`bcrypt` — 자체 암호 구현 금지
  - (선택) 서버: Retrofit + OkHttp + Gson/Moshi
- **하드코딩 금지** — 색/치수/문자열은 리소스로(UI 담당과 동일 규칙). 알람 채널 ID·키 등 상수는 한 곳에 모은다.
- minSdk 24 호환 주의: lint `NewApi` 에러로 빌드가 중단된다(`paddingHorizontal` 등 API26 속성·중간 BOM 금지).

---

## 4. 작업 범위 & 경계

### ✅ IN SCOPE (BE 가 만든다)
- `com.ninja.alarm.data/` — Room `@Entity`, `@Dao`, `@Database`, `Migration`, TypeConverter, 시드
- `repository` 인터페이스의 **실제 구현**(Room 백엔드) + `Repositories` 교체
- AlarmManager 예약/해제, 부팅 후 재예약(`BOOT_COMPLETED`), `BroadcastReceiver`, ForegroundService(알람음·진동·풀스크린 인텐트)
- 인증: 회원가입/로그인, 비밀번호 해시·검증, 세션 유지
- 경험치/레벨/칭호 계산, 통계 집계(DismissLog)
- (선택) 서버 동기화(계정·통계 백업)

### ⛔ OUT OF SCOPE (UI·AI 담당 — 이미 구현됨)
- Activity/Fragment/XML 화면, 커스텀 뷰, 애니메이션
- CameraX·손동작 인식·시퀀스 매칭(`ml/` 패키지)
- `model/`·`repository/` **인터페이스와 DTO 시그니처** (계약이므로 BE가 임의 변경 금지)

> **연동 방식:** BE는 `data/` 패키지를 신설해 Room 을 구현하고, `repository` 인터페이스를 구현한 `Room*Repository` 를 만들어 `Repositories` 로케이터에서 `Fake*` 대신 반환한다. UI/AI 코드는 한 줄도 바꾸지 않아도 동작해야 한다.

---

## 5. 패키지/파일 구조 규칙
```
com.ninja.alarm
├─ data/                  # ⭐ BE 신설 — Room 영역
│   ├─ entity/            #   AlarmEntity, SealEntity, SequenceEntity, SequenceStepEntity,
│   │                     #   UserEntity, DismissLogEntity, SealProgressEntity
│   ├─ dao/               #   AlarmDao, SequenceDao, UserDao, DismissDao, TutorialDao
│   ├─ NinjaDatabase.java #   @Database + Migration + seed(prepopulate)
│   ├─ Converters.java    #   TypeConverter (Difficulty, repeat_days 등)
│   └─ mapper/            #   Entity ↔ model DTO 매핑
├─ repository/            # (기존) 인터페이스 + Repositories 로케이터
│   ├─ room/              # ⭐ BE 신설 — RoomAlarmRepository 등 실제 구현
│   └─ fake/              # (기존) 개발용 — 교체 후에도 테스트용으로 유지 가능
├─ alarm/                 # ⭐ BE 신설 — 스케줄링/발화
│   ├─ AlarmScheduler.java       # AlarmManager 예약/취소/재예약
│   ├─ AlarmReceiver.java        # BroadcastReceiver(알람·BOOT_COMPLETED)
│   └─ AlarmService.java         # ForegroundService(알람음·풀스크린→DismissActivity)
├─ auth/                  # ⭐ BE 신설 — 회원가입/로그인/세션, 비밀번호 해시
├─ model/                 # (기존·공유) DTO — 변경 금지
└─ ml/, ui/               # (기존·UI/AI) — 건드리지 않음
```
- **`data/`(Room)·`alarm/`·`auth/` 는 BE 소유.** `ui/`·`ml/`·`model/`·`repository` 인터페이스 본문은 건드리지 않는다.

---

## 6. 데이터 모델 / ERD (Room Entity 설계)

> 통합 문서 5장이 SSOT. 아래는 본 저장소 `model/` DTO 와 UI 사용을 만족시키는 **최소 Entity 설계**다.
> 컬럼명·타입은 통합 문서와 충돌 시 통합 문서를 따르되, **DTO 로 매핑 가능**해야 한다.

### 6.1 엔티티
| 엔티티 | 주요 컬럼 | 비고 |
| --- | --- | --- |
| `SealEntity` | `seal_id`(PK,1~12), `name`, `zodiac`, `display_order`, `image_uri`(nullable) | **12간지 고정 시드**(6.3) |
| `SequenceEntity` | `sequence_id`(PK), `name`, `name_en`, `difficulty`, `is_custom`(0/1) | 프리셋 + 커스텀 |
| `SequenceStepEntity` | `sequence_id`(FK), `seal_id`(FK), `step_order` | **인을 맺는 순서. 인식의 핵심** |
| `AlarmEntity` | `alarm_id`(PK), `alarm_time`('HH:mm'), `repeat_days`(7비트 int), `label`, `time_limit`(초), `is_enabled`(0/1), `sequence_id`(FK) | `time_limit` 은 설정 시점 스냅샷 |
| `UserEntity` | `user_id`(PK), `email`, `nickname`, `password_hash`, `password_salt`, `exp`, `current_level`, `title` | 비번 평문 저장 금지 |
| `DismissLogEntity` | `log_id`(PK), `user_id`(FK), `alarm_id`, `is_success`(0/1), `duration_sec`, `fail_count`, `created_at` | 통계 원천 |
| `SealProgressEntity` | `user_id`(FK), `seal_id`(FK), `completed`(0/1) | 튜토리얼 진행 |

### 6.2 핵심 규칙 (UI 코드와 반드시 일치)
- **`repeat_days` 비트 규약:** `bit0=월, bit1=화, … bit5=토, bit6=일`. (UI `util/DayFormat` 와 동일. 0=반복 없음, 0b0011111=주중, 0b1100000=주말)
- **`seal_id` 매핑:** `1=子(쥐) 2=丑 3=寅 4=卯 5=辰 6=巳 7=午 8=未 9=申 10=酉 11=戌 12=亥`. (= `assets/labels.csv` 1~12행, YOLOX class_id+1)
- **난이도(`difficulty`)** — 인 개수로 결정(통합 문서 2.4):
  | 등급(enum) | 인 개수 | 인당 시간 | `time_limit` = 인당 × 개수 |
  | --- | --- | --- | --- |
  | `HAGEUP`(하급) | 3 | 3.0초 | |
  | `JUNGGEUP`(중급) | 4~5 | 2.0초 | |
  | `SANGGEUP`(상급) | 6~7 | 1.5초 | |
  | `CHOESANGGEUP`(최상급) | 8+ | 1.2초 | |
  - 저장은 `Difficulty` enum 이름 또는 ordinal. `time_limit` 은 알람 생성 시 계산된 값을 **그대로 스냅샷 저장**(재계산 금지).
- **칭호(`title`)**: 하급닌자 → 중급닌자 → 상급닌자 → 카게급 (레벨 구간은 통합 문서/팀 합의로 확정).

### 6.3 시드(Prepopulate) — 최초 DB 생성 시
- **SealEntity 12행**(위 매핑) 고정 시드.
- **프리셋 SequenceEntity + SequenceStep** — 최소 아래 6종(현재 `FakeSequenceRepository` 시드와 동일하게 맞춰 UI 회귀 방지). `assets/jutsu.csv` 에서 12간지만 쓰는 술법을 더 추가 가능.
  | 술법 | 영문 | 인 순서(seal_id) | 난이도 |
  | --- | --- | --- | --- |
  | 분신술 | Clone Jutsu | 8,6,3 | 하급 |
  | 수란파술 | Water Trumpet | 5,3,4 | 하급 |
  | 용화술 | Dragon Flame Jutsu | 6,5,4,3 | 중급 |
  | 변화술 | Substitution Jutsu | 8,12,2,11,6 | 중급 |
  | 호화구술 | Fireball Jutsu | 6,3,9,12,7,3 | 상급 |
  | 봉선화술 | Phoenix Flower Jutsu | 1,3,11,2,4,3 | 상급 |

---

## 7. 작업 목록 — 단계별 (가장 중요)

> 각 Phase 종료 시 **빌드·실행 가능**해야 한다. 인터페이스를 한 개씩 Room 구현으로 바꿔 끼우며 UI 회귀가 없는지 확인한다.

### Phase B0 — Room 기반 + 읽기 전용 교체
- [ ] `Room` 의존성 추가(`gradle/libs.versions.toml`, `app/build.gradle.kts`)
- [ ] `data/entity/*`, `data/dao/*`, `NinjaDatabase`(+Converters), 시드(6.3) 구현
- [ ] `data/mapper/*` — Entity ↔ `model` DTO 매핑
- [ ] `RoomSequenceRepository`, `RoomUserRepository` 구현 → `Repositories` 에서 교체
- **수용 기준:** 술법 도감·튜토리얼·프로필이 Room 시드 데이터로 표시(앱 재시작해도 유지).

### Phase B1 — 알람 CRUD 영속화
- [ ] `RoomAlarmRepository` — `getAlarms / upsert / setEnabled / delete`
- [ ] `Repositories.alarm()` 교체
- **수용 기준:** 알람 추가/편집/토글/삭제가 앱 재시작 후에도 유지.

### Phase B2 — 알람 스케줄링 & 발화 ⭐
- [ ] `AlarmScheduler` — 알람 enable/upsert 시 다음 발화 시각 계산(요일 반복 반영) 후 `AlarmManager` 예약, disable/delete 시 취소
- [ ] `AlarmReceiver` — 알람 트리거 수신 + `BOOT_COMPLETED` 재예약(권한 `RECEIVE_BOOT_COMPLETED`)
- [ ] `AlarmService`(Foreground) — 알람음/진동, 풀스크린 인텐트로 **`DismissActivity` 를 `EXTRA_ALARM_ID` 와 함께 실행**
- [ ] 권한: `SCHEDULE_EXACT_ALARM`/`USE_EXACT_ALARM`(API31+), `POST_NOTIFICATIONS`(API33+), `FOREGROUND_SERVICE`, 알람 NotificationChannel
- **수용 기준:** 설정한 시각에 알람이 울리고, 해제 화면이 자동으로 뜬다.

### Phase B3 — 해제 결과·성장·통계
- [ ] `RoomDismissRepository` — `recordResult` 시 `DismissLogEntity` 기록 + **성공 시 exp 가산 → level/title 재계산**, `getStats` 집계
- [ ] `RoomTutorialRepository` — `getProgress / markCompleted`
- [ ] 알람 해제 성공 신호로 `AlarmService` 종료(알람음 정지) 연결 — **9.3 핸드셰이크** 확정
- **수용 기준:** 해제 성공 시 알람이 꺼지고, 통계/경험치/레벨이 갱신된다.

### Phase B4 — 인증 (+ 선택: 서버 동기화)
- [ ] `auth/` — 회원가입/로그인, 비밀번호 해시(PBKDF2/bcrypt)+salt, 세션 유지(`EncryptedSharedPreferences` 등)
- [ ] 현재 `LoginActivity` 는 유효성만 검사하고 통과시킨다 → 실제 인증 호출로 교체(UI 담당과 연결 지점 합의)
- [ ] (선택) Retrofit 서버 동기화: 계정·통계 백업/복원
- **수용 기준:** 가입한 계정으로 로그인되고, 비밀번호는 해시로만 저장된다.

---

## 8. 인터페이스 계약 (UI/AI ↔ BE) — **절대 기준**

> 아래 시그니처와 DTO 는 **본 저장소에 이미 존재**한다(`com.ninja.alarm.repository.*`, `.../model.*`).
> BE 는 이 인터페이스를 **구현만** 한다. 변경이 필요하면 UI 담당과 합의 후 양쪽 동시 수정.

### 8.1 Repository 인터페이스 (구현 대상)
```java
interface AlarmRepository {
    List<Alarm> getAlarms();
    long upsert(Alarm alarm);                 // alarmId<=0 이면 신규, 확정 id 반환
    void setEnabled(long alarmId, boolean enabled);
    void delete(long alarmId);
}

interface SequenceRepository {
    DismissPlan getDismissPlan(long alarmId); // 알람의 술법·난이도·time_limit·순서대로의 인
    List<Sequence> getSequences(boolean includeCustom);
    Sequence getSequence(long sequenceId);
    long saveCustomSequence(Sequence seq, List<Integer> orderedSealIds); // 확정 id 반환
    List<Seal> getSeals();                    // 12종 인
}

interface DismissRepository {
    void recordResult(DismissResult result);  // is_success/duration/failCount 기록 + 성장 반영
    Stats getStats(long userId);
}

interface TutorialRepository {
    List<SealProgress> getProgress(long userId);
    void markCompleted(long userId, long sealId, boolean completed);
}

interface UserRepository {
    UserProfile getProfile(long userId);      // 닉네임·exp·현재 레벨·칭호
}
```

### 8.2 DTO (model 패키지 — 변경 금지)
```java
enum Difficulty { HAGEUP, JUNGGEUP, SANGGEUP, CHOESANGGEUP }   // label, perSealSec, fromSealCount(n)

class Seal        { int sealId; String name; String zodiac; int displayOrder; String imageUri; }
class SealUi      { int sealId; String name; String zodiac; String imageUri; }
class Sequence    { long sequenceId; String name; String nameEn; Difficulty difficulty;
                    boolean isCustom; List<Integer> orderedSealIds; }
class Alarm       { long alarmId; String timeHHmm; int repeatDays; String label;
                    int timeLimitSec; boolean enabled; long sequenceId;
                    String sequenceName; Difficulty difficulty; }
class UserProfile { String nickname; int exp; int currentLevel; String title;
                    int expIntoLevel; int expForLevel; }     // progressPercent() 제공
class Stats       { int totalAttempts; int successCount; int failCount; float avgDurationSec; }
class SealProgress{ int sealId; boolean completed; }
class DismissPlan { String sequenceName; String difficulty; int timeLimitSec; List<SealUi> orderedSeals; }
class DismissResult { long alarmId; boolean success; int durationSec; int failCount; }
```

### 8.3 교체 지점 (Repositories 로케이터)
현재:
```java
public static synchronized AlarmRepository alarm() {
    if (alarm == null) alarm = new FakeAlarmRepository();   // ← 이 줄만 RoomAlarmRepository 로 교체
    return alarm;
}
```
- BE 는 `RoomAlarmRepository(context)` 등 컨텍스트 주입형 구현을 만들고, `Repositories` 가 `Application` 컨텍스트를 보유하도록 초기화 방식을 추가한다(예: `Repositories.init(appContext)` 를 커스텀 `Application` 에서 호출). UI 호출부 `Repositories.alarm()` 는 그대로 유지.
- 규모가 커지면 Hilt/Dagger 로 승격 가능(11장: 무거운 의존성 추가 전 팀 확인).

---

## 9. 알람 스케줄링·발화 상세 (BE 핵심)

### 9.1 예약 계산
- `Alarm.repeatDays` 비트(6.2)와 `timeHHmm` 로 **다음 발화 시각**을 계산. 반복 없음(0)이면 1회성.
- `setEnabled(true)`/`upsert(enabled=true)` 시 예약, `false`/`delete` 시 취소. 같은 `alarm_id` 는 동일 `PendingIntent` requestCode 로 갱신.
- 정확 알람: API31+ 는 `SCHEDULE_EXACT_ALARM`/`USE_EXACT_ALARM` 권한 + `setAlarmClock()` 권장(잠금화면 표시·Doze 우회).

### 9.2 발화 → 해제 화면
- `AlarmReceiver` 가 트리거를 받아 `AlarmService`(Foreground) 시작.
- `AlarmService` 는 알람음/진동 재생 + 풀스크린 인텐트 알림으로 **`DismissActivity` 실행**:
  ```java
  Intent i = new Intent(ctx, DismissActivity.class)
      .putExtra(DismissActivity.EXTRA_ALARM_ID, alarmId)
      .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  ```
- 반복 알람이면 다음 회차를 재예약.

### 9.3 성공 핸드셰이크 (UI ↔ BE, **확정 필요**)
- 현재 `DismissActivity` 는 해제 성공 시 `DismissRepository.recordResult(success=true)` 호출 후 `finish()` 한다.
- 알람음을 멈추려면 `AlarmService` 가 "성공"을 알아야 한다. 아래 중 하나로 합의:
  1. **(권장)** `recordResult(success=true)` 구현 내부에서 `AlarmService` 에 정지 브로드캐스트/명령 전송.
  2. `DismissActivity` 가 종료 시 결과를 담은 브로드캐스트를 보내고 `AlarmService` 가 수신.
- 어느 쪽이든 **인터페이스 시그니처는 그대로** 두고 BE 내부에서 처리하는 1안이 UI 변경 0.

### 9.4 부팅/재부팅
- `BOOT_COMPLETED` 수신 시 활성 알람 전부 재예약(`RECEIVE_BOOT_COMPLETED`).

---

## 10. 인증·보안
- 비밀번호는 **평문 저장 금지** — PBKDF2(반복 횟수 충분)·bcrypt 등으로 해시 + per-user salt 저장.
- 세션 토큰/로그인 상태는 `EncryptedSharedPreferences` 등 안전한 저장소.
- 카메라 영상은 **온디바이스 처리, 서버 미전송**(UI/AI 원칙과 동일). 서버 동기화는 계정·통계 등 비영상 데이터로 한정.
- 입력 검증(이메일 형식·중복 가입)·에러 카피는 UI 카피 원칙(상대 문서 6.5)과 같은 톤으로.

---

## 11. 작업 방식 (에이전트 행동 규칙)
1. **항상 빌드 가능 상태 유지** — 인터페이스를 한 개씩 교체하고 매번 실행 확인. `assembleDebug`·`testDebugUnitTest`·`lintDebug` 통과.
2. **인터페이스/DTO 시그니처 변경 금지** — 8장은 계약. 변경 필요 시 UI 담당과 합의 후 동시 수정.
3. **`ui/`·`ml/` 수정 금지** — 연동은 `repository` 구현과 `Repositories` 교체로만.
4. **하드코딩 금지** — 문자열/치수/색은 리소스, 상수는 한 곳에.
5. **권한 처리** — 정확 알람·알림·포그라운드 서비스·부팅 수신 권한과 거부 시 안내.
6. **마이그레이션** — 스키마 변경 시 `Migration` 제공(파괴적 마이그레이션은 개발 초기에만).
7. **다음은 진행 전 반드시 질문** — ① 인터페이스/DTO 변경 ② 무거운 의존성(Hilt·Retrofit 등) ③ 스키마(ERD) 변경 ④ Kotlin 사용.
8. **결과 보고** — 무엇을 만들었고 어떻게 실행/확인하는지 짧은 메모(README 갱신).
9. **커밋 단위** — 인터페이스/기능 1개 = 1커밋, 메시지에 무엇을 했는지 명확히. (현재 작업 브랜치 컨벤션 따름)
10. **빌드 환경** — CLI 빌드 시 `JAVA_HOME` 이 미설정이면 Android Studio 번들 JDK 사용:
    `"$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'"` 후 `./gradlew assembleDebug`.

---

## 12. 산출물 체크리스트 (BE 파트)
- [ ] `data/` Room: Entity/DAO/@Database/Migration/Converters/시드(12간지·프리셋 6종)
- [ ] Entity ↔ `model` DTO 매퍼
- [ ] `Room*Repository` 5종 + `Repositories` 교체(`Fake*` → Room), `Repositories.init(appContext)`
- [ ] AlarmScheduler + AlarmReceiver(+BOOT) + AlarmService(풀스크린 → DismissActivity)
- [ ] 해제 성공 핸드셰이크(9.3)로 알람음 정지
- [ ] 경험치/레벨/칭호 계산 + DismissLog 통계 집계
- [ ] 인증(해시+salt, 세션) / (선택) 서버 동기화
- [ ] 권한·NotificationChannel·매니페스트 등록
- [ ] 빌드·테스트·lint 통과 + 실행 방법·구현 요약 README

---

## 부록 A. UI/AI 측 현재 구현 스냅샷 (참고)
- 패키지 `com.ninja.alarm`, Java + XML(Compose 금지), minSdk 24.
- 손동작 인식은 **YOLOX ONNX**(`ml/SealRecognizer`, `assets/yolox_nano_with_post.onnx`) + `SequenceMatcher`(디바운스·순서검증).
- 화면 11종(스플래시/로그인/알람목록/추가편집/해제/도감/커스텀빌더/튜토리얼/프로필/통계/설정) 모두 `Fake*Repository` 로 동작 중.
- `DismissActivity` 진입 파라미터: `EXTRA_ALARM_ID`(long), `EXTRA_SEQUENCE_ID`(long, 미리보기용).
- BE 가 `Repositories` 만 교체하면 위 화면이 실제 데이터로 즉시 동작하도록 설계됨.
