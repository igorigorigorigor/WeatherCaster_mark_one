package ru.elegion.weathercaster_mark_one.ui.activities;

import android.app.ProgressDialog;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
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

import java.util.ArrayList;


import ru.elegion.weathercaster_mark_one.models.City;
import ru.elegion.weathercaster_mark_one.models.CityLab;
import ru.elegion.weathercaster_mark_one.R;

public class CityListActivity extends BaseActivity {
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
                refreshList();
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

    private void refreshList() {
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


    private class UpdateCitiesTask extends GenericNetworkTask {
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
}
