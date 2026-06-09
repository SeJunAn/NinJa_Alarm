package com.ninja.alarm.ui.settings;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        cameraStatus = findViewById(R.id.cameraStatus);

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
}
