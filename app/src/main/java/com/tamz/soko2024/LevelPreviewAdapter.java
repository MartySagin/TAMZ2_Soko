package com.tamz.soko2024;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LevelPreviewAdapter extends RecyclerView.Adapter<LevelPreviewAdapter.LevelViewHolder> {
    private final Context context;
    private final List<String> levelNames;
    private final int totalLevels;
    private final int previewWidth;
    private final int previewHeight;

    public LevelPreviewAdapter(Context context, List<String> levelNames, int totalLevels, int previewWidth, int previewHeight) {
        this.context = context;
        this.levelNames = levelNames;
        this.totalLevels = totalLevels;
        this.previewWidth = previewWidth;
        this.previewHeight = previewHeight;
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

        SokoView sokoView = new SokoView(context);
        Bitmap levelPreview = sokoView.generateLevelPreview(levelNumber, previewWidth, previewHeight);
        holder.levelImage.setImageBitmap(levelPreview);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("SELECTED_LEVEL", levelNumber);
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

        public LevelViewHolder(@NonNull View itemView) {
            super(itemView);
            levelImage = itemView.findViewById(R.id.level_image);
            levelName = itemView.findViewById(R.id.level_name);
        }
    }
}
