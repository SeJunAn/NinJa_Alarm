# 인(印) 손모양 이미지 — AI 생성 & 교체 가이드

인 학습 튜토리얼 상세 화면(`SealDetailActivity`)의 손모양은 현재 **도식형 벡터**
(`res/drawable/seal_01.xml` ~ `seal_12.xml`)로 표시된다. 더 사실적인 손 그림으로
바꾸고 싶을 때 이 문서를 따른다.

## 표시 우선순위 (이미 배선됨)

`SealPagerAdapter`가 다음 순서로 그림을 고른다:

1. `Seal.imageUri` 가 있으면 그 이미지(파일/원격 URI) — 런타임·원격 교체용
2. 없으면 `seal_<sealId>` 드로어블(현재 도식형 벡터)
3. 그래도 없으면 한자 자리표시자

→ **교체 방법 두 가지:**
- **간단(권장)**: 생성한 이미지를 `seal_01`…`seal_12` 이름으로 `res/drawable`(또는
  `res/drawable-nodpi`)에 넣어 기존 벡터를 덮어쓴다. 코드 수정 불필요.
- **런타임**: `SealData`/Room의 `imageUri`에 파일·콘텐츠 URI를 채운다.

## 이미지 규격

- **형식**: PNG 또는 WebP, **투명 배경**
- **크기**: 1024×1024 (정사각, nodpi). 화면에선 약 188dp 원 안에 표시됨
- **여백**: 가장자리 ~12% 안전 여백(원형 마스크 안에 들어오도록)
- **색/톤**: 다크 배경 위에 표시되므로 밝은 선을 사용
  - 손/선: `#ECE7DD`(washi, 화선지색)
  - 강조(맺는 핵심 손가락): `#FF5A1F`(shinobi, 로고 오렌지)
  - 배경: 투명 (원형 카드 배경 `#1F1D24` 위에 얹힘)
- **스타일 통일**: 12장 모두 같은 시점·선 두께·조명. 손은 정면, 손목이 아래에서 올라오는 구도

## 공통 스타일 프리픽스 (모든 프롬프트 앞에 붙여 사용)

```
Minimal two-tone instructional illustration of a pair of human hands forming a
ninja hand seal, front view, wrists entering from the bottom. Clean thick line
art, off-white hands (#ECE7DD) on fully transparent background, the key fingers
of the seal highlighted in orange (#FF5A1F). Centered, square 1:1, generous
margin, no text, no background, flat vector look, consistent lighting.
Pose:
```

## 12개 인별 포즈 프롬프트

각 항목의 문장을 위 프리픽스 뒤에 이어 붙인다. (sealId / 한자 / 이름 / 포즈)

1. **子 · 쥐(Ne)** — both hands clasped together, only the two index fingers extended straight up with their tips touching, index fingers highlighted orange.
2. **丑 · 소(Ushi)** — both hands clasped, the two middle fingers extended straight up and tall, tips touching, middle fingers highlighted orange.
3. **寅 · 호랑이(Tora)** — palms pressed together prayer-style, index and middle fingers of both hands extended upward together, those fingers highlighted orange.
4. **卯 · 토끼(U)** — left hand a loose fist, right hand index and middle fingers extended and laid against the left wrist, right fingers highlighted orange.
5. **辰 · 용(Tatsu)** — fingers of both hands interlaced and pointing upward in a gentle fan, fingertips highlighted orange.
6. **巳 · 뱀(Mi)** — both hands clasped with palms firmly pressed together, no fingers raised, the pressed seam highlighted orange.
7. **午 · 말(Uma)** — hands clasped, the two index fingers extended and crossing each other diagonally to form an X, index fingers highlighted orange.
8. **未 · 양(Hitsuji)** — hands clasped, the two index fingers extended and brought together leaning into a peak/steeple shape, highlighted orange.
9. **申 · 원숭이(Saru)** — hands crossed over each other at the wrists, palms gripping, the crossing highlighted orange, no fingers raised.
10. **酉 · 닭(Tori)** — both palms and all fingers pressed flat together in a tall prayer (gassho) gesture pointing up, edges highlighted orange.
11. **戌 · 개(Inu)** — left palm open and flat, right hand a fist resting on top of the open palm, the fist highlighted orange.
12. **亥 · 돼지(I)** — fingers interlaced and the clasp turned over so the backs of the hands and knuckles face up, knuckles highlighted orange.

## 교체 후 확인

1. 12장을 `seal_01`…`seal_12`로 `res/drawable`에 배치(기존 `.xml` 벡터는 삭제 또는 덮어쓰기)
2. 빌드 후 프로필 → 인 학습 → 카드 탭 → 상세 캐러셀에서 각 인의 그림이 원형 안에
   바르게 들어오는지(여백·정렬) 확인
3. 도식형으로 되돌리려면 이 저장소의 벡터 버전을 복원하면 된다.
