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
        List<Integer> levelData = new ArrayList<>();
        boolean levelFound = false;

        lW = 0;
        lH = 0;

        try {
            InputStream inputStream = getContext().getAssets().open("levels/levels.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Pokud najdeme číslo levelu, zkontrolujeme, jestli odpovídá požadovanému levelu
                if (line.equals(String.valueOf(levelNumber))) {
                    levelFound = true;
                    continue; // Pokračujeme na další řádek, kde začíná mřížka levelu
                }

                // Pokud jsme našli požadovaný level, načítáme jeho mřížku
                if (levelFound) {
                    if (line.isEmpty()) {
                        break; // Prázdný řádek označuje konec aktuálního levelu
                    }

                    String[] values = line.split(" ");
                    if (lW == 0) {
                        lW = values.length; // Nastavení šířky levelu
                    }
                    for (String value : values) {
                        levelData.add(Integer.parseInt(value));
                    }
                    lH++; // Zvýšení počtu řádků
                }
            }
            reader.close();

            if (!levelFound) {
                Log.e("SokoView", "Level " + levelNumber + " nebyl nalezen v souboru.");
                Toast.makeText(getContext(), "Level " + levelNumber + " nebyl nalezen.", Toast.LENGTH_SHORT).show();
                return null;
            }

            Log.d("SokoView", "Level " + levelNumber + " načten: šířka " + lW + ", výška " + lH);

        } catch (IOException e) {
            Log.e("SokoView", "Chyba při načítání levelu z levels.txt", e);
            return null;
        }

        int[] loadedLevel = new int[levelData.size()];
        for (int i = 0; i < levelData.size(); i++) {
            loadedLevel[i] = levelData.get(i);
        }

        return loadedLevel;
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


    private void recalculateCellSize() {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        if (lW > 0 && lH > 0 && viewWidth > 0 && viewHeight > 0) {
            width = viewWidth / lW;
            height = viewHeight / lH;
        }
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

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        for (int y = 0; y < lH; y++) {
            for (int x = 0; x < lW; x++) {
                canvas.drawBitmap(bmp[level[y * lW + x]], null,
                        new Rect(x * width, y * height, (x + 1) * width, (y + 1) * height), null);
            }
        }

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




}
