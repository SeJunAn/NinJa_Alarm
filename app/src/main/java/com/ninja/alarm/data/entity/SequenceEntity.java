package com.ninja.alarm.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sequences")
public class SequenceEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "sequence_id")
    public long sequenceId;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "name_en")
    public String nameEn;

    /**
     * Difficulty enum name: HAGEUP, JUNGGEUP, SANGGEUP, CHOESANGGEUP
     */
    @ColumnInfo(name = "difficulty")
    public String difficulty;

    @ColumnInfo(name = "is_custom")
    public int isCustom;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    public SequenceEntity(long sequenceId, String name, String nameEn, String difficulty,
                          int isCustom, long createdAt) {
        this.sequenceId = sequenceId;
        this.name = name;
        this.nameEn = nameEn;
        this.difficulty = difficulty;
        this.isCustom = isCustom;
        this.createdAt = createdAt;
    }
}
