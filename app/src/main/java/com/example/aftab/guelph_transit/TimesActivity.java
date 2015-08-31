package com.example.aftab.guelph_transit;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/* Activity for displaying route times */
public class TimesActivity extends ActionBarActivity {

    private Toolbar toolbar;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    static String route = "", id = "", routeName = "";
    private List<RouteInfo> routeItems;
    private List<String[]> stops = new ArrayList<String[]>();

    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private TimesPagerAdapter tabAdapter;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.times_activity);

        /* set the toolbar */
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Context context = this;

        /* Handle the intent data */
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            route = extras.getString("stop_name");
            String test = extras.getString("stop_id");
            routeName = extras.getString("route_name");

            String split [] = test.split(":");
            split[1] = split[1].replace (" ", "");
            id = split[1];
        }

        /* Set the tabs */
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        pager = (ViewPager) findViewById(R.id.pager);
        tabAdapter = new TimesPagerAdapter(getSupportFragmentManager());

        tabs.setTextColor(Color.WHITE);
        tabs.setDividerColor(Color.WHITE);

        pager.setAdapter(tabAdapter);

        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);

        tabs.setViewPager(pager);

        pref = context.getSharedPreferences("bookmarks", Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        /* Set the search view */
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.stops_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (android.support.v7.widget.SearchView) menu.findItem(R.id.action_search).getActionView();

        ComponentName cn = new ComponentName(this, SearchActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(cn));

        /* Check if route has been bookmarked */
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> selections = sharedPrefs.getStringSet("favourties", null);

        if (selections != null){
            for(String s: selections){
                Log.i("StopsActivity_prefs", s);
            }
        }

        if (routeName.contains ("Route"))
            routeName = routeName.replace ("Route ", "");

        boolean fav = pref.getBoolean(routeName, false);
        Log.i("StopsActivity_Boolean", "Boolean is: " + fav);

        if (fav == false){
            Drawable myIcon = getResources().getDrawable( R.drawable.ic_star_rate_white );
            ColorFilter filter = new LightingColorFilter( Color.BLACK, Color.LTGRAY);
            myIcon.setColorFilter(filter);
        }
        else{
            int hex = 0xFFFF66;

            Drawable myIcon = getResources().getDrawable( R.drawable.ic_star_rate_white );
            ColorFilter filter = new LightingColorFilter( Color.BLACK, hex);
            myIcon.setColorFilter(filter);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            showSettings();
        }
        if (id == R.id.action_star) { // update route if it is being added or removed from favourites

            boolean fav = pref.getBoolean(route, false);

            if ( fav == false){
                Drawable myIcon = getResources().getDrawable( R.drawable.ic_star_rate_white );

                int hex = 0xFFFF66;
                ColorFilter filter = new LightingColorFilter( Color.BLACK, hex);
                myIcon.setColorFilter(filter);

                if (routeName.contains ("Route"))
                    Toast.makeText(this, routeName + " added to favourites.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Route " + routeName + " added to favourites.", Toast.LENGTH_SHORT).show();

                if (routeName.contains ("Route"))
                    routeName = routeName.replace ("Route ", "");

                editor.putBoolean(routeName, true);

                editor.commit();

            }
            else
            {
                Drawable myIcon = getResources().getDrawable( R.drawable.ic_star_rate_white );
                ColorFilter filter = new LightingColorFilter( Color.BLACK, Color.LTGRAY);
                myIcon.setColorFilter(filter);

                if (routeName.contains ("Route"))
                    Toast.makeText(this, routeName + " removed from favourites.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Route " + routeName + " removed from favourites.", Toast.LENGTH_SHORT).show();

                if (routeName.contains ("Route"))
                    routeName = routeName.replace ("Route ", "");

                editor.putBoolean(routeName, false);
                editor.commit();

            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
