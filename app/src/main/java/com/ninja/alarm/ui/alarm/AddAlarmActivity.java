package com.ninja.alarm.ui.alarm;

import android.app.Activity;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.ninja.alarm.util.AppPrefs;

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
    private TextView timeValue, sequenceName, sequenceMeta, soundValue;
    private TextInputEditText labelInput;

    private final ActivityResultLauncher<Intent> soundPicker =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
                Uri uri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (uri == null) return;

                AppPrefs.setAlarmSound(this, uri.toString(), titleFor(uri));
                renderSound();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        timeValue = findViewById(R.id.timeValue);
        sequenceName = findViewById(R.id.sequenceName);
        sequenceMeta = findViewById(R.id.sequenceMeta);
        soundValue = findViewById(R.id.soundValue);
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
        renderSound();

        findViewById(R.id.pickTimeButton).setOnClickListener(v -> showTimePicker());
        findViewById(R.id.sequenceRow).setOnClickListener(v -> showSequencePicker());
        findViewById(R.id.soundRow).setOnClickListener(v -> showSoundPicker());
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

    private void renderSound() {
        String title = AppPrefs.getAlarmSoundTitle(this);
        soundValue.setText(title == null || title.trim().isEmpty()
                ? getString(R.string.add_alarm_sound_default)
                : title);
    }

    private void showSoundPicker() {
        Uri current = currentSoundUri();
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.add_alarm_pick_sound))
                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, current);
        soundPicker.launch(intent);
    }

    private Uri currentSoundUri() {
        String saved = AppPrefs.getAlarmSoundUri(this);
        if (saved != null && !saved.trim().isEmpty()) return Uri.parse(saved);

        Uri fallback = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (fallback == null) fallback = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        return fallback;
    }

    private String titleFor(Uri uri) {
        try {
            Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
            if (ringtone != null) {
                String title = ringtone.getTitle(this);
                if (title != null && !title.trim().isEmpty()) return title;
            }
        } catch (Exception ignored) {
        }
        return getString(R.string.add_alarm_sound_default);
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
