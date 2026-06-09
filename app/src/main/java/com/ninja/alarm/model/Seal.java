package com.ninja.alarm.model;

/**
 * 인(印) — 12간지에 대응하는 손모양 1종. (지시서 2.3)
 *
 * sealId 는 labels.csv 줄 번호와 동일하다(1=Ne(子) ... 12=I(亥)).
 */
public class Seal {
    public final int sealId;       // 1~12
    public final String name;      // 한글/영문 명 (예: "쥐 · Ne")
    public final String zodiac;    // 12간지 한자 (子丑寅…)
    public final int displayOrder; // 1~12
    public final String imageUri;  // 손모양 가이드 이미지 (없으면 null)

    public Seal(int sealId, String name, String zodiac, int displayOrder, String imageUri) {
        this.sealId = sealId;
        this.name = name;
        this.zodiac = zodiac;
        this.displayOrder = displayOrder;
        this.imageUri = imageUri;
    }

    /** 화면 표시용 경량 DTO 로 변환. (지시서 9장 SealUi) */
    public SealUi toUi() {
        return new SealUi(sealId, name, zodiac, imageUri);
    }
}
