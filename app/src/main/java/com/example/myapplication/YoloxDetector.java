package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * YOLOX(hand sign) ONNX 추론기.
 *
 * yolox_nano_with_post.onnx 전용 — 그래프에 디코드(grid/stride)와 NMS가 이미 병합되어 있어
 * 자바 쪽에서는 디코드/NMS를 구현하지 않는다.
 *
 * 모델 출력: name="batchno_classid_score_x1y1x2y2", shape=[N, 7]
 *   각 행 = [batch_no, class_id, score, x1, y1, x2, y2]
 *   좌표는 416x416 letterbox 입력 좌표계 → 원본 좌표로 되돌리려면 1/ratio.
 *
 * 전처리는 레퍼런스 Python 구현과 동일하다:
 *   - letterbox 패딩(114), 비율 유지(top-left 정렬)
 *   - 정규화 없음(0~255 float)
 *   - 채널 순서 BGR (cv2 기준), NCHW
 */
public class YoloxDetector implements AutoCloseable {

    public static final String MODEL_ASSET = "yolox_nano_with_post.onnx";
    private static final int INPUT_W = 416;
    private static final int INPUT_H = 416;
    private static final int PAD_VALUE = 114;

    /** 한 건의 검출 결과 (원본 이미지 픽셀 좌표). */
    public static class Detection {
        public final int classId;
        public final float score;
        public final float x1, y1, x2, y2;

        public Detection(int classId, float score, float x1, float y1, float x2, float y2) {
            this.classId = classId;
            this.score = score;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }

    private final OrtEnvironment env;
    private final OrtSession session;
    private final String inputName;

    public YoloxDetector(Context context) throws Exception {
        byte[] modelBytes = readAsset(context, MODEL_ASSET);
        env = OrtEnvironment.getEnvironment();
        OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
        // 필요 시: opts.addNnapi(); 등 가속 옵션을 여기서 추가
        session = env.createSession(modelBytes, opts);
        inputName = session.getInputNames().iterator().next();
    }

    /**
     * 추론 실행.
     *
     * @param bitmap   원본 비트맵(ARGB_8888 권장)
     * @param scoreTh  이 점수 미만 검출은 버림
     * @return 점수 내림차순으로 정렬된 검출 목록(원본 좌표계)
     */
    public List<Detection> detect(Bitmap bitmap, float scoreTh) throws Exception {
        final int srcW = bitmap.getWidth();
        final int srcH = bitmap.getHeight();

        // ratio = min(416/h, 416/w) — 레퍼런스와 동일
        final float ratio = Math.min((float) INPUT_H / srcH, (float) INPUT_W / srcW);
        final int resizedW = (int) (srcW * ratio);
        final int resizedH = (int) (srcH * ratio);

        // 114로 채운 캔버스에 좌상단 정렬로 리사이즈 비트맵 그리기 (letterbox)
        Bitmap canvasBmp = Bitmap.createBitmap(INPUT_W, INPUT_H, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(canvasBmp);
        canvas.drawColor(Color.rgb(PAD_VALUE, PAD_VALUE, PAD_VALUE));
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, resizedW, resizedH, true);
        canvas.drawBitmap(resized, null, new Rect(0, 0, resizedW, resizedH), new Paint(Paint.FILTER_BITMAP_FLAG));

        // NCHW, BGR, 0~255 float (정규화 없음)
        int[] pixels = new int[INPUT_W * INPUT_H];
        canvasBmp.getPixels(pixels, 0, INPUT_W, 0, 0, INPUT_W, INPUT_H);

        float[] chw = new float[3 * INPUT_W * INPUT_H];
        final int plane = INPUT_W * INPUT_H;
        for (int i = 0; i < plane; i++) {
            int p = pixels[i];
            float r = (p >> 16) & 0xFF;
            float g = (p >> 8) & 0xFF;
            float b = p & 0xFF;
            chw[i] = b;                 // channel 0 = B
            chw[plane + i] = g;         // channel 1 = G
            chw[2 * plane + i] = r;     // channel 2 = R
        }

        long[] shape = {1, 3, INPUT_H, INPUT_W};
        List<Detection> results = new ArrayList<>();
        try (OnnxTensor input = OnnxTensor.createTensor(env, FloatBuffer.wrap(chw), shape)) {
            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put(inputName, input);
            try (OrtSession.Result out = session.run(inputs)) {
                float[][] dets = (float[][]) out.get(0).getValue(); // [N, 7]
                if (dets != null) {
                    for (float[] d : dets) {
                        float score = d[2];
                        if (score < scoreTh) continue;
                        int classId = (int) d[1];
                        // 416 좌표계 → 원본 좌표계
                        float x1 = d[3] / ratio;
                        float y1 = d[4] / ratio;
                        float x2 = d[5] / ratio;
                        float y2 = d[6] / ratio;
                        // 경계 clamp
                        x1 = Math.max(0, x1);
                        y1 = Math.max(0, y1);
                        x2 = Math.min(x2, srcW);
                        y2 = Math.min(y2, srcH);
                        results.add(new Detection(classId, score, x1, y1, x2, y2));
                    }
                }
            }
        }

        resized.recycle();
        canvasBmp.recycle();

        Collections.sort(results, (a, b) -> Float.compare(b.score, a.score));
        return results;
    }

    private static byte[] readAsset(Context context, String name) throws Exception {
        try (InputStream is = context.getAssets().open(name)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) != -1) {
                bos.write(buf, 0, n);
            }
            return bos.toByteArray();
        }
    }

    @Override
    public void close() throws Exception {
        if (session != null) session.close();
        // OrtEnvironment 는 프로세스 공용 싱글턴이라 보통 닫지 않는다.
    }
}
