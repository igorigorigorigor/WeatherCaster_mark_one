package ru.elegion.weathercaster_mark_one.ui.activities;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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


import ru.elegion.weathercaster_mark_one.models.City;
import ru.elegion.weathercaster_mark_one.models.CityLab;
import ru.elegion.weathercaster_mark_one.R;

public class CityListActivity extends ListActivity {
    private static String LOG_TAG = "CityListActivity";
    private ArrayList<City> mCities;
    private CityAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mCityList;
    private ProgressDialog mProgressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.cities_title);
        setContentView(R.layout.activity_city_list);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });

        mCities = CityLab.build(getApplicationContext()).getCities();
        UpdateCitiesTask initiateCitiesTask = new UpdateCitiesTask();
        initiateCitiesTask.execute(mCities);


        mCityList = (ListView) findViewById(android.R.id.list);
        mAdapter = new CityAdapter(mCities);
        mCityList.setAdapter(mAdapter);
    }

    private void refreshContent() {
        mCities = CityLab.build(getApplicationContext()).getCities();
        final UpdateCitiesTask refreshCitiesTask = new UpdateCitiesTask();
        refreshCitiesTask.execute(mCities);
        mAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private class CityAdapter extends ArrayAdapter<City> {
        public CityAdapter (ArrayList<City> cities) {
            super(CityListActivity.this, 0, cities);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){
                convertView = CityListActivity.this.getLayoutInflater().inflate(R.layout.list_item_city, null);
            }
            City c = getItem(position);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.city_list_item_nameTextView);
            nameTextView.setText(c.getName());

            TextView tempTextView = (TextView) convertView.findViewById(R.id.city_list_item_tempTextView);
            tempTextView.setText(c.getTemp());

            return convertView;
        }
    }

    private static void close(Closeable x) {
        try {
            if (x != null) {
                x.close();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException: Error occurred while closing stream");
        }
    }

    public class UpdateCitiesTask extends AsyncTask<ArrayList<City>, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(CityListActivity.this);
            if(!mSwipeRefreshLayout.isShown()) {
                mProgressDialog.setMessage(getString(R.string.downloading_data));
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
            }
        }
        @Override
        protected Void doInBackground(ArrayList<City>... citiesArray) {
            String responseBody = "";
            if (citiesArray.length == 1 && citiesArray[0] != null){
                ArrayList<City> cities = citiesArray[0];
                try {
                    URL url = provideURL(cities);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    if (urlConnection.getResponseCode() == 200) {
                        InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
                        BufferedReader r = new BufferedReader(inputStreamReader);

                        String line = null;
                        while ((line = r.readLine()) != null) {
                            responseBody += line + "\n";
                        }
                        mCities = parseResponseAndUpdateCities(cities, responseBody);
                        close(r);
                        urlConnection.disconnect();
                        // Background work finished successfully
                        Log.i(LOG_TAG, "UpdateCitiesTask: done successfully");
                        // Save date/time for latest successful result
                    }
                    else if (urlConnection.getResponseCode() == 429) {
                        // Too many requests
                        Log.i(LOG_TAG, "UpdateCitiesTask: too many requests");
                    }
                    else {
                        // Bad response from server
                        Log.i(LOG_TAG, "UpdateCitiesTask: bad response");
                    }
                } catch (IOException e) {
                    Log.e(LOG_TAG, "IOException Data: " + responseBody);
                    e.printStackTrace();
                    // Exception while reading data from url connection
                }
            } else {
                Log.e(LOG_TAG, "UpdateCitiesTask: Incorrect params");
            }
            return null;
        }

        private ArrayList<City> parseResponseAndUpdateCities(ArrayList<City> cities, String responseBody) {
            try {
                JSONObject responseJsonObject = new JSONObject(responseBody);
                JSONArray listJsonArray = responseJsonObject.getJSONArray("list");
                if (cities.size() == listJsonArray.length()){
                    for(int i=0; i < listJsonArray.length() ;i++)
                    {
                        JSONObject cityJsonObject = listJsonArray.getJSONObject(i);
                        cities.get(i).setName(cityJsonObject.getString("name"));
                        cities.get(i).setTemp(cityJsonObject.getJSONObject("main").getString("temp"));
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

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mAdapter.notifyDataSetChanged();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mProgressDialog.dismiss();
                }
            }, 500);
        }
    }

    private URL provideURL(ArrayList<City> cities) throws UnsupportedEncodingException, MalformedURLException {
        // http://api.openweathermap.org/data/2.5/group?id=524901,703448,2643743&units=metric

        StringBuilder urlBuilder = new StringBuilder(getString(R.string.API_BASE_URL));

        urlBuilder.append(getString(R.string.SELECTOR_TYPE));
        if (cities.size() == 1) {
            urlBuilder.append(cities.get(0).getId());
        } else {
            urlBuilder.append(cities.get(0).getId());
            for (int i = 1; i < cities.size(); i++){
                urlBuilder.append(",").append(cities.get(i).getId());
            }
        }

        urlBuilder.append("&units=metric");
        urlBuilder.append("&mode=json");
        final String apiKey = getString(R.string.API_KEY);
        urlBuilder.append("&appid=").append(apiKey);

        return new URL(urlBuilder.toString());
    }
}
