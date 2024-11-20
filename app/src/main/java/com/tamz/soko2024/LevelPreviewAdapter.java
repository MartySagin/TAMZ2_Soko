package com.tamz.soko2024;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Button;
import java.util.List;

public class LevelPreviewAdapter extends RecyclerView.Adapter<LevelPreviewAdapter.LevelViewHolder> {
    private final Context context;
    private final List<String> levelNames;
    private final int totalLevels;
    private final int previewWidth;
    private final int previewHeight;

    private final SharedPreferences sharedPreferences;
    private final HighScoreDatabaseHelper dbHelper;

    public LevelPreviewAdapter(Context context, List<String> levelNames, int totalLevels, int previewWidth, int previewHeight) {
        this.context = context;
        this.levelNames = levelNames;
        this.totalLevels = totalLevels;
        this.previewWidth = previewWidth;
        this.previewHeight = previewHeight;

        this.dbHelper = HighScoreDatabaseHelper.getInstance(context);

        this.sharedPreferences = context.getSharedPreferences("level_progress_prefs", Context.MODE_PRIVATE);

        Log.i("LevelPreviewAdapter", sharedPreferences.getAll().toString());
    }

    @NonNull
    @Override
    public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.level_preview_item, parent, false);
        return new LevelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
        int levelNumber = position + 1;
        holder.levelName.setText(levelNames.get(position));

        int savedLevel = sharedPreferences.getInt("savedCurrentLevel", -1);

        SokoView sokoView = new SokoView(context);
        Bitmap levelPreview = sokoView.generateLevelPreview(levelNumber, previewWidth, previewHeight, savedLevel == levelNumber);
        holder.levelImage.setImageBitmap(levelPreview);

        int topScore = dbHelper.getHighScore(levelNumber);
        String topScoreText = (topScore == -1) ? "No score yet" : "Top Score: " + topScore;
        holder.topScoreText.setText(topScoreText);

        holder.resumeLevelButton.setVisibility(savedLevel == levelNumber ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("SELECTED_LEVEL", levelNumber);
            context.startActivity(intent);
        });

        holder.resumeLevelButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("SELECTED_LEVEL", levelNumber);
            intent.putExtra("RESUME_LEVEL", true);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return totalLevels;
    }

    static class LevelViewHolder extends RecyclerView.ViewHolder {
        ImageView levelImage;
        TextView levelName;
        Button resumeLevelButton;
        TextView topScoreText;

        public LevelViewHolder(@NonNull View itemView) {
            super(itemView);
            topScoreText = itemView.findViewById(R.id.level_top_score);
            levelImage = itemView.findViewById(R.id.level_image);
            levelName = itemView.findViewById(R.id.level_name);
            resumeLevelButton = itemView.findViewById(R.id.resumeLevelButton);
        }
    }
}
