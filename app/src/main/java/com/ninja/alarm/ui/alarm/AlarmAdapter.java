package com.ninja.alarm.ui.alarm;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.ninja.alarm.R;
import com.ninja.alarm.model.Alarm;
import com.ninja.alarm.util.DayFormat;
import com.ninja.alarm.util.DifficultyUi;

import java.util.ArrayList;
import java.util.List;

/**
 * 알람 카드 어댑터 — 시간·요일·라벨·술법명·난이도 뱃지·ON/OFF 스위치.
 * 카드 클릭=편집, 길게 누르기=삭제, 스위치=활성 토글.
 */
public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.VH> {

    public interface Listener {
        void onClick(Alarm alarm);
        void onToggle(Alarm alarm, boolean enabled);
        void onLongClick(Alarm alarm);
    }

    private final List<Alarm> items = new ArrayList<>();
    private final Listener listener;

    public AlarmAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Alarm> alarms) {
        items.clear();
        items.addAll(alarms);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alarm, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Alarm a = items.get(position);
        h.time.setText(a.timeHHmm);

        String days = DayFormat.format(a.repeatDays);
        h.days.setText(a.label == null || a.label.isEmpty() ? days : days + " · " + a.label);

        h.sequence.setText(a.sequenceName);
        if (a.difficulty != null) {
            h.difficulty.setText(a.difficulty.label);
            int color = ContextCompat.getColor(h.itemView.getContext(),
                    DifficultyUi.badgeColor(a.difficulty));
            h.difficulty.setBackgroundTintList(ColorStateList.valueOf(color));
            h.difficulty.setVisibility(View.VISIBLE);
        } else {
            h.difficulty.setVisibility(View.GONE);
        }

        // 리스너를 떼고 상태를 세팅해 재활용 시 의도치 않은 콜백을 막는다.
        h.toggle.setOnCheckedChangeListener(null);
        h.toggle.setChecked(a.enabled);
        h.toggle.setOnCheckedChangeListener((btn, checked) -> listener.onToggle(a, checked));

        h.itemView.setOnClickListener(v -> listener.onClick(a));
        h.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(a);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView time, days, sequence, difficulty;
        final MaterialSwitch toggle;

        VH(@NonNull View v) {
            super(v);
            time = v.findViewById(R.id.alarmTime);
            days = v.findViewById(R.id.alarmDays);
            sequence = v.findViewById(R.id.alarmSequence);
            difficulty = v.findViewById(R.id.alarmDifficulty);
            toggle = v.findViewById(R.id.alarmSwitch);
        }
    }
}
