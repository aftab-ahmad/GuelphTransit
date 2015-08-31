package com.example.aftab.guelph_transit;

import android.annotation.SuppressLint;
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
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/* Activity to show bus stops for a particular route */
public class StopsActivity extends ActionBarActivity {

    private Thread myThread;
    private Runnable myRunnableThread;

    private RecyclerView recList;
    private Toolbar toolbar;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    String route = "";
    private List<RouteInfo> routeItems;
    private List<String[]> stops = new ArrayList<>();

    String routeID[] = new String[]{"1A","1B","2A","2B","3A","3B","4","5","6","7","8","9","10","11","12","13","14","15","16","20","50","56","57","58"};

    String description[] = new String[]{
            "College Edinburgh Clockwise",
            "College Edinburgh Counter Clockwise",
            "West Loop Clockwise",
            "West Loop Counter Clockwise",
            "East Loop Clockwise",
            "East Loop Counter Clockwise",
            "York",
            "Gordon",
            "Harvard Ironwood",
            "Kortright Downey",
            "Stone Road Mall",
            "Waterloo",
            "Imperial",
            "Willow West",
            "General Hospital",
            "Victoria Road Recreation Centre",
            "Grange",
            "University College",
            "Southgate",
            "Northwest Industrial",
            "Stone Road Express",
            "Victoria Express",
            "Harvard Express",
            "Edinburgh Express"
    };

