package com.ninja.alarm.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ninja.alarm.R;
import com.ninja.alarm.ui.alarm.AlarmListFragment;
import com.ninja.alarm.ui.profile.ProfileFragment;
import com.ninja.alarm.ui.sequence.SequenceListFragment;
import com.ninja.alarm.ui.settings.SettingsActivity;

/**
 * 홈 셸 — 하단 내비게이션으로 알람/도감/프로필 프래그먼트를 전환한다. (지시서 Phase 0)
 * 상단 툴바의 설정 메뉴로 설정 화면(스텁)에 진입한다.
 */
public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_alarm) {
                showFragment(new AlarmListFragment(), getString(R.string.alarm_title));
                return true;
            } else if (id == R.id.nav_sequence) {
                showFragment(new SequenceListFragment(), getString(R.string.sequence_title));
                return true;
            } else if (id == R.id.nav_profile) {
                showFragment(new ProfileFragment(), getString(R.string.profile_title));
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_alarm);
        }
    }

    private void showFragment(Fragment fragment, String title) {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setTitle(title);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
