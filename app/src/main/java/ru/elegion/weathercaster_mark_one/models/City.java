package ru.elegion.weathercaster_mark_one.models;

/**
 * Created by Freeman on 07.07.2016.
 */
public class City {
    private String mUID;
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

    public String getUID() { return mUID; }
    public void setUID(String UID) { mUID = UID; }

    public Weather getWeatherInfo() {
        if (mWeatherInfo == null) {
            mWeatherInfo = new Weather();
        }
        return mWeatherInfo;
    }

    public class Weather {
        private String mDescription;
        private String mIcon;
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
    }
}
