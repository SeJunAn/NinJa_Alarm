package com.ninja.alarm.util;

import androidx.annotation.ColorRes;

import com.ninja.alarm.R;
import com.ninja.alarm.model.Difficulty;

/**
 * 난이도 → 뱃지 색상 매핑. (색은 colors.xml 토큰만 사용)
 */
public final class DifficultyUi {

    private DifficultyUi() {}

    @ColorRes
    public static int badgeColor(Difficulty difficulty) {
        if (difficulty == null) return R.color.kasumi;
        switch (difficulty) {
            case HAGEUP:
                return R.color.seikou;
            case JUNGGEUP:
                return R.color.shinobi;
            case SANGGEUP:
                return R.color.hi;
            case CHOESANGGEUP:
            default:
                return R.color.shippai;
        }
    }
}
