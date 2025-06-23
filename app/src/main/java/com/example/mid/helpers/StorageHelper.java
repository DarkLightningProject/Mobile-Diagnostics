package com.example.mid.helpers;

import android.os.Environment;
import android.os.StatFs;
import java.io.File;
import java.text.DecimalFormat;

public class StorageHelper {

    public static String getInternalStorageInfo() {
        return getStorageInfo(Environment.getDataDirectory());
    }

    public static String getExternalStorageInfo() {
        File externalDir = Environment.getExternalStorageDirectory();
        if (externalDir == null || !externalDir.exists()) {
            return "External storage not available";
        }
        return getStorageInfo(externalDir);
    }

    private static String getStorageInfo(File path) {
        StatFs stat = new StatFs(path.getAbsolutePath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        long availableBlocks = stat.getAvailableBlocksLong();

        long total = totalBlocks * blockSize;
        long free = availableBlocks * blockSize;
        long used = total - free;

        return String.format(
                "Total: %s\nUsed: %s\nFree: %s",
                formatSize(total), formatSize(used), formatSize(free)
        );
    }

    private static String formatSize(long size) {
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double formattedSize = size;

        while (formattedSize >= 1024 && unitIndex < units.length - 1) {
            formattedSize /= 1024;
            unitIndex++;
        }

        return new DecimalFormat("#,##0.#").format(formattedSize) + " " + units[unitIndex];
    }
}