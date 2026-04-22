package com.darklightning.diagnostics.helpers;

import android.opengl.GLES20;
import android.util.Log;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

public class GpuInfoHelper {
    private static final String TAG = "GPUInfoHelper";

    // These files return two numbers: "busy total" — compute (busy/total)*100
    private static final String[] GPU_RATIO_PATHS = {
            "/sys/class/kgsl/kgsl-3d0/gpubusy",                          // Qualcomm
            "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/gpubusy",   // Qualcomm (older)
            "/sys/class/kgsl/kgsl-3d0/devfreq/load",                     // Qualcomm
    };

    // These files return a single percentage value directly (e.g. "45" or "45%")
    private static final String[] GPU_PERCENT_PATHS = {
            "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage",              // Qualcomm
            "/sys/kernel/gpu/gpu_busy",                                   // Generic (some Qualcomm)
            "/sys/class/mali/mali0/device/utilization",                   // Mali (Exynos, some MTK)
            "/sys/class/misc/mali0/device/utilization",                   // Mali variant
            "/sys/devices/platform/mali.0/utilization",                   // Mali variant
            "/sys/kernel/ged/hal/gpu_utilization",                        // MediaTek GED
            "/sys/bus/platform/drivers/ged/ged/hal/gpu_utilization",      // MediaTek GED variant
    };

    public static String getGpuVendor() {
        try {
            String vendor = GLES20.glGetString(GLES20.GL_VENDOR);
            return (vendor == null || vendor.isEmpty()) ? "N/A" : vendor;
        } catch (Exception e) {
            Log.e(TAG, "Error getting GPU vendor", e);
            return "N/A";
        }
    }

    public static String getGpuRenderer() {
        try {
            String renderer = GLES20.glGetString(GLES20.GL_RENDERER);
            return (renderer == null || renderer.isEmpty()) ? "N/A" : renderer;
        } catch (Exception e) {
            Log.e(TAG, "Error getting GPU renderer", e);
            return "N/A";
        }
    }

    public static float getGpuLoad() {
        // Try ratio-format paths first (Qualcomm: "busy total")
        for (String path : GPU_RATIO_PATHS) {
            File gpuFile = new File(path);
            if (gpuFile.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(gpuFile))) {
                    String line = br.readLine();
                    if (line != null) {
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length >= 2) {
                            long busy = Long.parseLong(parts[0]);
                            long total = Long.parseLong(parts[1]);
                            if (total == 0) return 0.0f;
                            return (busy * 100f) / total;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error reading GPU ratio load from " + path, e);
                }
            }
        }

        // Try direct percentage paths (Mali, MediaTek, generic: "45" or "45%")
        for (String path : GPU_PERCENT_PATHS) {
            File gpuFile = new File(path);
            if (gpuFile.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(gpuFile))) {
                    String line = br.readLine();
                    if (line != null) {
                        // Strip any non-numeric characters (%, spaces)
                        String cleaned = line.trim().replaceAll("[^0-9.]", "");
                        if (!cleaned.isEmpty()) {
                            float value = Float.parseFloat(cleaned);
                            if (value >= 0 && value <= 100) return value;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error reading GPU percent load from " + path, e);
                }
            }
        }

        return -1; // Not available on this device
    }
}