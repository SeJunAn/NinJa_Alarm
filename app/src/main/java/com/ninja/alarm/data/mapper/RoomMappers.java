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
                alarm.alarmId,
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

    public static UserProfile toProfile(UserEntity user) {
        if (user == null) {
            return new UserProfile("그림자", 0, 1, "하급닌자", 0, 100);
        }
        int expForLevel = 500;
        int expIntoLevel = Math.max(0, user.exp % expForLevel);
        return new UserProfile(
                user.nickname,
                user.exp,
                user.currentLevel,
                user.title,
                expIntoLevel,
                expForLevel
        );
    }

    public static String titleForExp(int exp) {
        if (exp >= 700) return "카게급";
        if (exp >= 300) return "상급닌자";
        if (exp >= 100) return "중급닌자";
        return "하급닌자";
    }

    public static int levelForExp(int exp) {
        if (exp < 100) return 1;
        if (exp < 300) return 2;
        if (exp < 700) return 3;
        return Math.max(4, 4 + ((exp - 700) / 300));
    }
}
