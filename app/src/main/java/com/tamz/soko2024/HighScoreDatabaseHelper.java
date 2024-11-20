// HighScoreDatabaseHelper.java
package com.tamz.soko2024;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

public class HighScoreDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "soko2024.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_HIGHSCORES = "highscores";
    public static final String COLUMN_LEVEL = "level";
    public static final String COLUMN_MOVES = "moves";

    private static HighScoreDatabaseHelper instance;

    private HighScoreDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized HighScoreDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new HighScoreDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_HIGHSCORES + " (" +
                COLUMN_LEVEL + " INTEGER PRIMARY KEY, " +
                COLUMN_MOVES + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HIGHSCORES);
        onCreate(db);
    }

    public void saveHighScore(int level, int moves) {
        SQLiteDatabase db = this.getWritableDatabase();
        int currentBestMoves = getHighScore(level);

        if (currentBestMoves == -1 || moves < currentBestMoves) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_LEVEL, level);
            values.put(COLUMN_MOVES, moves);

            db.replace(TABLE_HIGHSCORES, null, values);
        }
    }

    public int getHighScore(int level) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HIGHSCORES,
                new String[]{COLUMN_MOVES},
                COLUMN_LEVEL + "=?",
                new String[]{String.valueOf(level)},
                null, null, null);

        if (cursor.moveToFirst()) {
            int moves = cursor.getInt(0);
            cursor.close();
            return moves;
        } else {
            cursor.close();
            return -1;
        }
    }
}
