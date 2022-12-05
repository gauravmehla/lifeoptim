package com.example.lifeoptim;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlaceEvent extends RecyclerView.Adapter <PlaceEvent.ViewHolder> {

    public static ArrayList<ArrayList<String>> event_data = new ArrayList<ArrayList<String>>();

    public PlaceEvent(ArrayList<ArrayList<String>> data) {
        this.event_data = data;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView event_title;
        public TextView event_loc;
        public TextView event_start;
        public TextView event_end;
        public TextView event_suggestion;

        LinearLayout eventView;

        public int image;

        public ViewHolder(View itemView) {
            super(itemView);
            eventView = (LinearLayout) itemView.findViewById(R.id.eventView);

            this.event_title = (TextView) itemView.findViewById(R.id.text_event_description);
            this.event_start = (TextView) itemView.findViewById(R.id.text_start_date);
            this.event_end = (TextView) itemView.findViewById(R.id.text_end_date);
            this.event_loc = (TextView) itemView.findViewById(R.id.text_event_location);
            this.event_suggestion = (TextView) itemView.findViewById(R.id.text_suggestion);

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.layout_event, parent, false);

        return new ViewHolder(listItem);
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        String suggestion = "";

        if(event_data.get(4).get(position).equals("null")){
            holder.event_suggestion.setVisibility(View.INVISIBLE);
        } else {
            suggestion = event_data.get(4).get(position);
            holder.event_suggestion.setVisibility(View.VISIBLE);
        }

        String event_title = event_data.get(0).get(position);
        String event_start = event_data.get(2).get(position);
        String event_end = event_data.get(3).get(position);
        String event_loc = event_data.get(1).get(position);

        holder.event_suggestion.setText(suggestion);
        holder.event_title.setText(event_title);
        holder.event_start.setText("From " + event_start);
        holder.event_end.setText(" to " + event_end);
        holder.event_loc.setText(event_loc);

    }

    @Override
    public int getItemCount() {
        return event_data.get(0).size();
    }

}
