package com.example.mid.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CpuInfoHelper {
    private static final String CPU_DIR = "/sys/devices/system/cpu/";

    public static HashMap<String, List<Integer>> detectClusters() {
        HashMap<String, List<Integer>> clusters = new HashMap<>();
        HashMap<Long, List<Integer>> freqGroups = new HashMap<>();

        // Group cores by max frequency
        for (int i = 0; i < getCoreCount(); i++) {
            long maxFreq = getMaxFreq(i);
            if (!freqGroups.containsKey(maxFreq)) {
                freqGroups.put(maxFreq, new ArrayList<>());
            }
            freqGroups.get(maxFreq).add(i);
        }

        // Label as Big/Little
        int clusterNum = 0;
        for (Long freq : freqGroups.keySet()) {
            String label = (clusterNum == 0) ? "Little" : "Big";
            clusters.put(label + " Cluster", freqGroups.get(freq));
            clusterNum++;
        }
        return clusters;
    }
    public static String getProcessorInfo() {
        StringBuilder info = new StringBuilder();

        // Default values
        String modelName = "Unknown";
        String hardware = "Unknown";
        String architecture = "Unknown";
        String revision = "Unknown";

        // Try reading from /proc/cpuinfo
        try (BufferedReader br = new BufferedReader(new FileReader("/proc/cpuinfo"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length < 2) continue;

                String key = parts[0].trim();
                String value = parts[1].trim();

                if (key.equalsIgnoreCase("model name") || key.equalsIgnoreCase("Processor")) {
                    modelName = value;
                } else if (key.equalsIgnoreCase("Hardware")) {
                    hardware = value;
                } else if (key.equalsIgnoreCase("CPU architecture")) {
                    architecture = value;
                } else if (key.equalsIgnoreCase("Revision")) {
                    revision = value;
                }
            }
        } catch (IOException e) {
            return "Processor Info Unavailable";
        }

        // **Detecting Emulator**
        if (hardware.equalsIgnoreCase("ranchu")) {
            modelName = "Android Emulator";
            hardware = "ranchu (Emulator)";
            architecture = "x86_64 / arm64 (Emulator)";
            revision = "N/A";
        }

        // **Fixing Qualcomm Model Detection**
        if (modelName == null || modelName.contains("model") || modelName.equals("7")) {
            String qualcommModel = getSystemProperty("ro.soc.model");
            if (qualcommModel != null && !qualcommModel.isEmpty()) modelName = qualcommModel;
            else modelName = getSystemProperty("ro.board.platform");
        }

        // **Fixing Hardware Detection (If Still "Unknown")**
        if (hardware == null || hardware.equals("Unknown")) {
            hardware = getSystemProperty("ro.hardware");
        }

        // **Fixing Revision (If Still "Unknown")**
        if (revision == null || revision.equals("Unknown")) {
            revision = getSystemProperty("ro.revision");
        }

        info.append("Model: ").append(modelName != null ? modelName : "Unknown").append("\n");
        info.append("Hardware: ").append(hardware != null ? hardware : "Unknown").append("\n");
        info.append("Architecture: ").append(architecture != null ? architecture : "Unknown").append("\n");
        info.append("Revision: ").append(revision != null ? revision : "Unknown").append("\n");

        return info.toString();
    }

    // Fetch system properties (useful when /proc/cpuinfo is restricted)
    private static String getSystemProperty(String property) {
        try {
            Process process = Runtime.getRuntime().exec("getprop " + property);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String value = reader.readLine();
            reader.close();
            return (value != null && !value.isEmpty()) ? value : null;
        } catch (IOException e) {
            return null;
        }
    }


    public static String getGovernor(int core) {
        String path1 = CPU_DIR + "cpu" + core + "/cpufreq/scaling_governor";
        String path2 = CPU_DIR + "cpu" + core + "/cpufreq/policy/scaling_governor";

        String finalPath = new File(path1).exists() ? path1 : new File(path2).exists() ? path2 : null;

        if (finalPath == null) {
            return "N/A";
        }

        try (BufferedReader br = new BufferedReader(new FileReader(finalPath))) {
            return br.readLine();
        } catch (Exception e) {
            return "N/A";
        }
    }
    private static long getMaxFreq(int core) {
        try (BufferedReader br = new BufferedReader(new FileReader(
                CPU_DIR + "cpu" + core + "/cpufreq/cpuinfo_max_freq"))) {
            return Long.parseLong(br.readLine());
        } catch (Exception e) {
            return 0;
        }
    }

    public static String getLiveCpuInfo() {
        StringBuilder info = new StringBuilder();

        try {
            // Get CPU architecture
            String arch = System.getProperty("os.arch");
            int cores = getCoreCount();

            info.append("Architecture: ").append(arch).append("\n");
            info.append("Cores: ").append(cores).append("\n\n");

            // Detect and display Big/Little clusters
            HashMap<String, List<Integer>> clusters = detectClusters();
            for (String cluster : clusters.keySet()) {
                info.append(cluster).append(" (Cores ");
                for (int core : clusters.get(cluster)) {
                    info.append(core).append(", ");
                }
                info.delete(info.length() - 2, info.length()); // Remove last comma
                info.append(")\n");
            }

            // Display per-core details
            for (int i = 0; i < cores; i++) {
                info.append("\nCore ").append(i).append(":\n");
                info.append("Frequency: ").append(getCurrentFreq(i)).append(" MHz\n");
                info.append("Governor: ").append(getGovernor(i)).append("\n");
                info.append("Usage: ").append(getCoreUsage(i)).append("%\n");
            }

        } catch (Exception e) {
            return "SOC Info Unavailable";
        }
        return info.toString();
    }

    public static int getCoreCount() {
        File cpuDir = new File(CPU_DIR);
        File[] cpuFiles = cpuDir.listFiles(pathname ->
                pathname.getName().matches("cpu[0-9]+")
        );
        return cpuFiles != null ? cpuFiles.length : 0;
    }

    public static String getCurrentFreq(int core) {
        try (BufferedReader br = new BufferedReader(new FileReader(
                CPU_DIR + "cpu" + core + "/cpufreq/scaling_cur_freq"))) {
            // Convert from kHz to MHz
            return String.valueOf(Integer.parseInt(br.readLine()) / 1000);
        } catch (Exception e) {
            return "N/A";
        }
    }

    // New helper method to get the current frequency as an integer (in kHz)
    public static int getCurrentFreqValue(int core) {
        try (BufferedReader br = new BufferedReader(new FileReader(
                CPU_DIR + "cpu" + core + "/cpufreq/scaling_cur_freq"))) {
            return Integer.parseInt(br.readLine());
        } catch (Exception e) {
            return 0;
        }
    }

    // Updated getCoreUsage method that uses frequency as a proxy for usage.
    // This calculates the ratio of current frequency to max frequency.
    public static float getCoreUsage(int core) {
        int currentFreq = getCurrentFreqValue(core); // in kHz
        long maxFreq = getMaxFreq(core); // in kHz
        if (maxFreq == 0) return 0;
        float usage = ((float) currentFreq / maxFreq) * 100;
        return Math.round(usage * 100) / 100f;
    }
}

