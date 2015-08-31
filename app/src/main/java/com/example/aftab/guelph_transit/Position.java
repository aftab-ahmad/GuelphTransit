package com.example.aftab.guelph_transit;

/* Class to hold longitude and latitude */
public class Position {

    private double longitude;
    private double latitude;
    private String id;

    public Position(double longitude, double latitude, String id) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.id = id;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude (){
        return latitude;
    }

    public void setLatitude (double latitude){
        this.latitude = latitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
