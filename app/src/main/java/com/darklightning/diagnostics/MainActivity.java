package com.darklightning.diagnostics;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.darklightning.diagnostics.database.DeviceDatabase;
import com.darklightning.diagnostics.database.DeviceInfo;
import com.darklightning.diagnostics.helpers.DeviceInfoHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private DeviceDatabase database;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database
        database = DeviceDatabase.getInstance(this);

        // Fetch and display display info


        // Refresh device info: clear old rows and insert fresh data
        executorService.execute(() -> {
            DeviceInfo deviceInfo = DeviceInfoHelper.getDeviceDetails(this);
            database.deviceInfoDao().deleteAll();
            database.deviceInfoDao().insert(deviceInfo);
        });

        // Set click listeners for cards
        CardView aboutPhoneCard = findViewById(R.id.aboutPhonecard);
        aboutPhoneCard.setOnClickListener(v -> startActivity(new Intent(this, AboutPhoneActivity.class)));

        CardView simSettingsCard = findViewById(R.id.simSettingsCard);
        simSettingsCard.setOnClickListener(v -> startActivity(new Intent(this, SimSettingsActivity.class)));

        CardView batteryCard = findViewById(R.id.batteryCard);
        batteryCard.setOnClickListener(v -> startActivity(new Intent(this, BatteryActivity.class)));

        // In onCreate()
        CardView storageTestCard = findViewById(R.id.storageTestCard);
        storageTestCard.setOnClickListener(v -> startActivity(
                new Intent(this, StorageTestActivity.class)
        ));

        CardView wifiCard = findViewById(R.id.wifiCard);
        wifiCard.setOnClickListener(v -> startActivity(new Intent(this, WifiSettingsActivity.class)));

        CardView displaySettingsCard = findViewById(R.id.displaySettingsCard);
        displaySettingsCard.setOnClickListener(v -> startActivity(new Intent(this, DisplaySettingsActivity.class)));

        CardView sensorsCard = findViewById(R.id.sensorsCard);
        sensorsCard.setOnClickListener(v -> startActivity(new Intent(this, SensorsActivity.class)));

        CardView socCard = findViewById(R.id.socCard);
        socCard.setOnClickListener(this::openSocInfo);
    }
    public void openSocInfo(View view) {
        startActivity(new Intent(this, SocInfoActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}


