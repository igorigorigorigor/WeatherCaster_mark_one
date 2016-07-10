package ru.elegion.weathercaster_mark_one.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

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

public class CityListActivity extends Activity {
    private static String LOG_TAG = "CityListActivity";
    private CityLab mCityLab;
    private CityAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mCityList;
    private ProgressDialog mProgressDialog;
    private LinearLayoutManager mLayoutManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.cities_title);
        setContentView(R.layout.activity_city_list);

        if (getBooleanPreference("IS_FIRST_LAUNCH") && !isNetworkConnected()){
            setContentView(R.layout.activity_network_error);
            FrameLayout frmNetworkError = (FrameLayout) findViewById(R.id.flNetworkError);
            frmNetworkError.setVisibility(View.VISIBLE);
            TextView tvNetworkError = (TextView) findViewById(R.id.tvNetworkError);
            Button btnRefresh = (Button) findViewById(R.id.btnRefresh);
            btnRefresh .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recreate();
                }
            });
            return;
        };

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });


        mCityLab = CityLab.build(getApplicationContext());
        UpdateCitiesTask initiateCitiesTask = new UpdateCitiesTask();
        initiateCitiesTask.execute(mCityLab.getCities());


        mCityList = (RecyclerView) findViewById(android.R.id.list);
        mCityList.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mCityList.setLayoutManager(mLayoutManager);
        mAdapter = new CityAdapter(mCityLab.getCities());
        mCityList.setAdapter(mAdapter);

        setBooleanPreference("IS_FIRST_LAUNCH", false);

        FloatingActionButton btnAddCity = (FloatingActionButton) findViewById(R.id.btnAddCity);
        btnAddCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCityLab.getCities().size() < 11 ){
                    new MaterialDialog.Builder(CityListActivity.this)
                            .title(R.string.addCityDialogTitle)
                            .inputRangeRes(1, 30, R.color.material_red_500)
                            .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS)
                            .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(MaterialDialog dialog, CharSequence input) {
                                    ArrayList<City> newCities = new ArrayList<City>();
                                    City newCity = new City();
                                    newCity.setName(input.toString());
                                    newCities.add(newCity);
                                    UpdateCitiesTask addCityTask = new UpdateCitiesTask();
                                    addCityTask.execute(newCities);
                                }})
                            .positiveText(R.string.addCityDialogOk)
                            .negativeText(R.string.addCityDialogCancel)
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.cities_limit_error, Toast.LENGTH_SHORT).show();
                }
            };
         });
    }

    private void setBooleanPreference(String setting, boolean value) {
        final SharedPreferences preferences = getSharedPreferences("setting", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(setting, value);
        editor.apply();
    }

    private boolean getBooleanPreference(String setting) {
        final SharedPreferences preferences = getSharedPreferences("setting", Context.MODE_PRIVATE);
        return preferences.getBoolean(setting, true);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm =
                (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = (activeNetwork != null) && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    private void refreshContent() {
        mCityLab = CityLab.build(getApplicationContext());
        final UpdateCitiesTask refreshCitiesTask = new UpdateCitiesTask();
        refreshCitiesTask.execute(mCityLab.getCities());
        mAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder> {
        private ArrayList<City> mDataset;

        public CityAdapter (ArrayList<City> cities) {
            this.mDataset = cities;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_city, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            City city = mDataset.get(i);
            viewHolder.nameTextView.setText(city.getName());
            viewHolder.tempTextView.setText(city.getTemp());
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView nameTextView;
            private TextView tempTextView;

            public ViewHolder(View itemView) {
                super(itemView);
                nameTextView = (TextView) itemView.findViewById(R.id.city_list_item_nameTextView);
                tempTextView = (TextView) itemView.findViewById(R.id.city_list_item_tempTextView);
            }
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

    private class UpdateCitiesTask extends AsyncTask<ArrayList<City>, Void, Void> {
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
                        if (cities.size() == 1){
                            mCityLab.addCity(parseResponseAndUpdateCities(cities.get(0), responseBody));
                        } else {
                            mCityLab.updateCities(parseListResponseAndUpdateCities(cities, responseBody));
                        }
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

        private ArrayList<City> parseListResponseAndUpdateCities(ArrayList<City> cities, String responseBody) {
            try {
                JSONObject responseJsonObject = new JSONObject(responseBody);
                JSONArray listJsonArray = responseJsonObject.getJSONArray("list");
                if (cities.size() == listJsonArray.length()){
                    for(int i=0; i < listJsonArray.length() ;i++)
                    {
                        JSONObject cityJsonObject = listJsonArray.getJSONObject(i);
                        cities.get(i).setName(cityJsonObject.getString("id"));
                        cities.get(i).setName(cityJsonObject.getString("name"));
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
                String temp = String.valueOf(Math.round(cityJsonObject.getJSONObject("main").getDouble("temp")));
                city.setTemp(temp + " \u2103");
                return city;
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
}
