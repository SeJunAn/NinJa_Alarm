package com.ninja.alarm.ui.alarm;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ninja.alarm.R;
import com.ninja.alarm.model.Alarm;
import com.ninja.alarm.repository.Repositories;
import com.ninja.alarm.ui.dismiss.DismissActivity;

import java.util.List;

/**
 * 알람 목록(홈). AlarmRepository(Fake) 에서 알람을 읽어 카드로 보여준다.
 * 토글·삭제·빈 상태 카피·+ FAB(추가) 동작. (지시서 Phase 1 #3)
 */
public class AlarmListFragment extends Fragment implements AlarmAdapter.Listener {

    private AlarmAdapter adapter;
    private RecyclerView list;
    private View emptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alarm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        list = view.findViewById(R.id.alarmList);
        emptyState = view.findViewById(R.id.emptyState);

        adapter = new AlarmAdapter(this);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.addAlarmFab);
        fab.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddAlarmActivity.class)));

        // 빈 상태에서 해제 화면 흐름 체험(기본 술법으로)
        view.findViewById(R.id.previewDismissButton).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), DismissActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh(); // 추가/편집 후 돌아오면 갱신
    }

    private void refresh() {
        List<Alarm> alarms = Repositories.alarm().getAlarms();
        adapter.submit(alarms);
        boolean empty = alarms.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        list.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onClick(Alarm alarm) {
        Intent intent = new Intent(requireContext(), AddAlarmActivity.class);
        intent.putExtra(AddAlarmActivity.EXTRA_ALARM_ID, alarm.alarmId);
        startActivity(intent);
    }

    @Override
    public void onToggle(Alarm alarm, boolean enabled) {
        Repositories.alarm().setEnabled(alarm.alarmId, enabled);
    }

    @Override
    public void onLongClick(Alarm alarm) {
        new MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.alarm_delete_confirm)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.alarm_delete, (d, w) -> {
                    Repositories.alarm().delete(alarm.alarmId);
                    Toast.makeText(requireContext(), R.string.alarm_deleted, Toast.LENGTH_SHORT).show();
                    refresh();
                })
                .show();
    }
}
