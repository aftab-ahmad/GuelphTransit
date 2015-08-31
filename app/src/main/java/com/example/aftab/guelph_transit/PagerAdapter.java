package com.example.aftab.guelph_transit;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/* *Adapter for loading fragments on main activity */
public class PagerAdapter extends FragmentPagerAdapter {

    private final String[] TITLES = { "Favourites", "Next Bus", "Updates"};

    public PagerAdapter(FragmentManager fm) {
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
            case 0: // show favourites
                FavouritesFragment favFragment = new FavouritesFragment();
                data.putInt("current_page", position+1);
                favFragment.setArguments(data);
                return favFragment;

            case 1: // show nextbux
                NextbusFragment nextBus = new NextbusFragment();
                data.putInt("current_page", position+1);
                nextBus.setArguments(data);
                return nextBus;

            case 2:
                return SuperAwesomeCardFragment.newInstance(position);
        }

        return null;
    }
}