package com.example.mid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.TransportInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressLint("SetTextI18n")
public class WifiSettingsActivity extends AppCompatActivity {

    // Two executors: fast WiFi reads never block behind slow ping/jitter I/O
    private final ExecutorService wifiExecutor     = Executors.newSingleThreadExecutor();
    private final ExecutorService internetExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    // Captured inside onCapabilitiesChanged (API 31+) where it is non-redacted
    private volatile WifiInfo lastKnownWifiInfo = null;

    private static final long   INTERNET_UPDATE_INTERVAL  = 5000; // ms
    private static final int    LOCATION_PERMISSION_REQUEST = 1001;
    private static final String PING_HOST                  = "8.8.8.8";

    private final Runnable internetUpdateRunnable = new Runnable() {
        @Override public void run() {
            updateInternetStatus();
            mainHandler.postDelayed(this, INTERNET_UPDATE_INTERVAL);
        }
    };

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        setupNetworkCallback();
        connectivityManager.registerDefaultNetworkCallback(networkCallback);

        if (checkPermissions()) {
            updateWifiDetails();
            mainHandler.post(internetUpdateRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
        mainHandler.removeCallbacks(internetUpdateRunnable);
        wifiExecutor.shutdownNow();
        internetExecutor.shutdownNow();
    }

    // -------------------------------------------------------------------------
    // Network callback setup
    // -------------------------------------------------------------------------

    private void setupNetworkCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // FLAG_INCLUDE_LOCATION_INFO: onCapabilitiesChanged delivers a non-redacted
            // WifiInfo (real SSID/BSSID) when location permission is granted.
            networkCallback = new ConnectivityManager.NetworkCallback(
                    ConnectivityManager.NetworkCallback.FLAG_INCLUDE_LOCATION_INFO) {

                @Override public void onAvailable(@NonNull Network network) {
                    mainHandler.post(WifiSettingsActivity.this::updateWifiDetails);
                }

                @Override public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities caps) {
                    // Only place where getTransportInfo() returns unredacted WifiInfo on API 31+
                    TransportInfo ti = caps.getTransportInfo();
                    lastKnownWifiInfo = (ti instanceof WifiInfo) ? (WifiInfo) ti : null;
                    mainHandler.post(WifiSettingsActivity.this::updateWifiDetails);
                }

                @Override public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties lp) {
                    // Fires on IP changes: VPN connect/disconnect, DHCP renewal
                    mainHandler.post(WifiSettingsActivity.this::updateWifiDetails);
                }

