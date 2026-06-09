package com.ninja.alarm.ui.dismiss;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
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
import com.ninja.alarm.R;
import com.ninja.alarm.ml.Labels;
import com.ninja.alarm.ml.SealRecognitionListener;
import com.ninja.alarm.ml.SealRecognizer;
import com.ninja.alarm.ml.SealResult;
import com.ninja.alarm.ml.SequenceMatcher;
import com.ninja.alarm.model.DismissPlan;
import com.ninja.alarm.model.DismissResult;
import com.ninja.alarm.model.Seal;
import com.ninja.alarm.model.SealUi;
import com.ninja.alarm.model.Sequence;
import com.ninja.alarm.repository.Repositories;
import com.ninja.alarm.ui.common.CountdownRingView;
import com.ninja.alarm.ui.common.DetectionOverlayView;
import com.ninja.alarm.ui.common.SequenceProgressView;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 해제(인식) 화면 — 카메라로 인(印)을 순서대로 맺어 알람을 해제한다.
 *
 * 파이프라인: CameraX → SealRecognizer(YOLOX) → SequenceMatcher(디바운스·순서검증)
 *            → 성공/타임아웃 콜백. 카운트다운은 time_limit 으로 동작. (지시서 Phase 2)
 */
public class DismissActivity extends AppCompatActivity implements SealRecognitionListener {

    private static final String TAG = "Dismiss";
    private static final float SCORE_TH = 0.3f;     // 검출 점수 임계값
    private static final float CONF_TH = 0.5f;      // 확정 신뢰도 임계값
    private static final int DEBOUNCE_FRAMES = 5;   // 연속 N프레임(≈0.3초)
    private static final long DEFAULT_LIMIT_MS = 10_000L;
    private static final long SUCCESS_CLOSE_DELAY_MS = 1500L;

    public static final String EXTRA_ALARM_ID = "extra_alarm_id";
    public static final String EXTRA_SEQUENCE_ID = "extra_sequence_id";

    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final ExecutorService analysisExecutor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean inferring = new AtomicBoolean(false);

    private PreviewView previewView;
    private TextView statusText;
    private DetectionOverlayView overlay;
    private CountdownRingView countdownRing;
    private SequenceProgressView progressView;

    private SealRecognizer recognizer;
    private Labels labels;

    private DismissPlan plan;
    private SequenceMatcher matcher;
    private CountDownTimer timer;

    private long alarmId = -1;
    private long totalLimitMs = DEFAULT_LIMIT_MS;
    private int failCount = 0;
    private volatile boolean attemptActive = false;

