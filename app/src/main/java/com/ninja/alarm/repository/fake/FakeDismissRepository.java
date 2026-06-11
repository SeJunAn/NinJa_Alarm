package com.ninja.alarm.repository.fake;

import android.util.Log;

import com.ninja.alarm.model.DismissResult;
import com.ninja.alarm.model.Stats;
import com.ninja.alarm.repository.DismissRepository;

/**
 * 메모리 기반 Fake 해제 결과/통계 저장소.
 * TODO(BE): 실제 DismissRepository 로 교체(경험치/레벨 반영은 BE).
 */
public class FakeDismissRepository implements DismissRepository {

    private int total = 24;
    private int success = 19;
    private int fail = 5;
    private float avgSec = 6.8f;

    @Override
    public void recordResult(DismissResult result) {
        total++;
        if (result.success) success++;
        else fail++;
        Log.i("FakeDismissRepo", "recordResult success=" + result.success
                + " dur=" + result.durationSec + "s failCount=" + result.failCount);
    }

    @Override
    public Stats getStats(long userId) {
        return new Stats(total, success, fail, avgSec);
    }
}
