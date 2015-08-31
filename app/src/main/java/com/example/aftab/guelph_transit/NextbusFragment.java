package com.example.aftab.guelph_transit;

import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NextbusFragment extends Fragment {

    private int position=1;
    private MapView mMapView;
    private GoogleMap googleMap;
    private List<Position> positions = new ArrayList<Position>();
    private List<String[]> resultList = new ArrayList<String[]>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.nextbus_fragment, container, false);

        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        googleMap = mMapView.getMap();
        googleMap.setMyLocationEnabled(true);

        addUI();

        // latitude and longitude
        double latitude = 0;
        double longitude = 0;

        LocationTracker GPS = new LocationTracker(getActivity());
        if(GPS.canGetLocation()){
            latitude = GPS.getLatitude(); // returns latitude
            longitude = GPS.getLongitude(); // returns longitude
            //Toast.makeText(getActivity().getApplicationContext(), "latitude: " + latitude + " longitude: " + longitude, Toast.LENGTH_SHORT).show();
        }
        else {
            GPS.showSettingsAlert();
        }

        // create marker
        MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude)).title("Current Location");

        // Changing marker icon
        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        // adding marker
        googleMap.addMarker(marker);

        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(12).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        /*ConnectivityManager conMan = (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);

        NetworkInfo.State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        NetworkInfo.State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();

        boolean connected = false;
        if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
            //Toast.makeText(getActivity(),"Mobile is Enabled.",Toast.LENGTH_SHORT).show();
            connected = true;
        }
        else if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
            //Toast.makeText(getActivity(),"Wifi is Enabled.",Toast.LENGTH_SHORT).show();
            connected = true;
        }
        else {
            //Toast.makeText(getActivity(),"Wifi or Mobile is not Enabled.",Toast.LENGTH_SHORT).show();
        }*/

        /*if (connected) {
            new ReadFileTask().execute();
        }*/
        return view;
    }


    private class ReadFileTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... filename) {

            resultList = new ArrayList<>();

            InputStreamReader is = null;
            try {
                is = new InputStreamReader(getActivity().getApplicationContext().getAssets().open("GuelphTransitBusStops.csv"));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            BufferedReader reader = new BufferedReader(is);

            String line = "";
            try {
                reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            int x=0;
            try {
                while ((line = reader.readLine()) != null) {
                    Log.i("NextBusActivity", line);
                    String[] row = line.split(",");

                    if (x == 23)
                        break;
                    if (row != null){
                        resultList.add(row);
                        x++;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String result) {
            populateMap();
            while (position < positions.size()) {
                String url = getURL();
                DownloadTask downloadTask = new DownloadTask();

                // Start downloading json data from Google Directions API
                downloadTask.execute(url);
            }

            String url = getLastURL();
            DownloadTask downloadTask = new DownloadTask();

            // Start downloading json data from Google Directions API
            downloadTask.execute(url);
        }
    }

    private String getLastURL() {

        String waypoints = "&waypoints=optimize:true";

        Log.d("TEST_position_start", "position is: " + position);
        Log.d("TEST_positions_array_start", "positions size is: " + positions.size());

        int last = positions.size();
        waypoints = waypoints + "|" + positions.get(last-1).getLatitude() + "," +  positions.get(last-1).getLongitude();
        waypoints = waypoints + "|" + positions.get(0).getLatitude() + "," +  positions.get(0).getLongitude();

        Log.d("TEST_position_end", "position is: " + position);

        String sensor = "sensor=false";
        String origin = "&origin=" + positions.get(last-1).getLatitude() + "," +  positions.get(last-1).getLongitude();
        String destination = "&destination=" + positions.get(0).getLatitude() + "," +  positions.get(0).getLongitude();

        String params = origin + destination + waypoints + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"+ output + "?" + params;

        Log.d("TEST", url);
        return url;
    }

    private String getURL() {

        String waypoints = "&waypoints=";
        int i=0, count=0;

        Log.d("TEST_position_start", "position is: " + position);
        Log.d("TEST_positions_array_start", "positions size is: " + positions.size());

        int startPos = position;

        for (i=position-1; i<positions.size(); i++) {

            if (count <=9)
                waypoints = waypoints + "|" + positions.get(i).getLatitude() + "," +  positions.get(i).getLongitude();
            else {
                position = i;
                break;
            }
            count++;
        }

        if (count <=9)
            position = positions.size();

        Log.d("TEST_position_end", "position is: " + position);
        int endPos = position;

        Log.d ("TEST_start pos", "Start pos is: " + startPos);
        Log.d ("TEST_end pos", "End pos is: " + endPos);

        String sensor = "sensor=false";
        String origin = positions.get(startPos).getLatitude() + "," +  positions.get(startPos).getLongitude();
        String destination = positions.get(endPos-1).getLatitude() + "," +  positions.get(endPos-1).getLongitude();

        String params =  "&origin=" + origin + "&destination="+destination + waypoints;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"+ output + "?" + params;

        Log.d("TEST", url);
        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsParser parser = new DirectionsParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            googleMap.addPolyline(lineOptions);
        }
    }

    private void addUI ()
    {
        UiSettings settings = googleMap.getUiSettings();
        settings.setZoomControlsEnabled(true);
        settings.setMyLocationButtonEnabled(true);
    }

    private void populateMap ()
    {
        int x=0;
        for (String temp [] : resultList) {

            String name = temp[1];
            double lat = Double.parseDouble(temp[2]);
            double lon = Double.parseDouble(temp[3]);

            positions.add(new Position(lon, lat, name));
            MarkerOptions marker = new MarkerOptions().position(new LatLng(lat, lon)).title(name);

            // Changing marker icon
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.busstop));

            // adding marker
            googleMap.addMarker(marker);

            /*if (x==9)
                break;
            x++;*/
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


}
