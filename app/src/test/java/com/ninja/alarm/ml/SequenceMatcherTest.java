package com.ninja.alarm.ml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SequenceMatcher 순수 로직 단위 테스트 (디바운스·순서검증·타임아웃).
 */
public class SequenceMatcherTest {

    /** 콜백 기록용 리스너. */
    private static class Recorder implements SealRecognitionListener {
        final List<Integer> confirmedSteps = new ArrayList<>();
        long successDurationMs = -1;
        int timeoutFailCount = -1;

        @Override public void onStepConfirmed(int stepIndex, int sealId) {
            confirmedSteps.add(sealId);
        }
        @Override public void onSequenceSuccess(long durationMs) {
            successDurationMs = durationMs;
        }
        @Override public void onTimeout(int failCount) {
            timeoutFailCount = failCount;
        }
    }

    private static void feed(SequenceMatcher m, int sealId, int frames, long nowMs) {
        for (int i = 0; i < frames; i++) m.onFrame(sealId, 0.9f, nowMs);
    }

    @Test
    public void confirmsSealsInOrderAndSucceeds() {
        Recorder rec = new Recorder();
        SequenceMatcher m = new SequenceMatcher(Arrays.asList(8, 6, 3), 0.5f, 3, rec);
        m.start(0);

        feed(m, 8, 3, 100);
        feed(m, 6, 3, 200);
        feed(m, 3, 3, 300);

        assertEquals(Arrays.asList(8, 6, 3), rec.confirmedSteps);
        assertTrue(m.isFinished());
        assertEquals(300, rec.successDurationMs);
    }

    @Test
    public void debounceNeedsConsecutiveFrames() {
        Recorder rec = new Recorder();
        SequenceMatcher m = new SequenceMatcher(Arrays.asList(8), 0.5f, 5, rec);
        m.start(0);

        feed(m, 8, 4, 10);          // 4프레임만 — 아직 확정 안 됨
        assertEquals(0, rec.confirmedSteps.size());
        assertFalse(m.isFinished());

        feed(m, 8, 1, 20);          // 5번째 — 확정
        assertEquals(1, rec.confirmedSteps.size());
        assertTrue(m.isFinished());
    }

    @Test
    public void lowConfidenceResetsDebounce() {
        Recorder rec = new Recorder();
        SequenceMatcher m = new SequenceMatcher(Arrays.asList(8), 0.5f, 3, rec);
        m.start(0);

        feed(m, 8, 2, 10);
        m.onFrame(8, 0.1f, 15);     // 낮은 신뢰도 → 후보 초기화
        feed(m, 8, 2, 20);          // 다시 2프레임뿐 → 미확정
        assertFalse(m.isFinished());
        feed(m, 8, 1, 25);          // 3프레임 채움 → 확정
        assertTrue(m.isFinished());
    }

    @Test
    public void wrongSealIsIgnored() {
        Recorder rec = new Recorder();
        SequenceMatcher m = new SequenceMatcher(Arrays.asList(8, 6), 0.5f, 3, rec);
        m.start(0);

        feed(m, 8, 3, 100);         // step0 확정
        feed(m, 9, 3, 150);         // 기대(6)와 다름 → 무시
        assertEquals(1, rec.confirmedSteps.size());
        assertEquals(1, m.currentStep());

        feed(m, 6, 3, 200);         // 올바른 인 → 성공
        assertTrue(m.isFinished());
        assertEquals(Arrays.asList(8, 6), rec.confirmedSteps);
    }

    @Test
    public void timeoutReportsFailCount() {
        Recorder rec = new Recorder();
        SequenceMatcher m = new SequenceMatcher(Arrays.asList(8, 6), 0.5f, 3, rec);
        m.start(0);

        feed(m, 8, 3, 100);
        m.notifyTimeout(2);

        assertTrue(m.isFinished());
        assertEquals(2, rec.timeoutFailCount);

        // 타임아웃 후 프레임은 무시
        feed(m, 6, 3, 200);
        assertEquals(1, rec.confirmedSteps.size());
    }
}
