package com.ninja.alarm.ui.alarm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.ninja.alarm.R;
import com.ninja.alarm.model.Alarm;
import com.ninja.alarm.model.Sequence;
import com.ninja.alarm.repository.Repositories;
import com.ninja.alarm.ui.dismiss.DismissActivity;

import java.util.List;
import java.util.Locale;

/**
 * 알람 추가/편집. (지시서 Phase 1 #4)
 * 타임피커·요일 7토글·술법 선택(난이도 표시)·라벨. 저장 시 목록에 반영(Fake).
 */
public class AddAlarmActivity extends AppCompatActivity {

    public static final String EXTRA_ALARM_ID = "extra_alarm_id";

    private int hour = 7;
    private int minute = 0;
    private long editingId = -1;
    private Sequence selectedSequence;

    private Chip[] dayChips;
    private TextView timeValue, sequenceName, sequenceMeta;
    private TextInputEditText labelInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        timeValue = findViewById(R.id.timeValue);
        sequenceName = findViewById(R.id.sequenceName);
        sequenceMeta = findViewById(R.id.sequenceMeta);
        labelInput = findViewById(R.id.labelInput);

        dayChips = new Chip[]{
                findViewById(R.id.chipMon), findViewById(R.id.chipTue), findViewById(R.id.chipWed),
                findViewById(R.id.chipThu), findViewById(R.id.chipFri), findViewById(R.id.chipSat),
                findViewById(R.id.chipSun)
        };

        // 기본 선택 술법 = 첫 프리셋
        List<Sequence> sequences = Repositories.sequence().getSequences(true);
        if (!sequences.isEmpty()) selectedSequence = sequences.get(0);

        editingId = getIntent().getLongExtra(EXTRA_ALARM_ID, -1);
        if (editingId > 0) {
            toolbar.setTitle(R.string.edit_alarm_title);
            loadExisting(editingId);
        }

        renderTime();
        renderSequence();

        findViewById(R.id.pickTimeButton).setOnClickListener(v -> showTimePicker());
        findViewById(R.id.sequenceRow).setOnClickListener(v -> showSequencePicker());
        ((MaterialButton) findViewById(R.id.saveButton)).setOnClickListener(v -> save());
        ((MaterialButton) findViewById(R.id.tryDismissButton)).setOnClickListener(v -> tryDismiss());
    }

    private void loadExisting(long id) {
        for (Alarm a : Repositories.alarm().getAlarms()) {
            if (a.alarmId == id) {
                String[] hm = a.timeHHmm.split(":");
                hour = Integer.parseInt(hm[0]);
                minute = Integer.parseInt(hm[1]);
                for (int i = 0; i < dayChips.length; i++) {
                    dayChips[i].setChecked((a.repeatDays & (1 << i)) != 0);
                }
                labelInput.setText(a.label);
                Sequence seq = Repositories.sequence().getSequence(a.sequenceId);
                if (seq != null) selectedSequence = seq;
                return;
            }
        }
    }

    private void showTimePicker() {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText(R.string.add_alarm_pick_time)
                .build();
        picker.addOnPositiveButtonClickListener(v -> {
            hour = picker.getHour();
            minute = picker.getMinute();
            renderTime();
        });
        picker.show(getSupportFragmentManager(), "time");
    }

    private void showSequencePicker() {
        List<Sequence> sequences = Repositories.sequence().getSequences(true);
        String[] names = new String[sequences.size()];
        int checked = -1;
        for (int i = 0; i < sequences.size(); i++) {
            Sequence s = sequences.get(i);
            names[i] = s.name + " (" + s.difficulty.label + ")";
            if (selectedSequence != null && s.sequenceId == selectedSequence.sequenceId) checked = i;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.add_alarm_pick_sequence)
                .setSingleChoiceItems(names, checked, (dialog, which) -> {
                    selectedSequence = sequences.get(which);
                    renderSequence();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void renderTime() {
        timeValue.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
    }

    private void renderSequence() {
        if (selectedSequence == null) return;
        sequenceName.setText(selectedSequence.name);
        sequenceMeta.setText(selectedSequence.difficulty.label + " · "
                + getString(R.string.seal_count, selectedSequence.sealCount()));
    }

    private int currentRepeatDays() {
        int bits = 0;
        for (int i = 0; i < dayChips.length; i++) {
            if (dayChips[i].isChecked()) bits |= (1 << i);
        }
        return bits;
    }

    private Alarm buildAlarm() {
        int count = selectedSequence.sealCount();
        int timeLimit = selectedSequence.difficulty.recommendedTimeLimitSec(count);
        String label = labelInput.getText() == null ? "" : labelInput.getText().toString().trim();
        return new Alarm(
                editingId > 0 ? editingId : -1,
                String.format(Locale.getDefault(), "%02d:%02d", hour, minute),
                currentRepeatDays(),
                label,
                timeLimit,
                true,
                selectedSequence.sequenceId,
                selectedSequence.name,
                selectedSequence.difficulty);
    }

    private void save() {
        if (selectedSequence == null) {
            Toast.makeText(this, R.string.add_alarm_pick_sequence, Toast.LENGTH_SHORT).show();
            return;
        }
        Repositories.alarm().upsert(buildAlarm());
        Toast.makeText(this, R.string.action_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void tryDismiss() {
        if (selectedSequence == null) return;
        Intent intent = new Intent(this, DismissActivity.class);
        intent.putExtra(DismissActivity.EXTRA_SEQUENCE_ID, selectedSequence.sequenceId);
        startActivity(intent);
    }
}
