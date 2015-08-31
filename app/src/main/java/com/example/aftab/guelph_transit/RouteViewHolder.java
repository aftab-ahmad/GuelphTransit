package com.example.aftab.guelph_transit;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/* Class which holds the route info */
public class RouteViewHolder extends RecyclerView.ViewHolder  {

    private TextView vTitle;
    private TextView vRouteName;
    private TextView vRouteDescription;

    public RouteViewHolder(View v) {

        super(v);

        vTitle =  (TextView) v.findViewById(R.id.title);
        vRouteName = (TextView)  v.findViewById(R.id.routeName);
        vRouteDescription = (TextView) v.findViewById(R.id.routeDescription);
    }

    public String getTitle (){
        return (String) vTitle.getText();
    }

    public String getRouteName (){
        return (String) vRouteName.getText();
    }

    public String getRouteDescription (){
        return (String) vRouteDescription.getText();
    }

    public void setTitle (String title) {
        vTitle.setText(title);
    }

    public void setRouteName (String routeName) {
        vRouteName.setText(routeName);
    }

    public void setRouteDescription (String RouteDescription) {
        vRouteDescription.setText(RouteDescription);
    }
}
