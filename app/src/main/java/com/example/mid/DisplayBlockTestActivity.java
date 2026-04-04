package com.example.mid;

import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.GridLayout;
import androidx.appcompat.app.AppCompatActivity;

public class DisplayBlockTestActivity extends AppCompatActivity {

    private GridLayout blockGrid;
    private final int[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.WHITE};
    private final int INITIAL_COLOR = Color.WHITE;
    private final int MARGIN_DP = 2; // 2dp separation lines

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_block_test);

        // Fullscreen immersive mode
        WindowInsetsController controller = getWindow().getInsetsController();
        if (controller != null) {
            controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }

        blockGrid = findViewById(R.id.blockGrid);
        Button resetButton = findViewById(R.id.btnReset);

        // Get screen dimensions
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        // Convert margin to pixels
        int marginPx = (int) (MARGIN_DP * metrics.density);

        // Calculate block dimensions
        int blockWidth = (screenWidth - (2 * marginPx)) / 3;
        int blockHeight = (screenHeight - (2 * marginPx)) / 3;

        // Create 3x3 grid
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                View block = new View(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();

                // Set block size
                params.width = blockWidth;
                params.height = blockHeight;

                // Add margins (except first row/column)
                params.setMargins(
                        col == 0 ? 0 : marginPx,
                        row == 0 ? 0 : marginPx,
                        0,
                        0
                );

                block.setLayoutParams(params);
                block.setBackgroundColor(INITIAL_COLOR);
                block.setTag(INITIAL_COLOR); // track current color via tag

                // Set touch listener
                block.setOnClickListener(v -> {
                    int currentColor = (int) v.getTag();
                    int newColor = colors[(getColorIndex(currentColor) + 1) % colors.length];
                    v.setBackgroundColor(newColor);
                    v.setTag(newColor);
                });

                blockGrid.addView(block);
            }
        }

        resetButton.setOnClickListener(v -> resetAllBlocks());
    }

    private int getColorIndex(int color) {
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] == color) return i;
        }
        return 0;
    }

    private void resetAllBlocks() {
        for (int i = 0; i < blockGrid.getChildCount(); i++) {
            View block = blockGrid.getChildAt(i);
            block.setBackgroundColor(INITIAL_COLOR);
            block.setTag(INITIAL_COLOR);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}