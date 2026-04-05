package com.example.mid.helpers;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import java.io.File;
import java.text.DecimalFormat;

public class StorageHelper {

    public static String getInternalStorageInfo(Context context) {
        return getStorageInfo(Environment.getDataDirectory());
    }

    /**
     * Returns info for a real removable SD card, if present.
     * getExternalStorageDirectory() resolves to /storage/emulated/0, which lives
     * on the same physical NAND chip as /data — so StatFs reports identical
     * numbers to internal storage.  getExternalFilesDirs() (plural) returns an
     * extra entry for each mounted removable card; index 0 is always the
     * emulated path, index 1+ are real SD cards.
     */
    public static String getExternalStorageInfo(Context context) {
        File[] dirs = context.getExternalFilesDirs(null);
        if (dirs != null && dirs.length > 1 && dirs[1] != null && dirs[1].exists()) {
            // Walk up to the SD card root (skip the app-specific subdirectory)
            File sdRoot = dirs[1];
            while (sdRoot.getParentFile() != null
                    && !sdRoot.getParentFile().getAbsolutePath().equals("/")) {
                File parent = sdRoot.getParentFile();
                // Stop at the mount root (e.g. /storage/XXXX-XXXX)
                if (parent.getAbsolutePath().startsWith("/storage/")
                        && parent.getParentFile() != null
                        && parent.getParentFile().getAbsolutePath().equals("/storage")) {
                    sdRoot = parent;
                    break;
                }
                sdRoot = parent;
            }
            return getStorageInfo(sdRoot);
        }
        return "No external SD card detected";
    }

    private static String getStorageInfo(File path) {
        try {
            StatFs stat = new StatFs(path.getAbsolutePath());
            long blockSize      = stat.getBlockSizeLong();
            long totalBlocks    = stat.getBlockCountLong();
            long availableBlocks = stat.getAvailableBlocksLong();

            long total = totalBlocks * blockSize;
            long free  = availableBlocks * blockSize;
            long used  = total - free;

            return String.format(
                    "Total: %s\nUsed: %s\nFree: %s",
                    formatSize(total), formatSize(used), formatSize(free)
            );
        } catch (Exception e) {
            return "Storage info unavailable";
        }
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
