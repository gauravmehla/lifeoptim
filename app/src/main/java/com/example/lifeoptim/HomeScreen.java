package com.example.lifeoptim;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeScreen extends AppCompatActivity {

    private DBManager dbManager;
    private ArrayList<ArrayList<String>> events = new ArrayList<ArrayList<String>>();

    public ArrayList<ArrayList<String>> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<ArrayList<String>> events) {

        this.events.clear();
        this.events = events;
    }

    public static Calendar c;
    public static DatePickerDialog dpd;

    public static int selectedDay;
    public static int selectedMonth;
    public static int selectedYear;

    CalEvents Cal;
    int calendarID = 0;

    PlaceEvent adapter;

    public static String DATE_FORMAT = "EEE - MMM d, yyyy";

    LocationService locS;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbManager = new DBManager(this);
        dbManager.open();

        Cal = new CalEvents(this);
        c = Calendar.getInstance();

        selectedDay = c.get(Calendar.DAY_OF_MONTH);
        selectedMonth = c.get(Calendar.MONTH);
        selectedYear = c.get(Calendar.YEAR);

        locS = new LocationService(this);

        // Default values in the database
//        dbManager.delete("selectedCalendar");



        if(initializeDatabase().equals("")) {

            setContentView(R.layout.layout_about_us);
            Log.d("=-=> Initializing Database^", "");

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

            mainLayout(0);
//            // Calendar is selected
//            Log.d("=-=>", "Proceeeed");
//            setContentView(R.layout.layout_home);
//            // Update date
//            formatCurrentDate();
//            // Update the text on home screen
//            String username = getData("currentUser");
//            TextView welcomeUser = findViewById(R.id.text_welcome_user2);
//            welcomeUser.setText("Hello, " + username);
//
//
//            // get current calendar ID
//            String selectedCalendar = getData("selectedCalendar");
//
//            for(String calID: CalEvents.calendarsList.keySet()) {
//                HashMap<String, String> cal = CalEvents.calendarsList.get(calID);
//                String name = cal.get("name");
//
//                if(name.equals(selectedCalendar)) {
//                    calendarID = Integer.parseInt(calID);
//                }
//            }
//
//            Log.d("123",calendarID + " " + selectedCalendar);
//
//            long startTime = Cal.getDayStartTimestampInMilli(selectedYear, selectedMonth, selectedDay);
//            long endTime = Cal.getDayEndTimestampInMilli(selectedYear, selectedMonth, selectedDay);
//
//            try {
//                setEvents(Cal.fetchCalEvents(calendarID,startTime,endTime));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            Log.d("-event->","dsd");
//            for(int i = 0 ; i < events.size(); ++i){
//                if(i == 0){
//                    for(int j = 0 ; j < events.get(i).size(); ++j){
//                        Log.d("#event ", events.get(i).get(j));
//                    }
//                }
//                if(i == 1){
//                    for(int j = 0 ; j < events.get(i).size(); ++j){
//                        Log.d("#loc ", events.get(i).get(j));
//                    }
//                }
//            }
//
//            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rView);
//            adapter = new PlaceEvent(events);
//            recyclerView.setHasFixedSize(true);
//            recyclerView.setLayoutManager(new LinearLayoutManager(this));
//            recyclerView.setAdapter(adapter);
//            /* Calendar */

        }

    }

    public void mainLayout(int id){
        // Calendar is selected
        setContentView(R.layout.layout_home);
        Log.d("=-=>", "Proceeeed");
        // Update date
        formatCurrentDate();
        // Update the text on home screen
        String username = getData("currentUser");
        TextView welcomeUser = findViewById(R.id.text_welcome_user2);
        welcomeUser.setText("Hello, " + username);


        // get current calendar ID
        String selectedCalendar = getData("selectedCalendar");

        for(String calID: CalEvents.calendarsList.keySet()) {
            HashMap<String, String> cal = CalEvents.calendarsList.get(calID);
            String name = cal.get("name");

            if(name.equals(selectedCalendar)) {
                calendarID = Integer.parseInt(calID);
            }
        }

        Log.d("123",calendarID + " " + selectedCalendar);

        long startTime = Cal.getDayStartTimestampInMilli(selectedYear, selectedMonth, selectedDay);
        long endTime = Cal.getDayEndTimestampInMilli(selectedYear, selectedMonth, selectedDay);

        try {
            setEvents(Cal.fetchCalEvents(calendarID,startTime,endTime));
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
        if(id == 0){

            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rView);
            adapter = new PlaceEvent(events);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        }
        else if(id == 1){
            adapter.notifyDataSetChanged();
        }
    }

    public void onClickDate(View V){

        dpd = new DatePickerDialog(HomeScreen.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                selectedYear = year;
                selectedMonth = month;
                selectedDay = day;

                long startTime = Cal.getDayStartTimestampInMilli(selectedYear, selectedMonth, selectedDay);
                long endTime = Cal.getDayEndTimestampInMilli(selectedYear, selectedMonth, selectedDay);

//                setEvents(Cal.fetchCalEvents(calendarID,startTime,endTime));
                try {
                    PlaceEvent.event_data = Cal.fetchCalEvents(calendarID,startTime,endTime);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // Update date
//                formatCurrentDate();
//                adapter.notifyItemChanged();
//                adapter.notifyDataSetChanged();
                  mainLayout(0);


            }
        }, selectedYear, selectedMonth, selectedDay);

        dpd.show();
    }

    private void formatCurrentDate() {
        TextView currentDate = findViewById(R.id.current_date);
        Calendar calendar = Calendar.getInstance();
        calendar.set(selectedYear, selectedMonth, selectedDay);
        Date date = calendar.getTime();
        String formattedDate = new SimpleDateFormat(DATE_FORMAT).format(date);
        currentDate.setText(formattedDate.toString());
    }

    public void addData(View v) {

        Spinner spinnerCalList = (Spinner)findViewById(R.id.spinner_calendar);
        String calValue = spinnerCalList.getSelectedItem().toString();

        dbManager.update("selectedCalendar", calValue);

        EditText inputUserName = findViewById(R.id.input_user_name);
        String username = inputUserName.getText().toString();
        dbManager.update("currentUser", username);

        Log.d("<>", "Inserting data calValue " + calValue);
        Log.d("<>", "Inserting data InputUserName " + username);

        Intent i = new Intent(this,HomeScreen.class);
        startActivity(i);
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
                dbManager.insert("currentUser", "");
            }

        }
        return keyValue;
    }

    public void goHome(View V){
        Log.d("*going to home", "");

        mainLayout(0);
//        setContentView(R.layout.layout_home);

    }
    public void goProfile(View V){
        setContentView(R.layout.layout_profile);

        String username = getData("currentUser");
        TextView welcomeUserOnProfileView = findViewById(R.id.text_user_name);
        welcomeUserOnProfileView.setText(username);

        String connectedGmail = getData("selectedCalendar");
        TextView selectedMail = findViewById(R.id.text_connected_gmail);
        selectedMail.setText(connectedGmail);
        Log.d("*going to profile", "");
        if(LocationService.isGPSEnabled()) {
            TextView currentLoc = findViewById(R.id.currentLocation);
            currentLoc.setText(locS.getCurrentLocation());
        }
    }

    public String getData(String param) {
        String result = "blank";
        // move this to a RecyclerView
        Cursor c = dbManager.fetch();
        if (c != null) {
            c.moveToFirst();

            while(true) {
                Log.d("=> comp :", c.getString(0) + " - " + param + " = " + c.getString(0).equals(param));
                if(c.getString(1).equals(param)) {
                    return c.getString(2);
                }

                if(c.isLast()) {
                    break;
                } else {
                    c.moveToNext();
                }
            }
        }

        return result;

    }

    public void destroySession(View v) {

        // Clean database
        dbManager.delete("selectedCalendar");
        dbManager.delete("currentUser");

        //
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void changeDateToToday(View v) throws JSONException {
        selectedDay = c.get(Calendar.DAY_OF_MONTH);
        selectedMonth = c.get(Calendar.MONTH);
        selectedYear = c.get(Calendar.YEAR);

        long startTime = Cal.getDayStartTimestampInMilli(selectedYear, selectedMonth, selectedDay);
        long endTime = Cal.getDayEndTimestampInMilli(selectedYear, selectedMonth, selectedDay);

        PlaceEvent.event_data = Cal.fetchCalEvents(calendarID,startTime,endTime);
        adapter.notifyDataSetChanged();

        // Update date
        formatCurrentDate();
    }



}