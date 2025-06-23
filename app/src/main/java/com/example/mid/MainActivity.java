package com.example.mid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

//import com.example.mid.adapters.CustomAdapter;
import com.example.mid.database.DeviceDatabase;
import com.example.mid.database.DeviceInfo;
import com.example.mid.helpers.CallLogEntry;
import com.example.mid.helpers.CallLogHelper;
import com.example.mid.helpers.DeviceInfoHelper;
import com.example.mid.helpers.DisplayHelper;

import java.util.List;
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


        // Fetch device info and insert into database
        DeviceInfo deviceInfo = DeviceInfoHelper.getDeviceDetails(this);
        executorService.execute(() -> database.deviceInfoDao().insert(deviceInfo));

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

        CardView callLogCard = findViewById(R.id.callLogCard);
        callLogCard.setOnClickListener(v -> startActivity(
                new Intent(this, CallLogActivity.class)
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
    private void loadCallLogs() {
        ListView listView = findViewById(R.id.callLogListView);
        List<CallLogEntry> logs = CallLogHelper.getCallLogs(this);

        CustomAdapter adapter = new CustomAdapter(this, logs);
        listView.setAdapter(adapter);
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


