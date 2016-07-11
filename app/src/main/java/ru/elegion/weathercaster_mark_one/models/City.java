package ru.elegion.weathercaster_mark_one.models;

import android.graphics.Bitmap;

/**
 * Created by Freeman on 07.07.2016.
 */
public class City {
    private String mName;
    private String mId;
    private String mTemp;
    private String mIcon;
    private String mCountry;
    private Bitmap mIconBitmap;

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

    public String getCountry() { return mCountry; }
    public void setCountry(String country) { mCountry = country; }

    public String getIcon() { return mIcon; }
    public void setIcon(String icon) { mIcon = icon; }

    public Bitmap getIconBitmap() { return mIconBitmap; }
    public void setIconBitmap(Bitmap iconBitmap) { mIconBitmap = iconBitmap; }
}
