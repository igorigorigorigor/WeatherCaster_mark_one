package ru.elegion.weathercaster_mark_one.models;

/**
 * Created by Freeman on 07.07.2016.
 */
public class City {
    private String _name;
    private String _id;
    private String _temp;

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

    public String getTemp() {
        return _temp;
    }

    public void setTemp(String currentTemp) {
           _temp = currentTemp;
        }

}
