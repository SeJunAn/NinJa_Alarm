package com.ninja.alarm.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "dismiss_logs",
        foreignKeys = {
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "user_id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = {"user_id", "created_at"}),
                @Index("alarm_id")
        }
)
public class DismissLogEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "log_id")
    public long logId;

    @ColumnInfo(name = "user_id")
    public long userId;

    @ColumnInfo(name = "alarm_id")
    public long alarmId;

    @ColumnInfo(name = "is_success")
    public int isSuccess;

    @ColumnInfo(name = "duration_sec")
    public int durationSec;

    @ColumnInfo(name = "fail_count")
    public int failCount;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    public DismissLogEntity(long logId, long userId, long alarmId, int isSuccess,
                            int durationSec, int failCount, long createdAt) {
        this.logId = logId;
        this.userId = userId;
        this.alarmId = alarmId;
        this.isSuccess = isSuccess;
        this.durationSec = durationSec;
        this.failCount = failCount;
        this.createdAt = createdAt;
    }
}
