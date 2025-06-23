package com.example.mid;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.net.wifi.WifiInfo;

public class WifiSettingsActivity extends AppCompatActivity {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    private static final long UPDATE_INTERVAL = 3000; // 3 seconds
    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateWifiInfo();
            mainHandler.postDelayed(this, UPDATE_INTERVAL);
        }
    };

    private boolean isInternetAccessible() {
        try {
            // Try to reach Google's DNS server
            InetAddress inetAddress = InetAddress.getByName("8.8.8.8");
            return inetAddress.isReachable(1000); // Timeout: 1 second
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(android.net.Network network) {
                mainHandler.post(() -> updateWifiInfo()); // Update UI immediately when network changes
            }

            @Override
            public void onLost(android.net.Network network) {
                mainHandler.post(() -> updateWifiInfo()); // Update UI when network is lost
            }
        };
        connectivityManager.registerDefaultNetworkCallback(networkCallback);


        // Check and request permissions
        if (checkPermissions()) {
            mainHandler.post(updateRunnable);
        }
    }

    private void clearWifiDetails() {
        runOnUiThread(() -> {
            TextView ssidText = findViewById(R.id.ssidText);
            TextView ipText = findViewById(R.id.ipText);
            TextView rssiText = findViewById(R.id.rssiText);
            TextView linkSpeedText = findViewById(R.id.linkSpeedText);
            TextView frequencyText = findViewById(R.id.frequencyText);
            TextView bssidText = findViewById(R.id.bssidText);
            TextView wifiStandardText = findViewById(R.id.wifiStandardText);

            ssidText.setText("SSID: N/A");
            ipText.setText("IP: N/A");
            rssiText.setText("RSSI: N/A");
            linkSpeedText.setText("Link Speed: N/A");
            frequencyText.setText("Frequency: N/A");
            bssidText.setText("BSSID: N/A");
            wifiStandardText.setText("Wi-Fi Standard: N/A");
        });
    }



    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mainHandler.post(updateRunnable);
            } else {
                TextView internetStatusText = findViewById(R.id.internetStatusText);
                internetStatusText.setText("Location permission needed for network details");
            }
        }
    }

    private void updateWifiInfo() {
        executorService.execute(() -> {
            try {
                boolean hasInternet = isConnectedToInternet();
                boolean isWifi;
                boolean isMobile;

                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = null;

                if (cm != null) {
                    activeNetwork = cm.getActiveNetworkInfo();
                }

                if (activeNetwork != null) {
                    isWifi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
                    isMobile = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
                } else {
                    isWifi = false;
                    isMobile = false;
                }

                if (!isWifi) {
                    clearWifiDetails();
                }

                // Update Internet Status
                runOnUiThread(() -> {
                    TextView internetStatusText = findViewById(R.id.internetStatusText);
                    if (hasInternet) {
                        internetStatusText.setText("Internet: Connected ✅");
                    } else {
                        internetStatusText.setText("Internet: Not Connected ❌ (No Active Data)");
                    }
                });

                // Update Ping and Jitter
                if (hasInternet) {
                    String latency = getNetworkLatency("8.8.8.8");
                    String jitter = getJitter("8.8.8.8");



                    runOnUiThread(() -> {
                        TextView pingText = findViewById(R.id.pingText);
                        TextView jitterText = findViewById(R.id.jitterText);
                        pingText.setText("Ping: " + latency + " ms");
                        jitterText.setText("Jitter: " + jitter + " ms");
                    });
                }

                // Update Wi-Fi Details
                if (isWifi) {
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    if (wifiManager != null && checkPermissions()) {
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        if (wifiInfo != null && wifiInfo.getNetworkId() != -1) {
                            String bssid = wifiInfo.getBSSID();
                            boolean isValidBssid = bssid != null &&
                                    !bssid.equals("02:00:00:00:00:00") &&
                                    !bssid.equals("00:00:00:00:00:00");

                            String ssid = wifiInfo.getSSID().replace("\"", "");
                            String ipAddress = getIpAddress();
                            int rssi = wifiInfo.getRssi();
                            int linkSpeed = wifiInfo.getLinkSpeed();
                            String frequency = getFrequencyBand(wifiInfo.getFrequency());

                            runOnUiThread(() -> {
                                TextView ssidText = findViewById(R.id.ssidText);
                                TextView ipText = findViewById(R.id.ipText);
                                TextView rssiText = findViewById(R.id.rssiText);
                                TextView linkSpeedText = findViewById(R.id.linkSpeedText);
                                TextView frequencyText = findViewById(R.id.frequencyText);
                                TextView bssidText = findViewById(R.id.bssidText);
                                TextView wifiStandardText = findViewById(R.id.wifiStandardText);

                                ssidText.setText("SSID: " + ssid);
                                ipText.setText("IP: " + ipAddress);
                                rssiText.setText("RSSI: " + rssi + " dBm");
                                linkSpeedText.setText("Link Speed: " + linkSpeed + " Mbps");
                                frequencyText.setText("Frequency: " + frequency);

                                if (isValidBssid) {
                                    bssidText.setText("BSSID: " + bssid.toUpperCase());
                                } else {
                                    bssidText.setText("BSSID: N/A");
                                }

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    String standard = getWifiStandard(wifiInfo.getWifiStandard());
                                    wifiStandardText.setText("Wi-Fi Standard: " + standard);
                                } else {
                                    wifiStandardText.setText("Wi-Fi Standard: N/A");
                                }
                            });
                        }
                    }
                } runOnUiThread(() -> {
                    TextView wifiDetailsTitle = findViewById(R.id.wifiDetailsTitle);
                    if (isWifi) {
                        wifiDetailsTitle.setText("Wi-Fi Details"); // Or set to appropriate title
                    } else if (isMobile) {
                        wifiDetailsTitle.setText("Connected to Mobile Data");
                    } else {
                        wifiDetailsTitle.setText(""); // Not connected to either
                    }
                });

            } catch (SecurityException e) {
                Log.e("Security", "Missing permissions", e);
                runOnUiThread(() -> {
                    TextView wifiDetailsTitle = findViewById(R.id.wifiDetailsTitle);
                    wifiDetailsTitle.setText("(Enable location permissions for full details)");
                });
            } catch (Exception e) {
                Log.e("NetworkError", "Update failed", e);
                runOnUiThread(() -> {
                    TextView internetStatusText = findViewById(R.id.internetStatusText);
                    internetStatusText.setText("Error loading network info");
                });
            }
        });
    }

    private boolean isConnectedToInternet() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities != null &&
                        (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    // Perform an active internet test
                    return isInternetAccessible();
                }
            } else {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnected()) {
                    // Perform an active internet test
                    return isInternetAccessible();
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private String getIpAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            Log.e("IP Error", "Error getting IP", e);
        }
        return "N/A";
    }

    private String getFrequencyBand(int frequency) {
        if (frequency >= 2400 && frequency <= 2500) return "2.4 GHz";
        if (frequency >= 4900 && frequency <= 5900) return "5 GHz";
        if (frequency >= 5925 && frequency <= 7125) return "6 GHz";
        return String.valueOf(frequency) + " MHz";
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private String getWifiStandard(int standard) {
        switch (standard) {
            case 6:
                return "Wi-Fi 6 (802.11ax)";
            case 5:
                return "Wi-Fi 5 (802.11ac)";
            case 4:
                return "Wi-Fi 4 (802.11n)";
            case 3:
                return "Wi-Fi 3 (802.11g)";
            case 2:
                return "Wi-Fi 2 (802.11b)";
            case 1:
                return "Wi-Fi 1 (802.11a)";
            default:
                return "Unknown";
        }
    }

    private String getNetworkLatency(String host) {
        try {
            long startTime = System.currentTimeMillis();
            InetAddress.getByName(host).isReachable(1000);
            long endTime = System.currentTimeMillis();
            return String.valueOf(endTime - startTime);
        } catch (Exception e) {
            return "N/A";
        }
    }

    private String getJitter(String host) {
        try {
            long[] times = new long[5];
            for (int i = 0; i < 5; i++) {
                long startTime = System.currentTimeMillis();
                InetAddress.getByName(host).isReachable(500);
                long endTime = System.currentTimeMillis();
                times[i] = endTime - startTime;
            }
            long max = Long.MIN_VALUE, min = Long.MAX_VALUE;
            for (long time : times) {
                if (time > max) max = time;
                if (time < min) min = time;
            }
            return String.valueOf(max - min);
        } catch (Exception e) {
            return "N/A";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister network callback to prevent memory leaks
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }

        // Proper cleanup
        mainHandler.removeCallbacks(updateRunnable);
        executorService.shutdownNow();
    }

}