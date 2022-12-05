package com.example.lifeoptim;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlaceEvent extends RecyclerView.Adapter <PlaceEvent.ViewHolder> {

    public static ArrayList<ArrayList<String>> event_data = new ArrayList<ArrayList<String>>();

    int flag = 0;
    // constructor
    public PlaceEvent(ArrayList<ArrayList<String>> data) {
        this.event_data = data;

    }

    // ======================================
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView event_title;
        public TextView event_loc;
        public TextView event_start;
        public TextView event_end;
        public TextView event_suggestion;

        public int image;

        public ViewHolder(View itemView) {
            super(itemView);
            // R.id.txtPlaceName is in R.layout.item_layout
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
        Log.d("#", "" + event_data.size());
        return new ViewHolder(listItem);
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        String suggestion = "";
        Log.d("#suggestions", "" + event_data.get(4).get(position) + position);

        if(!event_data.get(4).get(position).isEmpty()){
            suggestion = event_data.get(4).get(0);
            holder.event_suggestion.setVisibility(View.VISIBLE);
            flag = 1;
        }
        else{
            holder.event_suggestion.setVisibility(View.INVISIBLE);
        }


        Log.d("#23 - 0", position + "");
        Log.d("#23 - -1", event_data.size() + "");
        Log.d("#23 - 0", event_data.get(0).get(position));
        String event_title = event_data.get(0).get(position);

        Log.d("#23 - 2", event_data.get(2).get(position));
        String event_start = event_data.get(2).get(position);

        Log.d("#23 - 3", event_data.get(3).get(position));
        String event_end = event_data.get(3).get(position);

        Log.d("#23 - 1", event_data.get(1).get(position));
        String event_loc = event_data.get(1).get(position);
//        String event_title = event.get(0);
//        String event_start = event.get(2);
//        String event_end = event.get(3);
//        String event_loc = event.get(4);

        Log.d("#bh event_title", event_title);
        Log.d("#bh event_s", event_start);
        Log.d("#bh event_e", event_end);
        Log.d("#bh event_loc", event_loc);

        holder.event_suggestion.setText(suggestion);
        holder.event_title.setText(event_title);
        holder.event_start.setText(event_start);
        holder.event_end.setText(event_end);
        holder.event_loc.setText(event_loc);

    }

    @Override
    public int getItemCount() {
        return event_data.get(0).size();
    }

}
