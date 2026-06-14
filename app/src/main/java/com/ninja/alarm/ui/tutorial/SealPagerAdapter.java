package com.ninja.alarm.ui.tutorial;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ninja.alarm.R;
import com.ninja.alarm.model.Seal;

import java.util.ArrayList;
import java.util.List;

/**
 * 인 상세 캐러셀 어댑터 — 한 페이지에 손모양 그림(자리표시자)·이름·맺는 법 설명.
 *
 * 손모양 그림이 준비되면 {@link Seal#imageUri} 에 채우는 것만으로 자리표시자(한자)가
 * 실제 이미지로 바뀐다. (현재는 모두 null 이라 한자 자리표시자가 보인다.)
 */
public class SealPagerAdapter extends RecyclerView.Adapter<SealPagerAdapter.VH> {

    /** sealId(1~12) → 손모양 일러스트(12간지). 인덱스 = sealId-1. */
    private static final int[] SEAL_ART = {
            R.drawable.rat,    // 1 子 쥐
            R.drawable.ox,     // 2 丑 소
            R.drawable.tiger,  // 3 寅 호랑이
            R.drawable.hare,   // 4 卯 토끼
            R.drawable.dragon, // 5 辰 용
            R.drawable.snake,  // 6 巳 뱀
            R.drawable.horse,  // 7 午 말
            R.drawable.ram,    // 8 未 양
            R.drawable.moneky, // 9 申 원숭이 (파일명 오타 'moneky' 그대로 참조)
            R.drawable.bird,   // 10 酉 닭
            R.drawable.dog,    // 11 戌 개
            R.drawable.boar,   // 12 亥 돼지
    };

    private final List<Seal> seals = new ArrayList<>();
    /** sealId(1~12) → 맺는 법 설명. seal_howto string-array(0-기반)에서 가져온다. */
    private final String[] howtos;

    public SealPagerAdapter(List<Seal> items, String[] howtos) {
        this.seals.addAll(items);
        this.howtos = howtos;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seal_page, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Seal s = seals.get(position);
        h.name.setText(s.name);
        h.zodiacLabel.setText(s.zodiac);
        h.howto.setText(howtoFor(s.sealId));

        // 카메라로 이 인 1개를 연습하는 화면 진입.
        h.practiceButton.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), SealPracticeActivity.class);
            intent.putExtra(SealPracticeActivity.EXTRA_SEAL_ID, s.sealId);
            v.getContext().startActivity(intent);
        });

        // 우선순위: 외부 실사 이미지(imageUri) > 도식형 벡터 > 한자 자리표시자.
        if (s.imageUri != null) {
            // 실제 손모양 그림(파일/원격 URI)이 준비된 경우.
            h.image.setImageURI(android.net.Uri.parse(s.imageUri));
            showImage(h);
        } else {
            int art = artFor(s.sealId);
            if (art != 0) {
                h.image.setImageResource(art);
                showImage(h);
            } else {
                // 마지막 폴백: 큰 한자를 원형 안에 표시.
                h.image.setVisibility(View.GONE);
                h.zodiacBig.setVisibility(View.VISIBLE);
                h.zodiacBig.setText(s.zodiac);
            }
        }
    }

    private static void showImage(VH h) {
        h.image.setVisibility(View.VISIBLE);
        h.zodiacBig.setVisibility(View.GONE);
    }

    /** sealId 에 해당하는 손모양 일러스트 리소스(범위를 벗어나면 0). 연습 화면에서도 재사용. */
    public static int artFor(int sealId) {
        int idx = sealId - 1;
        return (idx >= 0 && idx < SEAL_ART.length) ? SEAL_ART[idx] : 0;
    }

    /** sealId 에 해당하는 맺는 법 설명(배열 범위를 벗어나면 빈 문자열). */
    private String howtoFor(int sealId) {
        int idx = sealId - 1;
        if (howtos != null && idx >= 0 && idx < howtos.length) {
            return howtos[idx];
        }
        return "";
    }

    @Override
    public int getItemCount() {
        return seals.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView zodiacBig, name, zodiacLabel, howto;
        final View practiceButton;

        VH(@NonNull View v) {
            super(v);
            image = v.findViewById(R.id.sealImage);
            zodiacBig = v.findViewById(R.id.sealZodiacBig);
            name = v.findViewById(R.id.sealName);
            zodiacLabel = v.findViewById(R.id.sealZodiacLabel);
            howto = v.findViewById(R.id.sealHowto);
            practiceButton = v.findViewById(R.id.practiceButton);
        }
    }
}
