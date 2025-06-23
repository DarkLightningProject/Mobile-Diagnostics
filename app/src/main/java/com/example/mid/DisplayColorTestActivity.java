package com.example.mid;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;

public class DisplayColorTestActivity extends AppCompatActivity {

    private RelativeLayout colorLayout;
    private Button changeColorButton;
    private int currentColorIndex = 0;
    private final int[] colors = {
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.WHITE,
            Color.BLACK,
            Color.YELLOW,
            Color.CYAN,
            Color.MAGENTA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_color_test);

        // Set fullscreen immersive mode
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        colorLayout = findViewById(R.id.colorLayout);
        changeColorButton = findViewById(R.id.btnChangeColor);

        updateBackgroundColor();

        changeColorButton.setOnClickListener(v -> {
            currentColorIndex = (currentColorIndex + 1) % colors.length;
            updateBackgroundColor();
        });
    }

    private void updateBackgroundColor() {
        colorLayout.setBackgroundColor(colors[currentColorIndex]);

        // Update button text color for visibility
        if (colors[currentColorIndex] == Color.BLACK ||
                colors[currentColorIndex] == Color.BLUE) {
            changeColorButton.setTextColor(Color.WHITE);
        } else {
            changeColorButton.setTextColor(Color.BLACK);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}