package com.ninja.alarm.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.ninja.alarm.data.dao.AlarmDao;
import com.ninja.alarm.data.dao.DismissDao;
import com.ninja.alarm.data.dao.SealDao;
import com.ninja.alarm.data.dao.SequenceDao;
import com.ninja.alarm.data.dao.TutorialDao;
import com.ninja.alarm.data.dao.UserDao;
import com.ninja.alarm.data.entity.AlarmEntity;
import com.ninja.alarm.data.entity.DismissLogEntity;
import com.ninja.alarm.data.entity.SealEntity;
import com.ninja.alarm.data.entity.SealProgressEntity;
import com.ninja.alarm.data.entity.SequenceEntity;
import com.ninja.alarm.data.entity.SequenceStepEntity;
import com.ninja.alarm.data.entity.UserEntity;

@Database(
        entities = {
                SealEntity.class,
                SequenceEntity.class,
                SequenceStepEntity.class,
                AlarmEntity.class,
                UserEntity.class,
                DismissLogEntity.class,
                SealProgressEntity.class
        },
        version = 1,
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class NinjaDatabase extends RoomDatabase {
    private static final String DB_NAME = "ninja_alarm.db";
    private static volatile NinjaDatabase INSTANCE;

    public abstract SealDao sealDao();
    public abstract SequenceDao sequenceDao();
    public abstract AlarmDao alarmDao();
    public abstract UserDao userDao();
    public abstract DismissDao dismissDao();
    public abstract TutorialDao tutorialDao();

    public static NinjaDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (NinjaDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    NinjaDatabase.class,
                                    DB_NAME
                            )
                            // 현재 Repository 인터페이스가 동기식이라 UI 수정 없이 붙이기 위한 선택.
                            // 프로젝트가 안정화되면 Executor/LiveData 등 비동기 구조로 이전 권장.
                            .allowMainThreadQueries()
                            .addCallback(SEED_CALLBACK)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final Callback SEED_CALLBACK = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            seed(db);
        }
    };

    private static void seed(SupportSQLiteDatabase db) {
        long now = System.currentTimeMillis();

        // seals: 1=子(쥐) ... 12=亥(멧돼지)
        db.execSQL("INSERT OR IGNORE INTO seals(seal_id, name, zodiac, display_order, image_uri) VALUES (1, '쥐', '子', 1, NULL)");
        db.execSQL("INSERT OR IGNORE INTO seals(seal_id, name, zodiac, display_order, image_uri) VALUES (2, '소', '丑', 2, NULL)");
        db.execSQL("INSERT OR IGNORE INTO seals(seal_id, name, zodiac, display_order, image_uri) VALUES (3, '호랑이', '寅', 3, NULL)");
        db.execSQL("INSERT OR IGNORE INTO seals(seal_id, name, zodiac, display_order, image_uri) VALUES (4, '토끼', '卯', 4, NULL)");
        db.execSQL("INSERT OR IGNORE INTO seals(seal_id, name, zodiac, display_order, image_uri) VALUES (5, '용', '辰', 5, NULL)");
        db.execSQL("INSERT OR IGNORE INTO seals(seal_id, name, zodiac, display_order, image_uri) VALUES (6, '뱀', '巳', 6, NULL)");
        db.execSQL("INSERT OR IGNORE INTO seals(seal_id, name, zodiac, display_order, image_uri) VALUES (7, '말', '午', 7, NULL)");
        db.execSQL("INSERT OR IGNORE INTO seals(seal_id, name, zodiac, display_order, image_uri) VALUES (8, '양', '未', 8, NULL)");
        db.execSQL("INSERT OR IGNORE INTO seals(seal_id, name, zodiac, display_order, image_uri) VALUES (9, '원숭이', '申', 9, NULL)");
        db.execSQL("INSERT OR IGNORE INTO seals(seal_id, name, zodiac, display_order, image_uri) VALUES (10, '닭', '酉', 10, NULL)");
        db.execSQL("INSERT OR IGNORE INTO seals(seal_id, name, zodiac, display_order, image_uri) VALUES (11, '개', '戌', 11, NULL)");
        db.execSQL("INSERT OR IGNORE INTO seals(seal_id, name, zodiac, display_order, image_uri) VALUES (12, '멧돼지', '亥', 12, NULL)");

        // default local user
        db.execSQL("INSERT OR IGNORE INTO users(user_id, email, nickname, password_hash, password_salt, exp, current_level, title, created_at, updated_at) " +
                "VALUES (1, 'local@ninja.alarm', '그림자', NULL, NULL, 1340, 7, '중급닌자', " + now + ", " + now + ")");

        // preset sequences: FakeSequenceRepository와 동일한 6종
        insertSequence(db, 1, "분신술", "Clone Jutsu", "HAGEUP", now, new int[]{8, 6, 3});
        insertSequence(db, 2, "수란파술", "Water Trumpet", "HAGEUP", now, new int[]{5, 3, 4});
        insertSequence(db, 3, "용화술", "Dragon Flame Jutsu", "JUNGGEUP", now, new int[]{6, 5, 4, 3});
        insertSequence(db, 4, "변화술", "Substitution Jutsu", "JUNGGEUP", now, new int[]{8, 12, 2, 11, 6});
        insertSequence(db, 5, "호화구술", "Fireball Jutsu", "SANGGEUP", now, new int[]{6, 3, 9, 12, 7, 3});
        insertSequence(db, 6, "봉선화술", "Phoenix Flower Jutsu", "SANGGEUP", now, new int[]{1, 3, 11, 2, 4, 3});

        // FakeAlarmRepository와 같은 알람 2건. B1 확인용 seed.
        db.execSQL("INSERT OR IGNORE INTO alarms(alarm_id, alarm_time, repeat_days, label, time_limit, is_enabled, sequence_id, created_at, updated_at) " +
                "VALUES (1, '07:00', 31, '기상', 9, 1, 1, " + now + ", " + now + ")");
        db.execSQL("INSERT OR IGNORE INTO alarms(alarm_id, alarm_time, repeat_days, label, time_limit, is_enabled, sequence_id, created_at, updated_at) " +
                "VALUES (2, '09:30', 96, '주말 늦잠 방지', 9, 0, 5, " + now + ", " + now + ")");

        // tutorial progress rows
        for (int sealId = 1; sealId <= 12; sealId++) {
            db.execSQL("INSERT OR IGNORE INTO seal_progress(user_id, seal_id, completed, completed_at) " +
                    "VALUES (1, " + sealId + ", 0, 0)");
        }
    }

    private static void insertSequence(SupportSQLiteDatabase db, long id, String name, String nameEn,
                                       String difficulty, long now, int[] sealIds) {
        db.execSQL("INSERT OR IGNORE INTO sequences(sequence_id, name, name_en, difficulty, is_custom, created_at) " +
                "VALUES (" + id + ", '" + name + "', '" + nameEn + "', '" + difficulty + "', 0, " + now + ")");
        for (int i = 0; i < sealIds.length; i++) {
            db.execSQL("INSERT OR IGNORE INTO sequence_steps(sequence_id, seal_id, step_order) " +
                    "VALUES (" + id + ", " + sealIds[i] + ", " + (i + 1) + ")");
        }
    }
}
