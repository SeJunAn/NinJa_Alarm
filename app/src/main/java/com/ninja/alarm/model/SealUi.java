package com.ninja.alarm.model;

/**
 * 화면 표시용 인(印) DTO. (지시서 9장)
 */
public class SealUi {
    public final int sealId;
    public final String name;
    public final String zodiac;
    public final String imageUri;

    public SealUi(int sealId, String name, String zodiac, String imageUri) {
        this.sealId = sealId;
        this.name = name;
        this.zodiac = zodiac;
        this.imageUri = imageUri;
    }
}
