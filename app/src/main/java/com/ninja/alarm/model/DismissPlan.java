package com.ninja.alarm.model;

import java.util.List;

/**
 * 해제 화면이 알람 1건에 대해 받아오는 해제 계획. (지시서 9장)
 */
public class DismissPlan {
    public final String sequenceName;
    public final String difficulty;       // 표시용 라벨
    public final int timeLimitSec;        // 총 제한시간(알람 스냅샷)
    public final List<SealUi> orderedSeals;

    public DismissPlan(String sequenceName, String difficulty, int timeLimitSec,
                       List<SealUi> orderedSeals) {
        this.sequenceName = sequenceName;
        this.difficulty = difficulty;
        this.timeLimitSec = timeLimitSec;
        this.orderedSeals = orderedSeals;
    }
}
