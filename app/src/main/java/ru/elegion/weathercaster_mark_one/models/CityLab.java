package ru.elegion.weathercaster_mark_one.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Freeman on 07.07.2016.
 */
public class CityLab {
    private static final String CITY_ID_TAG = "ru.elegion.weathercaster_mark_one.city_id";
    private static final String LOG_TAG = "CityLab";
    private final Context mAppContext;
    private DBHelper mDBHelper;
    private static CityLab sCityLab;
    private static final String sFilename = "citylist.json";

    private CityLab(Context appContext) {
        mAppContext = appContext;
    }

    public static CityLab build(Context c) {
        if (sCityLab == null) {
            sCityLab = new CityLab(c.getApplicationContext());
        }
        return sCityLab;
    }

    public static String getCityIdTag() {
        return CITY_ID_TAG;
    }

    public void updateCitiesInDB(ArrayList<City> cities) {
        mDBHelper = DBHelper.build(mAppContext);
        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        for (City city : cities) {
            ContentValues cv = getContentValues(city);
            db.update(mDBHelper.CITIES_TABLE_NAME, cv, "id = ?", new String[]{city.getId()});
        }
    }

    public void addCityToDB(City city){
        mDBHelper = DBHelper.build(mAppContext);
        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        ContentValues cv = getContentValues(city);

        db.insert(mDBHelper.CITIES_TABLE_NAME, null, cv);
    }

    @NonNull
    private ContentValues getContentValues(City city) {
        ContentValues cv = new ContentValues();
        cv.put(mDBHelper.ID_COLUMN_NAME, city.getId());
        cv.put(mDBHelper.NAME_COLUMN_NAME, city.getName());
        cv.put(mDBHelper.COUNTRY_COLUMN_NAME, city.getCountry());
        cv.put(mDBHelper.ICON_COLUMN_NAME, new StringBuilder("w").append(city.getWeatherInfo().getIcon()).toString());
        cv.put(mDBHelper.TEMP_COLUMN_NAME, city.getWeatherInfo().getTemperature());
        cv.put(mDBHelper.DESCRIPTION_COLUMN_NAME, city.getWeatherInfo().getDescription());
        cv.put(mDBHelper.HUMIDITY_COLUMN_NAME, city.getWeatherInfo().getHumidity());
        cv.put(mDBHelper.PRESSURE_COLUMN_NAME, city.getWeatherInfo().getPressure());
        cv.put(mDBHelper.WINDSPEED_COLUMN_NAME, city.getWeatherInfo().getWindSpeed());
        return cv;
    }

    public void remove(int position) {
        mDBHelper = DBHelper.build(mAppContext);
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String removedCityID = getCities().get(position).getId();

        db.delete(mDBHelper.CITIES_TABLE_NAME, "id = ?", new String[]{removedCityID});
    }

    public ArrayList<City> getCities() {
        mDBHelper = DBHelper.build(mAppContext);
        ArrayList<City> cities = new ArrayList<>();
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        Cursor c = db.query(mDBHelper.CITIES_TABLE_NAME, null, null, null, null, null, null);

        if (c.moveToFirst()) {
            int uidColIndex = c.getColumnIndex(mDBHelper.UID_COLUMN_NAME);
            int idColIndex = c.getColumnIndex(mDBHelper.ID_COLUMN_NAME);
            int nameColIndex = c.getColumnIndex(mDBHelper.NAME_COLUMN_NAME);
            int countryColIndex = c.getColumnIndex(mDBHelper.COUNTRY_COLUMN_NAME);
            int iconColIndex = c.getColumnIndex(mDBHelper.ICON_COLUMN_NAME);
            int tempColIndex = c.getColumnIndex(mDBHelper.TEMP_COLUMN_NAME);
            int descriptionColIndex = c.getColumnIndex(mDBHelper.DESCRIPTION_COLUMN_NAME);
            int humidityColIndex = c.getColumnIndex(mDBHelper.HUMIDITY_COLUMN_NAME);
            int pressureColIndex = c.getColumnIndex(mDBHelper.PRESSURE_COLUMN_NAME);
            int windspeedColIndex = c.getColumnIndex(mDBHelper.WINDSPEED_COLUMN_NAME);

            do {
                City city = new City();
                city.setUID(c.getString(uidColIndex));
                city.setId(c.getString(idColIndex));
                city.setName(c.getString(nameColIndex));
                city.setCountry(c.getString(countryColIndex));
                city.getWeatherInfo().setIcon(c.getString(iconColIndex));
                city.getWeatherInfo().setTemperature(c.getString(tempColIndex));
                city.getWeatherInfo().setDescription(c.getString(descriptionColIndex));
                city.getWeatherInfo().setHumidity(c.getString(humidityColIndex));
                city.getWeatherInfo().setPressure(c.getString(pressureColIndex));
                city.getWeatherInfo().setWindSpeed(c.getString(windspeedColIndex));

                cities.add(city);
            } while (c.moveToNext());
        } else {
            Log.d(LOG_TAG, "0 rows");
        }
        c.close();
        return cities;
    }

    public City getCity(int position) {
        if (position >= 0 && position < getCities().size()){
            return getCities().get(position);
        }
        return null;
    }

    public City getCity(String id) {
        for (City city: getCities()){
            if (id.equalsIgnoreCase(city.getId())){
                return city;
            }
        }
        return null;
    }

    @Nullable
    public ArrayList<String> getAllCitiesNames(){

        String line = "";
        String cvsSplitBy = ",";
        ArrayList<String> allCitiesNames = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(mAppContext.getAssets().open(sFilename)));
            try {
                while ((line = br.readLine()) != null) {

                    String[] cityLine = line.split(cvsSplitBy);
                    String cityName = cityLine[1].split(":")[1].replace("\"", "");

                    allCitiesNames.add(cityName);
                }
            } finally {
                br.close();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        return allCitiesNames;
    }

    public int indexOf(City someCity){
        int size = getCities().size();
        if (someCity != null){
            for (int i = 0; i < size; i ++){
                if (someCity.getUID().equalsIgnoreCase(getCity(i).getUID()))
                    return i;
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (getCity(i) == null) {
                    return i;
                }
            }
        }
        return -1;
    }
}
