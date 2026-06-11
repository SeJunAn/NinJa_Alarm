package com.ninja.alarm.repository.room;

import android.content.Context;
import android.content.Intent;

import com.ninja.alarm.data.NinjaDatabase;
import com.ninja.alarm.data.entity.DismissLogEntity;
import com.ninja.alarm.data.entity.UserEntity;
import com.ninja.alarm.data.mapper.RoomMappers;
import com.ninja.alarm.model.DismissResult;
import com.ninja.alarm.model.Stats;
import com.ninja.alarm.repository.DismissRepository;

public class RoomDismissRepository implements DismissRepository {
    public static final String ACTION_STOP_ALARM = "com.ninja.alarm.action.STOP_ALARM";
    private static final long DEFAULT_USER_ID = 1L;
    private static final int EXP_SUCCESS = 10;

    private final Context appContext;
    private final NinjaDatabase db;

    public RoomDismissRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.db = NinjaDatabase.getInstance(context);
    }

    @Override
    public void recordResult(DismissResult result) {
        long now = System.currentTimeMillis();
        db.dismissDao().insert(new DismissLogEntity(
                0,
                DEFAULT_USER_ID,
                result.alarmId,
                result.success ? 1 : 0,
                result.durationSec,
                result.failCount,
                now
        ));

        if (result.success) {
            UserEntity user = db.userDao().getUser(DEFAULT_USER_ID);
            if (user != null) {
                int newExp = user.exp + EXP_SUCCESS;
                int newLevel = RoomMappers.levelForExp(newExp);
                String newTitle = RoomMappers.titleForExp(newExp);
                db.userDao().updateGrowth(DEFAULT_USER_ID, newExp, newLevel, newTitle, now);
            }

            // Phase B2의 AlarmService가 이 액션을 받으면 알람음/진동을 정지하도록 연결.
            appContext.sendBroadcast(new Intent(ACTION_STOP_ALARM));
        }
    }

    @Override
    public Stats getStats(long userId) {
        return RoomMappers.toModel(db.dismissDao().getStats(userId));
    }
}
