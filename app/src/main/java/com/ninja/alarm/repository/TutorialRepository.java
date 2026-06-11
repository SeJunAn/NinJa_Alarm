package com.ninja.alarm.repository;

import com.ninja.alarm.model.SealProgress;

import java.util.List;

/**
 * 튜토리얼(인 학습) 진행 경계. (지시서 9장)
 */
public interface TutorialRepository {
    List<SealProgress> getProgress(long userId);

    void markCompleted(long userId, long sealId, boolean completed);
}
