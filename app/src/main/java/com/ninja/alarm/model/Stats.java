package com.ninja.alarm.model;

/**
 * 해제 통계 요약. (지시서 2.3 DismissLog 집계)
 */
public class Stats {
    public final int totalAttempts;
    public final int successCount;
    public final int failCount;
    public final float avgDurationSec; // 성공 해제 평균 소요(초)

    public Stats(int totalAttempts, int successCount, int failCount, float avgDurationSec) {
        this.totalAttempts = totalAttempts;
        this.successCount = successCount;
        this.failCount = failCount;
        this.avgDurationSec = avgDurationSec;
    }

    /** 성공률 0~100. */
    public int successRatePercent() {
        if (totalAttempts <= 0) return 0;
        return Math.round(successCount * 100f / totalAttempts);
    }
}
