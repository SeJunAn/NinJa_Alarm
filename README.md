<div align="center">

# 🥷 NinJa Alarm (印)

**손동작 인식 기반 알람 애플리케이션 — "알람 해제의 술(術)"**

「나루토」의 인(印)에서 영감을 받아, 정해진 **인 시퀀스(술법)** 를 카메라 앞에서 순서대로 맺어야만 알람이 해제되는 인터랙티브 모바일 알람 앱.

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Kotlin%2FJava-7F52FF?logo=kotlin&logoColor=white)
![Model](https://img.shields.io/badge/Model-YOLOv10-00FFFF)
![Dataset](https://img.shields.io/badge/Dataset-Kaggle-20BEFF?logo=kaggle&logoColor=white)
![DB](https://img.shields.io/badge/DB-Room%20(SQLite)-003B57?logo=sqlite&logoColor=white)

</div>

---

## 📖 소개

기존 알람은 버튼·스와이프로 무의식 중에 끄고 다시 잠들기 쉽다. **NinJa Alarm**은 사용자의 **각성(覺醒)** 을 유도하기 위해, 카메라 앞에서 **12종의 인(印)** 을 정해진 술법 순서대로 맺어야만 알람이 해제되도록 설계한 앱이다. 손동작 인식에는 Kaggle의 **Naruto Hand Sign Dataset**을 참고하였고, **YOLOv10 기반 모델**로 인(印)을 판별한다. 알람·술법·기록 데이터는 **Room(SQLite)** 로 관리한다.

> 예) `그림자 분신술` = 토끼 → 뱀 → 호랑이 를 제한시간 안에 순서대로 맺기

## 🎯 목표

- 손동작 미션을 통한 무의식적 알람 해제 방지
- 나루토 인(印) 컨셉 게이미피케이션 알람
- YOLOv10 기반 카메라 손동작 인식
- 12종 인(印) 술법 시퀀스 검증
- Room(SQLite) 기반 알람·술법·기록 관리

## ✨ 핵심 기능

| 구분 | 기능 | 설명 |
| --- | --- | --- |
| F-01 | **알람 설정** | 시간·요일 반복·라벨·알람음 등록 및 관리 |
| F-02 | **술법 선택** | 프리셋 + 12종 인으로 만드는 커스텀 시퀀스 |
| F-03 | **카메라 인 인식** | 카메라 화면에서 인(印) 손동작 인식 |
| F-04 | **YOLOv10 판별** | Naruto Hand Sign Dataset 기반 클래스 판별 |
| F-05 | **연속 시퀀스 판정** | 순서대로 모두 맺어야 해제, 틀리면 재시작 |
| F-06 | **DB 기록 관리** | 알람·술법·해제 기록·실패 횟수 저장 |
| F-07 | **기록 및 통계** | 해제 소요 시간·실패 횟수·사용 술법 확인 |

## 🖐️ 인(印) 12종 — 12간지 기반

쥐(子)·소(丑)·호랑이(寅)·토끼(卯)·용(辰)·뱀(巳)·말(午)·양(未)·원숭이(申)·닭(酉)·개(戌)·멧돼지(亥)

## 📦 데이터셋

- Dataset: **Naruto Hand Sign Dataset** (Kaggle)
- URL: https://www.kaggle.com/datasets/vikranthkanumuru/naruto-hand-sign-dataset
- 활용 목적: 나루토 인(印) 손동작 인식

## 🛠️ 기술 스택

| 분류 | 기술 |
| --- | --- |
| **개발 플랫폼** | Android (Kotlin / Java), Android Studio |
| **손동작 인식** | YOLOv10 |
| **데이터셋** | Kaggle Naruto Hand Sign Dataset |
| **데이터 처리** | Python, OpenCV, NumPy |
| **로컬 저장소** | Room (SQLite) |
| **앱 내부 로직** | Alarm Logic, Sequence Logic, Repository, DAO |
| **협업 도구** | GitHub, Notion, Discord |

## 🔁 전체 동작 흐름

```text
[알람 설정] -> [Room DB 저장] -> [알람 시간 도달]
   -> [카메라 실행] -> [YOLOv10 손동작 인식]
   -> [술법 시퀀스 검증] -> [성공 시 해제] -> [기록 DB 저장]
```

## 🔁 인식 파이프라인

```text
[카메라] 프레임 입력
   -> [YOLOv10] 손동작 인식
   -> [인(印) 판별] 현재 손동작 클래스 확인
   -> [시퀀스 검증] 목표 술법 순서 일치 확인
   -> [해제] 모든 시퀀스 일치 시 알람 해제
```

## 🧩 백엔드 / 동작 로직

외부 서버 없이 앱 내부에서 데이터를 처리하는 **로컬 백엔드 구조**. 데이터는 Room(SQLite)에 저장하고 Repository와 DAO로 관리한다.

| 구분 | 설명 |
| --- | --- |
| **Alarm Logic** | 설정 시간·반복 요일에 따라 알람 실행 |
| **Sequence Logic** | 인식된 손동작이 목표 술법 순서와 일치하는지 검증 |
| **Dismiss Logic** | 시퀀스 성공 시 알람 해제 처리 |
| **Record Logic** | 해제 성공 여부·실패 횟수·소요 시간 저장 |
| **User Progress Logic** | 튜토리얼 진행도·레벨·경험치 관리 |

## 🗂️ 데이터베이스 (ERD)

8개 테이블. 마스터(`seals`, `levels`)는 앱 최초 실행 시 seed로 채워지고, `sequence_steps`가 술법-인의 N:M 관계와 **"순서대로 맺기"(step_order)** 를 책임진다.

```text
users --1:N-- alarms --1:N-- dismiss_logs
  |              |
  | 1:N          | N:1
  v              v
sequences --1:N-- sequence_steps --N:1-- seals
  |
levels --1:N-- users        tutorial_progress --N:1-- seals
```

| 테이블 | 역할 |
| --- | --- |
| `users` | 회원·인증·레벨/경험치 |
| `alarms` | 알람 설정 |
| `seals` | 12종 인(12간지) — 마스터 |
| `sequences` | 술법(프리셋 + 커스텀) |
| `sequence_steps` | 시퀀스 단계(N:M, step_order) |
| `dismiss_logs` | 해제 기록(통계용) |
| `tutorial_progress` | 인 학습 진행 |
| `levels` | 레벨/칭호 — 마스터 |

## 🚀 시작하기

```bash
git clone https://github.com/SeJunAn/NinJa_Alarm.git
cd NinJa_Alarm
# Android Studio 에서 Open -> Gradle Sync
git checkout -b feature/your-task
```

- **Minimum SDK**: API 24+ (카메라·Room 고려)

## 👥 팀

| 이름 | 역할 | 담당 |
| --- | --- | --- |
| **김동환** | 팀장 · BE | 동작 로직 설계, DB 구축 |
| 권성주 | BE (DB) | DB 구축 및 연동 |
| 김해수 | FE | UI 구성 |
| 안세준 | ML · FE | 손동작 인식, UI 구성 |

---

<div align="center">

**「印을 맺어라. 잠에서 깨어나라.」**

</div>
