package com.example.mid;

import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
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
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

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

                // Set touch listener
                block.setOnClickListener(v -> {
                    int currentColor = ((View) v).getBackgroundTintList() != null ?
                            ((View) v).getBackgroundTintList().getDefaultColor() : INITIAL_COLOR;
                    int newIndex = (getColorIndex(currentColor) + 1) % colors.length;
                    v.setBackgroundColor(colors[newIndex]);
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
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}