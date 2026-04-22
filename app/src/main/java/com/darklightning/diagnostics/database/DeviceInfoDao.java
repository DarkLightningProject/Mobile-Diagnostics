package com.darklightning.diagnostics.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DeviceInfoDao {
    @Insert
    void insert(DeviceInfo deviceInfo);

    @Query("DELETE FROM DeviceInfo")
    void deleteAll();

    @Query("SELECT * FROM DeviceInfo ORDER BY id DESC LIMIT 1")
    DeviceInfo getLatestDeviceInfo();

    @Query("SELECT * FROM DeviceInfo ORDER BY id DESC")
    List<DeviceInfo> getAllDeviceInfo();
}