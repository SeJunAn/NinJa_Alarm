package com.ninja.alarm.model;

/**
 * 튜토리얼(인 학습) 진행 상태 1건. (지시서 9장 TutorialRepository)
 */
public class SealProgress {
    public final int sealId;
    public boolean completed;

    public SealProgress(int sealId, boolean completed) {
        this.sealId = sealId;
        this.completed = completed;
    }
}
