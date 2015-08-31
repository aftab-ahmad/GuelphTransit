package com.example.aftab.guelph_transit;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/* Adapter for routes */
public class RouteAdapter extends RecyclerView.Adapter<RouteViewHolder>{

    private List<RouteInfo> routeList;

    public RouteAdapter(List<RouteInfo> routeList) {
        this.routeList = routeList;
    }

    @Override
    public void onBindViewHolder(RouteViewHolder routeViewHolder, int i) {

        RouteInfo info = routeList.get(i);

        routeViewHolder.setTitle(info.getTitle());
        routeViewHolder.setRouteName(info.getRouteName());
        routeViewHolder.setRouteDescription(info.getRouteDescription());

    }

    @Override
    public RouteViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout, viewGroup, false);

        return new RouteViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

}
