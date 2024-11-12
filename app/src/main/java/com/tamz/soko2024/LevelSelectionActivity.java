package com.tamz.soko2024;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class LevelSelectionActivity extends AppCompatActivity {
    private RecyclerView levelRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_selection);

        levelRecyclerView = findViewById(R.id.level_recycler_view);
        levelRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<String> levels = getLevelsFromFile();
        int previewSize = 150;

        LevelPreviewAdapter adapter = new LevelPreviewAdapter(this, levels, levels.size(), previewSize, previewSize);
        levelRecyclerView.setAdapter(adapter);

    }

    @Override
    protected void onResume() {
        super.onResume();

        ArrayList<String> levels = getLevelsFromFile();
        int previewSize = 150;

        LevelPreviewAdapter adapter = new LevelPreviewAdapter(this, levels, levels.size(), previewSize, previewSize);

        levelRecyclerView.setAdapter(adapter);
    }
    private ArrayList<String> getLevelsFromFile() {
        ArrayList<String> levels = new ArrayList<>();

        try {
            InputStream inputStream = getAssets().open("levels/levels.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("Level")) {
                    String[] parts = line.split(" ");
                    int levelNumber = Integer.parseInt(parts[1]);

                    String levelName = reader.readLine().trim().replace("'", "");

                    if (levelName.contains("#")){
                        levelName = "No name";
                    }

                    levels.add("Level " + levelNumber + ": " + levelName);

                    while ((line = reader.readLine()) != null && !line.isEmpty()) {

                    }
                }
            }
            reader.close();

        } catch (IOException e) {
            Log.e("LevelSelectionActivity", "Error reading levels file", e);
        }

        return levels;
    }
}
