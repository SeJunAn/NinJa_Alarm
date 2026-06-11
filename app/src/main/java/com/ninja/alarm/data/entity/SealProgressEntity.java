package com.ninja.alarm.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "seal_progress",
        primaryKeys = {"user_id", "seal_id"},
        foreignKeys = {
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "user_id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = SealEntity.class,
                        parentColumns = "seal_id",
                        childColumns = "seal_id",
                        onDelete = ForeignKey.RESTRICT
                )
        },
        indices = {
                @Index("user_id"),
                @Index("seal_id")
        }
)
public class SealProgressEntity {
    @ColumnInfo(name = "user_id")
    public long userId;

    @ColumnInfo(name = "seal_id")
    public int sealId;

    @ColumnInfo(name = "completed")
    public int completed;

    @ColumnInfo(name = "completed_at")
    public long completedAt;

    public SealProgressEntity(long userId, int sealId, int completed, long completedAt) {
        this.userId = userId;
        this.sealId = sealId;
        this.completed = completed;
        this.completedAt = completedAt;
    }
}
