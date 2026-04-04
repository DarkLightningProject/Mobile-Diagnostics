package com.example.mid;

import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mid.helpers.CpuInfoHelper;
import com.example.mid.helpers.GpuInfoHelper;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SocInfoActivity extends AppCompatActivity {
    // UI Components
    private TextView cpuInfoText;
    private TextView gpuInfoText;

    // Threading and Monitoring
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isMonitoring = true;

    // OpenGL Context
    private GLSurfaceView glSurfaceView;

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
        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                // Fetch GPU vendor and renderer
                gpuVendor = gl.glGetString(GL10.GL_VENDOR);
                gpuRenderer = gl.glGetString(GL10.GL_RENDERER);
                runOnUiThread(() -> updateGpuInfo());
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
                    "📊 Load: " + (load != -1 ? String.format("%.1f%%", load) : "N/A");
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

            // Add processor information
            String processorInfo = CpuInfoHelper.getProcessorInfo();
            cpuInfo.append("🔹 Processor Info:\n").append(processorInfo).append("\n\n");

            // Add cluster information
            HashMap<String, List<Integer>> clusters = CpuInfoHelper.detectClusters();
            cpuInfo.append("🔸 CPU Clusters:\n");
            for (String cluster : clusters.keySet()) {
                cpuInfo.append("  - ").append(cluster).append(" (Cores: ");
                for (int core : clusters.get(cluster)) {
                    cpuInfo.append(core).append(", ");
                }
                cpuInfo.delete(cpuInfo.length() - 2, cpuInfo.length()).append(")\n");
            }

            // Add core details
            cpuInfo.append("\n🖥 Core Details:\n");
            for (int i = 0; i < CpuInfoHelper.getCoreCount(); i++) {
                cpuInfo.append("  🔹 Core ").append(i).append("\n")
                        .append("    🔸 Freq: ").append(CpuInfoHelper.getCurrentFreq(i)).append(" MHz\n")
                        .append("    🔸 Gov: ").append(CpuInfoHelper.getGovernor(i)).append("\n")
                        .append("    🔸 Load: ").append(String.format("%.1f%%", CpuInfoHelper.getCoreUsage(i)))
                        .append("\n\n");
            }

            // Update UI
            runOnUiThread(() -> {
                if (cpuInfoText != null) {
                    cpuInfoText.setText(cpuInfo.toString());
                    cpuInfoText.setVisibility(TextView.VISIBLE); // Ensure visibility
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