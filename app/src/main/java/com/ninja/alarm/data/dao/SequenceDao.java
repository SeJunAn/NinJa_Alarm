package com.ninja.alarm.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.ninja.alarm.data.entity.SequenceEntity;
import com.ninja.alarm.data.entity.SequenceStepEntity;

import java.util.List;

@Dao
public interface SequenceDao {
    @Query("SELECT * FROM sequences WHERE (:includeCustom = 1 OR is_custom = 0) ORDER BY sequence_id ASC")
    List<SequenceEntity> getSequences(int includeCustom);

    @Query("SELECT * FROM sequences WHERE sequence_id = :sequenceId LIMIT 1")
    SequenceEntity getSequence(long sequenceId);

    @Query("SELECT seal_id FROM sequence_steps WHERE sequence_id = :sequenceId ORDER BY step_order ASC")
    List<Integer> getOrderedSealIds(long sequenceId);

    @Query("SELECT * FROM sequences ORDER BY sequence_id ASC LIMIT 1")
    SequenceEntity getFirstSequence();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSequence(SequenceEntity sequence);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSteps(List<SequenceStepEntity> steps);

    @Query("DELETE FROM sequence_steps WHERE sequence_id = :sequenceId")
    void deleteSteps(long sequenceId);

    @Transaction
    default long insertSequenceWithSteps(SequenceEntity sequence, List<Integer> orderedSealIds) {
        long id = insertSequence(sequence);
        deleteSteps(id);
        for (int i = 0; i < orderedSealIds.size(); i++) {
            insertStep(new SequenceStepEntity(id, orderedSealIds.get(i), i + 1));
        }
        return id;
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStep(SequenceStepEntity step);
}
