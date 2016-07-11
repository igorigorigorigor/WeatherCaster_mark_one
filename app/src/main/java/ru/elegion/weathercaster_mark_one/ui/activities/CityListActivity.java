package ru.elegion.weathercaster_mark_one.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
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

public class CityListActivity extends AppCompatActivity {
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

        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int swipedPosition = viewHolder.getAdapterPosition();
                CityAdapter adapter = (CityAdapter)mCityList.getAdapter();
                adapter.remove(swipedPosition);
            }
        });
        mItemTouchHelper.attachToRecyclerView(mCityList);

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
                            .input(R.string.city_name_hint, R.string.empty_prefill, new MaterialDialog.InputCallback() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
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
            viewHolder.nameTextView.setText(city.getName() + ", " + city.getCountry());
            viewHolder.iconImageView.setImageBitmap(city.getIconBitmap());
            viewHolder.tempTextView.setText(city.getTemp());
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView nameTextView;
            private ImageView iconImageView;
            private TextView tempTextView;

            public ViewHolder(View itemView) {
                super(itemView);
                nameTextView = (TextView) itemView.findViewById(R.id.city_list_item_nameTextView);
                iconImageView= (ImageView) itemView.findViewById(R.id.city_list_item_iconImageView);
                tempTextView = (TextView) itemView.findViewById(R.id.city_list_item_tempTextView);
            }
        }

        public void remove(int position ) {
            mCityLab.delete(mDataset.get(position));
            mDataset = mCityLab.getCities();
            notifyItemRemoved(position);
        }
    }

    private URL iconRequestURL(String param) throws MalformedURLException {
        // http://openweathermap.org/img/w/10d.png

        StringBuilder urlBuilder = new StringBuilder(getString(R.string.API_IMAGES_URL));
        urlBuilder.append(param);
        urlBuilder.append(".png");
        return new URL(urlBuilder.toString());
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
                    // Get weather data
                    URL url = weatherGroupRequestURL(cities);
                    if (url == null){return null;}
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
                            mCityLab.updateCitiesInDB(parseListResponseAndUpdateCities(cities, responseBody));
                        }
                        close(r);
                        urlConnection.disconnect();
                        // Background work finished successfully
                        Log.i(LOG_TAG, "UpdateCitiesTask: done successfully");
                    }
                    else if (urlConnection.getResponseCode() == 429) {
                        // Too many requests
                        Log.i(LOG_TAG, "UpdateCitiesTask: too many requests");
                    }
                    else {
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

    private URL weatherGroupRequestURL(ArrayList<City> cities) throws UnsupportedEncodingException, MalformedURLException {
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
