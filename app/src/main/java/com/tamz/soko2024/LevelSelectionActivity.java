package com.tamz.soko2024;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class LevelSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_selection);

        ListView levelListView = findViewById(R.id.level_list_view);

        // Seznam názvů levelů
        ArrayList<String> levels = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            levels.add("Level " + i);
        }

        // Nastavení ArrayAdapteru
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, levels);
        levelListView.setAdapter(adapter);

        // Nastavení klikací události pro každý level
        levelListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int selectedLevel = position + 1; // Levely začínají od 1
                Intent intent = new Intent(LevelSelectionActivity.this, MainActivity.class);
                intent.putExtra("SELECTED_LEVEL", selectedLevel);

                Log.d("LevelSelectionActivity", "Selected level: " + selectedLevel);

                startActivity(intent); // Spustí MainActivity
                finish();
            }
        });
    }
}
