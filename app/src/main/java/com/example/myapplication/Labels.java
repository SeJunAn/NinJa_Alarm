package com.example.myapplication;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * assets/labels.csv 로더.
 *
 * 각 줄 형식: "영문명,일본어"  (예: "Tora(Tiger),寅")
 * 줄 번호(0부터) = 모델의 class_id 와 일치한다.
 *   0=None, 1=Ne(Rat) ... 15=Mizunoe
 */
public class Labels {

    private final List<String> en = new ArrayList<>();  // 영문명
    private final List<String> jp = new ArrayList<>();   // 일본어(인 표기)

    public Labels(Context context) throws Exception {
        try (InputStream is = context.getAssets().open("labels.csv");
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) {
                    line = stripBom(line);  // UTF-8 BOM 제거
                    first = false;
                }
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                en.add(parts.length > 0 ? parts[0].trim() : "");
                jp.add(parts.length > 1 ? parts[1].trim() : "");
            }
        }
    }

    public int size() {
        return en.size();
    }

    /** class_id → 영문 라벨 (범위 밖이면 "id:N"). */
    public String name(int classId) {
        if (classId >= 0 && classId < en.size()) return en.get(classId);
        return "id:" + classId;
    }

    /** class_id → 일본어(인) 표기. */
    public String nameJp(int classId) {
        if (classId >= 0 && classId < jp.size()) return jp.get(classId);
        return "id:" + classId;
    }

    private static String stripBom(String s) {
        if (s != null && !s.isEmpty() && s.charAt(0) == '﻿') {
            return s.substring(1);
        }
        return s;
    }
}
