package com.ninja.alarm.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "sequence_steps",
        primaryKeys = {"sequence_id", "step_order"},
        foreignKeys = {
                @ForeignKey(
                        entity = SequenceEntity.class,
                        parentColumns = "sequence_id",
                        childColumns = "sequence_id",
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
                @Index("sequence_id"),
                @Index("seal_id")
        }
)
public class SequenceStepEntity {
    @ColumnInfo(name = "sequence_id")
    public long sequenceId;

    @ColumnInfo(name = "seal_id")
    public int sealId;

    @ColumnInfo(name = "step_order")
    public int stepOrder;

    public SequenceStepEntity(long sequenceId, int sealId, int stepOrder) {
        this.sequenceId = sequenceId;
        this.sealId = sealId;
        this.stepOrder = stepOrder;
    }
}
