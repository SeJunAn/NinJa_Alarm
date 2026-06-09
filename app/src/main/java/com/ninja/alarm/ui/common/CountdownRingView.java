package com.ninja.alarm.ui.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.ninja.alarm.R;

/**
 * 카운트다운 링 — 남은 시간 비율(0~1)을 원호로 채워 보여주는 커스텀 뷰.
 * 남은 시간이 적으면 실패색으로 전환한다. 중앙에는 남은 초를 그린다.
 */
public class CountdownRingView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arc = new RectF();

    private float remaining = 1f; // 0~1
    private int secondsLeft = 0;

    private int colorOk;
    private int colorLow;

    public CountdownRingView(Context context) {
        super(context);
        init();
    }

    public CountdownRingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        float stroke = dp(8);
        colorOk = ContextCompat.getColor(getContext(), R.color.shinobi);
        colorLow = ContextCompat.getColor(getContext(), R.color.shippai);

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(stroke);
        trackPaint.setColor(ContextCompat.getColor(getContext(), R.color.kuro_elevated));

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(stroke);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(colorOk);

        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.washi));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp(24));
        textPaint.setFakeBoldText(true);
    }

    /** 남은 비율(0~1)과 남은 초를 갱신. */
    public void setRemaining(float fraction, int secondsLeft) {
        this.remaining = Math.max(0f, Math.min(1f, fraction));
        this.secondsLeft = secondsLeft;
        progressPaint.setColor(this.remaining <= 0.25f ? colorLow : colorOk);
        setContentDescription(getContext().getString(R.string.cd_time_left, secondsLeft));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float pad = progressPaint.getStrokeWidth();
        arc.set(pad, pad, getWidth() - pad, getHeight() - pad);

        canvas.drawArc(arc, 0, 360, false, trackPaint);
        // 12시 방향에서 시계방향으로 남은 만큼
        canvas.drawArc(arc, -90, 360 * remaining, false, progressPaint);

        float cy = getHeight() / 2f - (textPaint.descent() + textPaint.ascent()) / 2f;
        canvas.drawText(String.valueOf(secondsLeft), getWidth() / 2f, cy, textPaint);
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }
}
