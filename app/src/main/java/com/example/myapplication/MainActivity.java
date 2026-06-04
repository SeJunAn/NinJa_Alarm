package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Yolox";
    private static final float SCORE_TH = 0.3f; // 이 점수 미만 검출은 버림

    // ONNX 추론은 무거우므로 UI 스레드가 아닌 단일 워커 스레드에서 돌린다.
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    // 카메라 프레임 분석 콜백용 executor
    private final ExecutorService analysisExecutor = Executors.newSingleThreadExecutor();
    // 추론 중이면 새 프레임을 버려 큐가 밀리지 않게 한다.
    private final AtomicBoolean inferring = new AtomicBoolean(false);

    private PreviewView previewView;
    private TextView statusText;

    private YoloxDetector detector;
    private Labels labels;

    // 카메라 권한 요청 런처
    private final ActivityResultLauncher<String> requestPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startCamera();
                } else {
                    Toast.makeText(this, "카메라 권한이 필요합니다", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        previewView = findViewById(R.id.previewView);
        statusText = findViewById(R.id.statusText);

        // 1) 모델/라벨 로딩 — 비용이 크므로 백그라운드에서 1회만.
        worker.execute(() -> {
            try {
                labels = new Labels(this);                 // assets/labels.csv
                detector = new YoloxDetector(this);        // assets/yolox_nano_with_post.onnx
                Log.i(TAG, "model loaded, labels=" + labels.size());
                runOnUiThread(() -> statusText.setText("ready"));
            } catch (Exception e) {
                Log.e(TAG, "init failed", e);
                runOnUiThread(() -> statusText.setText("init failed: " + e.getMessage()));
            }
        });

        // 2) 카메라 권한 확인 후 시작
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermission.launch(Manifest.permission.CAMERA);
        }
    }

    /** CameraX: Preview + ImageAnalysis 바인딩. */
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        // 최신 프레임만 유지(밀린 프레임 폐기)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        // YUV 변환 없이 바로 RGBA로 받음 → Bitmap 변환이 간단
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build();
                analysis.setAnalyzer(analysisExecutor, this::onFrame);

                // 후면 카메라
                CameraSelector selector = CameraSelector.DEFAULT_BACK_CAMERA;

                provider.unbindAll();
                provider.bindToLifecycle(this, selector, preview, analysis);
            } catch (Exception e) {
                Log.e(TAG, "camera start failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /** 카메라 프레임 1장 콜백 (analysisExecutor 스레드). */
    private void onFrame(@NonNull ImageProxy image) {
        try {
            // 아직 모델 로딩 전이거나 직전 추론이 진행 중이면 이 프레임은 버린다.
            if (detector == null || !inferring.compareAndSet(false, true)) {
                return;
            }
            Bitmap bitmap = toBitmap(image); // 회전 보정된 upright 비트맵
            analyzeFrame(bitmap);
        } finally {
            // ImageProxy는 반드시 닫아야 다음 프레임이 들어온다(비트맵은 이미 복사됨).
            image.close();
        }
    }

    /** 추론 실행 후 결과 처리. (worker 스레드) */
    private void analyzeFrame(Bitmap frame) {
        worker.execute(() -> {
            try {
                // === ONNX 추론: 반환값은 점수 내림차순 정렬된 검출 목록 ===
                List<YoloxDetector.Detection> dets = detector.detect(frame, SCORE_TH);

                if (!dets.isEmpty()) {
                    YoloxDetector.Detection top = dets.get(0);
                    // labels.csv 0번 줄("None")은 자리표시자라 모델 class_id 보다 1 크다.
                    // 레퍼런스(simple_demo.py / Ninjutsu_demo.py)와 동일하게 +1 해서 조회.
                    int labelId = top.classId + 1;
                    String name = labels.name(labelId);
                    Log.i(TAG, String.format("sign=%s labelId=%d (model=%d) score=%.2f box=[%.0f,%.0f,%.0f,%.0f]",
                            name, labelId, top.classId, top.score, top.x1, top.y1, top.x2, top.y2));
                    runOnUiThread(() ->
                            statusText.setText(String.format("%s  %.2f", name, top.score)));
                    onSignDetected(labelId, name, top.score);
                } else {
                    runOnUiThread(() -> statusText.setText("-"));
                }
            } catch (Exception e) {
                Log.e(TAG, "detect failed", e);
            } finally {
                frame.recycle();
                inferring.set(false); // 다음 프레임 허용
            }
        });
    }

    /**
     * 인(印) 1개가 인식되었을 때 호출. (2번 단계에서) 인 시퀀스를 누적해
     * jutsu.csv 패턴과 매칭되면 알람을 해제하는 로직을 여기 붙인다.
     */
    private void onSignDetected(int labelId, String name, float score) {
        // labelId = labels.csv 줄 번호(1=Ne(Rat) ... 15=Mizunoe). jutsu.csv 매칭의 기준 id.
        // TODO: 인 시퀀스 누적 → jutsu.csv 매칭 → 알람 해제
    }

    /** RGBA_8888 ImageProxy → 회전 보정된 ARGB_8888 Bitmap. */
    private static Bitmap toBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy plane = image.getPlanes()[0];
        ByteBuffer buffer = plane.getBuffer();
        int pixelStride = plane.getPixelStride();
        int rowStride = plane.getRowStride();
        int width = image.getWidth();
        int height = image.getHeight();
        int rowPadding = rowStride - pixelStride * width;

        // rowStride 패딩을 고려해 폭을 잡고 복사 후 잘라낸다.
        Bitmap padded = Bitmap.createBitmap(
                width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        buffer.rewind();
        padded.copyPixelsFromBuffer(buffer);

        Bitmap cropped = Bitmap.createBitmap(padded, 0, 0, width, height);
        if (padded != cropped) padded.recycle();

        // 센서 회전 보정 → upright
        int rotation = image.getImageInfo().getRotationDegrees();
        if (rotation != 0) {
            Matrix m = new Matrix();
            m.postRotate(rotation);
            Bitmap rotated = Bitmap.createBitmap(cropped, 0, 0,
                    cropped.getWidth(), cropped.getHeight(), m, true);
            if (rotated != cropped) cropped.recycle();
            return rotated;
        }
        return cropped;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        worker.execute(() -> {
            try {
                if (detector != null) detector.close();
            } catch (Exception ignored) {
            }
        });
        worker.shutdown();
        analysisExecutor.shutdown();
    }
}