                @Override public void onLost(@NonNull Network network) {
                    lastKnownWifiInfo = null;
                    mainHandler.post(WifiSettingsActivity.this::updateWifiDetails);
                }
            };
        } else {
            // API 30: onCapabilitiesChanged is available (API 26+); WifiInfo is read
            // via WifiManager.getConnectionInfo() inside updateWifiDetails().
            networkCallback = new ConnectivityManager.NetworkCallback() {

                @Override public void onAvailable(@NonNull Network network) {
                    mainHandler.post(WifiSettingsActivity.this::updateWifiDetails);
                }

                @Override public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities caps) {
                    mainHandler.post(WifiSettingsActivity.this::updateWifiDetails);
                }

                @Override public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties lp) {
                    mainHandler.post(WifiSettingsActivity.this::updateWifiDetails);
                }

                @Override public void onLost(@NonNull Network network) {
                    mainHandler.post(WifiSettingsActivity.this::updateWifiDetails);
                }
            };
        }
    }

    // -------------------------------------------------------------------------
    // Permissions
    // -------------------------------------------------------------------------

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateWifiDetails();
                mainHandler.post(internetUpdateRunnable);
            } else {
                updateUi(() -> ((TextView) findViewById(R.id.internetStatusText))
                        .setText("Location permission needed for network details"));
            }
        }
    }

    // -------------------------------------------------------------------------
    // UI helper — skips update if activity is already finishing/destroyed
    // -------------------------------------------------------------------------

    private void updateUi(Runnable action) {
        if (!isFinishing() && !isDestroyed()) {
            runOnUiThread(action);
        }
    }

    // -------------------------------------------------------------------------
    // Fast path: WiFi detail fields (no network I/O)
    // Called on every NetworkCallback event → real-time RSSI, IP, link speed
    // -------------------------------------------------------------------------

    private void updateWifiDetails() {
        wifiExecutor.execute(() -> {
            try {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkCapabilities caps = (cm != null)
                        ? cm.getNetworkCapabilities(cm.getActiveNetwork()) : null;

                boolean isWifi   = caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                boolean isMobile = caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);

                if (!isWifi) {
                    clearWifiDetails();
                    updateConnectionTitle(false, isMobile);
                    return;
                }

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    updateUi(() -> ((TextView) findViewById(R.id.wifiDetailsTitle))
                            .setText("(Enable location permission for full details)"));
                    return;
                }

                WifiInfo wifiInfo = getWifiInfo();

                if (wifiInfo == null) {
                    // API 31+: lastKnownWifiInfo not populated yet — onCapabilitiesChanged
                    // always follows onAvailable, so just update the title and wait.
                    // Do NOT call clearWifiDetails() here; that causes the N/A flash.
                    updateConnectionTitle(true, false);
                    return;
                }
                if (wifiInfo.getNetworkId() == -1) {
                    clearWifiDetails();
                    updateConnectionTitle(true, false);
                    return;
                }

                // SSID: getSSID() wraps with quotes and can return "<unknown ssid>" or null
                String rawSsid = wifiInfo.getSSID();
                String ssid = (rawSsid != null) ? rawSsid.replace("\"", "") : "Unknown";
                if (ssid.equals("<unknown ssid>") || ssid.isEmpty()) ssid = "Unknown";

                String bssid = wifiInfo.getBSSID();
                boolean validBssid = bssid != null
                        && !bssid.equals("02:00:00:00:00:00")
                        && !bssid.equals("00:00:00:00:00:00");

                String ipAddress = getIpAddress();

                int    rssiRaw   = wifiInfo.getRssi();
                String rssiStr   = (rssiRaw == Integer.MIN_VALUE) ? "Unknown" : rssiRaw + " dBm";

                int    speedRaw  = wifiInfo.getLinkSpeed();
                String speedStr  = (speedRaw < 0) ? "Unknown" : speedRaw + " Mbps";

                String frequency = getFrequencyBand(wifiInfo.getFrequency());
                String standard  = getWifiStandard(wifiInfo.getWifiStandard());

                // Capture finals for lambda
                final String displaySsid  = ssid;
                final String displayBssid = validBssid ? bssid.toUpperCase(Locale.ROOT) : "N/A";

                updateUi(() -> {
                    ((TextView) findViewById(R.id.ssidText)).setText("SSID: " + displaySsid);
                    ((TextView) findViewById(R.id.ipText)).setText("IP: " + ipAddress);
                    ((TextView) findViewById(R.id.rssiText)).setText("RSSI: " + rssiStr);
                    ((TextView) findViewById(R.id.linkSpeedText)).setText("Link Speed: " + speedStr);
                    ((TextView) findViewById(R.id.frequencyText)).setText("Frequency: " + frequency);
                    ((TextView) findViewById(R.id.bssidText)).setText("BSSID: " + displayBssid);
                    ((TextView) findViewById(R.id.wifiStandardText)).setText("Wi-Fi Standard: " + standard);
                });

                updateConnectionTitle(true, false);

            } catch (SecurityException e) {
                Log.e("WifiDetails", "Permission denied", e);
                updateUi(() -> ((TextView) findViewById(R.id.wifiDetailsTitle))
                        .setText("(Enable location permission for full details)"));
            } catch (Exception e) {
                Log.e("WifiDetails", "Update failed", e);
            }
        });
    }

    /** Returns non-redacted WifiInfo. API 31+ reads from last callback capture; API 30 uses WifiManager. */
    @SuppressLint("deprecation")
    @SuppressWarnings("deprecation")
    private WifiInfo getWifiInfo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return lastKnownWifiInfo;
        }
        // getConnectionInfo() is deprecated at API 31, but this branch only runs on API < 31.
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return (wm != null) ? wm.getConnectionInfo() : null;
    }

    private void updateConnectionTitle(boolean isWifi, boolean isMobile) {
        updateUi(() -> {
            TextView title = findViewById(R.id.wifiDetailsTitle);
            if (isWifi)        title.setText("Wi-Fi Details");
            else if (isMobile) title.setText("Connected to Mobile Data");
            else               title.setText("");
        });
    }

    private void clearWifiDetails() {
        updateUi(() -> {
            ((TextView) findViewById(R.id.ssidText)).setText("SSID: N/A");
            ((TextView) findViewById(R.id.ipText)).setText("IP: N/A");
            ((TextView) findViewById(R.id.rssiText)).setText("RSSI: N/A");
            ((TextView) findViewById(R.id.linkSpeedText)).setText("Link Speed: N/A");
            ((TextView) findViewById(R.id.frequencyText)).setText("Frequency: N/A");
            ((TextView) findViewById(R.id.bssidText)).setText("BSSID: N/A");
            ((TextView) findViewById(R.id.wifiStandardText)).setText("Wi-Fi Standard: N/A");
        });
    }

    // -------------------------------------------------------------------------
    // Slow path: internet connectivity + ping/jitter (real network I/O)
    // Only called by the 5-second timer — never blocks WiFi detail updates
    // -------------------------------------------------------------------------

    private void updateInternetStatus() {
        internetExecutor.execute(() -> {
            try {
                boolean hasInternet = isConnectedToInternet();

                updateUi(() -> ((TextView) findViewById(R.id.internetStatusText))
                        .setText(hasInternet
                                ? "Internet: Connected ✅"
                                : "Internet: Not Connected ❌ (No Active Data)"));

                if (hasInternet) {
                    String latency = getNetworkLatency();
                    String jitter  = getJitter();
                    updateUi(() -> {
                        ((TextView) findViewById(R.id.pingText)).setText("Ping: " + latency + " ms");
                        ((TextView) findViewById(R.id.jitterText)).setText("Jitter: " + jitter + " ms");
                    });
                }
            } catch (Exception e) {
                Log.e("InternetStatus", "Update failed", e);
                updateUi(() -> ((TextView) findViewById(R.id.internetStatusText))
                        .setText("Error loading network info"));
            }
        });
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private boolean isConnectedToInternet() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
            if (caps == null
                    || !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                return false;
            }
            // InetAddress.isReachable() uses ICMP/TCP-7 which is blocked on non-rooted Android.
            // Use TCP connect to port 53 (DNS) — same approach as ping measurement.
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(PING_HOST, 53), 1500);
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private String getIpAddress() {
        try {
            // getNetworkInterfaces() can return null on some devices (e.g. airplane mode)
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            if (ifaces == null) return "N/A";
            for (NetworkInterface intf : Collections.list(ifaces)) {
                for (InetAddress addr : Collections.list(intf.getInetAddresses())) {
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        // getHostAddress() can return null on rare JVM implementations
                        String hostAddr = addr.getHostAddress();
                        if (hostAddr != null) return hostAddr;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("WifiDetails", "Error getting IP", e);
        }
        return "N/A";
    }

    private String getFrequencyBand(int frequency) {
        if (frequency <= 0)                          return "Unknown";
        if (frequency >= 2400 && frequency <= 2500)  return "2.4 GHz";
        if (frequency >= 4900 && frequency <= 5900)  return "5 GHz";
        if (frequency >= 5925 && frequency <= 7125)  return "6 GHz";
        return frequency + " MHz";
    }

    private String getWifiStandard(int standard) {
        switch (standard) {
            case 6:  return "Wi-Fi 6 (802.11ax)";
            case 5:  return "Wi-Fi 5 (802.11ac)";
            case 4:  return "Wi-Fi 4 (802.11n)";
            case 3:  return "Wi-Fi 3 (802.11g)";
            case 2:  return "Wi-Fi 2 (802.11b)";
            case 1:  return "Wi-Fi 1 (802.11a)";
            default: return "Unknown";
        }
    }

    private long measureTcpLatency() throws Exception {
        long start = System.currentTimeMillis();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(PING_HOST, 53), 1000);
        }
        return System.currentTimeMillis() - start;
    }

    private String getNetworkLatency() {
        try {
            return String.valueOf(measureTcpLatency());
        } catch (Exception e) {
            return "N/A";
        }
    }

    private String getJitter() {
        try {
            long[] times = new long[5];
            for (int i = 0; i < 5; i++) times[i] = measureTcpLatency();
            long max = Long.MIN_VALUE, min = Long.MAX_VALUE;
            for (long t : times) {
                if (t > max) max = t;
                if (t < min) min = t;
            }
            return String.valueOf(max - min);
        } catch (Exception e) {
            return "N/A";
        }
    }
}
