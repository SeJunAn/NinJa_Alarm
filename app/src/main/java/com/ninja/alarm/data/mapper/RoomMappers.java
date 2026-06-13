package com.ninja.alarm.data.mapper;

import com.ninja.alarm.data.dao.AlarmWithSequence;
import com.ninja.alarm.data.dao.StatsRow;
import com.ninja.alarm.data.entity.AlarmEntity;
import com.ninja.alarm.data.entity.SealEntity;
import com.ninja.alarm.data.entity.SealProgressEntity;
import com.ninja.alarm.data.entity.SequenceEntity;
import com.ninja.alarm.data.entity.UserEntity;
import com.ninja.alarm.model.Alarm;
import com.ninja.alarm.model.Difficulty;
import com.ninja.alarm.model.Seal;
import com.ninja.alarm.model.SealProgress;
import com.ninja.alarm.model.Sequence;
import com.ninja.alarm.model.Stats;
import com.ninja.alarm.model.UserProfile;

import java.util.ArrayList;
import java.util.List;

public final class RoomMappers {
    private RoomMappers() {}

    public static Difficulty difficultyFromDb(String value) {
        if (value == null) return Difficulty.HAGEUP;
        try {
            return Difficulty.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return Difficulty.HAGEUP;
        }
    }

    public static Seal toModel(SealEntity e) {
        if (e == null) return null;
        return new Seal(e.sealId, e.name, e.zodiac, e.displayOrder, e.imageUri);
    }

    public static List<Seal> toSealModels(List<SealEntity> entities) {
        List<Seal> out = new ArrayList<>();
        if (entities == null) return out;
        for (SealEntity e : entities) out.add(toModel(e));
        return out;
    }

    public static Sequence toModel(SequenceEntity e, List<Integer> orderedSealIds) {
        if (e == null) return null;
        return new Sequence(
                e.sequenceId,
                e.name,
                e.nameEn,
                difficultyFromDb(e.difficulty),
                e.isCustom == 1,
                orderedSealIds == null ? new ArrayList<>() : orderedSealIds
        );
    }

    public static Alarm toModel(AlarmWithSequence row) {
        if (row == null) return null;
        return new Alarm(
                row.alarmId,
                row.alarmTime,
                row.repeatDays,
                row.label,
                row.timeLimit,
                row.isEnabled == 1,
                row.sequenceId,
                row.sequenceName,
                difficultyFromDb(row.difficulty)
        );
    }

    public static AlarmEntity toEntity(Alarm alarm) {
        long now = System.currentTimeMillis();
        return new AlarmEntity(
                alarm.alarmId > 0 ? alarm.alarmId : 0,
                alarm.timeHHmm,
                alarm.repeatDays,
                alarm.label,
                alarm.timeLimitSec,
                alarm.enabled ? 1 : 0,
                alarm.sequenceId,
                now,
                now
        );
    }

    public static SealProgress toModel(SealProgressEntity e) {
        return new SealProgress(e.sealId, e.completed == 1);
    }

    public static List<SealProgress> toProgressModels(List<SealProgressEntity> entities) {
        List<SealProgress> out = new ArrayList<>();
        if (entities == null) return out;
        for (SealProgressEntity e : entities) out.add(toModel(e));
        return out;
    }

    public static Stats toModel(StatsRow row) {
        if (row == null) return new Stats(0, 0, 0, 0f);
        return new Stats(row.totalAttempts, row.successCount, row.failCount, row.avgDurationSec);
    }

    /**
     * 레벨별 누적 경험치 경계. index i 는 (i+1)레벨 진입에 필요한 누적 exp 다.
     * 표 범위를 넘어가면 마지막 간격(LEVEL_STEP)으로 외삽한다.
     * 레벨·칭호·진행도는 모두 이 표(=exp)에서만 파생되므로 서로 어긋날 수 없다.
     */
    private static final int[] LEVEL_FLOORS = {0, 100, 300, 700, 1200, 1800, 2500};
    private static final int LEVEL_STEP = 800;

    /** 1-based 레벨의 누적 경험치 하한. */
    private static int floorForLevel(int level) {
        if (level < 1) level = 1;
        if (level <= LEVEL_FLOORS.length) return LEVEL_FLOORS[level - 1];
        int last = LEVEL_FLOORS[LEVEL_FLOORS.length - 1];
        return last + (level - LEVEL_FLOORS.length) * LEVEL_STEP;
    }

    public static UserProfile toProfile(UserEntity user) {
        // 저장된 level/title 컬럼이 아니라 exp 를 단일 소스로 삼아 파생한다.
        // (시드/마이그레이션으로 컬럼이 틀어져도 화면 표시는 항상 일관됨)
        String nickname = user == null ? "그림자" : user.nickname;
        int exp = user == null ? 0 : user.exp;

        int level = levelForExp(exp);
        int floor = floorForLevel(level);
        int nextFloor = floorForLevel(level + 1);
        int expIntoLevel = Math.max(0, exp - floor);
        int expForLevel = Math.max(1, nextFloor - floor);

        return new UserProfile(nickname, exp, level, titleForExp(exp), expIntoLevel, expForLevel);
    }

    public static String titleForExp(int exp) {
        int level = levelForExp(exp);
        if (level >= 4) return "카게급";
        if (level >= 3) return "상급닌자";
        if (level >= 2) return "중급닌자";
        return "하급닌자";
    }

    public static int levelForExp(int exp) {
        if (exp <= 0) return 1;
        int level = 1;
        while (exp >= floorForLevel(level + 1)) level++;
        return level;
    }
}
