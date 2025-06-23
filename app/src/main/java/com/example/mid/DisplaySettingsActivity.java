package com.example.mid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mid.helpers.DisplayHelper;

public class DisplaySettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_settings);

        Button testDisplayButton = findViewById(R.id.btnTestDisplay);
        testDisplayButton.setOnClickListener(v -> {
            startActivity(new Intent(this, DisplayColorTestActivity.class));
        });

        Button testBlocksButton = findViewById(R.id.btnTestBlocks);
        testBlocksButton.setOnClickListener(v -> {
            startActivity(new Intent(this, DisplayBlockTestActivity.class));
        });

        TextView resolutionText = findViewById(R.id.resolutionText);
        TextView densityText = findViewById(R.id.densityText);
        TextView sizeText = findViewById(R.id.sizeText);

        resolutionText.setText("Resolution: " + DisplayHelper.getScreenResolution(this));
        densityText.setText("Density: " + DisplayHelper.getScreenDensity(this));
        sizeText.setText("Screen Size: " + DisplayHelper.getScreenSizeInches(this));
    }
}