package com.ninja.alarm.ml;

/**
 * 시퀀스 인식 콜백. (지시서 8.5)
 */
public interface SealRecognitionListener {

    /** 단계 확정 — stepIndex(0-based) 의 기대 인이 sealId 로 확정됨. */
    void onStepConfirmed(int stepIndex, int sealId);

    /** 전체 시퀀스 성공. */
    void onSequenceSuccess(long durationMs);

    /** 제한시간 초과(타임아웃). failCount = 누적 실패 횟수. */
    void onTimeout(int failCount);
}
