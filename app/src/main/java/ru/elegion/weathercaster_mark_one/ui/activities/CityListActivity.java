package ru.elegion.weathercaster_mark_one.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.elegion.weathercaster_mark_one.R;
import ru.elegion.weathercaster_mark_one.models.City;
import ru.elegion.weathercaster_mark_one.models.CityLab;
import ru.elegion.weathercaster_mark_one.ui.fragments.CityFragment;


public class CityListActivity extends BaseActivity {
    private CityAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private ProgressDialog mProgressDialog;
    private ArrayList<String> mAllCitiesNames;
    private boolean mTwoPane;
    private ItemTouchHelper mItemTouchHelper;
    private Menu mOptionsMenu;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.cities_title);
        mCityLab = CityLab.build(getApplicationContext());

        if (getBooleanPreference("IS_FIRST_LAUNCH") && !isNetworkConnected()){
            setContentView(R.layout.activity_network_error);
            FrameLayout frmNetworkError = (FrameLayout) findViewById(R.id.flNetworkError);
            frmNetworkError.setVisibility(View.VISIBLE);
            Button btnRefresh = (Button) findViewById(R.id.btnRefresh);
            btnRefresh .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recreate();
                }
            });
            return;
        } else {
            setContentView(R.layout.activity_city_list);
            setBooleanPreference("IS_FIRST_LAUNCH", false);
        };


        if (findViewById(R.id.fragmentContainer) != null) {
            mTwoPane = true;
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshCities();
            }
        });

        refreshCities();

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new CityAdapter(getApplicationContext());
        mRecyclerView.setAdapter(mAdapter);

        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int swipedPosition = viewHolder.getAdapterPosition();
                mAdapter.remove(swipedPosition);
            }
        });
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);


        mAllCitiesNames = mCityLab.getAllCitiesNames();

        FloatingActionButton btnAddCity = (FloatingActionButton) findViewById(R.id.btnAddCity);
        btnAddCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCityLab.getCities().size() < 10 ){
                    showAlertDialog();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.cities_limit_error, Toast.LENGTH_SHORT).show();
                }
            };
         });

        showEmptyListMessage();
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CityListActivity.this);
        final View dialogView = this.getLayoutInflater().inflate(R.layout.alert_dialog_add_city, null);
        final AutoCompleteTextView input = (AutoCompleteTextView) dialogView.findViewById(R.id.dialog_input);
        input.setThreshold(3);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, mAllCitiesNames);

        input.setAdapter(adapter);
        input.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String cityName = (String) adapterView.getItemAtPosition(position);
                input.setText(cityName);
            }
        });

        builder
                .setTitle("Add city")
                .setView(dialogView)
                .setNegativeButton(R.string.addCityDialogCancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .setPositiveButton(R.string.addCityDialogOk,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ArrayList<City> newCities = new ArrayList<City>();
                                City newCity = new City();
                                newCity.setName(input.getText().toString());
                                newCities.add(newCity);
                                AddCityTask addCityTask = new AddCityTask();
                                addCityTask.execute(newCities);
                                ;}
                        });


        AlertDialog alert = builder.create();
        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isNetworkConnected()) {
            getMenuInflater().inflate(R.menu.main, menu);
            mOptionsMenu = menu;
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreshCities();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void refreshCities() {
        final UpdateCitiesTask refreshCitiesTask = new UpdateCitiesTask();
        refreshCitiesTask.execute(mCityLab.getCities());
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private class CityAdapter extends RecyclerView.Adapter<CityHolder> {
        private CityLab mCityLab;

        public CityAdapter(Context context) {
            mCityLab = CityLab.build(context);
        }

        @Override
        public CityHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(CityListActivity.this);
            View view = layoutInflater.inflate(R.layout.list_item_city, parent, false);
            return new CityHolder(view);
        }

        @Override
        public void onBindViewHolder(CityHolder holder, int position) {
            City city = mCityLab.getCityByPosition(position);
            holder.bindCity(city);
        }

        @Override
        public int getItemCount() {
            return mCityLab.getCities().size();
        }

        public void remove(int position) {
            mCityLab.remove(position);
            notifyItemRemoved(position);
            if (mTwoPane) {
                FragmentManager fm = getSupportFragmentManager();
                CityFragment fragment = (CityFragment) fm.findFragmentById(R.id.fragmentContainer);
                if (fragment != null) {
                    if (mCityLab.getCities().size() == 0) {
                        fm.beginTransaction()
                                .remove(fragment)
                                .commit();
                    } else {
                        fragment = CityFragment.newInstance(mCityLab.getCityByPosition(mCityLab.getCities().size() - 1).getUID());
                        fm.beginTransaction()
                                .replace(R.id.fragmentContainer, fragment)
                                .commit();
                    }
                }
            }

            showEmptyListMessage();
        }
    }

    private class CityHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private TextView mNameTextView;
        private ImageView mIconImageView;
        private TextView mTempTextView;

        private City mCity;

        public CityHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mNameTextView = (TextView) itemView.findViewById(R.id.city_list_item_nameTextView);
            mIconImageView = (ImageView) itemView.findViewById(R.id.city_list_item_iconImageView);
            mTempTextView = (TextView) itemView.findViewById(R.id.city_list_item_tempTextView);
        }

        public void bindCity(City city) {
            mCity = city;
            mNameTextView.setText(city.getName() + ", " + city.getCountry());
            mTempTextView.setText(city.getWeatherInfo().getTemperature() + " " + getString(R.string.temperature_units));
            if (city.getWeatherInfo().getIcon() != null){
                mIconImageView.setImageResource(getResources().getIdentifier(city.getWeatherInfo().getIcon(), "drawable", getPackageName()));
            }
        }

        @Override
        public void onClick(View v) {
            if (mTwoPane) {
                CityFragment fragment = CityFragment.newInstance(mCity.getUID());
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .commit();
            } else {
                Intent i = new Intent(getApplicationContext(), CityActivity.class);
                i.putExtra(CityLab.getCityUidTag(), mCity.getUID());
                startActivity(i);
            }

        }
    }

    private class UpdateCitiesTask extends GenericUpdateCitiesTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mAdapter.notifyDataSetChanged();
            hideProgressDialog();
            if (mTwoPane && mCityLab.getCities().size() > 0 ) {
                FragmentManager fm = getSupportFragmentManager();
                CityFragment fragment = (CityFragment) fm.findFragmentById(R.id.fragmentContainer);
                if (fragment == null) {
                    fragment = CityFragment.newInstance(mCityLab.getCityByPosition(0).getUID());
                    fm.beginTransaction()
                            .add(R.id.fragmentContainer, fragment)
                            .commit();
                }
            }

        }
    }

    private class AddCityTask extends GenericAddCityTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mAdapter.notifyDataSetChanged();
            hideProgressDialog();
            if (mTwoPane && mCityLab.getCities().size() > 0 ) {
                FragmentManager fm = getSupportFragmentManager();
                CityFragment fragment = (CityFragment) fm.findFragmentById(R.id.fragmentContainer);
                if (fragment == null) {
                    fragment = CityFragment.newInstance(mCityLab.getCityByPosition(mCityLab.getCities().size() - 1).getUID());
                    fm.beginTransaction()
                            .add(R.id.fragmentContainer, fragment)
                            .commit();
                } else {
                    fragment = CityFragment.newInstance(mCityLab.getCityByPosition(mCityLab.getCities().size() - 1).getUID());
                    fm.beginTransaction()
                            .replace(R.id.fragmentContainer, fragment)
                            .commit();
                }
            }
            showEmptyListMessage();
        }
    }

    private void hideProgressDialog() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.dismiss();
            }
        }, 500);
    }

    private void showProgressDialog() {
        mProgressDialog = new ProgressDialog(CityListActivity.this);
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mProgressDialog.setMessage(getString(R.string.downloading_data));
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
        }
    }

    private void showEmptyListMessage() {
        TextView tvEmptyList = (TextView) findViewById(R.id.tvEmptyList);
        if (mCityLab.getCities().size() == 0){
            tvEmptyList.setVisibility(View.VISIBLE);
        } else {
            tvEmptyList.setVisibility(View.GONE);
        }
    }
}
