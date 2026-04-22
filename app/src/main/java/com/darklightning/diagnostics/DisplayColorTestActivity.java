package com.darklightning.diagnostics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
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
        WindowInsetsController controller = getWindow().getInsetsController();
        if (controller != null) {
            controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }

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

}