    @SuppressLint("CommitPrefEdits")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.stops_activity);

        // set toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Context context = this;

        /* Handle the intent */
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            route = extras.getString("route_name");
        }

        final TextView routeView = (TextView) findViewById(R.id.routeID);

        if (route.contains ("Route")){
            String split [] = route.split(" ");

            int x=0;
            for(String s: routeID){
                if(s.equals(split[1]))
                    routeView.setText(description[x]);
                x++;
            }
        }
        else
        {
            int x=0;
            for(String s: routeID){
                if(s.equals(route))
                    routeView.setText(description[x]);
                x++;
            }
        }

        // read the file
        try {
            stops = readFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        recList = (RecyclerView) this.findViewById(R.id.cardList);
        recList.setHasFixedSize(true);

        // add listener
        recList.addOnItemTouchListener(
                new RecyclerItemClickListener(this.getApplicationContext(),
                        new RecyclerItemClickListener.OnItemClickListener() {

                            @Override public void onItemClick(View view, int position) {

                                // start new activity
                                RouteInfo item = routeItems.get(position);
                                Toast.makeText(StopsActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(StopsActivity.this, TimesActivity.class);

                                intent.putExtra("stop_name",routeItems.get(position).getTitle());
                                intent.putExtra("stop_id",routeItems.get(position).getRouteName());
                                intent.putExtra("route_name",route);
                                startActivity (intent);
                            }
                        })
        );

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        routeItems = createList();
        RouteAdapter ca = new RouteAdapter(routeItems);
        recList.setAdapter(ca);

        /* Load shared preferences for bookmarks
        * will be used to determine which routes have been favourited */
        pref = context.getSharedPreferences("bookmarks", Context.MODE_PRIVATE);
        editor = pref.edit();

        myRunnableThread = new CountDownRunner();
        myThread= new Thread(myRunnableThread);
        myThread.start();
    }

    /* Thread to update current time
    *  Updates nextbus time for each stop */
    public void doWork() {
        runOnUiThread(new Runnable() {
            @SuppressWarnings("deprecation")
            public void run() {
                try{
                    TextView txtCurrentTime= (TextView)findViewById(R.id.time);
                    Date dt = new Date();
                    int hours = dt.getHours();
                    int minutes = dt.getMinutes();
                    int seconds = dt.getSeconds();

                    String time = "AM";
                    String min = Integer.toString(minutes);
                    String sec =Integer.toString(seconds);

                    if (hours > 12) {
                        hours = hours - 12;
                        time = "PM";
                    }
                    if (hours == 12)
                    {
                        time = "PM";
                    }
                    if (minutes < 10){
                        DecimalFormat formatter = new DecimalFormat("00");
                        min = formatter.format(minutes);
                    }
                    if (seconds < 10){
                        DecimalFormat formatter = new DecimalFormat("00");
                        sec = formatter.format(seconds);
                    }
                    String curTime = hours + ":" + min + ":" + sec;
                    txtCurrentTime.setText("Current Time: " + curTime + " " + time);

                    int i=0, x=0;
                    for (String temp [] : stops){
                        int size = temp.length;

                        Log.i("StopsActivity_x", "x is: " + x);
                        for (i=2; i<size; i++){

                            if (temp[i].contains ("AM") || temp[i].contains ("PM")){

                                Log.i("StopsActivity_x", "temp is: " + temp[i]);
                                //Log.i("StopsActivity_stops", temp[i]);
                                String split[] = temp[i].split(":");
                                int stopTime = Integer.parseInt(split[0]);

                                if (split[1].contains("PM") && stopTime != 12) {
                                    stopTime = stopTime + 12;
                                } else if (split[1].contains("AM") && stopTime == 12)
                                    stopTime = 0;

                                //Log.i("StopsActivity_test", "12-hour time is: " + split[0] + " 24 hour time is: " + stopTime);

                                int currHour = hours;

                                if (time.contains("PM") && currHour != 12) {
                                    currHour = currHour + 12;
                                } else if (time.contains("AM") && currHour == 12)
                                    currHour = 0;

                                //Log.i("StopsActivity_test", "12-hour current time is: " + hours + " 24 hour current time is: " + currHour);

                                Log.i ("StopsActivity_x", "stopTime is: " + stopTime + " and currHour is: " + currHour);
                                if (stopTime == currHour) {

                                    String busSplit[] = split[1].split(" ");

                                    int curMin = Integer.parseInt(min);
                                    int busMin = Integer.parseInt(busSplit[0]);

                                    if (busMin > curMin) {
                                        String des = "Next Bus at: " + temp[i];
                                        routeItems.get(x).setRouteDescription(des);
                                        //TextView nextTime = (TextView) view.findViewById(R.id.nextbus);
                                        //nextTime.setText("Next Bus at: " + temp[i]);
                                        break;
                                    }
                                }
                                else if (stopTime > currHour) {

                                    //TextView nextTime = (TextView) view.findViewById(R.id.nextbus);
                                    //nextTime.setText("Next Bus at: " + temp[i]);
                                    String des = "Next Bus at: " + temp[i];
                                    routeItems.get(x).setRouteDescription(des);
                                    break;
                                }
                            }
                        }
                        x++;
                    }
                }
                catch (Exception e) {}
            }
        });
    }

    class CountDownRunner implements Runnable{
        // @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()){
                try {
                    doWork();
                    Thread.sleep(1000); // Pause of 1 Second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }catch(Exception e){
                }
            }
        }
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

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> selections = sharedPrefs.getStringSet("favourties", null);

        if (selections != null){
            for(String s: selections){
                Log.i("StopsActivity_prefs", s);
            }
        }

        /* Check if route has been favourited, and color the star if needed*/
        if (route.contains ("Route"))
            route = route.replace ("Route ", "");

        boolean fav = pref.getBoolean(route, false);
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
        if (id == R.id.action_star) { // check if route needs to be added or removed from favorites

            boolean fav = pref.getBoolean(route, false);

            if ( fav == false){
                Drawable myIcon = getResources().getDrawable( R.drawable.ic_star_rate_white );

                int hex = 0xFFFF66;
                ColorFilter filter = new LightingColorFilter( Color.BLACK, hex);
                myIcon.setColorFilter(filter);

                if (route.contains ("Route"))
                    Toast.makeText(this, route + " added to favourites.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Route " + route + " added to favourites.", Toast.LENGTH_SHORT).show();

                if (route.contains ("Route"))
                    route = route.replace ("Route ", "");

                editor.putBoolean(route, true);

                editor.commit();

            }
            else
            {
                Drawable myIcon = getResources().getDrawable( R.drawable.ic_star_rate_white );
                ColorFilter filter = new LightingColorFilter( Color.BLACK, Color.LTGRAY);
                myIcon.setColorFilter(filter);

                if (route.contains ("Route"))
                    Toast.makeText(this, route + " removed from favourites.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Route " + route + " removed from favourites.", Toast.LENGTH_SHORT).show();

                if (route.contains ("Route"))
                    route = route.replace ("Route ", "");

                editor.putBoolean(route, false);
                editor.commit();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /* Read file */
    public List<String[]> readFile () throws IOException
    {
        String filename = "";
        String line;


        if (route.contains ("Route")) {
            filename = route.replace("Route ", "");
            filename = filename + ".csv";
        }
        else {
            filename = route + ".csv";
        }

        Log.i("StopsActivity_filename", filename);
        List<String[]> resultList = new ArrayList<String[]>();

        InputStreamReader is = new InputStreamReader(getAssets().open(filename));
        BufferedReader reader = new BufferedReader(is);

        reader.readLine();
        while ((line = reader.readLine()) != null) {
            String[] row = line.split(",");

            if (row != null){
                resultList.add(row);
            }
        }

        for (String temp [] : resultList) {
            for (String t : temp) {
                Log.i("StopsActivity", t);
            }
        }

        is.close();
        return resultList;
    }

    private void showSettings()
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity (intent);
    }

    /* Create list of stops */
    private List<RouteInfo> createList() {

        List<RouteInfo> results = new ArrayList<>();

        for (String temp [] : stops) {

            RouteInfo info = new RouteInfo();
            info.setTitle(temp[0]);
            info.setRouteName("Stop ID: " + temp[1]);
            info.setRouteDescription("Next bus at: ");
            results.add(info);
        }

        return results;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (myThread.isInterrupted())
            myThread.start();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (myThread.isInterrupted())
            myThread.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        myThread.interrupt();
    }
}
