package com.example.mid.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrength;
import android.telephony.TelephonyManager;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import java.util.List;

public class NetworkHelper {
    private final Context context;

    public NetworkHelper(Context context) {
        this.context = context;
    }

    // ✅ Get WiFi Signal Strength
    public String getWifiSignalStrength() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5);
            return "WiFi Signal Strength: " + level + "/5";
        }
        return "WiFi Not Connected";
    }

    // ✅ Get Mobile Network Signal Strength (Handles Permission Issues)
    public String getMobileSignalStrength() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return "Mobile Signal: Permission Denied";
        }

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();
            if (cellInfos != null) {
                for (CellInfo cellInfo : cellInfos) {
                    if (cellInfo instanceof CellInfoLte) {
                        CellSignalStrength signalStrength = ((CellInfoLte) cellInfo).getCellSignalStrength();
                        return "Mobile Signal Strength: " + signalStrength.getDbm() + " dBm";
                    }
                }
            }
        }
        return "No Mobile Signal";
    }

    // ✅ Check Network Connection
    public String getNetworkStatus() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return "Connected to WiFi";
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                return "Connected to Mobile Data";
            }
        }
        return "No Internet Connection";
    }
}
