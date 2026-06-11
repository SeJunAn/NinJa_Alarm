package com.ninja.alarm.ml;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.List;

/**
 * 인(印) 인식기 — 기존 YOLOX ONNX 검출기를 감싼다. (지시서 결정: MediaPipe 대신 YOLOX 유지)
 *
 * YOLOX 는 12간지 인을 박스+클래스로 직접 검출하므로, 최상위 검출을 SealResult 로 변환한다.
 * labels.csv 줄 번호 = model class_id + 1 이고, 1~12 가 12간지(= seal_id)와 일치한다.
 */
public class SealRecognizer implements AutoCloseable {

    private final YoloxDetector detector;

    public SealRecognizer(Context context) throws Exception {
        this.detector = new YoloxDetector(context);
    }

    /**
     * 프레임 1장 인식. 최상위 검출을 SealResult 로 반환(없으면 EMPTY).
     *
     * @param frame   원본 프레임(ARGB_8888)
     * @param scoreTh 이 점수 미만은 버림
     */
    public SealResult recognize(Bitmap frame, float scoreTh) throws Exception {
        int w = frame.getWidth();
        int h = frame.getHeight();
        List<YoloxDetector.Detection> dets = detector.detect(frame, scoreTh);
        if (dets.isEmpty()) return SealResult.EMPTY;

        YoloxDetector.Detection top = dets.get(0);
        int sealId = top.classId + 1; // labels.csv 0번("None") 보정
        return new SealResult(
                sealId, top.score, true,
                clamp01(top.x1 / w), clamp01(top.y1 / h),
                clamp01(top.x2 / w), clamp01(top.y2 / h));
    }

    private static float clamp01(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }

    @Override
    public void close() throws Exception {
        detector.close();
    }
}
