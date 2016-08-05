package ru.elegion.weathercaster_mark_one.ui.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
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
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = (activeNetwork != null) && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    abstract protected class GenericUpdateCitiesTask extends AsyncTask<ArrayList<City>, Void, Void> {

        @Override
        protected Void doInBackground(ArrayList<City>... citiesArray) {
            if (citiesArray.length == 1 && citiesArray[0] != null) {
                ArrayList<City> cities = citiesArray[0];
                try {
                    String responseBody = getWeatherDataFromApi(cities);
                    cities = parseListResponseAndUpdateCities(cities, responseBody);
                    for (City city:cities){
                        city.getWeatherInfo().setIconBitmap(getIconFromApi(city));
                    }
                    mCityLab.updateCitiesInDB(cities);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "GenericAddCityTask Exception");
                    e.printStackTrace();
                }
            } else {
                Log.e(LOG_TAG, "GenericAddCityTask: Incorrect params");
            }
            return null;
        }
    }

    abstract protected class GenericAddCityTask extends AsyncTask<ArrayList<City>, Void, Void> {
        @Override
        protected Void doInBackground(ArrayList<City>... citiesArray) {
            if (citiesArray.length == 1 && citiesArray[0] != null) {
                ArrayList<City> cities = citiesArray[0];
                try {
                    String responseBody = getWeatherDataFromApi(cities);
                    City newCity = parseResponseAndUpdateCity(cities.get(0), responseBody);
                    newCity.getWeatherInfo().setIconBitmap(getIconFromApi(newCity));
                    mCityLab.addCityToDB(newCity);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "GenericAddCityTask Exception");
                    e.printStackTrace();
                }
            } else {
                Log.e(LOG_TAG, "GenericAddCityTask: Incorrect params");
            }
            return null;
        }
    }

    @Nullable
    private Bitmap getIconFromApi(City city) throws IOException {
        URL url = iconRequestURL(city.getWeatherInfo().getIcon());

        if (url == null) { return null;  }

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        Bitmap icon = null;
        if (urlConnection.getResponseCode() == 200) {
            icon = BitmapFactory.decodeStream(urlConnection.getInputStream());
            urlConnection.disconnect();
        }
        urlConnection.disconnect();
        return icon;
    }

    @Nullable
    private String getWeatherDataFromApi(ArrayList<City> cities) throws IOException {
        URL url = provideRequestURL(cities);

        if (url == null) { return null;  }

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        StringBuilder responseBuilder = new StringBuilder();
        if (urlConnection.getResponseCode() == 200) {
            Log.d(LOG_TAG, "getWeatherDataFromApi: started gracefully");
            InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
            BufferedReader r = new BufferedReader(inputStreamReader);
            String line = null;
            while ((line = r.readLine()) != null) {
                responseBuilder.append(line).append("\n");
                Log.d(LOG_TAG, line);
            }
            close(r);
            urlConnection.disconnect();
            Log.d(LOG_TAG, "getWeatherDataFromApi: done successfully");
        } else if (urlConnection.getResponseCode() == 429) {
            // Too many requests
            Log.i(LOG_TAG, "getWeatherDataFromApi: too many requests");
        } else {
            // Bad response from server
            Log.i(LOG_TAG, "getWeatherDataFromApi: bad response");
        }
        return responseBuilder.toString();
    }


    private ArrayList<City> parseListResponseAndUpdateCities(ArrayList<City> outdatedCities, String responseBody) {
        try {
            JSONObject responseJsonObject = new JSONObject(responseBody);
            JSONArray listJsonArray = responseJsonObject.getJSONArray("list");
            // TODO: find out, why API returns less cities in response
            if (outdatedCities.size() >= listJsonArray.length()){

                ArrayList<City> updatedCities = new ArrayList<>();
                for(City city : outdatedCities)
                {
                    String cityJSON = listJsonArray.getJSONObject(outdatedCities.indexOf(city)).toString();
                    updatedCities.add(parseResponseAndUpdateCity(city, cityJSON));
                }
                return updatedCities;
            } else {
                Log.i(LOG_TAG, "ResponseBody JSONArray size is not equal to cities list size");
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSONException Data: " + responseBody);
            e.printStackTrace();
        }
        return null;
    }
    private City parseResponseAndUpdateCity(City city, String responseBody) throws JSONException {
        JSONObject cityJsonObject = new JSONObject(responseBody);
        city.setId(cityJsonObject.getString("id"));
        city.setName(cityJsonObject.getString("name"));
        city.setCountry(cityJsonObject.getJSONObject("sys").getString("country"));
        city.getWeatherInfo().setIcon(cityJsonObject.getJSONArray("weather").getJSONObject(0).getString("icon"));
        city.getWeatherInfo().setTemperature(String.valueOf(Math.round(cityJsonObject.getJSONObject("main").getDouble("temp"))));
        return city;
    }


    protected URL provideRequestURL(ArrayList<City> cities) throws UnsupportedEncodingException, MalformedURLException {
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
