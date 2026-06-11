package com.ninja.alarm.ui.sequence;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ninja.alarm.R;
import com.ninja.alarm.model.Sequence;
import com.ninja.alarm.util.DifficultyUi;
import com.ninja.alarm.util.SealText;

import java.util.ArrayList;
import java.util.List;

/**
 * 술법 도감 카드 어댑터 — 이름·영문명·난이도 뱃지·인 시퀀스 미리보기·커스텀 표시.
 */
public class SequenceAdapter extends RecyclerView.Adapter<SequenceAdapter.VH> {

    public interface Listener {
        void onClick(Sequence sequence);
    }

    private final List<Sequence> items = new ArrayList<>();
    private final Listener listener;

    public SequenceAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Sequence> sequences) {
        items.clear();
        items.addAll(sequences);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sequence, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Sequence s = items.get(position);
        h.name.setText(s.name);
        h.nameEn.setText(s.nameEn);
        h.seals.setText(SealText.zodiac(s.orderedSealIds));

        h.difficulty.setText(s.difficulty.label);
        int color = ContextCompat.getColor(h.itemView.getContext(),
                DifficultyUi.badgeColor(s.difficulty));
        h.difficulty.setBackgroundTintList(ColorStateList.valueOf(color));

        h.customBadge.setVisibility(s.isCustom ? View.VISIBLE : View.GONE);
        h.itemView.setOnClickListener(v -> listener.onClick(s));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name, nameEn, seals, difficulty, customBadge;

        VH(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.seqName);
            nameEn = v.findViewById(R.id.seqNameEn);
            seals = v.findViewById(R.id.seqSeals);
            difficulty = v.findViewById(R.id.seqDifficulty);
            customBadge = v.findViewById(R.id.seqCustomBadge);
        }
    }
}
