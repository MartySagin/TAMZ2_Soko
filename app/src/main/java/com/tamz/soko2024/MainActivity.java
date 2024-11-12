package com.tamz.soko2024;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.LogPrinter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SokoView.OnLevelCompleteListener {

    private static final int LEVEL_SELECTION_REQUEST_CODE = 1;
    private TextView timerTextView;
    private Button restartButton;
    private Button selectLevelButton; // Nové tlačítko pro výběr levelu
    private Handler timerHandler = new Handler();
    public long startTime = 0;
    private SokoView sokoView;

    public int currentLevel = 1;

    Button nextLevelButton;


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

        if (sokoView != null) {
            sokoView.setOnLevelCompleteListener(this);
        }

        restartButton.setOnClickListener(v -> restartLevel());
        selectLevelButton.setOnClickListener(v -> openLevelSelection()); // Nastavení posluchače pro tlačítko

        currentLevel = getIntent().getIntExtra("SELECTED_LEVEL", 1); // Výchozí hodnota 1

        nextLevelButton = findViewById(R.id.nextLevelButton);

        sokoView.setNextLevelButton(nextLevelButton);

        boolean resumeLevel = getIntent().getBooleanExtra("RESUME_LEVEL", false);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if (sokoView != null) {
                    sokoView.saveGameState();
                }

                SharedPreferences sharedPref = getSharedPreferences("level_progress_prefs", MODE_PRIVATE);

                Log.e("MainActivity", sharedPref.getAll().toString());

                finish();
            }
        };

        getOnBackPressedDispatcher().addCallback(this, callback);

        if (resumeLevel) {
            sokoView.loadGameState();

            SharedPreferences sharedPref = getSharedPreferences("level_progress_prefs", MODE_PRIVATE);
            int savedMinutes = sharedPref.getInt("savedMinutes_" + currentLevel, 0);
            int savedSeconds = sharedPref.getInt("savedSeconds_" + currentLevel, 0);

            timerTextView.setText(String.format("%d:%02d", savedMinutes, savedSeconds));

            long elapsedMillis = savedMinutes * 60000 + savedSeconds * 1000;
            resumeTimer(elapsedMillis);

        } else {
            loadSelectedLevel(currentLevel);
            startTimer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (sokoView != null) {
            sokoView.saveGameState();
        }
    }



    @Override
    public void onLevelComplete() {
        currentLevel++;

        Toast.makeText(this, "Načítám další level: " + currentLevel, Toast.LENGTH_SHORT).show();

        loadSelectedLevel(currentLevel);
    }


    public void startTimer() {
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
    }

    public void resumeTimer(long elapsedTime) {
        startTime = System.currentTimeMillis() - elapsedTime;
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

        sokoView.saveGameState();

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
