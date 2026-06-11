package com.ninja.alarm.model;

/**
 * 사용자 프로필/레벨. (지시서 2.3)
 */
public class UserProfile {
    public final String nickname;
    public final int exp;            // 현재 경험치
    public final int currentLevel;
    public final String title;       // 칭호 (하급닌자→중급닌자→상급닌자→카게급)
    public final int expIntoLevel;   // 현재 레벨에서 쌓은 경험치
    public final int expForLevel;    // 현재 레벨 → 다음 레벨에 필요한 총량

    public UserProfile(String nickname, int exp, int currentLevel, String title,
                       int expIntoLevel, int expForLevel) {
        this.nickname = nickname;
        this.exp = exp;
        this.currentLevel = currentLevel;
        this.title = title;
        this.expIntoLevel = expIntoLevel;
        this.expForLevel = expForLevel;
    }

    /** 다음 레벨까지 진행률 0~100. */
    public int progressPercent() {
        if (expForLevel <= 0) return 0;
        return Math.min(100, Math.round(expIntoLevel * 100f / expForLevel));
    }
}
