package com.example.mid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mid.helpers.BatteryHelper;

public class BatteryActivity extends AppCompatActivity {
    private TextView batteryStatusText, batteryHealthText, powerSourceText, voltageTempText, technologyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery);

        // Initialize TextViews
        batteryStatusText = findViewById(R.id.battery_status_text);
        batteryHealthText = findViewById(R.id.battery_health_text);
        powerSourceText = findViewById(R.id.power_source_text);
        voltageTempText = findViewById(R.id.voltage_temp_text);
        technologyText = findViewById(R.id.technology_text);

        // Register for battery updates
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
    }

    // BroadcastReceiver to update battery info
    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String batteryDetails = BatteryHelper.getBatteryDetails(BatteryActivity.this);
            String[] details = batteryDetails.split("\n"); // Split the string into parts

            // Assign values to TextViews
            if (details.length >= 7) {
                batteryStatusText.setText(details[0].replace("Status: ", ""));
                batteryHealthText.setText(details[1].replace("Health: ", ""));
                powerSourceText.setText(details[2].replace("Source: ", ""));
                voltageTempText.setText(details[4] + " | " + details[5]);
                technologyText.setText(details[3].replace("Tech: ", ""));
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryReceiver);
    }
}