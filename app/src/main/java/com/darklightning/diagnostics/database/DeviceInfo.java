package com.darklightning.diagnostics.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DeviceInfo {
    @PrimaryKey(autoGenerate = true)
    public int id;

    private String deviceName;
    private String manufacturer;
    private String androidVersion;
    private String kernelVersion;
    private String imei;
    private String totalRAM;
    private String internalStorage;
    private String modelNumber;
    private String board; // Added field
    private String kernelArch; // Added field

    // Constructor
    public DeviceInfo(String deviceName, String manufacturer, String androidVersion, String kernelVersion,
                      String imei, String totalRAM, String internalStorage, String modelNumber,
                      String board, String kernelArch) {
        this.deviceName = deviceName;
        this.manufacturer = manufacturer;
        this.androidVersion = androidVersion;
        this.kernelVersion = kernelVersion;
        this.imei = imei;
        this.totalRAM = totalRAM;
        this.internalStorage = internalStorage;
        this.modelNumber = modelNumber;
        this.board = board;
        this.kernelArch = kernelArch;
    }

    // Getters and setters
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getAndroidVersion() { return androidVersion; }
    public void setAndroidVersion(String androidVersion) { this.androidVersion = androidVersion; }

    public String getKernelVersion() { return kernelVersion; }
    public void setKernelVersion(String kernelVersion) { this.kernelVersion = kernelVersion; }

    public String getImei() { return imei; }
    public void setImei(String imei) { this.imei = imei; }

    public String getTotalRAM() { return totalRAM; }
    public void setTotalRAM(String totalRAM) { this.totalRAM = totalRAM; }

    public String getInternalStorage() { return internalStorage; }
    public void setInternalStorage(String internalStorage) { this.internalStorage = internalStorage; }

    public String getModelNumber() { return modelNumber; }
    public void setModelNumber(String modelNumber) { this.modelNumber = modelNumber; }

    public String getBoard() { return board; } // Added getter
    public void setBoard(String board) { this.board = board; } // Added setter

    public String getKernelArch() { return kernelArch; } // Added getter
    public void setKernelArch(String kernelArch) { this.kernelArch = kernelArch; } // Added setter
}