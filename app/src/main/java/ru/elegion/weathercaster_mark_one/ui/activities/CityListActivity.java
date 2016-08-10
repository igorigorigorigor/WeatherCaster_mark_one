package ru.elegion.weathercaster_mark_one.ui.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ru.elegion.weathercaster_mark_one.R;
import ru.elegion.weathercaster_mark_one.models.City;
import ru.elegion.weathercaster_mark_one.models.CityLab;
import ru.elegion.weathercaster_mark_one.ui.fragments.CityFragment;


public class CityListActivity extends BaseActivity {
    private CityAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private ProgressDialog mProgressDialog;
    private LinearLayoutManager mLayoutManager;
    private ArrayList<String> mAllCitiesNames;
    private boolean mTwoPane;
    private ItemTouchHelper mItemTouchHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.cities_title);
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
            setBooleanPreference("IS_FIRST_LAUNCH", false);
            return;
        } else {
            setContentView(R.layout.activity_city_list);
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

        mCityLab = CityLab.build(getApplicationContext());
        refreshCities();

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new CityAdapter(mCityLab.getCities());
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

        if (mTwoPane && mCityLab.getCities().size() > 0 ) {
            Bundle arguments = new Bundle();
            arguments.putString(CityLab.getCityIdTag(), mCityLab.getCity(0).getId());
            CityFragment fragment = CityFragment.newInstance(mCityLab.getCity(0).getId());
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        }

        Thread t = new Thread(new Runnable() {
            public void run() {
                mAllCitiesNames = mCityLab.getAllCitiesNames();
            }
        });
        t.start();
    }



    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CityListActivity.this);
        final View dialogView = this.getLayoutInflater().inflate(R.layout.alert_dialog_add_city, null);
        final AutoCompleteTextView input = (AutoCompleteTextView) dialogView.findViewById(R.id.dialog_input);
        input.setThreshold(3);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, mAllCitiesNames){
            @Override
            public int getCount() {
                return 1;
            }
        };
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
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    private void refreshCities() {
        final UpdateCitiesTask refreshCitiesTask = new UpdateCitiesTask();
        refreshCitiesTask.execute(mCityLab.getCities());
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private class CityAdapter extends RecyclerView.Adapter<CityHolder> {
        private List<City> mCities;

        public CityAdapter(List<City> cities) {
            mCities = cities;
        }

        @Override
        public CityHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(CityListActivity.this);
            View view = layoutInflater.inflate(R.layout.list_item_city, parent, false);
            return new CityHolder(view);
        }

        @Override
        public void onBindViewHolder(CityHolder holder, int position) {
            City city = mCities.get(position);
            holder.bindCity(city);
        }

        @Override
        public int getItemCount() {
            return mCities.size();
        }

        public void remove(int position) {
            mCityLab.remove(position);
            mCities = mCityLab.getCities();
            notifyItemRemoved(position);
        }

        public void updateDataset() {
            mCities = mCityLab.getCities();
            notifyDataSetChanged();
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
            mIconImageView.setImageResource(getResources().getIdentifier(city.getWeatherInfo().getIcon(), "drawable", getPackageName()));
            mTempTextView.setText(city.getWeatherInfo().getTemperature() + " " + getString(R.string.temperature_units));
        }

        @Override
        public void onClick(View v) {
            if (mTwoPane) {
                Bundle arguments = new Bundle();
                arguments.putString(CityLab.getCityIdTag(), mCity.getId());
                CityFragment fragment = CityFragment.newInstance(mCity.getId());
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .commit();
            } else {
                Intent i = new Intent(getApplicationContext(), CityActivity.class);
                i.putExtra(CityLab.getCityIdTag(), mCity.getId());
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
            mAdapter.updateDataset();
            hideProgressDialog();
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
            mAdapter.updateDataset();
            hideProgressDialog();
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
        if (!mSwipeRefreshLayout.isShown()) {
            mProgressDialog.setMessage(getString(R.string.downloading_data));
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
        }
    }
}
