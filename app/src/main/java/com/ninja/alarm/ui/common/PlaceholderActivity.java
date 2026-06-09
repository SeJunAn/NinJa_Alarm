package com.ninja.alarm.ui.common;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.ninja.alarm.R;

/**
 * 아직 구현되지 않은 화면을 위한 공용 스텁.
 * Phase 0 내비게이션을 끝까지 클릭 가능하게 만들기 위한 자리표시자이며,
 * Phase 1 에서 실제 화면(알람 추가/편집, 설정 등)으로 교체된다.
 */
public class PlaceholderActivity extends AppCompatActivity {

    private static final String EXTRA_TITLE = "extra_title";

    /** 제목만 다른 스텁 화면을 띄운다. */
    public static void start(@NonNull Context context, @NonNull String title) {
        Intent intent = new Intent(context, PlaceholderActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeholder);

        String title = getIntent().getStringExtra(EXTRA_TITLE);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setTitle(title != null ? title : getString(R.string.app_name));
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView body = findViewById(R.id.placeholderText);
        body.setText(R.string.placeholder_screen);
    }
}
