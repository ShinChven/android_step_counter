package net.atlassc.shinchven.stepcounter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ShinChven on 16/4/9.
 */
public class StepCounterDB extends SQLiteOpenHelper {


    /**
     * @param context      android application context
     * @param userIdentity user identity
     * @param date         date
     * @param stepCounting steps to add up to
     * @return current steps
     */
    public static int saveCounting(Context context, String userIdentity, Date date, int stepCounting) {
        date = iNeedDateOnly(date);

        ContentValues values = new ContentValues();
        StepCounterDB dbHelper = new StepCounterDB(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            if (db != null) {

                String selections = COL_USER_IDENTITY + " = ? AND " + COL_STEP_DATE + " = ?";
                String[] args = new String[]{userIdentity, String.valueOf(date.getTime())};

                Cursor cursor = db.query(TABLE_STEP_COUNT, new String[]{COL_STEPS}, selections, args, null, null,
                        null);
                if (cursor.moveToNext()) {
                    int steps = cursor.getInt(cursor.getColumnIndex(COL_STEPS));
                    stepCounting = steps+stepCounting;
                    values.put(COL_STEPS, stepCounting);
                    int update = db.update(TABLE_STEP_COUNT, values, selections, args);
                    if (update == 0) {
                        return 0;
                    }
                } else {
                    values.put(COL_STEPS, stepCounting);
                    values.put(COL_STEP_DATE,date.getTime());
                    values.put(COL_USER_IDENTITY,userIdentity);
                    long insert = db.insert(TABLE_STEP_COUNT, COL_ID, values);
                    if (insert == 0) {
                        return 0;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return stepCounting;
    }

    private static Date iNeedDateOnly(Date date) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        String format = fmt.format(date);
        try {
            date = fmt.parse(format);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * @param context      android application context
     * @param date         query date
     * @param userIdentity user identity
     * @return steps
     */
    public static int getStepsByDate(Context context, Date date, String userIdentity) {
        int steps = 0;

        date = iNeedDateOnly(date);
        StepCounterDB dbHelper = new StepCounterDB(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            if (db != null) {

                String selections = COL_USER_IDENTITY + " = ? AND " + COL_STEP_DATE + " = ?";
                String[] args = new String[]{userIdentity, String.valueOf(date.getTime())};

                Cursor cursor = db.query(TABLE_STEP_COUNT, new String[]{COL_STEPS}, selections, args, null, null,
                        null);
                if (cursor.moveToNext()) {
                    steps = cursor.getInt(cursor.getColumnIndex(COL_STEPS));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return steps;
    }


    //region DB_BASE
    private static final String DB_FILE_NAME = "step_counter.db";
    private static final int DB_VERSION = 1;
    public static final String TABLE_STEP_COUNT = "STEP_COUNT";
    public static final String COL_ID = "_ID";
    public static final String COL_USER_IDENTITY = "USER_IDENTITY";
    public static final String COL_STEP_DATE = "STEP_DATE";
    public static final String COL_STEPS = "STEPS";

    public StepCounterDB(Context context) {
        super(context, DB_FILE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    private void createTable(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE " + TABLE_STEP_COUNT + " (\n" +
                    "  " + COL_ID + "           INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "  " + COL_USER_IDENTITY + " TEXT,\n" +
                    "  " + COL_STEP_DATE + "     LONG,\n" +
                    "  " + COL_STEPS + "         INTEGER\n" +
                    ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE " + TABLE_STEP_COUNT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        createTable(db);
    }
    //endregion
}
