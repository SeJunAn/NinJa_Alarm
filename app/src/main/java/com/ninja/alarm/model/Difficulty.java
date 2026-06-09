package com.ninja.alarm.model;

/**
 * 난이도 등급. 인 개수에 따라 등급이 정해지고, 등급마다 인당 제한시간이 다르다. (지시서 2.4)
 *
 * 총 제한시간은 알람의 time_limit(초)로 전달받아 사용하며 여기서 새로 계산하지 않는다.
 * perSealSec/classify 는 커스텀 빌더에서 난이도 자동 표시와 권장 시간 안내용으로만 쓴다.
 */
public enum Difficulty {
    HAGEUP("하급", 3.0f),
    JUNGGEUP("중급", 2.0f),
    SANGGEUP("상급", 1.5f),
    CHOESANGGEUP("최상급", 1.2f);

    public final String label;     // 화면 표시용 한글
    public final float perSealSec; // 인당 권장 시간(초)

    Difficulty(String label, float perSealSec) {
        this.label = label;
        this.perSealSec = perSealSec;
    }

    /** 인 개수 → 등급 (지시서 2.4: 3=하급, 4~5=중급, 6~7=상급, 8+=최상급) */
    public static Difficulty fromSealCount(int count) {
        if (count <= 3) return HAGEUP;
        if (count <= 5) return JUNGGEUP;
        if (count <= 7) return SANGGEUP;
        return CHOESANGGEUP;
    }

    /** 인 개수 기준 권장 총 제한시간(초) = 인당 시간 × 개수. (참고용) */
    public int recommendedTimeLimitSec(int count) {
        return Math.round(perSealSec * count);
    }
}
