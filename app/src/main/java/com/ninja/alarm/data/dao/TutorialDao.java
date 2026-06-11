package com.ninja.alarm.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ninja.alarm.data.entity.SealProgressEntity;

import java.util.List;

@Dao
public interface TutorialDao {
    @Query("SELECT * FROM seal_progress WHERE user_id = :userId ORDER BY seal_id ASC")
    List<SealProgressEntity> getProgress(long userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(SealProgressEntity progress);

    @Query("UPDATE seal_progress SET completed = :completed, completed_at = :completedAt WHERE user_id = :userId AND seal_id = :sealId")
    int updateCompleted(long userId, int sealId, int completed, long completedAt);
}
