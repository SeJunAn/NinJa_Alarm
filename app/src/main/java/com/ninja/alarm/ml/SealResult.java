package com.ninja.alarm.ml;

/**
 * 인식 결과 1건 — 분류된 인(seal_id)과 신뢰도, 그리고 정규화된 검출 박스. (지시서 8.5)
 *
 * sealId 는 labels.csv 의 12간지(1~12). 손이 안 잡히거나 12간지가 아니면 NONE.
 * 박스 좌표는 원본 프레임 기준 0~1 정규화 값(오버레이 그릴 때 뷰 크기로 환산).
 */
public class SealResult {

    public static final int NONE = 0;

    public final int sealId;       // 1~12, 없으면 NONE
    public final float confidence; // 0~1
    public final boolean hasBox;
    public final float left, top, right, bottom; // 정규화 0~1

    public static final SealResult EMPTY = new SealResult(NONE, 0f);

    public SealResult(int sealId, float confidence) {
        this(sealId, confidence, false, 0f, 0f, 0f, 0f);
    }

    public SealResult(int sealId, float confidence, boolean hasBox,
                      float left, float top, float right, float bottom) {
        this.sealId = sealId;
        this.confidence = confidence;
        this.hasBox = hasBox;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public boolean isSeal() {
        return sealId >= 1 && sealId <= 12;
    }
}
