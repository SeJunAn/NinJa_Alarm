package com.ninja.alarm.ui.settings;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.ninja.alarm.R;
import com.ninja.alarm.util.AppPrefs;

/**
 * 설정 — 알람음·카메라 권한·계정·다크/모션 옵션. (지시서 Phase 1 #11)
 * 다크 테마는 브랜드 고정이라 안내만, 모션 줄이기는 저장해 Phase 3 모션에서 존중한다.
 */
public class SettingsActivity extends AppCompatActivity {

    private TextView cameraStatus;
    private TextView soundStatus;

    private final ActivityResultLauncher<Intent> soundPicker =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
                Uri uri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (uri == null) return;

                AppPrefs.setAlarmSound(this, uri.toString(), titleFor(uri));
                updateSoundStatus();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        cameraStatus = findViewById(R.id.cameraStatus);
        soundStatus = findViewById(R.id.soundStatus);

        findViewById(R.id.rowSound).setOnClickListener(v -> showSoundPicker());

        // 카메라 권한 행 → 시스템 앱 설정 열기
        findViewById(R.id.rowCamera).setOnClickListener(v -> openAppSettings());

        // 모션 줄이기 스위치
        MaterialSwitch motionSwitch = findViewById(R.id.motionSwitch);
        motionSwitch.setChecked(AppPrefs.isReduceMotion(this));
        motionSwitch.setOnCheckedChangeListener((btn, checked) ->
                AppPrefs.setReduceMotion(this, checked));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCameraStatus(); // 설정에서 권한을 바꾸고 돌아오면 갱신
        updateSoundStatus();
    }

    private void updateCameraStatus() {
        boolean granted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
        cameraStatus.setText(granted ? R.string.settings_camera_granted : R.string.settings_camera_denied);
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

    private void updateSoundStatus() {
        String title = AppPrefs.getAlarmSoundTitle(this);
        soundStatus.setText(title == null || title.trim().isEmpty()
                ? getString(R.string.add_alarm_sound_default)
                : title);
    }

    private void showSoundPicker() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.add_alarm_pick_sound))
                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentSoundUri());
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
}
