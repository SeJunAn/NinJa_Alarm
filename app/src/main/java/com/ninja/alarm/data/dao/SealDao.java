package com.ninja.alarm.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ninja.alarm.data.entity.SealEntity;

import java.util.List;

@Dao
public interface SealDao {
    @Query("SELECT * FROM seals ORDER BY display_order ASC")
    List<SealEntity> getAll();

    @Query("SELECT * FROM seals WHERE seal_id = :sealId LIMIT 1")
    SealEntity getById(int sealId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SealEntity> seals);
}
