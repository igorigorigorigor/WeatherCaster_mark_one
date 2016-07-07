package ru.elegion.weathercaster_mark_one.models;

import android.content.Context;

import java.util.ArrayList;

import ru.elegion.weathercaster_mark_one.api.Api;

/**
 * Created by Freeman on 07.07.2016.
 */
public class CityLab {
    private final Context mAppContext;
    private ArrayList<City> mCities;
    private static CityLab sCityLab;

    //{"_id":551487,"name":"Kazan","country":"RU","coord":{"lon":49.122139,"lat":55.788738}}

    public ArrayList<City> getCities() {
        return mCities;
    }

    private CityLab(Context appContext){
        mAppContext = appContext;
        mCities = new ArrayList<City>();

        //for (int i = 0; i < 100; i++){
            City c = new City();
            //c.setName("Kazan #" + i);
            c.setName("Kazan");
            c.setId("551487");

            Api.build(mAppContext).setCurrentTempforCity("551487");
            mCities.add(c);
        //}
    }

    public static CityLab build(Context c){
        if (sCityLab == null){
            sCityLab  = new CityLab(c.getApplicationContext());
        }
        return sCityLab;
    }

    public City get(String cityId) {
        for (City city : mCities){
            if (city.getId().equalsIgnoreCase(cityId)){
                return city;
            }
        }
        return null;
    }
}
