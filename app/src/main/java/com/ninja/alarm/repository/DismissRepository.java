package com.ninja.alarm.repository;

import com.ninja.alarm.model.DismissResult;
import com.ninja.alarm.model.Stats;

/**
 * 해제 결과 기록·통계 경계. (지시서 9장)
 * 통계·경험치 반영 계산은 BE 가 처리한다.
 */
public interface DismissRepository {
    void recordResult(DismissResult result);

    Stats getStats(long userId);
}
