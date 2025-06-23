package com.example.mid.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import com.example.mid.database.DeviceInfo;
import com.example.mid.repository.DeviceRepository;

public class DeviceViewModel extends AndroidViewModel {
    private final DeviceRepository repository;

    public DeviceViewModel(Application application) {
        super(application);
        repository = new DeviceRepository(application);
    }

    public void insert(DeviceInfo deviceInfo) {
        repository.insert(deviceInfo);
    }

    public DeviceInfo getLatestDeviceInfo() {
        return repository.getLatestDeviceInfo();
    }
}
