package com.ninja.alarm.ui.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.ninja.alarm.R;

/**
 * 검출 오버레이 — 최근 인식된 인의 박스와 라벨을 카메라 프리뷰 위에 그린다.
 * 박스는 정규화 좌표(0~1)로 받아 뷰 크기에 맞춰 환산한다. (랜드마크 오버레이 역할)
 */
public class DetectionOverlayView extends View {

    private final Paint boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelBg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelText = new Paint(Paint.ANTI_ALIAS_FLAG);

    private boolean hasBox = false;
    private float left, top, right, bottom; // 0~1
    private String label = "";

    public DetectionOverlayView(Context context) {
        super(context);
        init();
    }

    public DetectionOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(dp(3));
        boxPaint.setColor(ContextCompat.getColor(getContext(), R.color.shinobi));

        labelBg.setColor(ContextCompat.getColor(getContext(), R.color.shinobi));
        labelText.setColor(ContextCompat.getColor(getContext(), R.color.sumi));
        labelText.setTextSize(dp(14));
        labelText.setFakeBoldText(true);

        // 카메라 위에 박스를 그리는 장식용 오버레이 → 접근성 트리에서 제외
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    /** 정규화 박스(0~1)와 라벨로 갱신. */
    public void setDetection(float left, float top, float right, float bottom, String label) {
        this.hasBox = true;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.label = label == null ? "" : label;
        invalidate();
    }

    /** 검출 없음(손을 놓침). */
    public void clear() {
        hasBox = false;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!hasBox) return;

        float l = left * getWidth();
        float t = top * getHeight();
        float r = right * getWidth();
        float b = bottom * getHeight();
        canvas.drawRect(l, t, r, b, boxPaint);

        if (!label.isEmpty()) {
            float pad = dp(4);
            float tw = labelText.measureText(label);
            float th = labelText.getTextSize();
            float bgTop = Math.max(0, t - th - pad * 2);
            canvas.drawRect(l, bgTop, l + tw + pad * 2, bgTop + th + pad * 2, labelBg);
            canvas.drawText(label, l + pad, bgTop + th + pad / 2, labelText);
        }
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }
}
