package com.ninja.alarm.repository.room;

import android.content.Context;

import com.ninja.alarm.data.NinjaDatabase;
import com.ninja.alarm.data.entity.SealProgressEntity;
import com.ninja.alarm.data.mapper.RoomMappers;
import com.ninja.alarm.model.SealProgress;
import com.ninja.alarm.repository.TutorialRepository;

import java.util.ArrayList;
import java.util.List;

public class RoomTutorialRepository implements TutorialRepository {
    private final NinjaDatabase db;

    public RoomTutorialRepository(Context context) {
        this.db = NinjaDatabase.getInstance(context);
    }

    @Override
    public List<SealProgress> getProgress(long userId) {
        List<SealProgressEntity> rows = db.tutorialDao().getProgress(userId);
        if (rows == null || rows.isEmpty()) {
            long now = System.currentTimeMillis();
            for (int sealId = 1; sealId <= 12; sealId++) {
                db.tutorialDao().upsert(new SealProgressEntity(userId, sealId, 0, 0));
            }
            rows = db.tutorialDao().getProgress(userId);
        }
        return RoomMappers.toProgressModels(rows);
    }

    @Override
    public void markCompleted(long userId, long sealId, boolean completed) {
        long completedAt = completed ? System.currentTimeMillis() : 0;
        int updated = db.tutorialDao().updateCompleted(userId, (int) sealId, completed ? 1 : 0, completedAt);
        if (updated == 0) {
            db.tutorialDao().upsert(new SealProgressEntity(
                    userId,
                    (int) sealId,
                    completed ? 1 : 0,
                    completedAt
            ));
        }
    }
}
