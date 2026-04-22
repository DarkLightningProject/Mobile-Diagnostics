package com.darklightning.diagnostics.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class CpuInfoHelper {
    private static final String CPU_DIR = "/sys/devices/system/cpu/";

    public static HashMap<String, List<Integer>> detectClusters() {
        HashMap<String, List<Integer>> clusters = new HashMap<>();
        // TreeMap sorts by key ascending: lowest max freq = Little, highest = Big
        TreeMap<Long, List<Integer>> freqGroups = new TreeMap<>();

        for (int i = 0; i < getCoreCount(); i++) {
            long maxFreq = getMaxFreq(i);
            if (!freqGroups.containsKey(maxFreq)) {
                freqGroups.put(maxFreq, new ArrayList<>());
            }
            freqGroups.get(maxFreq).add(i);
        }

        String[] labels = {"Little", "Medium", "Big"};
        int idx = 0;
        for (List<Integer> cores : freqGroups.values()) {
            String label = (idx < labels.length) ? labels[idx] : "Cluster " + idx;
            clusters.put(label + " Cluster", cores);
            idx++;
        }
        return clusters;
    }

    public static String getProcessorInfo() {
        StringBuilder info = new StringBuilder();

        String modelName = "Unknown";
        String hardware = "Unknown";
        String architecture = "Unknown";
        String revision = "Unknown";

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

        if (hardware.equalsIgnoreCase("ranchu")) {
            modelName = "Android Emulator";
            hardware = "ranchu (Emulator)";
            architecture = "x86_64 / arm64 (Emulator)";
            revision = "N/A";
        }

        if (modelName == null || modelName.contains("model") || modelName.equals("7")) {
            String qualcommModel = getSystemProperty("ro.soc.model");
            if (qualcommModel != null && !qualcommModel.isEmpty()) modelName = qualcommModel;
            else modelName = getSystemProperty("ro.board.platform");
        }

        if (hardware == null || hardware.equals("Unknown")) {
            hardware = getSystemProperty("ro.hardware");
        }

        if (revision == null || revision.equals("Unknown")) {
            revision = getSystemProperty("ro.revision");
        }

        info.append("Model: ").append(modelName != null ? modelName : "Unknown").append("\n");
        info.append("Hardware: ").append(hardware != null ? hardware : "Unknown").append("\n");
        info.append("Architecture: ").append(architecture != null ? architecture : "Unknown").append("\n");
        info.append("Revision: ").append(revision != null ? revision : "Unknown").append("\n");

        return info.toString();
    }

    private static String getSystemProperty(String property) {
        try {
            Process process = Runtime.getRuntime().exec("getprop " + property);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String value = reader.readLine();
                process.destroy();
                return (value != null && !value.isEmpty()) ? value : null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    public static String getGovernor(int core) {
        String path1 = CPU_DIR + "cpu" + core + "/cpufreq/scaling_governor";
        String path2 = CPU_DIR + "cpu" + core + "/cpufreq/policy/scaling_governor";

        String finalPath = new File(path1).exists() ? path1 : new File(path2).exists() ? path2 : null;
        if (finalPath == null) return "N/A";

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

    public static int getCoreCount() {
        File cpuDir = new File(CPU_DIR);
        File[] cpuFiles = cpuDir.listFiles(pathname -> pathname.getName().matches("cpu[0-9]+"));
        return cpuFiles != null ? cpuFiles.length : 0;
    }

    public static String getCurrentFreq(int core) {
        try (BufferedReader br = new BufferedReader(new FileReader(
                CPU_DIR + "cpu" + core + "/cpufreq/scaling_cur_freq"))) {
            return String.valueOf(Integer.parseInt(br.readLine()) / 1000);
        } catch (Exception e) {
            return "N/A";
        }
    }

    /**
     * Reads scaling_cur_freq exactly once and returns both Freq and Load
     * from that single snapshot — they are guaranteed to be consistent.
     *
     * @return long[2]: [0] = frequency in MHz for display,
     *                  [1] = load % (0–100, integer),
     *                  both -1 if the file is unavailable (core offline).
     */
    public static long[] getCoreFreqData(int core) {
        try (BufferedReader br = new BufferedReader(new FileReader(
                CPU_DIR + "cpu" + core + "/cpufreq/scaling_cur_freq"))) {
            long curKhz = Long.parseLong(br.readLine().trim());
            long maxKhz = getMaxFreq(core);
            long mhz    = curKhz / 1000;
            long load   = (maxKhz > 0) ? Math.min(100L, curKhz * 100L / maxKhz) : 0L;
            return new long[]{mhz, load};
        } catch (Exception e) {
            return new long[]{-1, -1};
        }
    }
}
