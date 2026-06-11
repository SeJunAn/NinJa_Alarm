package com.ninja.alarm.ml;

import java.util.List;

/**
 * 인 시퀀스 매칭 상태머신 — 디바운스 + step_order 순서 검증. (지시서 8.3)
 *
 * 안드로이드에 의존하지 않는 순수 로직이라 단위 테스트가 가능하다.
 * 시간/카운트다운은 호출 측(DismissActivity)이 관리하고, 타임아웃은 notifyTimeout 으로 알린다.
 *
 * 디바운스: 동일 인이 confidence ≥ 임계값으로 연속 N프레임 유지될 때만 "확정".
 * 확정 인이 기대 인과 일치하면 다음 단계로, 불일치면 무시(진행 상태 유지).
 */
public class SequenceMatcher {

    private final int[] expected;
    private final float confThreshold;
    private final int debounceFrames;
    private final SealRecognitionListener listener;

    private int currentStep;
    private int candidateSeal;
    private int candidateCount;
    private boolean finished;
    private long startMs;

    public SequenceMatcher(List<Integer> expectedSealIds, float confThreshold,
                           int debounceFrames, SealRecognitionListener listener) {
        this.expected = new int[expectedSealIds.size()];
        for (int i = 0; i < expected.length; i++) expected[i] = expectedSealIds.get(i);
        this.confThreshold = confThreshold;
        this.debounceFrames = Math.max(1, debounceFrames);
        this.listener = listener;
    }

    /** 새 시도 시작. */
    public void start(long nowMs) {
        currentStep = 0;
        candidateSeal = SealResult.NONE;
        candidateCount = 0;
        finished = false;
        startMs = nowMs;
    }

    /** 프레임 1장의 인식 결과를 투입. */
    public void onFrame(int sealId, float confidence, long nowMs) {
        if (finished) return;

        boolean stable = confidence >= confThreshold && sealId >= 1 && sealId <= 12;
        if (!stable) {
            // 손을 놓치거나 신뢰도가 낮으면 후보 초기화
            candidateSeal = SealResult.NONE;
            candidateCount = 0;
            return;
        }

        if (sealId == candidateSeal) {
            candidateCount++;
        } else {
            candidateSeal = sealId;
            candidateCount = 1;
        }

        if (candidateCount >= debounceFrames) {
            confirm(sealId, nowMs);
            // 다음 인은 다시 연속 프레임을 모아야 확정되도록 후보 초기화
            candidateSeal = SealResult.NONE;
            candidateCount = 0;
        }
    }

    private void confirm(int sealId, long nowMs) {
        if (sealId != expected[currentStep]) {
            return; // 기대 인과 다르면 무시(진행 유지)
        }
        currentStep++;
        listener.onStepConfirmed(currentStep - 1, sealId);
        if (currentStep >= expected.length) {
            finished = true;
            listener.onSequenceSuccess(nowMs - startMs);
        }
    }

    /** 호출 측 카운트다운이 만료되면 호출. failCount = 누적 실패 횟수. */
    public void notifyTimeout(int failCount) {
        if (finished) return;
        finished = true;
        listener.onTimeout(failCount);
    }

    public int currentStep() {
        return currentStep;
    }

    public int size() {
        return expected.length;
    }

    public int expectedAt(int index) {
        return expected[index];
    }

    public boolean isFinished() {
        return finished;
    }
}
