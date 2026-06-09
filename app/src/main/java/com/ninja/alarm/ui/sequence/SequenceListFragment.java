package com.ninja.alarm.ui.sequence;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ninja.alarm.R;
import com.ninja.alarm.model.Sequence;
import com.ninja.alarm.repository.Repositories;
import com.ninja.alarm.util.SealText;

import java.util.ArrayList;
import java.util.List;

/**
 * 술법 도감 — 프리셋 + 커스텀 술법 리스트, 난이도 뱃지, 인 시퀀스 미리보기.
 * 필터(전체/프리셋/커스텀), 카드 클릭 시 상세(맺는 순서) 다이얼로그. (지시서 Phase 1 #6)
 */
public class SequenceListFragment extends Fragment implements SequenceAdapter.Listener {

    private SequenceAdapter adapter;
    private ChipGroup filterGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sequence, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView list = view.findViewById(R.id.sequenceList);
        adapter = new SequenceAdapter(this);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);

        filterGroup = view.findViewById(R.id.filterGroup);
        filterGroup.setOnCheckedStateChangeListener((group, ids) -> applyFilter());

        view.findViewById(R.id.createSequenceFab).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CustomSequenceBuilderActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        applyFilter(); // 커스텀 저장 후 돌아오면 갱신
    }

    private void applyFilter() {
        List<Sequence> all = Repositories.sequence().getSequences(true);
        int checked = filterGroup.getCheckedChipId();
        List<Sequence> filtered = new ArrayList<>();
        for (Sequence s : all) {
            if (checked == R.id.filterPreset && s.isCustom) continue;
            if (checked == R.id.filterCustom && !s.isCustom) continue;
            filtered.add(s);
        }
        adapter.submit(filtered);
    }

    @Override
    public void onClick(Sequence sequence) {
        String body = getString(R.string.sequence_detail_order) + "\n\n"
                + SealText.numberedZodiac(sequence.orderedSealIds);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(sequence.name + " · " + sequence.difficulty.label)
                .setMessage(body)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
