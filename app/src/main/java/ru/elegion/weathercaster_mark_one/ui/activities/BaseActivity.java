package ru.elegion.weathercaster_mark_one.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import ru.elegion.weathercaster_mark_one.R;
import ru.elegion.weathercaster_mark_one.models.City;
import ru.elegion.weathercaster_mark_one.models.CityLab;

/**
 * Created by Freeman on 12.07.2016.
 */
abstract public class BaseActivity extends AppCompatActivity {
    protected static String LOG_TAG = "MyLog";
    protected CityLab mCityLab;

    protected static void close(Closeable x) {
        try {
            if (x != null) {
                x.close();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException: Error occurred while closing stream");
        }
    }

    protected void setBooleanPreference(String setting, boolean value) {
        final SharedPreferences preferences = getSharedPreferences("setting", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(setting, value);
        editor.apply();
    }

    protected boolean getBooleanPreference(String setting) {
        final SharedPreferences preferences = getSharedPreferences("setting", Context.MODE_PRIVATE);
        return preferences.getBoolean(setting, true);
    }

    protected boolean isNetworkConnected() {
        ConnectivityManager cm =
                (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = (activeNetwork != null) && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    abstract protected class GenericNetworkTask extends AsyncTask<ArrayList<City>, Void, Void> {

        @Override
        protected Void doInBackground(ArrayList<City>... citiesArray) {
            String responseBody = "";
            if (citiesArray.length == 1 && citiesArray[0] != null) {
                ArrayList<City> cities = citiesArray[0];
                try {
                    // Get weather data
                    URL url = weatherGroupRequestURL(cities);
                    if (url == null) {
                        return null;
                    }
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    if (urlConnection.getResponseCode() == 200) {
                        InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
                        BufferedReader r = new BufferedReader(inputStreamReader);

                        String line = null;
                        while ((line = r.readLine()) != null) {
                            responseBody += line + "\n";
                        }
                        if (cities.size() == 1) {
                            mCityLab.addCity(parseResponseAndUpdateCities(cities.get(0), responseBody));
                        } else {
                            mCityLab.updateCitiesInDB(parseListResponseAndUpdateCities(cities, responseBody));
                        }
                        close(r);
                        urlConnection.disconnect();
                        // Background work finished successfully
                        Log.i(LOG_TAG, "UpdateCitiesTask: done successfully");
                    } else if (urlConnection.getResponseCode() == 429) {
                        // Too many requests
                        Log.i(LOG_TAG, "UpdateCitiesTask: too many requests");
                    } else {
                        // Bad response from server
                        Log.i(LOG_TAG, "UpdateCitiesTask: bad response");
                    }

                    // Get weather icons
                    for (City city : cities) {
                        url = iconRequestURL(city.getIcon());
                        if (url == null) {
                            return null;
                        }
                        urlConnection = (HttpURLConnection) url.openConnection();
                        if (urlConnection.getResponseCode() == 200) {
                            city.setIconBitmap(BitmapFactory.decodeStream(urlConnection.getInputStream()));
                            urlConnection.disconnect();
                            Log.i(LOG_TAG, "UpdateCitiesTask: done successfully");
                        }
                        urlConnection.disconnect();
                        mCityLab.updateCities(cities);
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "UpdateCitiesTask Exception");
                    e.printStackTrace();
                }
            } else {
                Log.e(LOG_TAG, "UpdateCitiesTask: Incorrect params");
            }
            return null;
        }

        private ArrayList<City> parseListResponseAndUpdateCities(ArrayList<City> cities, String responseBody) {
            try {
                JSONObject responseJsonObject = new JSONObject(responseBody);
                JSONArray listJsonArray = responseJsonObject.getJSONArray("list");
                // TODO: find out, why API returns less cities in response
                if (cities.size() >= listJsonArray.length()){
                    for(int i=0; i < listJsonArray.length() ;i++)
                    {
                        JSONObject cityJsonObject = listJsonArray.getJSONObject(i);
                        cities.get(i).setName(cityJsonObject.getString("id"));
                        cities.get(i).setName(cityJsonObject.getString("name"));
                        cities.get(i).setCountry(cityJsonObject.getJSONObject("sys").getString("country"));
                        cities.get(i).setIcon(cityJsonObject.getJSONArray("weather").getJSONObject(0).getString("icon"));
                        String temp = String.valueOf(Math.round(cityJsonObject.getJSONObject("main").getDouble("temp")));
                        cities.get(i).setTemp(temp + " \u2103");
                    }
                    return cities;
                } else {
                    Log.e(LOG_TAG, "ResponseBody JSONArray size is not equal to cities list size");
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "JSONException Data: " + responseBody);
                e.printStackTrace();
            }
            return null;
        }
        private City parseResponseAndUpdateCities(City city, String responseBody) {
            try {
                JSONObject cityJsonObject = new JSONObject(responseBody);
                city.setName(cityJsonObject.getString("id"));
                city.setName(cityJsonObject.getString("name"));
                city.setCountry(cityJsonObject.getJSONObject("sys").getString("country"));
                city.setIcon(cityJsonObject.getJSONArray("weather").getJSONObject(0).getString("icon"));
                String temp = String.valueOf(Math.round(cityJsonObject.getJSONObject("main").getDouble("temp")));
                city.setTemp(temp + " \u2103");
                return city;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "JSONException Data: " + responseBody);
                e.printStackTrace();
            }
            return null;
        }
    }

    protected URL weatherGroupRequestURL(ArrayList<City> cities) throws UnsupportedEncodingException, MalformedURLException {
        // http://api.openweathermap.org/data/2.5/group?id=524901,703448,2643743&units=metric

        StringBuilder urlBuilder = new StringBuilder(getString(R.string.API_BASE_URL));

        if (cities.size() == 1 && cities.get(0).getName() != null){
            urlBuilder.append("weather?q=");
            urlBuilder.append(cities.get(0).getName());
        } else if (cities.size() > 1){
            urlBuilder.append("group?id=");
            urlBuilder.append(cities.get(0).getId());
            for (int i = 1; i < cities.size(); i++){
                urlBuilder.append(",").append(cities.get(i).getId());
            }
        } else {
            return null;
        }
        urlBuilder.append("&units=metric");
        urlBuilder.append("&mode=json");
        final String apiKey = getString(R.string.API_KEY);
        urlBuilder.append("&appid=").append(apiKey);

        return new URL(urlBuilder.toString());
    }

    protected URL iconRequestURL(String param) throws MalformedURLException {
        // http://openweathermap.org/img/w/10d.png

        StringBuilder urlBuilder = new StringBuilder(getString(R.string.API_IMAGES_URL));
        urlBuilder.append(param);
        urlBuilder.append(".png");
        return new URL(urlBuilder.toString());
    }
}
