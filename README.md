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