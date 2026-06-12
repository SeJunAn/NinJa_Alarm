package com.ninja.alarm.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.ninja.alarm.R;
import com.ninja.alarm.model.UserProfile;
import com.ninja.alarm.repository.Repositories;
import com.ninja.alarm.util.AppPrefs;
import com.ninja.alarm.util.Session;

/**
 * 프로필/레벨 — 닉네임·칭호·레벨·경험치 바. 통계/튜토리얼 진입. (지시서 Phase 1 #9)
 */
public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        UserProfile profile = Repositories.user().getProfile(Session.userId(requireContext()));

        ((TextView) view.findViewById(R.id.profileNickname)).setText(profile.nickname);
        ((TextView) view.findViewById(R.id.profileTitle)).setText(profile.title);
        ((TextView) view.findViewById(R.id.profileLevel))
                .setText(getString(R.string.profile_level, profile.currentLevel));

        boolean animate = !AppPrefs.isReduceMotion(requireContext());

        LinearProgressIndicator expBar = view.findViewById(R.id.expBar);
        expBar.setMax(100);
        // setProgressCompat 가 채움을 애니메이션한다(모션 줄이기 시 즉시 표시).
        expBar.setProgressCompat(profile.progressPercent(), animate);

        ((TextView) view.findViewById(R.id.expText))
                .setText(getString(R.string.profile_exp, profile.expIntoLevel, profile.expForLevel));

        // 레벨업 느낌의 가벼운 칭호 팝
        if (animate) {
            View title = view.findViewById(R.id.profileTitle);
            title.setScaleX(0.8f);
            title.setScaleY(0.8f);
            title.animate().scaleX(1f).scaleY(1f).setStartDelay(150).setDuration(300).start();
        }

        view.findViewById(R.id.openStatsButton).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), StatsActivity.class)));
        view.findViewById(R.id.openTutorialButton).setOnClickListener(v ->
                startActivity(new Intent(requireContext(),
                        com.ninja.alarm.ui.tutorial.TutorialActivity.class)));
    }
}
