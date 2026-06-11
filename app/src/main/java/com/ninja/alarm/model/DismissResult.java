package com.ninja.alarm.model;

/**
 * 해제 결과 기록. (지시서 9장 DismissRepository.recordResult)
 */
public class DismissResult {
    public final long alarmId;
    public final boolean success;
    public final int durationSec;
    public final int failCount;

    public DismissResult(long alarmId, boolean success, int durationSec, int failCount) {
        this.alarmId = alarmId;
        this.success = success;
        this.durationSec = durationSec;
        this.failCount = failCount;
    }
}
