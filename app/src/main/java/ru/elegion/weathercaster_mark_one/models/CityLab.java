package ru.elegion.weathercaster_mark_one.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Freeman on 07.07.2016.
 */
public class CityLab {
    private static final String LOG_TAG = "CityLab";
    private final Context mAppContext;
    private DBHelper mDBHelper;
    private ArrayList<City> mCities;
    private static CityLab sCityLab;

    //{"_id":551487,"name":"Kazan","country":"RU","coord":{"lon":49.122139,"lat":55.788738}}

    public ArrayList<City> getCities() {
        return mCities;
    }
    public void updateCities(ArrayList<City> cities) {
        mCities = cities;
        if (mDBHelper == null) {
            mDBHelper = new DBHelper(mAppContext);
        }
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        Log.d(LOG_TAG, "--- Update cities: ---");
        ContentValues cv = new ContentValues();
        for (City city : mCities) {
            cv.put("id", city.getId());
            cv.put("name", city.getName());
            cv.put("temp", city.getTemp());
            int updCount = db.update("cities", cv, "id = ?", new String[]{city.getId()});
            Log.d(LOG_TAG, "updated rows count = " + updCount);
        }
        mDBHelper.close();
    }

    public void addCity(City city){
        mCities.add(city);

        if (mDBHelper == null) {
            mDBHelper = new DBHelper(mAppContext);
        }
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();

        Log.d(LOG_TAG, "--- Insert in cities: ---");
        cv.put("id", city.getId());
        cv.put("name", city.getName());
        cv.put("temp", city.getTemp());
        long rowID = db.insert("cities", null, cv);
        Log.d(LOG_TAG, "row inserted, ID = " + rowID);

        mDBHelper.close();
    }

    public void delete(City city){
        mCities.remove(city);
        if (mDBHelper == null) {
            mDBHelper = new DBHelper(mAppContext);
        }
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();

        Log.d(LOG_TAG, "--- Insert in cities: ---");
        cv.put("id", city.getId());
        cv.put("name", city.getName());
        cv.put("temp", city.getTemp());
        long rowID = db.delete("cities", "id = ?", new String[]{city.getId()});
        Log.d(LOG_TAG, "row deleted, ID = " + rowID);

        mDBHelper.close();
    }

    private CityLab(Context appContext) {
        mAppContext = appContext;
        mCities = new ArrayList<City>();

        mDBHelper = new DBHelper(mAppContext);
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        Cursor c = db.query("cities", null, null, null, null, null, null);

        if (c.moveToFirst()) {
            int idColIndex = c.getColumnIndex("id");
            int nameColIndex = c.getColumnIndex("name");
            int tempColIndex = c.getColumnIndex("temp");

            do {
                Log.d(LOG_TAG,
                        "ID = " + c.getInt(idColIndex) + ", name = "
                                + c.getString(nameColIndex) + ", temp = "
                                + c.getString(tempColIndex));
                City city = new City();
                city.setId(c.getString(idColIndex));
                city.setName(c.getString(nameColIndex));
                city.setTemp(c.getString(tempColIndex));
                mCities.add(city);
            } while (c.moveToNext());
        } else
            Log.d(LOG_TAG, "0 rows");
        c.close();
    }

    public static CityLab build(Context c) {
        if (sCityLab == null) {
            sCityLab = new CityLab(c.getApplicationContext());
        }
        return sCityLab;
    }

    public City getCity(int position) {
        if (position > 0 && position < mCities.size()){
            return mCities.get(position - 1);
        }
        return null;
    }

    private class DBHelper extends SQLiteOpenHelper {
        private String[] mInitialCityIDs = {"5202009", "498817", "554234", "491422", "551487"};

        public DBHelper(Context context) {
            super(context, "myDB", null, 1);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(LOG_TAG, "--- onCreate database ---");
            db.execSQL("create table cities ("
                    + "id integer primary key,"
                    + "name text,"
                    + "temp text" + ");");

            for (int i = 0; i < mInitialCityIDs.length; i++){
                ContentValues cv = new ContentValues();
                Log.d(LOG_TAG, "--- Insert in cities: ---");
                cv.put("id", mInitialCityIDs[i]);
                long rowID = db.insert("cities", null, cv);
                Log.d(LOG_TAG, "row inserted, ID = " + rowID);
            }
        }
    }
}
