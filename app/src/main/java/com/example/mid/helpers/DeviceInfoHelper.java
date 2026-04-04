package com.example.mid.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import com.example.mid.database.DeviceInfo;
import android.util.Log;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DeviceInfoHelper {
    private final Context context;

    public DeviceInfoHelper(Context context) {
        this.context = context;
    }

    @SuppressLint("HardwareIds")
    public String getIMEI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } else {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ?
                        telephonyManager.getMeid() :
                        telephonyManager.getDeviceId();
            }
        }
        return "Not Available";
    }

    public static DeviceInfo getDeviceDetails(Context context) {
        DeviceInfoHelper helper = new DeviceInfoHelper(context);
        return new DeviceInfo(
                helper.getDeviceName(),
                helper.getManufacturer(),
                helper.getAndroidVersion(),
                helper.getKernelVersion(),
                helper.getIMEI(),
                helper.getTotalRAM(),
                helper.getInternalStorage(),
                helper.getModelNumber(),
                helper.getBoard(),      // Added
                helper.getKernelArch()  // Added
        );
    }

    public String getDeviceName() {
        return Build.MODEL;
    }

    public String getManufacturer() {
        return Build.MANUFACTURER;
    }

    public String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    public String getKernelVersion() {
        try {
            Process process = Runtime.getRuntime().exec("uname -a");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String result = reader.readLine();
                process.destroy();
                return result != null ? result : "Unknown";
            }
        } catch (IOException e) {
            return "Unknown";
        }
    }

    public String getTotalRAM() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/meminfo"))) {
            String line = reader.readLine();
            if (line != null) {
                String[] parts = line.split("\\s+");
                if (parts.length > 1) {
                    long totalRamKB = Long.parseLong(parts[1]);
                    return (totalRamKB / 1024 / 1024) + " GB";
                }
            }
        } catch (IOException e) {
            Log.e("DeviceInfoHelper", "Failed to read meminfo", e);
        }
        return "Unknown";
    }

    public String getInternalStorage() {
        try {
            StatFs statFs = new StatFs("/data");
            long totalBytes = statFs.getTotalBytes();
            return (totalBytes / (1024 * 1024 * 1024)) + " GB";
        } catch (Exception e) {
            return "Unknown";
        }
    }

    public String getModelNumber() {
        return Build.DEVICE;
    }

    public String getBoard() {
        return Build.BOARD; // ✅ Get board info
    }

    public String getKernelArch() {
        String arch = System.getProperty("os.arch");
        return arch != null ? arch : "Unknown";
    }
}