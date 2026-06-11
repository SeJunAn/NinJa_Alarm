package com.ninja.alarm.repository.room;

import android.content.Context;

import com.ninja.alarm.data.NinjaDatabase;
import com.ninja.alarm.data.dao.AlarmWithSequence;
import com.ninja.alarm.data.entity.SealEntity;
import com.ninja.alarm.data.entity.SequenceEntity;
import com.ninja.alarm.data.mapper.RoomMappers;
import com.ninja.alarm.model.Difficulty;
import com.ninja.alarm.model.DismissPlan;
import com.ninja.alarm.model.Seal;
import com.ninja.alarm.model.SealUi;
import com.ninja.alarm.model.Sequence;
import com.ninja.alarm.repository.SequenceRepository;

import java.util.ArrayList;
import java.util.List;

public class RoomSequenceRepository implements SequenceRepository {
    private final NinjaDatabase db;

    public RoomSequenceRepository(Context context) {
        this.db = NinjaDatabase.getInstance(context);
    }

    @Override
    public DismissPlan getDismissPlan(long alarmId) {
        AlarmWithSequence alarm = db.alarmDao().getWithSequence(alarmId);

        Sequence sequence = null;
        int timeLimitSec = 0;

        if (alarm != null) {
            sequence = getSequence(alarm.sequenceId);
            timeLimitSec = alarm.timeLimit;
        }

        if (sequence == null) {
            SequenceEntity first = db.sequenceDao().getFirstSequence();
            if (first != null) {
                sequence = RoomMappers.toModel(
                        first,
                        db.sequenceDao().getOrderedSealIds(first.sequenceId)
                );
            }
        }

        if (sequence == null) {
            return new DismissPlan("-", "-", 0, new ArrayList<>());
        }

        if (timeLimitSec <= 0) {
            timeLimitSec = sequence.difficulty.recommendedTimeLimitSec(sequence.sealCount());
        }

        List<SealUi> orderedSeals = new ArrayList<>();
        for (int sealId : sequence.orderedSealIds) {
            SealEntity sealEntity = db.sealDao().getById(sealId);
            Seal seal = RoomMappers.toModel(sealEntity);
            if (seal != null) orderedSeals.add(seal.toUi());
        }

        return new DismissPlan(
                sequence.name,
                sequence.difficulty.label,
                timeLimitSec,
                orderedSeals
        );
    }

    @Override
    public List<Sequence> getSequences(boolean includeCustom) {
        List<Sequence> out = new ArrayList<>();
        List<SequenceEntity> entities = db.sequenceDao().getSequences(includeCustom ? 1 : 0);
        for (SequenceEntity e : entities) {
            out.add(RoomMappers.toModel(e, db.sequenceDao().getOrderedSealIds(e.sequenceId)));
        }
        return out;
    }

    @Override
    public Sequence getSequence(long sequenceId) {
        SequenceEntity entity = db.sequenceDao().getSequence(sequenceId);
        if (entity == null) return null;
        return RoomMappers.toModel(entity, db.sequenceDao().getOrderedSealIds(sequenceId));
    }

    @Override
    public long saveCustomSequence(Sequence seq, List<Integer> orderedSealIds) {
        Difficulty difficulty = Difficulty.fromSealCount(orderedSealIds.size());
        SequenceEntity entity = new SequenceEntity(
                0,
                seq.name,
                seq.nameEn,
                difficulty.name(),
                1,
                System.currentTimeMillis()
        );
        return db.sequenceDao().insertSequenceWithSteps(entity, orderedSealIds);
    }

    @Override
    public List<Seal> getSeals() {
        return RoomMappers.toSealModels(db.sealDao().getAll());
    }
}
