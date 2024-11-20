package com.tamz.soko2024;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SokoView extends View {

    Bitmap[] bmp;

    int lW;
    int lH;

    int pX;
    int pY;

    int width;
    int height;

    private int[] level;
    private int[] levelCopy;

    public MainActivity mainActivity;
    boolean finishedLevel = false;


    public interface OnLevelCompleteListener {
        void onLevelComplete();
    }

    private OnLevelCompleteListener levelCompleteListener;

    public void setOnLevelCompleteListener(OnLevelCompleteListener listener) {
        this.levelCompleteListener = listener;
    }

    public HighScoreDatabaseHelper dbHelper;

    public SokoView(Context context) {
        super(context);
        init(context);
    }

    public SokoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        mainActivity = (MainActivity) context;

    }

    public SokoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    void init(Context context) {
        bmp = new Bitmap[6];
        bmp[0] = BitmapFactory.decodeResource(getResources(), R.drawable.empty);
        bmp[1] = BitmapFactory.decodeResource(getResources(), R.drawable.wall);
        bmp[2] = BitmapFactory.decodeResource(getResources(), R.drawable.box);
        bmp[3] = BitmapFactory.decodeResource(getResources(), R.drawable.goal);
        bmp[4] = BitmapFactory.decodeResource(getResources(), R.drawable.hero);
        bmp[5] = BitmapFactory.decodeResource(getResources(), R.drawable.boxok);


        dbHelper = HighScoreDatabaseHelper.getInstance(context);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        recalculateCellSize();
    }

    public void saveGameState() {
        final String SHARED_PREF_NAME = "level_progress_prefs";
        SharedPreferences sharedPref = getContext().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();


        editor.putInt("savedCurrentLevel", mainActivity.currentLevel);

        long elapsedTimeMillis = System.currentTimeMillis() - mainActivity.startTime;
        int elapsedMinutes = (int) (elapsedTimeMillis / 60000);
        int elapsedSeconds = (int) ((elapsedTimeMillis / 1000) % 60);

        editor.putInt("savedMinutes_" + mainActivity.currentLevel, elapsedMinutes);
        editor.putInt("savedSeconds_" + mainActivity.currentLevel, elapsedSeconds);

        StringBuilder levelString = new StringBuilder();

        for (int i = 0; i < level.length; i++) {
            levelString.append(level[i]);

            if (i < level.length - 1) {
                levelString.append(",");
            }
        }
        editor.putString("savedLevel", levelString.toString());

        StringBuilder originalLevelString = new StringBuilder();

        for (int i = 0; i < levelCopy.length; i++) {
            originalLevelString.append(levelCopy[i]);

            if (i < levelCopy.length - 1) {
                originalLevelString.append(",");
            }
        }

        editor.putString("originalLevel", originalLevelString.toString());

        editor.putInt("savedPX", pX);
        editor.putInt("savedPY", pY);

        editor.putInt("savedLW", lW);
        editor.putInt("savedLH", lH);

        editor.putInt("savedMoveCount", mainActivity.moveCount);

        editor.apply();
    }

    public void loadOriginalLevelState() {
        final String SHARED_PREF_NAME = "level_progress_prefs";
        SharedPreferences sharedPref = getContext().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        String originalLevelString = sharedPref.getString("originalLevel", null);

        if (originalLevelString != null) {
            String[] originalLevelArray = originalLevelString.split(",");

            levelCopy = new int[originalLevelArray.length];

            for (int i = 0; i < originalLevelArray.length; i++) {
                levelCopy[i] = Integer.parseInt(originalLevelArray[i]);
            }

            recalculateCellSize();

            requestLayout();

            invalidate();
        } else {
            Log.e("SokoView", "Failed to load original level data.");
        }
    }

    public void loadGameState() {
        final String SHARED_PREF_NAME = "level_progress_prefs";
        SharedPreferences sharedPref = getContext().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        int savedCurrentLevel = sharedPref.getInt("savedCurrentLevel", -1);

        if (savedCurrentLevel == -1) {
            Log.e("SokoView", "No saved game state found.");
            return;
        }

        mainActivity.currentLevel = savedCurrentLevel;

        long savedElapsedTime = sharedPref.getLong("savedElapsedTime_" + savedCurrentLevel, 0);
        mainActivity.startTime = System.currentTimeMillis() - savedElapsedTime;

        lW = sharedPref.getInt("savedLW", 0);
        lH = sharedPref.getInt("savedLH", 0);

        pX = sharedPref.getInt("savedPX", 0);
        pY = sharedPref.getInt("savedPY", 0);

        mainActivity.moveCount = sharedPref.getInt("savedMoveCount", 0);

        String savedLevelString = sharedPref.getString("savedLevel", null);

        if (savedLevelString != null) {
            String[] levelStringArray = savedLevelString.split(",");

            level = new int[levelStringArray.length];

            for (int i = 0; i < levelStringArray.length; i++) {
                level[i] = Integer.parseInt(levelStringArray[i]);
            }
        } else {
            Log.e("SokoView", "Failed to load saved level data.");

            return;
        }

        loadOriginalLevelState();

        finishedLevel = false;

        recalculateCellSize();

        requestLayout();

        invalidate();

        Log.i("SokoView", "Game state loaded with elapsed time: " + savedElapsedTime + " ms for level " + mainActivity.currentLevel);
    }


    private int[] loadLevelFromFile(int levelNumber) {
        List<String> layoutLines = new ArrayList<>();
        boolean levelFound = false;

        lW = 0;
        lH = 0;

        try {
            InputStream inputStream = getContext().getAssets().open("levels/levels.txt");

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.equals("Level " + levelNumber)) {
                    levelFound = true;

                    layoutLines.clear();

                    continue;
                }

                if (levelFound) {

                    if (line.startsWith("'")) {
                        continue;
                    }

                    if (line.isEmpty()) {
                        break;
                    }

                    layoutLines.add(line);

                    lW = Math.max(lW, line.length());
                }
            }
            reader.close();

            if (!levelFound) {
                Log.e("SokoView", "Level " + levelNumber + " not found in the file.");

                Toast.makeText(getContext(), "Level " + levelNumber + " not found.", Toast.LENGTH_SHORT).show();

                return null;
            }

            lH = layoutLines.size();


            List<Integer> levelData = new ArrayList<>();

            for (String layoutLine : layoutLines) {
                for (int i = 0; i < layoutLine.length(); i++) {
                    char ch = layoutLine.charAt(i);
                    switch (ch) {
                        case ' ':
                            levelData.add(0); // Floor
                            break;
                        case '#':
                            levelData.add(1); // Wall
                            break;
                        case '$':
                            levelData.add(2); // Box
                            break;
                        case '.':
                            levelData.add(3); // Cross
                            break;
                        case '@':
                            levelData.add(4); // Player
                            break;
                        case '*':
                            levelData.add(5); // Box on cross
                            break;
                        case '+':
                            levelData.add(4); // Player on cross
                            break;
                        default:
                            levelData.add(0); // Unknown character treated as floor
                    }
                }

                for (int i = layoutLine.length(); i < lW; i++) {
                    levelData.add(0);
                }
            }

            Log.d("SokoView", "Level " + levelNumber + " loaded: width " + lW + ", height " + lH);

            int[] loadedLevel = new int[levelData.size()];

            for (int i = 0; i < levelData.size(); i++) {
                loadedLevel[i] = levelData.get(i);
            }

            return loadedLevel;

        } catch (IOException e) {
            Log.e("SokoView", "Error loading level from levels.txt", e);
            return null;
        }
    }


    public void loadLevel(int levelNumber) {
        int[] loadedLevel = loadLevelFromFile(levelNumber);

        if (loadedLevel == null) {
            Toast.makeText(getContext(), "Nepodařilo se načíst level " + levelNumber, Toast.LENGTH_SHORT).show();
            return;
        }

        level = loadedLevel;

        levelCopy = new int[level.length];
        System.arraycopy(level, 0, levelCopy, 0, level.length);

        findPlayerStartPosition();

        Log.e("SokoView",  "(" + pX + ", " + pY + ")");

        finishedLevel = false;

        recalculateCellSize();

        requestLayout();
        invalidate();
    }




    private void findPlayerStartPosition() {
        for (int y = 0; y < lH; y++) {
            for (int x = 0; x < lW; x++) {
                if (level[y * lW + x] == 4) {
                    pX = x;
                    pY = y;

                    return;
                }
            }
        }

        pX = 0;
        pY = 0;
    }

    public void restartLevel() {
        System.arraycopy(levelCopy, 0, level, 0, level.length);
        findPlayerStartPosition();
        finishedLevel = false;

        nextLevelButton.setEnabled(false);

        invalidate();
    }

    private void recalculateCellSize() {
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        if (lW > 0 && lH > 0 && viewWidth > 0 && viewHeight > 0) {

            float cellWidth = (float) viewWidth / lW;
            float cellHeight = (float) viewHeight / lH;

            float cellSize = Math.min(cellWidth, cellHeight);

            width = (int) cellSize;
            height = (int) cellSize;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (width <= 0 || height <= 0) return;

        int offsetX = (getWidth() - (width * lW)) / 2;
        int offsetY = (getHeight() - (height * lH)) / 2;

        for (int y = 0; y < lH; y++) {
            for (int x = 0; x < lW; x++) {
                int cellValue = level[y * lW + x];
                Rect dstRect = new Rect(
                        offsetX + x * width,
                        offsetY + y * height,
                        offsetX + (x + 1) * width,
                        offsetY + (y + 1) * height
                );
                canvas.drawBitmap(bmp[cellValue], null, dstRect, null);
            }
        }

        if (!finishedLevel) {
            CheckIfLevelIsComplete();
        }
    }

    public boolean CheckIfLevelIsComplete() {
        if (level == null) {
            return false;
        }

        for (int cell : level) {
            if (cell == 2) { // Box still remaining
                nextLevelButton.setEnabled(false);
                return false;
            }
        }

        finishedLevel = true;
        mainActivity.stopTimer();

        int currentMoves = mainActivity.moveCount;
        int bestMoves = dbHelper.getHighScore(mainActivity.currentLevel);

        if (bestMoves == -1 || currentMoves < bestMoves) {
            Log.d("SokoView", "New high score achieved: " + currentMoves);
            dbHelper.saveHighScore(mainActivity.currentLevel, currentMoves);
            Toast.makeText(getContext(), "New high score!", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("SokoView", "Level completed without high score.");
            Toast.makeText(getContext(), "Level completed!", Toast.LENGTH_SHORT).show();
        }

        if (nextLevelButton != null) {
            nextLevelButton.setEnabled(true);
            nextLevelButton.setOnClickListener(v -> {
                loadNextLevel();
                nextLevelButton.setEnabled(false);
                mainActivity.startTimer();
            });
        }

        return true;
    }



    public void loadNextLevel() {
        if (levelCompleteListener != null) {
            levelCompleteListener.onLevelComplete();

            mainActivity.resetMoveCount();
        }
    }

    private float startX;
    private float startY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();

                return true;

            case MotionEvent.ACTION_UP:
                float endX = event.getX();
                float endY = event.getY();

                float deltaX = endX - startX;
                float deltaY = endY - startY;

                if (finishedLevel) {
                    break;
                }

                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    if (deltaX > 0) { // Right swipe
                        if (level[pY * lW + pX + 1] == 1) break;

                        if (level[pY * lW + pX + 1] == 2 || level[pY * lW + pX + 1] == 5) {
                            if (level[pY * lW + pX + 2] == 1 || level[pY * lW + pX + 2] == 2 || level[pY * lW + pX + 2] == 5) break;

                            if (levelCopy[pY * lW + pX] == 5) {
                                level[pY * lW + pX] = 3;
                            } else if (level[pY * lW + pX] == 0 || level[pY * lW + pX] == 4) {
                                level[pY * lW + pX] = 0;
                            }
                            if (levelCopy[pY * lW + pX] == 3) level[pY * lW + pX] = 3;

                            level[pY * lW + pX + 1] = 4;

                            if (levelCopy[pY * lW + pX + 2] == 3 || levelCopy[pY * lW + pX + 2] == 5) level[pY * lW + pX + 2] = 5;
                            else level[pY * lW + pX + 2] = 2;

                            pX++;
                            mainActivity.updateMoveCount();
                            break;
                        }

                        if (levelCopy[pY * lW + pX] == 3 || levelCopy[pY * lW + pX] == 5) {
                            level[pY * lW + pX] = 3;
                            level[pY * lW + pX + 1] = 4;
                            pX++;
                            mainActivity.updateMoveCount();
                            break;
                        }

                        if (level[pY * lW + pX] == 0) {
                            level[pY * lW + pX] = 0;
                            level[pY * lW + pX + 1] = 4;
                            pX++;
                            mainActivity.updateMoveCount();
                            break;
                        }

                        if (level[pY * lW + pX + 1] == 0 || level[pY * lW + pX + 1] == 3) {
                            level[pY * lW + pX] = 0;
                            level[pY * lW + pX + 1] = 4;
                            pX++;
                            mainActivity.updateMoveCount();
                            break;
                        }

                    } else { // Left swipe
                        if (level[pY * lW + pX - 1] == 1) break;

                        if (level[pY * lW + pX - 1] == 2 || level[pY * lW + pX - 1] == 5) {
                            if (level[pY * lW + pX - 2] == 1 || level[pY * lW + pX - 2] == 2 || level[pY * lW + pX - 2] == 5) break;

                            if (levelCopy[pY * lW + pX] == 5) {
                                level[pY * lW + pX] = 3;
                            } else if (level[pY * lW + pX] == 0 || level[pY * lW + pX] == 4) {
                                level[pY * lW + pX] = 0;
                            }
                            if (levelCopy[pY * lW + pX] == 3) level[pY * lW + pX] = 3;

                            level[pY * lW + pX - 1] = 4;

                            if (levelCopy[pY * lW + pX - 2] == 3 || levelCopy[pY * lW + pX - 2] == 5) level[pY * lW + pX - 2] = 5;
                            else level[pY * lW + pX - 2] = 2;

                            pX--;
                            mainActivity.updateMoveCount();
                            break;
                        }

                        if (levelCopy[pY * lW + pX] == 3 || levelCopy[pY * lW + pX] == 5) {
                            level[pY * lW + pX] = 3;
                            level[pY * lW + pX - 1] = 4;
                            pX--;
                            mainActivity.updateMoveCount();
                            break;
                        }

                        if (level[pY * lW + pX] == 0) {
                            level[pY * lW + pX] = 0;
                            level[pY * lW + pX - 1] = 4;
                            pX--;
                            mainActivity.updateMoveCount();
                            break;
                        }

                        if (level[pY * lW + pX - 1] == 0 || level[pY * lW + pX - 1] == 3) {
                            level[pY * lW + pX] = 0;
                            level[pY * lW + pX - 1] = 4;
                            pX--;
                            mainActivity.updateMoveCount();
                            break;
                        }
                    }

                } else {
                    if (deltaY > 0) { // Down swipe
                        if (level[(pY + 1) * lW + pX] == 1) break;

                        if (level[(pY + 1) * lW + pX] == 2 || level[(pY + 1) * lW + pX] == 5) {
                            if (level[(pY + 2) * lW + pX] == 1 || level[(pY + 2) * lW + pX] == 2 || level[(pY + 2) * lW + pX] == 5) break;

                            if (levelCopy[pY * lW + pX] == 5) {
                                level[pY * lW + pX] = 3;
                            } else if (level[pY * lW + pX] == 0 || level[pY * lW + pX] == 4) {
                                level[pY * lW + pX] = 0;
                            }
                            if (levelCopy[pY * lW + pX] == 3) level[pY * lW + pX] = 3;

                            level[(pY + 1) * lW + pX] = 4;

                            if (levelCopy[(pY + 2) * lW + pX] == 3 || levelCopy[(pY + 2) * lW + pX] == 5) level[(pY + 2) * lW + pX] = 5;
                            else level[(pY + 2) * lW + pX] = 2;

                            pY++;
                            mainActivity.updateMoveCount();
                            break;
                        }

                        if (levelCopy[pY * lW + pX] == 3 || levelCopy[pY * lW + pX] == 5) {
                            level[pY * lW + pX] = 3;
                            level[(pY + 1) * lW + pX] = 4;
                            pY++;
                            mainActivity.updateMoveCount();
                            break;
                        }

                        if (level[pY * lW + pX] == 0) {
                            level[pY * lW + pX] = 0;
                            level[(pY + 1) * lW + pX] = 4;
                            pY++;
                            mainActivity.updateMoveCount();
                            break;
                        }

                        if (level[(pY + 1) * lW + pX] == 0 || level[(pY + 1) * lW + pX] == 3) {
                            level[pY * lW + pX] = 0;
                            level[(pY + 1) * lW + pX] = 4;
                            pY++;
                            mainActivity.updateMoveCount();
                            break;
                        }

                    } else { // Up swipe
                        if (level[(pY - 1) * lW + pX] == 1) break;

                        if (level[(pY - 1) * lW + pX] == 2 || level[(pY - 1) * lW + pX] == 5) {
                            if (level[(pY - 2) * lW + pX] == 1 || level[(pY - 2) * lW + pX] == 2 || level[(pY - 2) * lW + pX] == 5) break;

                            if (levelCopy[pY * lW + pX] == 5) {
                                level[pY * lW + pX] = 3;
                            } else if (level[pY * lW + pX] == 0 || level[pY * lW + pX] == 4) {
                                level[pY * lW + pX] = 0;
                            }
                            if (levelCopy[pY * lW + pX] == 3) level[pY * lW + pX] = 3;

                            level[(pY - 1) * lW + pX] = 4;

                            if (levelCopy[(pY - 2) * lW + pX] == 3 || levelCopy[(pY - 2) * lW + pX] == 5) level[(pY - 2) * lW + pX] = 5;
                            else level[(pY - 2) * lW + pX] = 2;

                            pY--;
                            mainActivity.updateMoveCount();
                            break;
                        }

                        if (levelCopy[pY * lW + pX] == 3 || levelCopy[pY * lW + pX] == 5) {
                            level[pY * lW + pX] = 3;
                            level[(pY - 1) * lW + pX] = 4;
                            pY--;
                            mainActivity.updateMoveCount();
                            break;
                        }

                        if (level[pY * lW + pX] == 0) {
                            level[pY * lW + pX] = 0;
                            level[(pY - 1) * lW + pX] = 4;
                            pY--;
                            mainActivity.updateMoveCount();
                            break;
                        }

                        if (level[(pY - 1) * lW + pX] == 0 || level[(pY - 1) * lW + pX] == 3) {
                            level[pY * lW + pX] = 0;
                            level[(pY - 1) * lW + pX] = 4;
                            pY--;
                            mainActivity.updateMoveCount();
                            break;
                        }
                    }
                }
        }

        invalidate();
        return super.onTouchEvent(event);
    }

    public Bitmap generateLevelPreview(int levelNumber, int previewWidth, int previewHeight, boolean savedLevel) {

        if (!savedLevel) {
            loadLevel(levelNumber);
        }else{
            SharedPreferences sharedPref = getContext().getSharedPreferences("level_progress_prefs", Context.MODE_PRIVATE);

            String savedLevelString = sharedPref.getString("savedLevel", null);

            if (savedLevelString != null) {
                String[] levelStringArray = savedLevelString.split(",");

                level = new int[levelStringArray.length];

                for (int i = 0; i < levelStringArray.length; i++) {
                    level[i] = Integer.parseInt(levelStringArray[i]);
                }
            } else {
                Log.e("SokoView", "Failed to load saved level data.");

            }

            lW = sharedPref.getInt("savedLW", 0);

            lH = sharedPref.getInt("savedLH", 0);
        }

        Bitmap previewBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        Canvas previewCanvas = new Canvas(previewBitmap);

        int cellSize = Math.min(previewWidth / lW, previewHeight / lH);

        for (int y = 0; y < lH; y++) {
            for (int x = 0; x < lW; x++) {
                Rect dstRect = new Rect(
                        x * cellSize,
                        y * cellSize,
                        (x + 1) * cellSize,
                        (y + 1) * cellSize
                );
                previewCanvas.drawBitmap(bmp[level[y * lW + x]], null, dstRect, null);
            }
        }

        return previewBitmap;
    }


    private Button nextLevelButton;

    public void setNextLevelButton(Button button) {
        this.nextLevelButton = button;
    }




}
