package ru.elegion.weathercaster_mark_one.ui.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import ru.elegion.weathercaster_mark_one.R;
import ru.elegion.weathercaster_mark_one.models.City;
import ru.elegion.weathercaster_mark_one.models.CityLab;
import ru.elegion.weathercaster_mark_one.ui.fragments.CityFragment;

public class CityActivity extends BaseActivity {

    private ViewPager mViewPager;
    private City mCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);

        mViewPager = (ViewPager) findViewById(R.id.vpPager);

        mCityLab = CityLab.build(getApplicationContext());
        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public Fragment getItem(int position) {
                City city = mCityLab.getCity(position);
                return CityFragment.newInstance(city.getId());
            }

            @Override
            public int getCount() {
                return mCityLab.getCities().size();
            }
        });

        String cityID = getIntent().getStringExtra(CityLab.getCityIdTag());
        mCity = mCityLab.getCity(cityID);
        int i = mCityLab.getCities().indexOf(mCity);
        mViewPager.setCurrentItem(i);

        setTitle(mCity.getName() + ", " + mCity.getCountry());

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                City city = CityLab.build(getApplicationContext()).getCity(position);
                getSupportActionBar().setTitle(city.getName() + ", " + city.getCountry());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }
}
