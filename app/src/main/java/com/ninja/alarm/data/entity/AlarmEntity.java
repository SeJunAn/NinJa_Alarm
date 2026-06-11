package com.ninja.alarm.data.entity;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "alarms",
        foreignKeys = @ForeignKey(
                entity = SequenceEntity.class,
                parentColumns = "sequence_id",
                childColumns = "sequence_id",
                onDelete = ForeignKey.RESTRICT
        ),
        indices = {
                @Index("sequence_id"),
                @Index("alarm_time")
        }
)
public class AlarmEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "alarm_id")
    public long alarmId;

    @ColumnInfo(name = "alarm_time")
    public String alarmTime;

    /**
     * bit0=Mon ... bit6=Sun, 0 = one-shot
     */
    @ColumnInfo(name = "repeat_days")
    public int repeatDays;

    @Nullable
    @ColumnInfo(name = "label")
    public String label;

    @ColumnInfo(name = "time_limit")
    public int timeLimit;

    @ColumnInfo(name = "is_enabled")
    public int isEnabled;

    @ColumnInfo(name = "sequence_id")
    public long sequenceId;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    public AlarmEntity(long alarmId, String alarmTime, int repeatDays, @Nullable String label,
                       int timeLimit, int isEnabled, long sequenceId, long createdAt, long updatedAt) {
        this.alarmId = alarmId;
        this.alarmTime = alarmTime;
        this.repeatDays = repeatDays;
        this.label = label;
        this.timeLimit = timeLimit;
        this.isEnabled = isEnabled;
        this.sequenceId = sequenceId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
