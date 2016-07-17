package ru.elegion.weathercaster_mark_one.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Freeman on 07.07.2016.
 */
public class CityLab {
    private static final String CITY_ID_TAG = "ru.elegion.weathercaster_mark_one.city_id";
    private static final String LOG_TAG = "CityLab";
    private final Context mAppContext;
    private DBHelper mDBHelper;
    private ArrayList<City> mCities;
    private static CityLab sCityLab;
    private static final String sFilename = "citylist.json";

    public static String getCityIdTag() {
        return CITY_ID_TAG;
    }

    public ArrayList<City> getCities() {
        return mCities;
    }

    public void updateCities(ArrayList<City> cities) {
        for (int i = 0; i < cities.size(); i++)
        {
            cities.get(i);
        }
        mCities = cities;
    }
    public void updateCitiesInDB(ArrayList<City> cities) {
        mCities = cities;
        if (mDBHelper == null) {
            mDBHelper = new DBHelper(mAppContext);
        }
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        for (City city : mCities) {
            cv.put("id", city.getId());
            cv.put("name", city.getName());
            cv.put("country", city.getCountry());
            cv.put("icon", city.getIcon());
            cv.put("temp", city.getTemp());
            int updCount = db.update("cities", cv, "id = ?", new String[]{city.getId()});
        }
        mDBHelper.close();
    }

    public void addCityToDB(City city){
        mCities.add(city);

        if (mDBHelper == null) {
            mDBHelper = new DBHelper(mAppContext);
        }
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("id", city.getId());
        cv.put("name", city.getName());
        cv.put("country", city.getCountry());
        cv.put("icon", city.getIcon());
        cv.put("temp", city.getTemp());
        long rowID = db.insert("cities", null, cv);

        mDBHelper.close();
    }

    public void remove(int position) {
        String removedCityID = mCities.get(position).getId();
        mCities.remove(position);

        if (mDBHelper == null) {
            mDBHelper = new DBHelper(mAppContext);
        }
        
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        long rowID = db.delete("cities", "id = ?", new String[]{removedCityID});

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
            int countryColIndex = c.getColumnIndex("country");
            int iconColIndex = c.getColumnIndex("icon");
            int tempColIndex = c.getColumnIndex("temp");

            do {
                Log.d(LOG_TAG,
                        "ID = " + c.getInt(idColIndex) + ", name = "
                                + c.getString(nameColIndex) + ", country = "
                                + c.getString(countryColIndex) + ", icon = "
                                + c.getString(iconColIndex) + ", temp = "
                                + c.getString(tempColIndex));
                City city = new City();
                city.setId(c.getString(idColIndex));
                city.setName(c.getString(nameColIndex));
                city.setCountry(c.getString(countryColIndex));
                city.setIcon(c.getString(iconColIndex));
                city.setTemp(c.getString(tempColIndex));
                mCities.add(city);
            } while (c.moveToNext());
        } else
            Log.d(LOG_TAG, "0 rows");
        c.close();
        mDBHelper.close();
    }

    public static CityLab build(Context c) {
        if (sCityLab == null) {
            sCityLab = new CityLab(c.getApplicationContext());
        }
        return sCityLab;
    }

    public City getCity(int position) {
        if (position >= 0 && position < mCities.size()){
            return mCities.get(position);
        }
        return null;
    }

    public City getCity(String id) {
        for (City city: mCities){
            if (id.equalsIgnoreCase(city.getId())){
                return city;
            }
        }
        return null;
    }

    private class DBHelper extends SQLiteOpenHelper {
        private final String[] mInitialCityIDs = {"524901", "498817", "554234", "491422", "551487"};

        public DBHelper(Context context) {
            super(context, "myDB", null, 1);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(LOG_TAG, "--- onCreate database ---");
            db.execSQL("create table cities ("
                    + "uid integer primary key autoincrement,"
                    + "id integer,"
                    + "name text,"
                    + "country text,"
                    + "icon text,"
                    + "temp text" + ");");

            for (int i = 0; i < mInitialCityIDs.length; i++){
                ContentValues cv = new ContentValues();
                cv.put("id", mInitialCityIDs[i]);
                long rowID = db.insert("cities", null, cv);
            }
        }
    }

    @Nullable
    public ArrayList<String> getAllCitiesNames(){
        ArrayList<String> allCitiesNames = new ArrayList<>();
        try {
            ArrayList<String> allCities = getAllCities();
            for (String line : allCities){
                JSONObject city = new JSONObject(line);
                allCitiesNames.add(city.getString("name"));
            }
        } catch (JSONException | IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return allCitiesNames;
    }


    @Nullable
    private ArrayList<String> getAllCities() throws IOException, JSONException {
        BufferedReader reader = null;
        String line = null;
        ArrayList<String> cities = new ArrayList<>();
        try {
            InputStream in = mAppContext.getAssets().open(sFilename);
            reader = new BufferedReader(new InputStreamReader(in));
            while ((line = reader.readLine()) != null) {
                cities.add(line);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return cities;
    }
}
