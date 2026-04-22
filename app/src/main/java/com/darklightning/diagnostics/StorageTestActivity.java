package com.darklightning.diagnostics;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.darklightning.diagnostics.helpers.StorageHelper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressLint("SetTextI18n")
public class StorageTestActivity extends AppCompatActivity {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_test);

        TextView internalText = findViewById(R.id.internalStorageText);
        TextView externalText = findViewById(R.id.externalStorageText);

        internalText.setText("Loading...");
        externalText.setText("Loading...");

        executorService.execute(() -> {
            String internal = StorageHelper.getInternalStorageInfo(StorageTestActivity.this);
            String external = StorageHelper.getExternalStorageInfo(StorageTestActivity.this);
            runOnUiThread(() -> {
                internalText.setText(internal);
                externalText.setText(external);
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}