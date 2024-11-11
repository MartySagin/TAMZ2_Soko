package com.tamz.soko2024;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SokoView extends View {

    Bitmap[] bmp;

    int lW; // Dynamická šířka levelu
    int lH; // Dynamická výška levelu

    int pX; // Pozice hráče X
    int pY; // Pozice hráče Y

    int width; // Šířka jedné buňky
    int height; // Výška jedné buňky

    private int[] level; // Dynamické pole pro aktuální level
    private int[] levelCopy; // Kopie levelu pro restart

    private MainActivity mainActivity;
    boolean finishedLevel = false;


    public interface OnLevelCompleteListener {
        void onLevelComplete();
    }

    private OnLevelCompleteListener levelCompleteListener;

    public void setOnLevelCompleteListener(OnLevelCompleteListener listener) {
        this.levelCompleteListener = listener;
    }

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
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Přepočítání velikostí buněk po změně velikosti zobrazení
        recalculateCellSize();
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

            // First pass: Locate the level and determine the maximum width (lW) and height (lH)
            while ((line = reader.readLine()) != null) {
                // Check if this line matches "Level X"
                if (line.equals("Level " + levelNumber)) {
                    levelFound = true;
                    layoutLines.clear(); // Clear any previous data
                    continue; // Move to the next line for level name
                }

                if (levelFound) {
                    // Skip the level name line after finding the level
                    if (line.startsWith("'")) {
                        continue;
                    }

                    // Stop reading if we reach an empty line indicating the end of the level
                    if (line.isEmpty()) {
                        break;
                    }

                    layoutLines.add(line); // Store each line of the layout temporarily
                    lW = Math.max(lW, line.length()); // Update max width based on longest row
                }
            }
            reader.close();

            if (!levelFound) {
                Log.e("SokoView", "Level " + levelNumber + " not found in the file.");
                Toast.makeText(getContext(), "Level " + levelNumber + " not found.", Toast.LENGTH_SHORT).show();
                return null;
            }

            lH = layoutLines.size(); // Total lines determine the height

            // Second pass: Build the level data array based on determined dimensions (lW x lH)
            List<Integer> levelData = new ArrayList<>();
            for (String layoutLine : layoutLines) {
                // Add the character codes for each cell in the row
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

                // Pad the row with floor tiles (0) up to the maximum width (lW)
                for (int i = layoutLine.length(); i < lW; i++) {
                    levelData.add(0);
                }
            }

            Log.d("SokoView", "Level " + levelNumber + " loaded: width " + lW + ", height " + lH);

            // Convert List to array for returning
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

        // Aktualizace šířky a výšky buněk podle nových rozměrů levelu
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
        invalidate();
    }

    private void recalculateCellSize() {
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        if (lW > 0 && lH > 0 && viewWidth > 0 && viewHeight > 0) {
            // Determine the scaling factor based on the shorter dimension to maintain aspect ratio
            float cellWidth = (float) viewWidth / lW;
            float cellHeight = (float) viewHeight / lH;

            // Use the minimum cell size to prevent stretching in one direction
            float cellSize = Math.min(cellWidth, cellHeight);

            // Set cell width and height to the uniform cell size
            width = (int) cellSize;
            height = (int) cellSize;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (width <= 0 || height <= 0) return; // Skip drawing if cell size is not set properly

        // Calculate offsets to center the level if there is extra space
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

        // Check if the level is complete
        if (CheckIfLevelIsComplete() && !finishedLevel) {
            Toast.makeText(getContext(), "FINISHED!!!!", Toast.LENGTH_SHORT).show();
            finishedLevel = true;

            if (levelCompleteListener != null) {
                levelCompleteListener.onLevelComplete();
            }
        }
    }

    boolean CheckIfLevelIsComplete() {
        for (int cell : level) {
            if (cell == 2) {
                return false;
            }
        }
        return true;
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

                            // Set current position back to 3 if it was 5 in levelCopy
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
                            break;
                        }

                        if (levelCopy[pY * lW + pX] == 3 || levelCopy[pY * lW + pX] == 5) {
                            level[pY * lW + pX] = 3;
                            level[pY * lW + pX + 1] = 4;
                            pX++;
                            break;
                        }

                        if (level[pY * lW + pX] == 0) {
                            level[pY * lW + pX] = 0;
                            level[pY * lW + pX + 1] = 4;
                            pX++;
                            break;
                        }

                        if (level[pY * lW + pX + 1] == 0 || level[pY * lW + pX + 1] == 3) {
                            level[pY * lW + pX] = 0;
                            level[pY * lW + pX + 1] = 4;
                            pX++;
                            break;
                        }

                    } else { // Left swipe
                        if (level[pY * lW + pX - 1] == 1) break;

                        if (level[pY * lW + pX - 1] == 2 || level[pY * lW + pX - 1] == 5) {
                            if (level[pY * lW + pX - 2] == 1 || level[pY * lW + pX - 2] == 2 || level[pY * lW + pX - 2] == 5) break;

                            // Set current position back to 3 if it was 5 in levelCopy
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
                            break;
                        }

                        if (levelCopy[pY * lW + pX] == 3 || levelCopy[pY * lW + pX] == 5) {
                            level[pY * lW + pX] = 3;
                            level[pY * lW + pX - 1] = 4;
                            pX--;
                            break;
                        }

                        if (level[pY * lW + pX] == 0) {
                            level[pY * lW + pX] = 0;
                            level[pY * lW + pX - 1] = 4;
                            pX--;
                            break;
                        }

                        if (level[pY * lW + pX - 1] == 0 || level[pY * lW + pX - 1] == 3) {
                            level[pY * lW + pX] = 0;
                            level[pY * lW + pX - 1] = 4;
                            pX--;
                            break;
                        }
                    }

                } else {
                    if (deltaY > 0) { // Down swipe
                        if (level[(pY + 1) * lW + pX] == 1) break;

                        if (level[(pY + 1) * lW + pX] == 2 || level[(pY + 1) * lW + pX] == 5) {
                            if (level[(pY + 2) * lW + pX] == 1 || level[(pY + 2) * lW + pX] == 2 || level[(pY + 2) * lW + pX] == 5) break;

                            // Set current position back to 3 if it was 5 in levelCopy
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
                            break;
                        }

                        if (levelCopy[pY * lW + pX] == 3 || levelCopy[pY * lW + pX] == 5) {
                            level[pY * lW + pX] = 3;
                            level[(pY + 1) * lW + pX] = 4;
                            pY++;
                            break;
                        }

                        if (level[pY * lW + pX] == 0) {
                            level[pY * lW + pX] = 0;
                            level[(pY + 1) * lW + pX] = 4;
                            pY++;
                            break;
                        }

                        if (level[(pY + 1) * lW + pX] == 0 || level[(pY + 1) * lW + pX] == 3) {
                            level[pY * lW + pX] = 0;
                            level[(pY + 1) * lW + pX] = 4;
                            pY++;
                            break;
                        }

                    } else { // Up swipe
                        if (level[(pY - 1) * lW + pX] == 1) break;

                        if (level[(pY - 1) * lW + pX] == 2 || level[(pY - 1) * lW + pX] == 5) {
                            if (level[(pY - 2) * lW + pX] == 1 || level[(pY - 2) * lW + pX] == 2 || level[(pY - 2) * lW + pX] == 5) break;

                            // Set current position back to 3 if it was 5 in levelCopy
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
                            break;
                        }

                        if (levelCopy[pY * lW + pX] == 3 || levelCopy[pY * lW + pX] == 5) {
                            level[pY * lW + pX] = 3;
                            level[(pY - 1) * lW + pX] = 4;
                            pY--;
                            break;
                        }

                        if (level[pY * lW + pX] == 0) {
                            level[pY * lW + pX] = 0;
                            level[(pY - 1) * lW + pX] = 4;
                            pY--;
                            break;
                        }

                        if (level[(pY - 1) * lW + pX] == 0 || level[(pY - 1) * lW + pX] == 3) {
                            level[pY * lW + pX] = 0;
                            level[(pY - 1) * lW + pX] = 4;
                            pY--;
                            break;
                        }
                    }
                }
        }

        invalidate();
        return super.onTouchEvent(event);
    }

    public Bitmap generateLevelPreview(int levelNumber, int previewWidth, int previewHeight) {
        // Načíst level
        loadLevel(levelNumber);

        // Vytvořit bitmapu pro náhled
        Bitmap previewBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        Canvas previewCanvas = new Canvas(previewBitmap);

        // Velikost jedné buňky pro náhled (upravte dle potřeby)
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



}
