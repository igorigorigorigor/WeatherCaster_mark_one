package ru.elegion.weathercaster_mark_one.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

import ru.elegion.weathercaster_mark_one.R;

/**
 * Created by Freeman on 07.07.2016.
 */
public class City {
    private String mName;
    private String mId;
    private String mCountry;
    private Weather mWeatherInfo;

    public String getName() {
        return mName;
    }
    public void setName(String name) { mName = name; }

    public String getId() {
        return mId;
    }
    public void setId(String id) {
        mId = id;
    }

    public String getCountry() { return mCountry; }
    public void setCountry(String country) { mCountry = country; }

    public Weather getWeatherInfo() {
        if (mWeatherInfo == null) {
            mWeatherInfo = new Weather();
        }
        return mWeatherInfo;
    }

    public class Weather {
        private String mDescription;
        private String mIcon;
        private Bitmap mIconBitmap;
        private String mTemperature;
        private String mHumidity;
        private String mPressure;
        private String mWindSpeed;

        public String getWindSpeed() {
            return mWindSpeed;
        }
        public void setWindSpeed(String windSpeed) { this.mWindSpeed = windSpeed; }

        public String getDescription() {
            return mDescription;
        }
        public void setDescription(String description) {
            this.mDescription = description;
        }

        public String getTemperature() {
            return mTemperature;
        }
        public void setTemperature(String temperature) {  this.mTemperature = temperature; }

        public String getHumidity() {
            return mHumidity;
        }
        public void setHumidity(String humidity) {
            this.mHumidity = humidity;
        }

        public String getPressure() { return mPressure; }
        public void setPressure(String pressure) {
            this.mPressure = pressure;
        }

        public String getIcon() { return mIcon; }
        public void setIcon(String icon) { mIcon = icon; }

        public Bitmap getIconBitmap() { return mIconBitmap; }
        public void setIconBitmap(Bitmap iconBitmap) { mIconBitmap = iconBitmap; }

        public byte[] getByteArrayFromIconBitmap(){
            Bitmap bmp = this.mIconBitmap;
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return  stream.toByteArray();
        }

        public void setIconBitmapFromByteArray(byte [] image){
            if (image != null && image.length > 0){
                this.mIconBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            }
        }
    }
}
