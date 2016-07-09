package ru.elegion.weathercaster_mark_one.models;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by Freeman on 07.07.2016.
 */
public class CityLab {
    private final Context mAppContext;
    private ArrayList<City> mCities;
    private static CityLab sCityLab;
    private String[] mInitialCityIDs = {"5202009", "498817", "1496747", "554234", "491422", "839788", "1486209", "472757", "551487", "2013348"};

    //{"_id":551487,"name":"Kazan","country":"RU","coord":{"lon":49.122139,"lat":55.788738}}

    public ArrayList<City> getCities() {
        return mCities;
    }

    private CityLab(Context appContext){
        mAppContext = appContext;
        mCities = new ArrayList<City>();

        for (int i = 0; i < mInitialCityIDs.length; i++){
            City c = new City();
            c.setId(mInitialCityIDs[i]);
            mCities.add(c);
        }
    }

    public static CityLab build(Context c){
        if (sCityLab == null){
            sCityLab  = new CityLab(c.getApplicationContext());
        }
        return sCityLab;
    }

    public City getCity(String cityId) {
        for (City city : mCities){
            if (city.getId().equalsIgnoreCase(cityId)){
                return city;
            }
        }
        return null;
    }
}
