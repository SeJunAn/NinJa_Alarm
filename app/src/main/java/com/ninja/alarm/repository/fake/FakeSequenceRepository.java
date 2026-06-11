package com.ninja.alarm.repository.fake;

import com.ninja.alarm.model.Alarm;
import com.ninja.alarm.model.Difficulty;
import com.ninja.alarm.model.DismissPlan;
import com.ninja.alarm.model.Seal;
import com.ninja.alarm.model.SealUi;
import com.ninja.alarm.model.Sequence;
import com.ninja.alarm.repository.AlarmRepository;
import com.ninja.alarm.repository.SequenceRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 메모리 기반 Fake 술법 저장소.
 *
 * 프리셋 시드는 assets/jutsu.csv 의 술법 중 12간지(1~12)만 사용하는 것들을 골라
 * seal_id 시퀀스로 옮긴 것이다. BE 구현이 들어오면 이 클래스만 교체한다.
 *
 * TODO(BE): 실제 SequenceRepository(Room) 로 교체.
 */
public class FakeSequenceRepository implements SequenceRepository {

    private final AlarmRepository alarmRepository; // getDismissPlan 에서 알람 조회용
    private final List<Sequence> sequences = new ArrayList<>();
    private long nextId = 1;

    public FakeSequenceRepository(AlarmRepository alarmRepository) {
        this.alarmRepository = alarmRepository;
        seedPresets();
    }

    private void seedPresets() {
        // 子1 丑2 寅3 卯4 辰5 巳6 午7 未8 申9 酉10 戌11 亥12
        addPreset("분신술", "Clone Jutsu", Arrays.asList(8, 6, 3));            // 未巳寅
        addPreset("수란파술", "Water Trumpet", Arrays.asList(5, 3, 4));         // 辰寅卯
        addPreset("용화술", "Dragon Flame Jutsu", Arrays.asList(6, 5, 4, 3));   // 巳辰卯寅
        addPreset("변화술", "Substitution Jutsu", Arrays.asList(8, 12, 2, 11, 6)); // 未亥丑戌巳
        addPreset("호화구술", "Fireball Jutsu", Arrays.asList(6, 3, 9, 12, 7, 3)); // 巳寅申亥午寅
        addPreset("봉선화술", "Phoenix Flower Jutsu", Arrays.asList(1, 3, 11, 2, 4, 3)); // 子寅戌丑卯寅
    }

    private void addPreset(String name, String nameEn, List<Integer> sealIds) {
        Difficulty diff = Difficulty.fromSealCount(sealIds.size());
        sequences.add(new Sequence(nextId++, name, nameEn, diff, false, sealIds));
    }

    @Override
    public DismissPlan getDismissPlan(long alarmId) {
        Sequence seq = null;
        int timeLimit = 0;
        for (Alarm a : alarmRepository.getAlarms()) {
            if (a.alarmId == alarmId) {
                seq = getSequence(a.sequenceId);
                timeLimit = a.timeLimitSec;
                break;
            }
        }
        if (seq == null && !sequences.isEmpty()) {
            seq = sequences.get(0); // 알람을 못 찾으면 첫 프리셋으로 시연
        }
        if (seq == null) {
            return new DismissPlan("-", "-", 0, new ArrayList<>());
        }
        if (timeLimit <= 0) {
            timeLimit = seq.difficulty.recommendedTimeLimitSec(seq.sealCount());
        }
        List<SealUi> orderedSeals = new ArrayList<>();
        for (int sealId : seq.orderedSealIds) {
            Seal s = SealData.byId(sealId);
            if (s != null) orderedSeals.add(s.toUi());
        }
        return new DismissPlan(seq.name, seq.difficulty.label, timeLimit, orderedSeals);
    }

    @Override
    public List<Sequence> getSequences(boolean includeCustom) {
        List<Sequence> out = new ArrayList<>();
        for (Sequence s : sequences) {
            if (includeCustom || !s.isCustom) out.add(s);
        }
        return out;
    }

    @Override
    public Sequence getSequence(long sequenceId) {
        for (Sequence s : sequences) {
            if (s.sequenceId == sequenceId) return s;
        }
        return null;
    }

    @Override
    public long saveCustomSequence(Sequence seq, List<Integer> orderedSealIds) {
        Difficulty diff = Difficulty.fromSealCount(orderedSealIds.size());
        Sequence created = new Sequence(nextId++, seq.name, seq.nameEn, diff, true, orderedSealIds);
        sequences.add(created);
        return created.sequenceId;
    }

    @Override
    public List<Seal> getSeals() {
        return SealData.all();
    }
}
