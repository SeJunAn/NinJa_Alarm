package com.ninja.alarm.ui.tutorial;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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

import com.google.android.material.appbar.MaterialToolbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.ninja.alarm.R;
import com.ninja.alarm.ml.Labels;
import com.ninja.alarm.ml.SealRecognitionListener;
import com.ninja.alarm.ml.SealRecognizer;
import com.ninja.alarm.ml.SealResult;
import com.ninja.alarm.ml.SequenceMatcher;
import com.ninja.alarm.model.Seal;
import com.ninja.alarm.repository.Repositories;
import com.ninja.alarm.ui.common.DetectionOverlayView;
import com.ninja.alarm.ui.common.SequenceProgressView;
import com.ninja.alarm.ui.common.StampView;
import com.ninja.alarm.util.AppPrefs;
import com.ninja.alarm.util.Session;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 인 연습(카메라) — 튜토리얼에서 단일 인 1개를 카메라로 맺어보고 정확히 맺었는지 검증한다.
 *
 * 해제 화면({@link com.ninja.alarm.ui.dismiss.DismissActivity})의 카메라·인식 파이프라인을
 * 재사용하되, 시퀀스 대신 목표 인 1개만 검증하고 시간 제한은 두지 않는다(학습용).
 * 성공하면 해당 인을 "학습 완료"로 표시하고 스탬프 모션 후 화면을 닫는다.
 */
public class SealPracticeActivity extends AppCompatActivity implements SealRecognitionListener {

    private static final String TAG = "SealPractice";
    private static final float SCORE_TH = 0.3f;     // 검출 점수 임계값
    private static final float CONF_TH = 0.5f;      // 확정 신뢰도 임계값
    private static final int DEBOUNCE_FRAMES = 5;   // 연속 N프레임(≈0.3초)
    private static final boolean USE_FRONT_CAMERA = true;

    /** 연습할 인의 sealId(1~12). */
    public static final String EXTRA_SEAL_ID = "extra_seal_id";

    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final ExecutorService analysisExecutor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean inferring = new AtomicBoolean(false);

    private PreviewView previewView;
    private TextView statusText;
    private DetectionOverlayView overlay;
    private SequenceProgressView progressView;
    private StampView stampView;

    private SealRecognizer recognizer;
    private Labels labels;
    private SequenceMatcher matcher;

    private int targetSealId = -1;
    private volatile boolean practiceActive = false;

