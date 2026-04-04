package com.example.mid;

import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mid.database.DeviceInfo;
import com.example.mid.database.DeviceDatabase;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AboutPhoneActivity extends AppCompatActivity {
    private TextView titleText;
    private TextView deviceNameText;
    private TextView manufacturerText;
    private TextView androidVersionText;
    private TextView kernelVersionText;
    private TextView imeiText;
    private TextView ramText;
    private TextView storageText;
    private TextView modelNumberText;
    private TextView boardText;
    private TextView openGlText;
    private TextView apiLevelText;
    private TextView kernelArchText;
    private TextView rootAccessText;
    private TextView googlePlayServicesText;
    private TextView uptimeText;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_phone);

        // Initialize all TextViews
        titleText = findViewById(R.id.aboutPhoneText);
        deviceNameText = findViewById(R.id.deviceNameText);
        manufacturerText = findViewById(R.id.manufacturerText);
        androidVersionText = findViewById(R.id.androidVersionText);
        kernelVersionText = findViewById(R.id.kernelVersionText);
        imeiText = findViewById(R.id.imeiText);
        ramText = findViewById(R.id.ramText);
        storageText = findViewById(R.id.storageText);
        modelNumberText = findViewById(R.id.modelNumberText);
        boardText = findViewById(R.id.boardText);
        openGlText = findViewById(R.id.openGlText);
        apiLevelText = findViewById(R.id.apiLevelText);
        kernelArchText = findViewById(R.id.kernelArchText);
        rootAccessText = findViewById(R.id.rootAccessText);
        googlePlayServicesText = findViewById(R.id.googlePlayServicesText);
        uptimeText = findViewById(R.id.uptimeText);

        // Fetch and display device information
        executorService.execute(() -> {
            DeviceInfo latestDeviceInfo = DeviceDatabase.getInstance(this)
                    .deviceInfoDao().getLatestDeviceInfo();
            boolean rooted = isRooted(); // computed on background thread, safe
            runOnUiThread(() -> {
                if (latestDeviceInfo != null) {
                    // Set database fields
                    deviceNameText.setText("Device: " + latestDeviceInfo.getDeviceName());
                    manufacturerText.setText("Manufacturer: " + latestDeviceInfo.getManufacturer());
                    androidVersionText.setText("Android Version: " + latestDeviceInfo.getAndroidVersion());
                    kernelVersionText.setText("Kernel Version: " + latestDeviceInfo.getKernelVersion());
                    imeiText.setText("IMEI: " + latestDeviceInfo.getImei());
                    ramText.setText("Total RAM: " + latestDeviceInfo.getTotalRAM());
                    storageText.setText("Internal Storage: " + latestDeviceInfo.getInternalStorage());
                    modelNumberText.setText("Model Number: " + latestDeviceInfo.getModelNumber());
                    boardText.setText("Board: " + latestDeviceInfo.getBoard());
                    kernelArchText.setText("Kernel Arch: " + latestDeviceInfo.getKernelArch());

                    // Set dynamic fields
                    apiLevelText.setText("API Level: " + Build.VERSION.SDK_INT);
                    openGlText.setText("OpenGL ES: " + getOpenGlVersion());
                    rootAccessText.setText("Root Access: " + (rooted ? "Yes" : "No"));
                    googlePlayServicesText.setText("Google Play Services: " + getPlayServicesVersion());
                    uptimeText.setText("Uptime: " + getSystemUptime());
                } else {
                    titleText.setText("No device info found in database.");
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    // Helper methods
    private String getOpenGlVersion() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ConfigurationInfo configInfo = am.getDeviceConfigurationInfo();
        return configInfo.getGlEsVersion();
    }

    private boolean isRooted() {
        boolean isRooted = false;
        try {
            File file = new File("/system/app/Superuser.apk");
            if (file.exists()) isRooted = true;

            Process process = Runtime.getRuntime().exec("su");
            process.getOutputStream().close();
            int exitValue = process.waitFor();
            if (exitValue == 0) isRooted = true;
        } catch (Exception e) {
            isRooted = false;
        }
        return isRooted;
    }

    private String getPlayServicesVersion() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo("com.google.android.gms", 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "Not installed";
        }
    }

    private String getSystemUptime() {
        long uptimeMillis = SystemClock.elapsedRealtime();
        long hours = TimeUnit.MILLISECONDS.toHours(uptimeMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMillis) % 60;
        return hours + "h " + minutes + "m";
    }
}