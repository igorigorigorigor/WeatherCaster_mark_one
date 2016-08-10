package ru.elegion.weathercaster_mark_one.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Freeman on 07.07.2016.
 */
public class CityLab {
    private static final String CITY_ID_TAG = "ru.elegion.weathercaster_mark_one.city_id";
    private static final String LOG_TAG = "CityLab";
    private static final String CITIES_TABLE_NAME = "cities";
    private static final String[] INITIAL_CITIES_IDS = {"524901", "498817", "554234", "491422", "551487"};
    private static final String ID_COLUMN_NAME = "id";
    private static final String COUNTRY_COLUMN_NAME = "country";
    private static final String NAME_COLUMN_NAME = "name";
    private static final String ICON_COLUMN_NAME = "icon";
    private static final String TEMP_COLUMN_NAME = "temp";
    private static final String DESCRIPTION_COLUMN_NAME = "description";
    private static final String HUMIDITY_COLUMN_NAME = "humidity";
    private static final String PRESSURE_COLUMN_NAME = "pressure";
    private static final String WINDSPEED_COLUMN_NAME = "windspeed";
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

        for (City city : mCities) {
            ContentValues cv = getContentValues(city);
            int updCount = db.update(CITIES_TABLE_NAME, cv, "id = ?", new String[]{city.getId()});
        }
        mDBHelper.close();
    }

    public void addCityToDB(City city){
        mCities.add(city);

        if (mDBHelper == null) {
            mDBHelper = new DBHelper(mAppContext);
        }
        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        ContentValues cv = getContentValues(city);

        long rowID = db.insert(CITIES_TABLE_NAME, null, cv);

        mDBHelper.close();
    }

    @NonNull
    private ContentValues getContentValues(City city) {
        ContentValues cv = new ContentValues();
        cv.put(ID_COLUMN_NAME, city.getId());
        cv.put(NAME_COLUMN_NAME, city.getName());
        cv.put(COUNTRY_COLUMN_NAME, city.getCountry());
        cv.put(ICON_COLUMN_NAME, city.getWeatherInfo().getIcon());
        cv.put(TEMP_COLUMN_NAME, city.getWeatherInfo().getTemperature());
        cv.put(DESCRIPTION_COLUMN_NAME, city.getWeatherInfo().getDescription());
        cv.put(HUMIDITY_COLUMN_NAME, city.getWeatherInfo().getHumidity());
        cv.put(PRESSURE_COLUMN_NAME, city.getWeatherInfo().getPressure());
        cv.put(WINDSPEED_COLUMN_NAME, city.getWeatherInfo().getWindSpeed());
        return cv;
    }

    public void remove(int position) {
        String removedCityID = mCities.get(position).getId();
        mCities.remove(position);

        if (mDBHelper == null) {
            mDBHelper = new DBHelper(mAppContext);
        }
        
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        long rowID = db.delete(CITIES_TABLE_NAME, "id = ?", new String[]{removedCityID});

        mDBHelper.close();
    }

    private CityLab(Context appContext) {
        mAppContext = appContext;
        mCities = new ArrayList<City>();

        mDBHelper = new DBHelper(mAppContext);
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        Cursor c = db.query(CITIES_TABLE_NAME, null, null, null, null, null, null);

        if (c.moveToFirst()) {
            int idColIndex = c.getColumnIndex(ID_COLUMN_NAME);
            int nameColIndex = c.getColumnIndex(NAME_COLUMN_NAME);
            int countryColIndex = c.getColumnIndex(COUNTRY_COLUMN_NAME);
            int iconColIndex = c.getColumnIndex(ICON_COLUMN_NAME);
            int tempColIndex = c.getColumnIndex(TEMP_COLUMN_NAME);
            int descriptionColIndex = c.getColumnIndex(DESCRIPTION_COLUMN_NAME);
            int humidityColIndex = c.getColumnIndex(HUMIDITY_COLUMN_NAME);
            int pressureColIndex = c.getColumnIndex(PRESSURE_COLUMN_NAME);
            int windspeedColIndex = c.getColumnIndex(WINDSPEED_COLUMN_NAME);

            do {
                Log.d(LOG_TAG,
                        "ID = " + c.getInt(idColIndex) + ", name = "
                                + c.getString(nameColIndex) + ", country = "
                                + c.getString(countryColIndex) + ", icon = "
                                + c.getString(iconColIndex) + ", temp = "
                                + c.getString(tempColIndex) + ", description = "
                                + c.getString(descriptionColIndex) + ", humidity = "
                                + c.getString(humidityColIndex) + ", pressure = "
                                + c.getString(pressureColIndex) + ", windspeed"
                                + c.getString(windspeedColIndex));
                City city = new City();
                city.setId(c.getString(idColIndex));
                city.setName(c.getString(nameColIndex));
                city.setCountry(c.getString(countryColIndex));
                city.getWeatherInfo().setIcon(c.getString(iconColIndex));
                city.getWeatherInfo().setTemperature(c.getString(tempColIndex));
                city.getWeatherInfo().setDescription(c.getString(descriptionColIndex));
                city.getWeatherInfo().setHumidity(c.getString(humidityColIndex));
                city.getWeatherInfo().setPressure(c.getString(pressureColIndex));
                city.getWeatherInfo().setWindSpeed(c.getString(windspeedColIndex));

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
                    .append(CITIES_TABLE_NAME).append(" (uid integer primary key autoincrement, ")
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

    @Nullable
    public ArrayList<String> getAllCitiesNames(){

        ArrayList<String> allCitiesNames = new ArrayList<>();
        try {
            JsonFactory jfactory = new JsonFactory();
            JsonParser jParser = jfactory.createParser(mAppContext.getAssets().open(sFilename));

            while (jParser.nextToken() != JsonToken.END_ARRAY) {
                if (jParser.getCurrentName() != null && jParser.getCurrentName().equalsIgnoreCase(NAME_COLUMN_NAME)){
                    jParser.nextToken();
                    allCitiesNames.add(jParser.getText());
                }
            }
            jParser.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return allCitiesNames;
    }
}