    private final ActivityResultLauncher<String> requestPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startCamera();
                else Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_LONG).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dismiss);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dismiss_root), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        previewView = findViewById(R.id.previewView);
        statusText = findViewById(R.id.statusText);
        overlay = findViewById(R.id.overlay);
        countdownRing = findViewById(R.id.countdownRing);
        progressView = findViewById(R.id.progressView);

        alarmId = getIntent().getLongExtra(EXTRA_ALARM_ID, -1);
        plan = resolvePlan();
        setupPlan(plan);

        statusText.setText(R.string.dismiss_loading);

        // 모델 로딩(무거움) → 준비되면 첫 시도 시작
        worker.execute(() -> {
            try {
                labels = new Labels(this);
                recognizer = new SealRecognizer(this);
                Log.i(TAG, "recognizer loaded");
                runOnUiThread(this::beginAttempt);
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

    // === 해제 계획 → 매처/진행뷰 구성 ===

    private void setupPlan(DismissPlan plan) {
        ((TextView) findViewById(R.id.planTitle)).setText(plan.sequenceName + " · " + plan.difficulty);

        List<Integer> expected = new ArrayList<>();
        List<String> zodiacs = new ArrayList<>();
        for (SealUi s : plan.orderedSeals) {
            expected.add(s.sealId);
            zodiacs.add(s.zodiac);
        }
        progressView.bind(zodiacs);
        matcher = new SequenceMatcher(expected, CONF_TH, DEBOUNCE_FRAMES, this);

        totalLimitMs = plan.timeLimitSec > 0 ? plan.timeLimitSec * 1000L : DEFAULT_LIMIT_MS;
    }

    /** 새 시도 시작(최초/재시도 공통): 매처 리셋 + 카운트다운 시작. */
    private void beginAttempt() {
        if (matcher == null) return;
        matcher.start(SystemClock.elapsedRealtime());
        progressView.setCurrentStep(0);
        statusText.setText(R.string.dismiss_hint);
        startCountdown();
        attemptActive = true;
    }

    private void startCountdown() {
        if (timer != null) timer.cancel();
        timer = new CountDownTimer(totalLimitMs, 50) {
            @Override public void onTick(long msLeft) {
                float frac = (float) msLeft / totalLimitMs;
                countdownRing.setRemaining(frac, (int) Math.ceil(msLeft / 1000.0));
            }
            @Override public void onFinish() {
                countdownRing.setRemaining(0f, 0);
                attemptActive = false;
                failCount++;
                recordResult(false, (int) (totalLimitMs / 1000));
                if (matcher != null) matcher.notifyTimeout(failCount); // → onTimeout
            }
        }.start();
    }

    // === SealRecognitionListener (매처 콜백, UI 스레드에서 호출됨) ===

    @Override
    public void onStepConfirmed(int stepIndex, int sealId) {
        progressView.setCurrentStep(stepIndex + 1);
    }

    @Override
    public void onSequenceSuccess(long durationMs) {
        attemptActive = false;
        if (timer != null) timer.cancel();
        statusText.setText(R.string.dismiss_success);
        Toast.makeText(this, R.string.dismiss_success, Toast.LENGTH_SHORT).show();
        recordResult(true, (int) (durationMs / 1000));
        // TODO(Phase 3): 인장(印) 스탬프 성공 모션 + 먹 번짐
        statusText.postDelayed(this::finish, SUCCESS_CLOSE_DELAY_MS);
    }

    @Override
    public void onTimeout(int failCount) {
        statusText.setText(R.string.dismiss_retry);
        Toast.makeText(this, R.string.dismiss_retry, Toast.LENGTH_SHORT).show();
        // 같은 술법으로 재시도
        beginAttempt();
    }

    private void recordResult(boolean success, int durationSec) {
        Repositories.dismiss().recordResult(new DismissResult(alarmId, success, durationSec, failCount));
    }

    // === 진입 경로에 따른 해제 계획 ===

    private DismissPlan resolvePlan() {
        long sequenceId = getIntent().getLongExtra(EXTRA_SEQUENCE_ID, -1);
        if (sequenceId > 0) {
            Sequence seq = Repositories.sequence().getSequence(sequenceId);
            if (seq != null) {
                List<SealUi> seals = orderedSealUis(seq);
                int limit = seq.difficulty.recommendedTimeLimitSec(seq.sealCount());
                return new DismissPlan(seq.name, seq.difficulty.label, limit, seals);
            }
        }
        return Repositories.sequence().getDismissPlan(alarmId);
    }

    private List<SealUi> orderedSealUis(Sequence seq) {
        List<SealUi> seals = new ArrayList<>();
        List<Seal> all = Repositories.sequence().getSeals();
        for (int wantId : seq.orderedSealIds) {
            for (Seal s : all) {
                if (s.sealId == wantId) {
                    seals.add(s.toUi());
                    break;
                }
            }
        }
        return seals;
    }

    // === CameraX ===

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

                provider.unbindAll();
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis);
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
                    if (attemptActive && matcher != null) {
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

    /** 검출 박스/라벨 + 상태 텍스트 갱신. (UI 스레드) */
    private void updateOverlay(SealResult r) {
        if (r.hasBox) {
            String jp = labels != null ? labels.nameJp(r.sealId) : "";
            overlay.setDetection(r.left, r.top, r.right, r.bottom, jp);
            if (attemptActive) {
                String name = labels != null ? labels.name(r.sealId) : String.valueOf(r.sealId);
                statusText.setText(String.format("%s  %.2f", name, r.confidence));
            }
        } else {
            overlay.clear();
        }
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
        if (timer != null) timer.cancel();
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