    private final ActivityResultLauncher<String> requestPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startCamera();
                else Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_LONG).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_seal_practice);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.practice_root), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        previewView = findViewById(R.id.previewView);
        statusText = findViewById(R.id.statusText);
        overlay = findViewById(R.id.overlay);
        overlay.setMirror(USE_FRONT_CAMERA);
        progressView = findViewById(R.id.progressView);
        stampView = findViewById(R.id.stampView);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        targetSealId = getIntent().getIntExtra(EXTRA_SEAL_ID, -1);
        Seal target = findSeal(targetSealId);
        if (target == null) {
            Toast.makeText(this, R.string.seal_practice_waiting, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setupTarget(target);

        // 모델 로딩(무거움) → 준비되면 연습 시작
        worker.execute(() -> {
            try {
                labels = new Labels(this);
                recognizer = new SealRecognizer(this);
                runOnUiThread(this::beginPractice);
            } catch (Exception e) {
                Log.e(TAG, "init failed", e);
                runOnUiThread(() -> statusText.setText("init failed: " + e.getMessage()));
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermission.launch(Manifest.permission.CAMERA);
        }
    }

    private void setupTarget(Seal target) {
        ((TextView) findViewById(R.id.targetName)).setText(target.name);

        int art = SealPagerAdapter.artFor(target.sealId);
        if (art != 0) ((ImageView) findViewById(R.id.targetImage)).setImageResource(art);

        progressView.bind(Collections.singletonList(target.zodiac));
        progressView.setCurrentStep(0);
        matcher = new SequenceMatcher(Collections.singletonList(target.sealId),
                CONF_TH, DEBOUNCE_FRAMES, this);
    }

    private void beginPractice() {
        if (matcher == null) return;
        matcher.start(SystemClock.elapsedRealtime());
        statusText.setText(R.string.seal_practice_hint);
        practiceActive = true;
    }

    private Seal findSeal(int sealId) {
        for (Seal s : Repositories.sequence().getSeals()) {
            if (s.sealId == sealId) return s;
        }
        return null;
    }

    // === SealRecognitionListener (UI 스레드에서 호출됨) ===

    @Override
    public void onStepConfirmed(int stepIndex, int sealId) {
        progressView.setCurrentStep(stepIndex + 1);
        progressView.pulse(stepIndex, !AppPrefs.isReduceMotion(this));
    }

    @Override
    public void onSequenceSuccess(long durationMs) {
        practiceActive = false;
        statusText.setText(R.string.seal_practice_success);
        // 연습 성공 → 해당 인을 학습 완료로 표시
        Repositories.tutorial().markCompleted(Session.userId(this), targetSealId, true);
        // 성공 스탬프 모션(모션 줄이기 시 정적) → 끝나면 화면 종료
        stampView.play(!AppPrefs.isReduceMotion(this), this::finish);
    }

    @Override
    public void onTimeout(int failCount) {
        // 연습은 시간 제한이 없어 타임아웃을 사용하지 않는다.
    }

    // === CameraX (해제 화면과 동일 파이프라인) ===

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build();
                analysis.setAnalyzer(analysisExecutor, this::onFrame);

                CameraSelector selector = USE_FRONT_CAMERA
                        ? CameraSelector.DEFAULT_FRONT_CAMERA
                        : CameraSelector.DEFAULT_BACK_CAMERA;
                provider.unbindAll();
                provider.bindToLifecycle(this, selector, preview, analysis);
            } catch (Exception e) {
                Log.e(TAG, "camera start failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void onFrame(@NonNull ImageProxy image) {
        try {
            if (recognizer == null || !inferring.compareAndSet(false, true)) return;
            Bitmap bitmap = toBitmap(image);
            analyzeFrame(bitmap);
        } finally {
            image.close();
        }
    }

    private void analyzeFrame(Bitmap frame) {
        worker.execute(() -> {
            try {
                final SealResult result = recognizer.recognize(frame, SCORE_TH);
                final long now = SystemClock.elapsedRealtime();
                runOnUiThread(() -> {
                    updateOverlay(result);
                    if (practiceActive && matcher != null) {
                        matcher.onFrame(result.sealId, result.confidence, now);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "recognize failed", e);
            } finally {
                frame.recycle();
                inferring.set(false);
            }
        });
    }

    private void updateOverlay(SealResult r) {
        if (r.hasBox) {
            String jp = labels != null ? labels.nameJp(r.sealId) : "";
            overlay.setDetection(r.left, r.top, r.right, r.bottom, jp);
            if (practiceActive) {
                String name = labels != null ? labels.name(r.sealId) : String.valueOf(r.sealId);
                statusText.setText(String.format("%s  %.2f", name, r.confidence));
            }
        } else {
            overlay.clear();
            if (practiceActive) statusText.setText(R.string.seal_practice_waiting);
        }
    }

    /** RGBA_8888 ImageProxy → 회전 보정된 ARGB_8888 Bitmap. (해제 화면과 동일) */
    private static Bitmap toBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy plane = image.getPlanes()[0];
        ByteBuffer buffer = plane.getBuffer();
        int pixelStride = plane.getPixelStride();
        int rowStride = plane.getRowStride();
        int width = image.getWidth();
        int height = image.getHeight();
        int rowPadding = rowStride - pixelStride * width;

        Bitmap padded = Bitmap.createBitmap(
                width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        buffer.rewind();
        padded.copyPixelsFromBuffer(buffer);

        Bitmap cropped = Bitmap.createBitmap(padded, 0, 0, width, height);
        if (padded != cropped) padded.recycle();

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
        practiceActive = false;
        worker.execute(() -> {
            try {
                if (recognizer != null) recognizer.close();
            } catch (Exception ignored) {
            }
        });
        worker.shutdown();
        analysisExecutor.shutdown();
    }
}
