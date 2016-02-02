package com.lecz.android.tiltmazes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class TiltMazesDBAdapter {
    private static final String DATABASE_NAME = "tiltmazes.db";
    private static final String DATABASE_TABLE = "mazes";
    private static final int DATABASE_VERSION = 5;

    public static final String KEY_ID = "_id";
    public static final int ID_COLUMN = 0;

    public static final String KEY_NAME = "name";
    public static final int NAME_COLUMN = 1;

    public static final String KEY_SOLUTION_STEPS = "solution_steps";
    public static final int SOLUTION_STEPS_COLUMN = 2;

    public static final String[] COLUMNS = {
            KEY_ID,
            KEY_NAME,
            KEY_SOLUTION_STEPS,
    };

    private SQLiteDatabase mDB;
    private TiltMazesDBOpenHelper mDBHelper;

    public TiltMazesDBAdapter(Context context) {
        mDBHelper = new TiltMazesDBOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public TiltMazesDBAdapter open() throws SQLException {
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDB.close();
    }

    // Update "solution steps" column (only if it's 0 or the new value is less then the current)
    public void updateMaze(int id, int solution_steps) {
        ContentValues values = new ContentValues();
        values.put(KEY_SOLUTION_STEPS, solution_steps);

        mDB.update(
                DATABASE_TABLE,
                values,
                KEY_ID + " = ? AND ("
                        + KEY_SOLUTION_STEPS + " = ? OR "
                        + KEY_SOLUTION_STEPS + " > ?)",
                new String[]{"" + id, "0", "" + solution_steps}
        );
    }

    public Cursor allMazes() {
        return mDB.query(
                DATABASE_TABLE,
                COLUMNS,
                /*selection:*/ null,
                /*selectionArgs:*/ null,
                /*groupBy:*/ null,
                /*having:*/ null,
                /*orderBy:*/ KEY_ID
        );
    }

    public Cursor unsolvedMazes() {
        return mDB.query(
                DATABASE_TABLE,
                /*:columns:*/ new String[]{KEY_ID},
                /*selection:*/ KEY_SOLUTION_STEPS + " = ?",
                /*selectionArgs:*/ new String[]{"0"},
                /*groupBy:*/ null,
                /*having:*/ null,
                /*orderBy:*/ KEY_ID
        );
    }

    public Cursor solvedMazes() {
        return mDB.query(
                DATABASE_TABLE,
                /*:columns:*/ new String[]{KEY_ID},
                /*selection:*/ KEY_SOLUTION_STEPS + " <> ?",
                /*selectionArgs:*/ new String[]{"0"},
                /*groupBy:*/ null,
                /*having:*/ null,
                /*orderBy:*/ KEY_ID
        );
    }

    public int getFirstUnsolved() {
        Cursor c = unsolvedMazes();

        if (!c.moveToFirst()) {
            // There are no more unsolved mazes
            return 0;
        }

        return c.getInt(ID_COLUMN);
    }

    private static class TiltMazesDBOpenHelper extends SQLiteOpenHelper {
        private static final String DATABASE_CREATE =
                "create table " + DATABASE_TABLE + " ("
                        + KEY_ID + " integer primary key autoincrement, "
                        + KEY_NAME + " text not null, "
                        + KEY_SOLUTION_STEPS + " integer"
                        + ");";


        public TiltMazesDBOpenHelper(Context context, String name,
                                     CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);

            db.beginTransaction();
            try {
                ContentValues values = new ContentValues();
                values.put(KEY_SOLUTION_STEPS, 0);

                int id = 0;
                for (MapDesign map : MapDesigns.designList) {
                    values.put(KEY_ID, id);
                    values.put(KEY_NAME, map.getName());
                    db.insert(DATABASE_TABLE, null, values);
                    id = id + 1;
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // FIXME(leczbalazs) implement data migration instead of the DROP+CREATE
            db.execSQL("drop table if exists " + DATABASE_TABLE);
            onCreate(db);
        }

    }
}
