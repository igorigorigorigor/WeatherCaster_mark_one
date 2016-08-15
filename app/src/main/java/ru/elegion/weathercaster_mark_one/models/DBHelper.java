package ru.elegion.weathercaster_mark_one.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Freeman on 14.08.2016.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = "DBHelper";
    public static final String CITIES_TABLE_NAME = "cities";
    public static final String[] INITIAL_CITIES_IDS = {"524901", "498817", "554234", "491422", "551487"};
    public static final String UID_COLUMN_NAME = "uid";
    public static final String ID_COLUMN_NAME = "id";
    public static final String COUNTRY_COLUMN_NAME = "country";
    public static final String NAME_COLUMN_NAME = "name";
    public static final String ICON_COLUMN_NAME = "icon";
    public static final String TEMP_COLUMN_NAME = "temp";
    public static final String DESCRIPTION_COLUMN_NAME = "description";
    public static final String HUMIDITY_COLUMN_NAME = "humidity";
    public static final String PRESSURE_COLUMN_NAME = "pressure";
    public static final String WINDSPEED_COLUMN_NAME = "windspeed";
    public static DBHelper sDBHelper;

    public static DBHelper build(Context c) {
        if (sDBHelper == null) {
            sDBHelper = new DBHelper(c.getApplicationContext());
        }
        return sDBHelper;
    }

    public DBHelper(Context context) {
        super(context, "myDB", null, 1);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "--- onCreate database ---");
        StringBuilder createCitiesTable = new StringBuilder();
        createCitiesTable.append("create table ")
                .append(CITIES_TABLE_NAME).append(" (")
                .append(UID_COLUMN_NAME).append(" integer primary key autoincrement, ")
                .append(ID_COLUMN_NAME).append(" integer, ")
                .append(NAME_COLUMN_NAME).append(" text, ")
                .append(COUNTRY_COLUMN_NAME).append(" text, ")
                .append(ICON_COLUMN_NAME).append(" text, ")
                .append(TEMP_COLUMN_NAME).append(" text, ")
                .append(DESCRIPTION_COLUMN_NAME).append(" text, ")
                .append(HUMIDITY_COLUMN_NAME).append(" text, ")
                .append(PRESSURE_COLUMN_NAME).append(" text, ")
                .append(WINDSPEED_COLUMN_NAME).append(" text);");

        db.execSQL(createCitiesTable.toString());

        for (int i = 0; i < INITIAL_CITIES_IDS.length; i++){
            ContentValues cv = new ContentValues();
            cv.put(ID_COLUMN_NAME, INITIAL_CITIES_IDS[i]);
            long rowID = db.insert(CITIES_TABLE_NAME, null, cv);
        }
    }
}

