package com.example.lifeoptim;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;

public class HomeScreen extends AppCompatActivity {

    private DBManager dbManager;
    private ArrayList<ArrayList<String>> events = new ArrayList<ArrayList<String>>();

    public ArrayList<ArrayList<String>> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<ArrayList<String>> events) {
        this.events = events;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbManager = new DBManager(this);
        dbManager.open();


        // dbManager.delete("selectedCalendar");

        // Default values in the database


        if(initializeDatabase().equals("")) {

            setContentView(R.layout.layout_about_us);

            Spinner spinnerCalList = (Spinner)findViewById(R.id.spinner_calendar);
            ArrayList<String> personName = new ArrayList<String>();

            CalEvents.showCalendarIDs();

            for(String calID: CalEvents.calendarsList.keySet()) {
                HashMap<String, String> cal = CalEvents.calendarsList.get(calID);
                String accountName = cal.get("accountName");

                Log.d("<>", accountName);

                if(!personName.contains(accountName)) {
                    personName.add(accountName);
                }
            }

            ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, personName);
            spinnerCalList.setAdapter(arrayAdapter);

        } else {
            // Calendar is selected
            Log.d("==>", "Proceeeed");

            setContentView(R.layout.layout_home);

            CalEvents Cal = new CalEvents(this);

            setEvents(Cal.fetchCalEvents(5,1669697737310L,1672390800000L));
            Log.d("-event->","dsd");
            for(int i = 0 ; i < events.size(); ++i){
                if(i == 0){
                    for(int j = 0 ; j < events.get(i).size(); ++j){
                        Log.d("#event ", events.get(i).get(j));
                    }
                }
                if(i == 1){
                    for(int j = 0 ; j < events.get(i).size(); ++j){
                        Log.d("#loc ", events.get(i).get(j));
                    }
                }
            }

            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rView);
            PlaceEvent adapter = new PlaceEvent(events);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);


        }

    }

    public void addData(View v) {

        Spinner spinnerCalList = (Spinner)findViewById(R.id.spinner_calendar);

        String value = spinnerCalList.getSelectedItem().toString();

        Log.d("<>", "Inserting data " + value);

        dbManager.update("selectedCalendar", value);

        setContentView(R.layout.layout_home);
    }

    // Currently implementing
    public String initializeDatabase() {
        Cursor c = dbManager.fetch();
        boolean isKeyPresent = false;
        String keyValue = "";

        Log.d("=>", "Trying");

        if (c != null) {

            c.moveToFirst();
            while(true) {
                Log.d("=>", "in loop");
                if(c.getCount() > 0 && c.getString(1).equals("selectedCalendar")) {
                    isKeyPresent = true;
                    keyValue = c.getString(2);
                }

                if(c.isLast() || c.getCount() == 0) {
                    break;
                } else {
                    c.moveToNext();
                }
            }

            if(!isKeyPresent) {
                Log.d("=>", "Adding default value");
                dbManager.insert("selectedCalendar", "");
            }

        }
        return keyValue;
    }

    public void goHome(View V){
        Intent intent = new Intent(this, HomeScreen.class);
        startActivity(intent);
//        setContentView(R.layout.layout_home);
    }
    public void goProfile(View V){
        setContentView(R.layout.layout_profile);

    }
}