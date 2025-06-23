package com.example.mid.helpers;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class DisplayHelper {
    public static String getScreenResolution(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels + " x " + metrics.heightPixels + " pixels";
    }

    public static String getScreenDensity(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.densityDpi + " dpi (" + getDensityName(metrics.densityDpi) + ")";
    }

    public static String getScreenSizeInches(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        float widthInches = metrics.widthPixels / metrics.xdpi;
        float heightInches = metrics.heightPixels / metrics.ydpi;
        double diagonalInches = Math.sqrt(Math.pow(widthInches, 2) + Math.pow(heightInches, 2));
        return String.format("%.1f inches", diagonalInches);
    }

    private static String getDensityName(int densityDpi) {
        switch (densityDpi) {
            case DisplayMetrics.DENSITY_LOW: return "ldpi";
            case DisplayMetrics.DENSITY_MEDIUM: return "mdpi";
            case DisplayMetrics.DENSITY_HIGH: return "hdpi";
            case DisplayMetrics.DENSITY_XHIGH: return "xhdpi";
            case DisplayMetrics.DENSITY_XXHIGH: return "xxhdpi";
            case DisplayMetrics.DENSITY_XXXHIGH: return "xxxhdpi";
            default: return "Unknown";
        }
    }
}