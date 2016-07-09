package ru.elegion.weathercaster_mark_one.models;

/**
 * Created by Freeman on 07.07.2016.
 */
public class City {
    private String mName;
    private String mId;
    private String mTemp;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getTemp() {
        return mTemp;
    }

    public void setTemp(String currentTemp) {
           mTemp = currentTemp;
        }

}
