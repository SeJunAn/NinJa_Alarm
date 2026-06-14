package com.ninja.alarm.ui.tutorial;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.ninja.alarm.R;
import com.ninja.alarm.model.Seal;
import com.ninja.alarm.model.SealProgress;
import com.ninja.alarm.repository.Repositories;
import com.ninja.alarm.util.Session;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 튜토리얼(인 학습) — 12종 인 가이드 + 완료 체크 + 진행률. (지시서 Phase 1 #8)
 * 완료 토글·진행 저장(Fake).
 */
public class TutorialActivity extends AppCompatActivity implements SealAdapter.Listener {

    private SealAdapter adapter;
    private TextView progressText;
    private LinearProgressIndicator progressBar;
    private int total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        progressText = findViewById(R.id.progressText);
        progressBar = findViewById(R.id.progressBar);

        RecyclerView list = findViewById(R.id.sealList);
        adapter = new SealAdapter(this);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        List<Seal> seals = Repositories.sequence().getSeals();
        total = seals.size();

        Set<Integer> completed = new HashSet<>();
        for (SealProgress p : Repositories.tutorial().getProgress(Session.userId(this))) {
            if (p.completed) completed.add(p.sealId);
        }

        adapter.submit(seals, completed);
        updateProgress(completed.size());
    }

    @Override
    public void onOpen(int position) {
        Intent intent = new Intent(this, SealDetailActivity.class);
        intent.putExtra(SealDetailActivity.EXTRA_START_POSITION, position);
        startActivity(intent);
    }

    @Override
    public void onToggle(int sealId, boolean completed) {
        Repositories.tutorial().markCompleted(Session.userId(this), sealId, completed);
        // 저장소에서 최신 완료 수를 다시 계산
        int done = 0;
        for (SealProgress p : Repositories.tutorial().getProgress(Session.userId(this))) {
            if (p.completed) done++;
        }
        updateProgress(done);
    }

    private void updateProgress(int done) {
        progressText.setText(getString(R.string.tutorial_progress, done, total));
        progressBar.setMax(total);
        progressBar.setProgress(done);
    }
}
