package com.ninja.alarm.ui.tutorial;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.ninja.alarm.R;
import com.ninja.alarm.model.Seal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 인 학습 카드 어댑터 — 12간지 한자·이름·학습 완료 체크.
 */
public class SealAdapter extends RecyclerView.Adapter<SealAdapter.VH> {

    public interface Listener {
        void onToggle(int sealId, boolean completed);
    }

    private final List<Seal> seals = new ArrayList<>();
    private final Set<Integer> completed = new HashSet<>();
    private final Listener listener;

    public SealAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Seal> items, Set<Integer> completedIds) {
        seals.clear();
        seals.addAll(items);
        completed.clear();
        completed.addAll(completedIds);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seal, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Seal s = seals.get(position);
        h.zodiac.setText(s.zodiac);
        h.name.setText(s.name);

        h.done.setOnCheckedChangeListener(null);
        h.done.setChecked(completed.contains(s.sealId));
        h.done.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) completed.add(s.sealId);
            else completed.remove(s.sealId);
            listener.onToggle(s.sealId, checked);
        });
    }

    @Override
    public int getItemCount() {
        return seals.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView zodiac, name;
        final MaterialCheckBox done;

        VH(@NonNull View v) {
            super(v);
            zodiac = v.findViewById(R.id.sealZodiac);
            name = v.findViewById(R.id.sealName);
            done = v.findViewById(R.id.sealDone);
        }
    }
}
