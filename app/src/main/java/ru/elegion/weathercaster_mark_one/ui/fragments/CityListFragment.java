package ru.elegion.weathercaster_mark_one.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ru.elegion.weathercaster_mark_one.R;
import ru.elegion.weathercaster_mark_one.models.City;
import ru.elegion.weathercaster_mark_one.models.CityLab;

/**
 * Created by Freeman on 06.07.2016.
 */
public class CityListFragment extends android.support.v4.app.ListFragment {
    private ArrayList<City> mCities;

    public CityListFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.cities_title);

        mCities = CityLab.build(getActivity()).getCities();

        CityAdapter adapter = new CityAdapter(mCities);
        setListAdapter(adapter);
    }

    private class CityAdapter extends ArrayAdapter<City> {
        public CityAdapter (ArrayList<City> cities) {
            super(getActivity(), 0, cities);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_city, null);
            }
            City c = getItem(position);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.city_list_item_nameTextView);
            nameTextView.setText(c.getName());

            TextView tempTextView = (TextView) convertView.findViewById(R.id.city_list_item_tempTextView);
            tempTextView.setText(c.getTemp());

            return convertView;
        }
    }
}
