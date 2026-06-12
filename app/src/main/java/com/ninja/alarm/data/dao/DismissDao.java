package com.ninja.alarm.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ninja.alarm.data.entity.DismissLogEntity;

@Dao
public interface DismissDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(DismissLogEntity log);

    @Query("SELECT " +
            "COUNT(*) AS totalAttempts, " +
            "SUM(CASE WHEN is_success = 1 THEN 1 ELSE 0 END) AS successCount, " +
            "SUM(CASE WHEN is_success = 0 THEN 1 ELSE 0 END) AS failCount, " +
            "COALESCE(AVG(CASE WHEN is_success = 1 THEN duration_sec END), 0) AS avgDurationSec " +
            "FROM dismiss_logs WHERE user_id = :userId")
    StatsRow getStats(long userId);
}
