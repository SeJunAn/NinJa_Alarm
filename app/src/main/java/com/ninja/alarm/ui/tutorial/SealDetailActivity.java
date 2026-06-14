package com.ninja.alarm.ui.tutorial;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.ninja.alarm.R;
import com.ninja.alarm.model.Seal;
import com.ninja.alarm.repository.Repositories;

import java.util.List;

/**
 * 인 상세(맺는 법) — 12종 인을 좌우 스와이프로 넘기며 손모양 그림과 설명을 보여준다.
 * 튜토리얼 목록에서 항목을 탭하면 해당 인 위치로 열린다. (인 학습 → 상세)
 */
public class SealDetailActivity extends AppCompatActivity {

    /** 처음 보여줄 인의 목록상 위치(0-기반). */
    public static final String EXTRA_START_POSITION = "start_position";

    private TextView positionText;
    private LinearProgressIndicator positionBar;
    private int total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seal_detail);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        positionText = findViewById(R.id.positionText);
        positionBar = findViewById(R.id.positionBar);

        // 목록과 동일한 출처·순서를 사용해 위치(index)가 일치하도록 한다.
        List<Seal> seals = Repositories.sequence().getSeals();
        total = seals.size();
        String[] howtos = getResources().getStringArray(R.array.seal_howto);

        ViewPager2 pager = findViewById(R.id.sealPager);
        pager.setAdapter(new SealPagerAdapter(seals, howtos));
        positionBar.setMax(total);

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updatePosition(position);
            }
        });

        int start = clampStart(getIntent().getIntExtra(EXTRA_START_POSITION, 0));
        pager.setCurrentItem(start, false);
        updatePosition(start);
    }

    private int clampStart(int requested) {
        if (requested < 0) return 0;
        if (requested >= total) return total - 1;
        return requested;
    }

    private void updatePosition(int position) {
        positionText.setText(getString(R.string.seal_detail_position, position + 1, total));
        positionBar.setProgress(position + 1);
    }
}
