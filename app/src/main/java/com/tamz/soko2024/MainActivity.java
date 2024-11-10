package com.tamz.soko2024;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.LogPrinter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SokoView.OnLevelCompleteListener {

    private static final int LEVEL_SELECTION_REQUEST_CODE = 1;
    private TextView timerTextView;
    private Button restartButton;
    private Button selectLevelButton; // Nové tlačítko pro výběr levelu
    private Handler timerHandler = new Handler();
    private long startTime = 0;
    private SokoView sokoView;

    private int currentLevel = 1;

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;

            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;

            seconds = seconds % 60;

            timerTextView.setText(String.format("%d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerTextView = findViewById(R.id.timerTextView);
        restartButton = findViewById(R.id.restartButton);
        selectLevelButton = findViewById(R.id.selectLevelButton); // Přidání tlačítka výběru levelu
        sokoView = findViewById(R.id.sokoView);

        startTimer();

        if (sokoView != null) {
            sokoView.setOnLevelCompleteListener(this);
        }

        restartButton.setOnClickListener(v -> restartLevel());
        selectLevelButton.setOnClickListener(v -> openLevelSelection()); // Nastavení posluchače pro tlačítko

        currentLevel = getIntent().getIntExtra("SELECTED_LEVEL", 1); // Výchozí hodnota 1
        Log.d("MainActivity", "Načtený level: " + currentLevel);

        loadSelectedLevel(currentLevel); // Načte vybraný level do SokoView
    }

    @Override
    public void onLevelComplete() {
        currentLevel++; // Posun na další level
        Toast.makeText(this, "Načítám další level: " + currentLevel, Toast.LENGTH_SHORT).show();
        loadSelectedLevel(currentLevel); // Načtení dalšího levelu
    }

    private void startTimer() {
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
    }

    public void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void restartLevel() {
        stopTimer();
        startTimer();

        if (sokoView != null) {
            sokoView.restartLevel();
        }
    }

    private void openLevelSelection() {
        // Spustí novou aktivitu pro výběr levelu
        Log.e("MainActivity", "openLevelSelection() not implemented yet!");

        Intent intent = new Intent(this, LevelSelectionActivity.class);
        startActivityForResult(intent, LEVEL_SELECTION_REQUEST_CODE);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LEVEL_SELECTION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            int selectedLevel = data.getIntExtra("SELECTED_LEVEL", 1);
            Toast.makeText(this, "Načítám level " + selectedLevel, Toast.LENGTH_SHORT).show();
            loadSelectedLevel(selectedLevel);
        }
    }

    private void loadSelectedLevel(int level) {
        Log.d("MainActivity", "Načítám level: " + level);
        stopTimer();
        startTimer();

        if (sokoView != null) {
            sokoView.loadLevel(level); // Načte vybraný level v SokoView
        } else {
            Log.e("MainActivity", "sokoView is null!");
        }
    }
}
