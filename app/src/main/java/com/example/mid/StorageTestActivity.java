package com.example.mid;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mid.helpers.StorageHelper;

public class StorageTestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_test);

        TextView internalText = findViewById(R.id.internalStorageText);
        TextView externalText = findViewById(R.id.externalStorageText);

        internalText.setText(StorageHelper.getInternalStorageInfo());
        externalText.setText(StorageHelper.getExternalStorageInfo());
    }
}