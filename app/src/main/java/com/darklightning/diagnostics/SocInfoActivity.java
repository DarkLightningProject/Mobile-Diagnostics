package com.darklightning.diagnostics;

import android.annotation.SuppressLint;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.darklightning.diagnostics.helpers.CpuInfoHelper;
import com.darklightning.diagnostics.helpers.GpuInfoHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

@SuppressLint("SetTextI18n")
public class SocInfoActivity extends AppCompatActivity {
    // UI Components
    private TextView cpuInfoText;
    private TextView gpuInfoText;

    // Threading and Monitoring
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isMonitoring = true;

    // OpenGL Context
    // GPU Info
    private String gpuVendor = "Unknown";
    private String gpuRenderer = "Unknown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soc);

        // Initialize UI Components
        initializeViews();

        // Initialize OpenGL Context
        initializeOpenGLContext();

        // Start CPU and GPU Monitoring
        startCpuMonitoring();
        startGpuMonitoring();
    }

    // Initialize UI Components
    private void initializeViews() {
        cpuInfoText = findViewById(R.id.cpuInfoText);
        gpuInfoText = findViewById(R.id.gpuInfoText);
    }

    // Initialize OpenGL Context
    private void initializeOpenGLContext() {
        GLSurfaceView glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                // Fetch GPU vendor and renderer
                gpuVendor = gl.glGetString(GL10.GL_VENDOR);
                gpuRenderer = gl.glGetString(GL10.GL_RENDERER);
                runOnUiThread(SocInfoActivity.this::updateGpuInfo);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {}

            @Override
            public void onDrawFrame(GL10 gl) {}
        });

        // Add GLSurfaceView to the root layout
        ((ViewGroup) findViewById(android.R.id.content)).addView(
                glSurfaceView,
                new ViewGroup.LayoutParams(1, 1) // Hidden view
        );

        // Make GLSurfaceView transparent
        glSurfaceView.setZOrderOnTop(true);
        glSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }

    // Update GPU Information — file I/O runs on executor, UI update on main thread
    private void updateGpuInfo() {
        executor.execute(() -> {
            float load = GpuInfoHelper.getGpuLoad();
            String vendorCopy = gpuVendor;
            String rendererCopy = gpuRenderer;
            String formattedGpuInfo = "🎮 GPU Vendor: " + (vendorCopy != null ? vendorCopy : "Unknown") + "\n" +
                    "🖥 Renderer: " + (rendererCopy != null ? rendererCopy : "Unknown") + "\n" +
                    "📊 Load: " + (load != -1 ? String.format(Locale.ROOT, "%.1f%%", load) : "N/A");
            runOnUiThread(() -> {
                if (gpuInfoText != null) {
                    gpuInfoText.setText(formattedGpuInfo);
                    gpuInfoText.setVisibility(TextView.VISIBLE);
                }
            });
        });
    }

    // Start GPU Monitoring
    private void startGpuMonitoring() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isMonitoring) return;
                updateGpuInfo();
                handler.postDelayed(this, 1500); // Update every 1.5 seconds
            }
        }, 1500);
    }

    // Start CPU Monitoring
    private void startCpuMonitoring() {
        // Initial update
        updateCpuInfo();

        // Schedule periodic updates
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isMonitoring) return;
                updateCpuInfo();
                handler.postDelayed(this, 1500); // Update every 1.5 seconds
            }
        }, 1500);
    }

    // Update CPU Information
    private void updateCpuInfo() {
        executor.execute(() -> {
            StringBuilder cpuInfo = new StringBuilder();

            // Processor info
            String processorInfo = CpuInfoHelper.getProcessorInfo();
            cpuInfo.append("🔹 Processor Info:\n").append(processorInfo).append("\n\n");

            // Cluster info
            HashMap<String, List<Integer>> clusters = CpuInfoHelper.detectClusters();
            cpuInfo.append("🔸 CPU Clusters:\n");
            for (String cluster : clusters.keySet()) {
                List<Integer> cores = clusters.get(cluster);
                if (cores == null) continue;
                cpuInfo.append("  - ").append(cluster).append(" (Cores: ");
                for (int core : cores) {
                    cpuInfo.append(core).append(", ");
                }
                cpuInfo.delete(cpuInfo.length() - 2, cpuInfo.length()).append(")\n");
            }

            // Core details — single file read per core so Freq and Load always match
            int coreCount = CpuInfoHelper.getCoreCount();
            cpuInfo.append("\n🖥 Core Details:\n");
            for (int i = 0; i < coreCount; i++) {
                long[] freqData = CpuInfoHelper.getCoreFreqData(i);
                String freqStr = (freqData[0] >= 0) ? freqData[0] + " MHz" : "Offline";
                String loadStr = (freqData[1] >= 0) ? freqData[1] + "%" : "Offline";
                cpuInfo.append("  🔹 Core ").append(i).append("\n")
                        .append("    🔸 Freq: ").append(freqStr).append("\n")
                        .append("    🔸 Gov: ").append(CpuInfoHelper.getGovernor(i)).append("\n")
                        .append("    🔸 Load: ").append(loadStr)
                        .append("\n\n");
            }

            // Update UI
            runOnUiThread(() -> {
                if (cpuInfoText != null) {
                    cpuInfoText.setText(cpuInfo.toString());
                    cpuInfoText.setVisibility(TextView.VISIBLE);
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isMonitoring = false;
        handler.removeCallbacksAndMessages(null);
        executor.shutdownNow();
    }
}