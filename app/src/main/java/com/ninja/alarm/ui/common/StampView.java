package com.ninja.alarm.ui.common;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.ninja.alarm.R;

/**
 * 시그니처 인장(印) 스탬프 — 해제 성공 시 "쾅" 찍히는 모션 + 먹 번짐. (지시서 6.1)
 *
 * 이 앱을 기억하게 만드는 단 하나의 보너스 요소. 모션 줄이기 옵션이 켜져 있으면
 * 애니메이션 없이 정적으로 즉시 표시한다.
 */
public class StampView extends View {

    private final Paint inkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sealFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sealStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint kanjiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF sealRect = new RectF();

    // 먹 번짐 위치(중심 기준 정규화 오프셋)와 상대 크기
    private static final float[][] BLOTS = {
            {0f, 0f, 1.0f}, {-0.35f, -0.2f, 0.5f}, {0.32f, -0.28f, 0.45f},
            {-0.28f, 0.32f, 0.55f}, {0.34f, 0.3f, 0.5f}, {0f, -0.42f, 0.4f}
    };

    private String kanji = "解"; // 해제
    private float inkProgress = 0f;
    private float stampProgress = 0f;

    @Nullable private Runnable onEnd;

    public StampView(Context context) {
        super(context);
        init();
    }

    public StampView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inkPaint.setColor(ContextCompat.getColor(getContext(), R.color.sumi));
        sealFill.setColor(ContextCompat.getColor(getContext(), R.color.hi));
        sealStroke.setStyle(Paint.Style.STROKE);
        sealStroke.setColor(ContextCompat.getColor(getContext(), R.color.washi));
        kanjiPaint.setColor(ContextCompat.getColor(getContext(), R.color.washi));
        kanjiPaint.setTextAlign(Paint.Align.CENTER);
        kanjiPaint.setFakeBoldText(true);
        setContentDescription(getContext().getString(R.string.dismiss_success));
        setVisibility(GONE);
    }

    /** 스탬프를 재생. animated=false 면 즉시 정적 표시(모션 줄이기). */
    public void play(boolean animated, @Nullable Runnable onEnd) {
        this.onEnd = onEnd;
        setVisibility(VISIBLE);
        if (!animated) {
            inkProgress = 1f;
            stampProgress = 1f;
            invalidate();
            if (onEnd != null) postDelayed(onEnd, 600);
            return;
        }
        ValueAnimator ink = ValueAnimator.ofFloat(0f, 1f);
        ink.setDuration(500);
        ink.setInterpolator(new DecelerateInterpolator());
        ink.addUpdateListener(a -> {
            inkProgress = (float) a.getAnimatedValue();
            invalidate();
        });

        ValueAnimator stamp = ValueAnimator.ofFloat(0f, 1f);
        stamp.setDuration(420);
        stamp.setStartDelay(80);
        stamp.setInterpolator(new OvershootInterpolator(2.2f));
        stamp.addUpdateListener(a -> {
            stampProgress = (float) a.getAnimatedValue();
            invalidate();
        });
        stamp.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(android.animation.Animator animation) {
                if (StampView.this.onEnd != null) postDelayed(StampView.this.onEnd, 500);
            }
        });

        ink.start();
        stamp.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float base = Math.min(getWidth(), getHeight()) * 0.30f;

        // 먹 번짐
        if (inkProgress > 0f) {
            int alpha = (int) (130 * Math.min(1f, inkProgress * 2f));
            inkPaint.setAlpha(alpha);
            float spread = 0.4f + 0.6f * inkProgress;
            for (float[] b : BLOTS) {
                float bx = cx + b[0] * base * 1.6f;
                float by = cy + b[1] * base * 1.6f;
                canvas.drawCircle(bx, by, base * b[2] * spread, inkPaint);
            }
        }

        // 인장: 오버슈트로 쾅 찍힘
        if (stampProgress > 0f) {
            float scale = 1.0f + 0.8f * (1f - Math.min(stampProgress, 1f));
            int sealAlpha = (int) (255 * Math.min(1f, stampProgress * 2f));
            float half = base * scale;

            sealRect.set(cx - half, cy - half, cx + half, cy + half);
            float r = base * 0.28f;

            sealFill.setAlpha(sealAlpha);
            canvas.drawRoundRect(sealRect, r, r, sealFill);

            sealStroke.setAlpha(sealAlpha);
            sealStroke.setStrokeWidth(base * 0.06f);
            canvas.drawRoundRect(sealRect, r, r, sealStroke);

            kanjiPaint.setAlpha(sealAlpha);
            kanjiPaint.setTextSize(half * 1.1f);
            float ty = cy - (kanjiPaint.descent() + kanjiPaint.ascent()) / 2f;
            canvas.drawText(kanji, cx, ty, kanjiPaint);
        }
    }
}
