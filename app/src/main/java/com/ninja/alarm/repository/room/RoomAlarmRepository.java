package com.ninja.alarm.repository.room;

import android.content.Context;

import com.ninja.alarm.alarm.AlarmScheduler;
import com.ninja.alarm.data.NinjaDatabase;
import com.ninja.alarm.data.dao.AlarmWithSequence;
import com.ninja.alarm.data.entity.AlarmEntity;
import com.ninja.alarm.data.mapper.RoomMappers;
import com.ninja.alarm.model.Alarm;
import com.ninja.alarm.repository.AlarmRepository;

import java.util.ArrayList;
import java.util.List;

public class RoomAlarmRepository implements AlarmRepository {
    private final NinjaDatabase db;
    private final AlarmScheduler scheduler;

    public RoomAlarmRepository(Context context) {
        this.db = NinjaDatabase.getInstance(context);
        this.scheduler = new AlarmScheduler(context);
    }

    @Override
    public List<Alarm> getAlarms() {
        List<Alarm> out = new ArrayList<>();
        List<AlarmWithSequence> rows = db.alarmDao().getAllWithSequence();
        for (AlarmWithSequence row : rows) {
            out.add(RoomMappers.toModel(row));
        }
        return out;
    }

    @Override
    public long upsert(Alarm alarm) {
        AlarmEntity entity = RoomMappers.toEntity(alarm);
        long now = System.currentTimeMillis();
        entity.updatedAt = now;

        if (alarm.alarmId <= 0) {
            entity.createdAt = now;
            long id = db.alarmDao().insert(entity);
            alarm.alarmId = id;
            if (alarm.enabled) scheduler.schedule(alarm);
            return id;
        } else {
            AlarmWithSequence old = db.alarmDao().getWithSequence(alarm.alarmId);
            entity.createdAt = old == null ? now : now;
            scheduler.cancel(alarm.alarmId);
            db.alarmDao().update(entity);
            if (alarm.enabled) scheduler.schedule(alarm);
            return alarm.alarmId;
        }
    }

    @Override
    public void setEnabled(long alarmId, boolean enabled) {
        db.alarmDao().setEnabled(alarmId, enabled ? 1 : 0, System.currentTimeMillis());
        if (enabled) {
            AlarmWithSequence row = db.alarmDao().getWithSequence(alarmId);
            scheduler.schedule(RoomMappers.toModel(row));
        } else {
            scheduler.cancel(alarmId);
        }
    }

    @Override
    public void delete(long alarmId) {
        scheduler.cancel(alarmId);
        db.alarmDao().delete(alarmId);
    }
}
