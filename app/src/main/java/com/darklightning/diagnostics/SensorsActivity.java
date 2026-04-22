package com.darklightning.diagnostics;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SensorsActivity extends AppCompatActivity {
    private SensorAdapter adapter;
    private RecyclerView recyclerView;
    private SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors);

        recyclerView = findViewById(R.id.sensorRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        adapter = new SensorAdapter(sensorList, sensorManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister all active sensor listeners to save battery
        if (adapter != null) adapter.unregisterAll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-register listeners for all visible items
        if (adapter != null) {
            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Safety net: release any listeners not yet cleaned up
        if (adapter != null) adapter.unregisterAll();
    }
}