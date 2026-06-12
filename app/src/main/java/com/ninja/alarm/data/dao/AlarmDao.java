package com.ninja.alarm.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ninja.alarm.data.entity.AlarmEntity;

import java.util.List;

@Dao
public interface AlarmDao {
    @Query("SELECT a.alarm_id, a.alarm_time, a.repeat_days, a.label, a.time_limit, " +
            "a.is_enabled, a.sequence_id, s.name AS sequence_name, s.difficulty " +
            "FROM alarms a LEFT JOIN sequences s ON a.sequence_id = s.sequence_id " +
            "ORDER BY a.alarm_time ASC, a.alarm_id ASC")
    List<AlarmWithSequence> getAllWithSequence();

    @Query("SELECT a.alarm_id, a.alarm_time, a.repeat_days, a.label, a.time_limit, " +
            "a.is_enabled, a.sequence_id, s.name AS sequence_name, s.difficulty " +
            "FROM alarms a LEFT JOIN sequences s ON a.sequence_id = s.sequence_id " +
            "WHERE a.alarm_id = :alarmId LIMIT 1")
    AlarmWithSequence getWithSequence(long alarmId);

    @Query("SELECT * FROM alarms WHERE is_enabled = 1")
    List<AlarmEntity> getEnabledAlarms();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AlarmEntity entity);

    @Update
    void update(AlarmEntity entity);

    @Query("UPDATE alarms SET is_enabled = :enabled, updated_at = :updatedAt WHERE alarm_id = :alarmId")
    void setEnabled(long alarmId, int enabled, long updatedAt);

    @Query("DELETE FROM alarms WHERE alarm_id = :alarmId")
    void delete(long alarmId);
}
