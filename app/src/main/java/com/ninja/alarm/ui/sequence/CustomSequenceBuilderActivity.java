package com.ninja.alarm.ui.sequence;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ninja.alarm.R;
import com.ninja.alarm.model.Difficulty;
import com.ninja.alarm.model.Seal;
import com.ninja.alarm.model.Sequence;
import com.ninja.alarm.repository.Repositories;
import com.ninja.alarm.util.SealText;

import java.util.ArrayList;
import java.util.List;

/**
 * 커스텀 술법 빌더 — 12종 인을 순서대로 탭해 시퀀스를 구성한다.
 * 난이도는 인 개수로 자동 표시(지시서 2.4), 저장 시 Fake 저장소에 반영. (지시서 Phase 1 #7)
 */
public class CustomSequenceBuilderActivity extends AppCompatActivity {

    private static final int MIN_SEALS = 3;

    private final List<Integer> order = new ArrayList<>(); // 선택한 seal_id 순서

    private TextView orderText, difficultyText;
    private TextInputLayout nameLayout;
    private TextInputEditText nameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_sequence);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        orderText = findViewById(R.id.orderText);
        difficultyText = findViewById(R.id.difficultyText);
        nameLayout = findViewById(R.id.nameLayout);
        nameInput = findViewById(R.id.nameInput);

        buildSealChips();

        ((MaterialButton) findViewById(R.id.clearButton)).setOnClickListener(v -> {
            order.clear();
            renderOrder();
        });
        ((MaterialButton) findViewById(R.id.saveButton)).setOnClickListener(v -> save());

        renderOrder();
    }

    private void buildSealChips() {
        ChipGroup group = findViewById(R.id.sealGroup);
        for (Seal seal : Repositories.sequence().getSeals()) {
            Chip chip = new Chip(this);
            chip.setText(seal.zodiac + "  " + seal.name);
            chip.setCheckable(false);
            chip.setClickable(true);
            chip.setChipBackgroundColorResource(R.color.kuro_elevated);
            chip.setTextColor(getColor(R.color.washi));
            chip.setOnClickListener(v -> {
                order.add(seal.sealId);
                renderOrder();
            });
            group.addView(chip);
        }
    }

    private void renderOrder() {
        if (order.isEmpty()) {
            orderText.setText(R.string.builder_order_empty);
            difficultyText.setText("");
        } else {
            orderText.setText(SealText.numberedZodiac(order));
            Difficulty diff = Difficulty.fromSealCount(order.size());
            difficultyText.setText(getString(R.string.builder_auto_difficulty, diff.label, order.size()));
        }
    }

    private void save() {
        String name = nameInput.getText() == null ? "" : nameInput.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            nameLayout.setError(getString(R.string.builder_name_empty));
            return;
        }
        nameLayout.setError(null);

        if (order.size() < MIN_SEALS) {
            Toast.makeText(this, R.string.builder_min_seals, Toast.LENGTH_SHORT).show();
            return;
        }

        Difficulty diff = Difficulty.fromSealCount(order.size());
        Sequence draft = new Sequence(-1, name, name, diff, true, order);
        Repositories.sequence().saveCustomSequence(draft, order);

        Toast.makeText(this, R.string.builder_saved, Toast.LENGTH_SHORT).show();
        finish();
    }
}
