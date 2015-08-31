package com.example.aftab.guelph_transit;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/* Adapter to switch fragments for times activity */
public class TimesPagerAdapter extends FragmentPagerAdapter {

    private final String[] TITLES = { "Weekday", "Saturday", "Sunday"};

    public TimesPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }

    @Override
    public int getCount() {
        return TITLES.length;
    }

    @Override
    public Fragment getItem(int position) {

        Bundle data = new Bundle();

        switch (position)
        {
            case 0:
                WeekdayFragment weekFrag = new WeekdayFragment();
                data.putString("type", "0");
                weekFrag.setArguments(data);
                return weekFrag;

            case 1:
                SaturdayFragment satFrag = new SaturdayFragment();
                data.putString("type", "1");
                satFrag.setArguments(data);
                return satFrag;

            case 2:
                SundayFragment sunFrag = new SundayFragment();
                data.putString("type", "2");
                sunFrag.setArguments(data);
                return sunFrag;
        }

        return null;
    }
}