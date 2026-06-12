package com.ninja.alarm.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.ninja.alarm.R;
import com.ninja.alarm.model.Stats;
import com.ninja.alarm.repository.Repositories;
import com.ninja.alarm.util.Session;

/**
 * 통계 — 성공률·평균 소요·실패 횟수·총 시도. (지시서 Phase 1 #10)
 */
public class StatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        Stats stats = Repositories.dismiss().getStats(Session.userId(this));

        boolean empty = stats.totalAttempts <= 0;
        findViewById(R.id.statsEmpty).setVisibility(empty ? View.VISIBLE : View.GONE);

        ((TextView) findViewById(R.id.successRate))
                .setText(getString(R.string.stats_percent, stats.successRatePercent()));
        LinearProgressIndicator bar = findViewById(R.id.successBar);
        bar.setMax(100);
        bar.setProgress(stats.successRatePercent());

        bindRow(R.id.rowTotal, getString(R.string.stats_total),
                getString(R.string.stats_count, stats.totalAttempts));
        bindRow(R.id.rowAvg, getString(R.string.stats_avg_duration),
                getString(R.string.stats_seconds, stats.avgDurationSec));
        bindRow(R.id.rowFail, getString(R.string.stats_fail_count),
                getString(R.string.stats_count, stats.failCount));
    }

    /** include 한 통계 행에 라벨/값을 채운다(자식 id 는 행 단위로 스코프). */
    private void bindRow(int rowId, String label, String value) {
        View row = findViewById(rowId);
        ((TextView) row.findViewById(R.id.statLabel)).setText(label);
        ((TextView) row.findViewById(R.id.statValue)).setText(value);
    }
}
