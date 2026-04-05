package com.example.mid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressLint("SetTextI18n")
public class SimSettingsActivity extends AppCompatActivity {
    private TextView simSlotText, carrierText, countryText, simCodeText, volteText;
    private Spinner simSlotSpinner;
    private Button refreshButton;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final int PERMISSION_REQUEST_CODE = 1;
    private List<SubscriptionInfo> subscriptionInfoList;
    private SubscriptionManager subscriptionManager;
    private TelephonyManager telephonyManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sim);

        simSlotText = findViewById(R.id.simSlotText);
        carrierText = findViewById(R.id.carrierText);
        countryText = findViewById(R.id.countryText);
        simCodeText = findViewById(R.id.simCodeText); // Changed from networkOperatorText
        volteText = findViewById(R.id.volteText);
        refreshButton = findViewById(R.id.refreshButton);
        simSlotSpinner = findViewById(R.id.simSlotSpinner);

        subscriptionManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        refreshButton.setOnClickListener(v -> requestPermissionsIfNeeded());

        requestPermissionsIfNeeded();
    }

    private void requestPermissionsIfNeeded() {
        List<String> requiredPermissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.READ_PHONE_NUMBERS);
        }
        if (!requiredPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, requiredPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            executorService.execute(this::setupSimSelection);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Only need READ_PHONE_STATE to proceed; READ_PHONE_NUMBERS is best-effort
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                executorService.execute(this::setupSimSelection);
            }
        }
    }

    private void setupSimSelection() {
        if (subscriptionManager == null) return;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
        if (subscriptionInfoList == null || subscriptionInfoList.isEmpty()) {
            runOnUiThread(() -> simSlotText.setText("No Active SIM Cards Found"));
            return;
        }

        List<String> simOptions = new ArrayList<>();
        for (SubscriptionInfo info : subscriptionInfoList) {
            int simSlotIndex = info.getSimSlotIndex();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && info.isEmbedded()) {
                simOptions.add("eSIM");
            } else {
                simOptions.add("SIM " + (simSlotIndex + 1));
            }
        }

        runOnUiThread(() -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, simOptions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            simSlotSpinner.setAdapter(adapter);
            simSlotSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                    executorService.execute(() -> getSimDetails(subscriptionInfoList.get(position)));
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {}
            });
        });
    }

    private void getSimDetails(SubscriptionInfo info) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        int subId = info.getSubscriptionId();
        TelephonyManager specificTelephonyManager = telephonyManager.createForSubscriptionId(subId);

        int simSlotIndex = info.getSimSlotIndex();
        String carrierName = info.getCarrierName().toString();
        String countryIso = info.getCountryIso();

        // MCC and MNC
        int mcc = info.getMcc();
        int mnc = info.getMnc();

        // Get Network Operator code (MCC + MNC)
        String networkOperator = specificTelephonyManager.getNetworkOperator();
        String msc = (networkOperator.length() >= 5) ? networkOperator.substring(3) : "N/A"; // Extract MSC if available

        String simState = getSimStateString(specificTelephonyManager.getSimState());

        runOnUiThread(() -> {
            simSlotText.setText("SIM Slot: " + (info.isEmbedded() ? "eSIM" : (simSlotIndex + 1)));
            carrierText.setText("Carrier: " + carrierName);
            countryText.setText("Country: " + countryIso);
            simCodeText.setText("MCC: " + mcc + ", MNC: " + mnc + ", MSC: " + msc); // Updated to show MCC, MNC, MSC
            volteText.setText("SIM State: " + simState);
        });
    }


    private String getSimStateString(int simState) {
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                return "No SIM";
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                return "PIN Required";
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                return "PUK Required";
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                return "Network Locked";
            case TelephonyManager.SIM_STATE_READY:
                return "Ready";
            case TelephonyManager.SIM_STATE_NOT_READY:
                return "Not Ready";
            case TelephonyManager.SIM_STATE_PERM_DISABLED:
                return "Permanently Disabled";
            case TelephonyManager.SIM_STATE_CARD_IO_ERROR:
                return "Card IO Error";
            case TelephonyManager.SIM_STATE_CARD_RESTRICTED:
                return "Card Restricted";
            default:
                return "Unknown";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}