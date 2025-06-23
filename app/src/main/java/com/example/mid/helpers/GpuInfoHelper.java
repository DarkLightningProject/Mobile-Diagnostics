package com.example.mid.helpers;

import android.opengl.GLES20;
import android.util.Log;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

public class GpuInfoHelper {
    private static final String TAG = "GPUInfoHelper";
    private static final String[] GPU_BUSY_PATHS = {
            "/sys/class/kgsl/kgsl-3d0/gpubusy",
            "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/gpubusy",
            "/sys/kernel/gpu/gpu_busy",
            "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage",
            "/sys/class/mali/mali0/device/utilization",
            "/sys/class/kgsl/kgsl-3d0/devfreq/load"

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
        for (String path : GPU_BUSY_PATHS) {
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
                    Log.e(TAG, "Error reading GPU load from " + path, e);
                }
            }
        }
        return -1;
    }
}