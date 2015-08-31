package com.example.aftab.guelph_transit;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Fragment for showing times on the weekdays
 */
public class WeekdayFragment extends Fragment {

    Thread myThread;

    Runnable myRunnableThread;

    private View view;
    private List<RouteInfo> routeList;
    private List<String[]> stops = new ArrayList<String[]>();

    private String type ="0";

    private String routeID[] = new String[]{"1A","1B","2A","2B","3A","3B","4","5","6","7","8","9","10","11","12","13","14","15","16","20","50","56","57","58"};
    private String description[] = new String[]{
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

    private RecyclerView recList;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        recList.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity().getApplicationContext(),
                        new RecyclerItemClickListener.OnItemClickListener() {

                            @Override public void onItemClick(View view, int position) {

                                RouteInfo item = routeList.get(position);
                                Toast.makeText(getActivity(), item.getTitle(), Toast.LENGTH_SHORT).show();

                                NotificationCompat.Builder mBuilder =
                                        new NotificationCompat.Builder(getActivity().getApplicationContext())
                                                .setSmallIcon(R.drawable.ic_launcher)
                                                .setContentTitle("Route " + TimesActivity.routeName + "-" + TimesActivity.route)
                                                .setContentText(TimesActivity.routeName + " arriving at: " + item.getTitle() + ".")
                                                .setPriority(1);

                                Intent resultIntent = new Intent(getActivity().getApplicationContext(), SettingsActivity.class);
                                PendingIntent resultPendingIntent =
                                        PendingIntent.getActivity(
                                                getActivity().getApplicationContext(),
                                                0,
                                                resultIntent,
                                                PendingIntent.FLAG_UPDATE_CURRENT
                                        );

                                mBuilder.setContentIntent(resultPendingIntent);
                                // Sets an ID for the notification
                                int mNotificationId = 001;
                                // Gets an instance of the NotificationManager service
                                NotificationManager mNotifyMgr =
                                        (NotificationManager) getActivity().getApplicationContext().getSystemService(getActivity().getApplicationContext().NOTIFICATION_SERVICE);
                                // Builds the notification and issues it.
                                mNotifyMgr.notify(mNotificationId, mBuilder.build());
                            }
                        })
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.times_fragment, container, false);
        type = getArguments().getString("type");

        recList = (RecyclerView) view.findViewById(R.id.timesList);

        TextView routeName = (TextView) view.findViewById(R.id.routeID);
        routeName.setText(TimesActivity.route);
        //recList.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(view.getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        myThread = null;

        myRunnableThread = new CountDownRunner();
        myThread= new Thread(myRunnableThread);
        myThread.start();

        updateList();

        return view;
    }

    public void updateList ()
    {
        try {
            stops = readFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        routeList  = createList();
        RouteAdapter ca = new RouteAdapter(routeList);
        recList.setAdapter(ca);
    }

    public List<String[]> readFile () throws IOException
    {
        List<String[]> resultList = new ArrayList<String[]>();

        String line;
        String filename = "";

        if (TimesActivity.routeName.contains ("Route")){
            filename = TimesActivity.routeName.replace ("Route ", "");
        }
        else{
            filename = TimesActivity.routeName;
        }

        if (type.equalsIgnoreCase("1"))
            filename = filename + "_Saturday";
        else if (type.equalsIgnoreCase("2"))
            filename = filename + "_Sunday";

        filename = filename + ".csv";
        InputStreamReader is = new InputStreamReader(view.getContext().getAssets().open(filename));
        BufferedReader reader = new BufferedReader(is);

        Log.i("TimesActivity_filename", filename);
        Log.i("TimesActivity_filename", TimesActivity.id);

        reader.readLine();
        while ((line = reader.readLine()) != null) {
            Log.i("TimesActivity_line", line);
            String[] row = line.split(",");

            if (row != null && row[1].contains((TimesActivity.id))){
                resultList.add(row);
            }
        }

        for (String temp [] : resultList) {
            for (String t : temp) {
                Log.i("TimesActivity", t);
            }
        }

        is.close();
        return resultList;
    }

    public void doWork() {
        getActivity().runOnUiThread(new Runnable() {
            @SuppressWarnings("deprecation")
            public void run() {
                try {
                    TextView txtCurrentTime = (TextView) view.findViewById(R.id.time);
                    Date dt = new Date();
                    int hours = dt.getHours();
                    int minutes = dt.getMinutes();
                    int seconds = dt.getSeconds();

                    String time = "AM";
                    String min = Integer.toString(minutes);
                    String sec = Integer.toString(seconds);

                    if (hours > 12) {
                        hours = hours - 12;
                        time = "PM";
                    }
                    if (hours == 12) {
                        time = "PM";
                    }
                    if (minutes < 10) {
                        DecimalFormat formatter = new DecimalFormat("00");
                        min = formatter.format(minutes);
                    }
                    if (seconds < 10) {
                        DecimalFormat formatter = new DecimalFormat("00");
                        sec = formatter.format(seconds);
                    }

                    String curTime = hours + ":" + min + ":" + sec;
                    txtCurrentTime.setText("Current Time: " + curTime + " " + time);

                    for (RouteInfo temp : routeList) {
                        String routes = temp.getTitle();

                        String split[] = routes.split(":");
                        int stopTime = Integer.parseInt(split[0]);

                        if (split[1].contains("PM") && stopTime != 12) {
                            stopTime = stopTime + 12;
                        } else if (split[1].contains("AM") && stopTime == 12)
                            stopTime = 0;

                        //Log.i("TimesActivity_test", "12-hour time is: " + split[0] + " 24 hour time is: " + stopTime);

                        int currHour = hours;

                        if (time.contains("PM") && currHour != 12) {
                            currHour = currHour + 12;
                        } else if (time.contains("AM") && currHour == 12)
                            currHour = 0;

                        //Log.i("TimesActivity_test", "12-hour current time is: " + hours + " 24 hour current time is: " + currHour);

                        if (stopTime == currHour) {

                            String busSplit[] = split[1].split(" ");

                            int curMin = Integer.parseInt(min);
                            int busMin = Integer.parseInt(busSplit[0]);

                            if (busMin > curMin) {
                                TextView nextTime = (TextView) view.findViewById(R.id.nextbus);
                                nextTime.setText("Next Bus at: " + routes);
                                break;
                            }
                        }
                        else if (stopTime > currHour) {

                            TextView nextTime = (TextView) view.findViewById(R.id.nextbus);
                            nextTime.setText("Next Bus at: " + routes);
                            break;
                        }
                    }

                }
                catch (Exception e) {

                }
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

    private ArrayList<RouteInfo> createList() {

        ArrayList<RouteInfo> results = new ArrayList<RouteInfo>();

        int i=0;
        for (String temp [] : stops) {
            int size = temp.length;

            for (i=2; i<size; i++){
                Log.i("TimesActivity", "temp is: " + temp[i]);

                if (temp[i].contains ("AM") || temp[i].contains ("PM")){

                    Log.i("TimesActivity", "temp inside is: " + temp[i]);
                    RouteInfo newsData = new RouteInfo();
                    newsData.setTitle(temp[i]);
                    newsData.setRouteName(TimesActivity.route);
                    newsData.setRouteDescription("Description");
                    results.add(newsData);
                }
            }
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
