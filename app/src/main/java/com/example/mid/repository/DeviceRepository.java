package com.example.mid.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.mid.database.DeviceDatabase;
import com.example.mid.database.DeviceInfo;
import com.example.mid.database.DeviceInfoDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeviceRepository {
    private final DeviceInfoDao deviceInfoDao;
    private final ExecutorService executorService;

    public DeviceRepository(Context context) {
        DeviceDatabase db = DeviceDatabase.getInstance(context);
        deviceInfoDao = db.deviceInfoDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    // ✅ Insert device info in background thread
    public void insert(DeviceInfo deviceInfo) {
        executorService.execute(() -> deviceInfoDao.insert(deviceInfo));
    }

    // ✅ Get the latest device info (Most Recent Entry)
    public DeviceInfo getLatestDeviceInfo() {
        return deviceInfoDao.getLatestDeviceInfo();
    }

    // ✅ Get all stored device info records
    public List<DeviceInfo> getAllDeviceInfo() {
        return deviceInfoDao.getAllDeviceInfo();
    }
}
