package com.ninja.alarm.repository.fake;

import com.ninja.alarm.model.Seal;
import com.ninja.alarm.model.SealProgress;
import com.ninja.alarm.repository.TutorialRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 메모리 기반 Fake 튜토리얼 진행 저장소. 12종 인 각각의 학습 완료 여부를 유지한다.
 * TODO(BE): 실제 TutorialRepository 로 교체.
 */
public class FakeTutorialRepository implements TutorialRepository {

    private final Map<Integer, Boolean> completed = new LinkedHashMap<>();

    public FakeTutorialRepository() {
        // 앞쪽 4개 인은 이미 학습 완료한 것으로 시드
        for (Seal s : SealData.all()) {
            completed.put(s.sealId, s.sealId <= 4);
        }
    }

    @Override
    public List<SealProgress> getProgress(long userId) {
        List<SealProgress> out = new ArrayList<>();
        for (Map.Entry<Integer, Boolean> e : completed.entrySet()) {
            out.add(new SealProgress(e.getKey(), e.getValue()));
        }
        return out;
    }

    @Override
    public void markCompleted(long userId, long sealId, boolean done) {
        completed.put((int) sealId, done);
    }
}
