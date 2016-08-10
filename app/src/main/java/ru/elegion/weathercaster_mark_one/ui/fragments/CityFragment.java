package ru.elegion.weathercaster_mark_one.ui.fragments;

import android.app.ProgressDialog;
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
    private TextView tvCityName;
    private ImageView ivIcon;
    private TextView tvWeatherDescription;
    private TextView tvTemperature;
    private TextView tvHumidity;
    private TextView tvPressure;
    private TextView tvWindSpeed;
    private ProgressDialog mProgressDialog;

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
        tvCityName = (TextView) view.findViewById(R.id.tvCityName);
        ivIcon = (ImageView) view.findViewById(R.id.ivIcon);
        tvWeatherDescription = (TextView) view.findViewById(R.id.tvWeatherDescription);
        tvTemperature = (TextView) view.findViewById(R.id.tvTemperature);
        tvHumidity = (TextView) view.findViewById(R.id.tvHumidity);
        tvPressure = (TextView) view.findViewById(R.id.tvPressure);
        tvWindSpeed = (TextView) view.findViewById(R.id.tvWindSpeed);

        tvCityName.setText(mCity.getName() + ", " + mCity.getCountry());
        tvWeatherDescription.setText(mCity.getWeatherInfo().getDescription());
        tvTemperature.setText(mCity.getWeatherInfo().getTemperature() + " " + getString(R.string.temperature_units));
        tvHumidity.setText(mCity.getWeatherInfo().getHumidity() + " " + getString(R.string.humidity_units));
        tvPressure.setText(mCity.getWeatherInfo().getPressure() + " " + getString(R.string.pressure_units));
        tvWindSpeed.setText(mCity.getWeatherInfo().getWindSpeed() + " " + getString(R.string.wind_speed_units));

        return view;
    }
}
