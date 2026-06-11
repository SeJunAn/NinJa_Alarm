package com.ninja.alarm.data.entity;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "seals")
public class SealEntity {
    @PrimaryKey
    @ColumnInfo(name = "seal_id")
    public int sealId;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "zodiac")
    public String zodiac;

    @ColumnInfo(name = "display_order")
    public int displayOrder;

    @Nullable
    @ColumnInfo(name = "image_uri")
    public String imageUri;

    public SealEntity(int sealId, String name, String zodiac, int displayOrder, @Nullable String imageUri) {
        this.sealId = sealId;
        this.name = name;
        this.zodiac = zodiac;
        this.displayOrder = displayOrder;
        this.imageUri = imageUri;
    }
}
