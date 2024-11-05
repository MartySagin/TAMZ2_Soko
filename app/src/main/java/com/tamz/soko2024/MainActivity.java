package com.tamz.soko2024;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import com.tamz.soko2024.SokoView;

public class MainActivity extends AppCompatActivity {

    private TextView timerTextView;
    private Button restartButton;
    private Handler timerHandler = new Handler();
    private long startTime = 0;
    private SokoView sokoView;

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
        sokoView = findViewById(R.id.sokoView);

        startTimer();

        restartButton.setOnClickListener(v -> restartLevel());
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
}
