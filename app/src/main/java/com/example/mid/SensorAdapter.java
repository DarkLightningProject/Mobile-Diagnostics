package com.example.mid;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@SuppressLint("SetTextI18n")
public class SensorAdapter extends RecyclerView.Adapter<SensorAdapter.ViewHolder> {
    private final List<Sensor> sensorList;
    private final SensorManager sensorManager;
    // Tracks every live listener so unregisterAll() can release them all at once
    private final Set<SensorEventListener> activeListeners = new HashSet<>();

    public SensorAdapter(List<Sensor> sensorList, SensorManager sensorManager) {
        this.sensorList = sensorList;
        this.sensorManager = sensorManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sensor_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        // Unregister previous listener before binding a new sensor to this view
        if (holder.currentListener != null) {
            sensorManager.unregisterListener(holder.currentListener);
            activeListeners.remove(holder.currentListener);
            holder.currentListener = null;
        }

        final Sensor sensor = this.sensorList.get(position);
        String name   = sensor.getName()   != null ? sensor.getName()   : "Unknown";
        String vendor = sensor.getVendor() != null ? sensor.getVendor() : "Unknown";
        holder.sensorName.setText(name);
        holder.sensorType.setText("Type: " + getTypeName(sensor.getType()));
        holder.sensorVendor.setText("Vendor: " + vendor);
        holder.sensorData.setText("Data: Waiting...");

        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                String data = formatSensorData(event.values, getUnitForSensor(sensor.getType()));
                holder.sensorData.setText("Data: " + data);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };

        holder.setListener(listener);
        boolean isRegistered = sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        if (isRegistered) {
            activeListeners.add(listener);
        } else {
            holder.sensorData.setText("Data: Unavailable");
            Log.e("SensorAdapter", "Failed to register: " + name);
        }
    }

    /** Unregisters every active sensor listener. Call from onPause and onDestroy. */
    public void unregisterAll() {
        for (SensorEventListener l : activeListeners) {
            sensorManager.unregisterListener(l);
        }
        activeListeners.clear();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.currentListener != null) {
            sensorManager.unregisterListener(holder.currentListener);
            activeListeners.remove(holder.currentListener);
            holder.currentListener = null;
        }
    }

    @Override
    public int getItemCount() {
        return this.sensorList.size();
    }

    private String formatSensorData(float[] values, String unit) {
        StringBuilder data = new StringBuilder();
        for (float value : values) {
            data.append(String.format(Locale.ROOT, "%.2f", value)).append(", ");
        }
        if (data.length() > 0) {
            data.delete(data.length() - 2, data.length()); // Remove the last ", "
        }
        return data.toString() + " " + unit;
    }

    private String getTypeName(int type) {
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER: return "Accelerometer";
            case Sensor.TYPE_MAGNETIC_FIELD: return "Magnetic Field";
            case Sensor.TYPE_GYROSCOPE: return "Gyroscope";
            case Sensor.TYPE_LIGHT: return "Light";
            case Sensor.TYPE_PRESSURE: return "Pressure";
            case Sensor.TYPE_PROXIMITY: return "Proximity";
            case Sensor.TYPE_RELATIVE_HUMIDITY: return "Humidity";
            case Sensor.TYPE_AMBIENT_TEMPERATURE: return "Temperature";
            case Sensor.TYPE_STEP_COUNTER: return "Step Counter";
            case Sensor.TYPE_HEART_RATE: return "Heart Rate";
            default: return "Unknown (" + type + ")";
        }
    }

    private String getUnitForSensor(int sensorType) {
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER: return "m/s²";
            case Sensor.TYPE_MAGNETIC_FIELD: return "μT";
            case Sensor.TYPE_GYROSCOPE: return "rad/s";
            case Sensor.TYPE_LIGHT: return "lux";
            case Sensor.TYPE_PRESSURE: return "hPa";
            case Sensor.TYPE_PROXIMITY: return "cm";
            case Sensor.TYPE_RELATIVE_HUMIDITY: return "%";
            case Sensor.TYPE_AMBIENT_TEMPERATURE: return "°C";
            case Sensor.TYPE_STEP_COUNTER: return "steps";
            case Sensor.TYPE_HEART_RATE: return "bpm";
            default: return "";
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView sensorData, sensorName, sensorType, sensorVendor;
        SensorEventListener currentListener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sensorName = itemView.findViewById(R.id.sensorName);
            sensorType = itemView.findViewById(R.id.sensorType);
            sensorVendor = itemView.findViewById(R.id.sensorVendor);
            sensorData = itemView.findViewById(R.id.sensorData);
        }

        public void setListener(SensorEventListener listener) {
            this.currentListener = listener;
        }
    }
}