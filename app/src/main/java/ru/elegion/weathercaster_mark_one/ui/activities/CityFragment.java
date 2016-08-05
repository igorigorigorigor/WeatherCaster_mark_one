package ru.elegion.weathercaster_mark_one.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ru.elegion.weathercaster_mark_one.R;
import ru.elegion.weathercaster_mark_one.models.City;
import ru.elegion.weathercaster_mark_one.models.CityLab;


public class CityFragment extends Fragment {

    private City mCity;

    public City getCity() { return mCity; }
    public void setCity(City city) { mCity = city; }


    public static CityFragment newInstance(String id) {
        Bundle args = new Bundle();
        args.putSerializable(CityLab.getCityIdTag(), id);

        CityFragment fragment = new CityFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String id = getArguments().getString(CityLab.getCityIdTag());
        mCity = CityLab.build(getActivity()).getCity(id);
    }


    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_city, container, false);
        TextView tvLabel = (TextView) view.findViewById(R.id.tvCityName);
        tvLabel.setText(mCity.getName() + ", " + mCity.getCountry() + " --- " + mCity.getWeatherInfo().getTemperature());
        ImageView iconImageView = (ImageView) view.findViewById(R.id.iconImageView);
        iconImageView.setImageBitmap(mCity.getWeatherInfo().getIconBitmap());
        return view;
    }
}
