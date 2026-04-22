package com.darklightning.diagnostics.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import java.util.Locale;

public class BatteryHelper {

    // Get all battery details as a formatted string
    public static String getBatteryDetails(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        if (batteryStatus == null) {
            return "Status: N/A\nHealth: N/A\nSource: N/A\nTech: N/A\nTemp: N/A\nVoltage: N/A\nLevel: N/A";
        }

        // Percentage
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = (scale > 0) ? level * 100 / (float) scale : 0f;

        // Health
        String health = getBatteryHealth(batteryStatus);

        // Power Source
        String powerSource = getPowerSource(batteryStatus);

        // Status (Charging/Discharging)
        String status = getChargingStatus(batteryStatus);

        // Technology (Li-ion, etc.) — can be null on some devices
        String technology = batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
        if (technology == null) technology = "Unknown";

        // Temperature
        float temp = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f;

        // Voltage
        float voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000f;

        return String.format(Locale.ROOT,
                "Status: %s\nHealth: %s\nSource: %s\nTech: %s\nTemp: %.1f°C\nVoltage: %.2fV\nLevel: %.1f%%",
                status, health, powerSource, technology, temp, voltage, batteryPct
        );
    }

    // Helper methods
    private static String getBatteryHealth(Intent batteryStatus) {
        int health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_GOOD: return "Good";
            case BatteryManager.BATTERY_HEALTH_OVERHEAT: return "Overheat";
            case BatteryManager.BATTERY_HEALTH_DEAD: return "Dead";
            case BatteryManager.BATTERY_HEALTH_COLD: return "Cold";
            default: return "Unknown";
        }
    }

    private static String getPowerSource(Intent batteryStatus) {
        int plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        switch (plugged) {
            case BatteryManager.BATTERY_PLUGGED_AC: return "AC";
            case BatteryManager.BATTERY_PLUGGED_USB: return "USB";
            case BatteryManager.BATTERY_PLUGGED_WIRELESS: return "Wireless";
            default: return "Battery";
        }
    }

    private static String getChargingStatus(Intent batteryStatus) {
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING: return "Charging";
            case BatteryManager.BATTERY_STATUS_DISCHARGING: return "Discharging";
            case BatteryManager.BATTERY_STATUS_FULL: return "Full";
            default: return "Unknown";
        }
    }
}