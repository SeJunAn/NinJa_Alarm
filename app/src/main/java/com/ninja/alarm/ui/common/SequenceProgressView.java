package com.ninja.alarm.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.ninja.alarm.R;
import com.ninja.alarm.util.SealText;

import java.util.List;

/**
 * 시퀀스 진행 뷰 — 맺어야 할 인을 ①②③ 순서로 보여주고, 완료/현재/대기 상태를 색으로 구분한다.
 */
public class SequenceProgressView extends LinearLayout {

    private int currentStep = 0;

    public SequenceProgressView(Context context) {
        super(context);
        init();
    }

    public SequenceProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
    }

    /** 인 시퀀스(12간지 한자)로 단계 칩들을 구성. */
    public void bind(List<String> zodiacs) {
        removeAllViews();
        int pad = (int) dp(6);
        for (int i = 0; i < zodiacs.size(); i++) {
            TextView tv = new TextView(getContext());
            tv.setText(SealText.circled(i + 1) + " " + zodiacs.get(i));
            tv.setTextSize(18);
            tv.setPadding(pad, pad / 2, pad, pad / 2);
            LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            lp.setMargins(pad / 2, 0, pad / 2, 0);
            tv.setLayoutParams(lp);
            addView(tv);
        }
        setCurrentStep(0);
    }

    /** 현재 맺을 단계(0-based)를 갱신해 색을 다시 칠한다. */
    public void setCurrentStep(int step) {
        this.currentStep = step;
        int done = ContextCompat.getColor(getContext(), R.color.seikou);
        int current = ContextCompat.getColor(getContext(), R.color.shinobi);
        int pending = ContextCompat.getColor(getContext(), R.color.kasumi);
        for (int i = 0; i < getChildCount(); i++) {
            TextView tv = (TextView) getChildAt(i);
            if (i < step) {
                tv.setTextColor(done);
                tv.setAlpha(0.7f);
            } else if (i == step) {
                tv.setTextColor(current);
                tv.setAlpha(1f);
                tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
            } else {
                tv.setTextColor(pending);
                tv.setAlpha(1f);
            }
        }
    }

    public int getCurrentStep() {
        return currentStep;
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }
}
