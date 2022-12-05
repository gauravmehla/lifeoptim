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
import android.widget.Toast;

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

    // Database Service
    private DBManager dbManager;

    // Calendar & Events Service
    CalEvents Cal;
    PlaceEvent adapter;

    private ArrayList<ArrayList<String>> events = new ArrayList<ArrayList<String>>();

    public ArrayList<ArrayList<String>> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<ArrayList<String>> events) {
        this.events.clear();
        this.events = events;
    }

    // Calendar Pop-Up Service
    int calendarID = 0;

    public static Calendar c;
    public static DatePickerDialog dpd;

    public static int selectedDay;
    public static int selectedMonth;
    public static int selectedYear;


    // Location Service
    LocationService locS;

    // @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create Database Service Instance
        dbManager = new DBManager(this);
        dbManager.open();

        // Create Calendar Service Instance
        Cal = new CalEvents(this);

        // Pop-up Calendar Instance
        c = Calendar.getInstance();
        selectedDay = c.get(Calendar.DAY_OF_MONTH);
        selectedMonth = c.get(Calendar.MONTH);
        selectedYear = c.get(Calendar.YEAR);

        // Create Location Service Instance
        locS = new LocationService(this);

        // Clean the database
        // dbManager.delete("selectedCalendar");
        // dbManager.delete("currentUser");

        if(initializeDatabase().equals("")) {
            // First time user
            setContentView(R.layout.layout_about_us);


            Spinner spinnerCalList = (Spinner)findViewById(R.id.spinner_calendar);

            // Prepare a list of available calendars on the device
            ArrayList<String> calendarsList = new ArrayList<String>();

            for(String calID: CalEvents.calendarsList.keySet()) {
                HashMap<String, String> cal = CalEvents.calendarsList.get(calID);
                String accountName = cal.get("accountName");
                if(!calendarsList.contains(accountName)) {
                    calendarsList.add(accountName);
                }
            }

            // Show the available calendar list on the view
            ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, calendarsList);
            spinnerCalList.setAdapter(arrayAdapter);
        } else {
            // Already has account; show events to the user
            showEventsOnHome(0);
        }
    }

    public void showEventsOnHome(int id){
        // First, set the layout
        setContentView(R.layout.layout_home);

        // Update date on the home screen
        formatCurrentDate();

        // Update the welcome text on home screen
        String username = getData("currentUser");   // fetch current username
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

        // Start and End timestamp of the day in milliseconds
        long startTime = Cal.getDayStartTimestampInMilli(selectedYear, selectedMonth, selectedDay);
        long endTime = Cal.getDayEndTimestampInMilli(selectedYear, selectedMonth, selectedDay);

        try {
            setEvents(Cal.fetchCalEvents(calendarID,startTime,endTime));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rView);
        adapter = new PlaceEvent(events);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    // Pop-up Calendar
    public void showPopUpCalendar(View V){
        dpd = new DatePickerDialog(HomeScreen.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                selectedYear = year;
                selectedMonth = month;
                selectedDay = day;

                long startTime = Cal.getDayStartTimestampInMilli(selectedYear, selectedMonth, selectedDay);
                long endTime = Cal.getDayEndTimestampInMilli(selectedYear, selectedMonth, selectedDay);

                // Update the events on the home screen after date change
                try {
                    PlaceEvent.event_data = Cal.fetchCalEvents(calendarID,startTime,endTime);
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Update date on the home screen
                formatCurrentDate();
            }
        }, selectedYear, selectedMonth, selectedDay);

        // Show the pop-up calendar after click
        dpd.show();
    }

    private void formatCurrentDate() {
        TextView currentDate = findViewById(R.id.current_date);
        Calendar calendar = Calendar.getInstance();
        calendar.set(selectedYear, selectedMonth, selectedDay);
        Date date = calendar.getTime();
        String formattedDate = new SimpleDateFormat(Constants.HOME_SCREEN_DATE_FORMAT).format(date);
        currentDate.setText(formattedDate.toString());
    }

    public void addData(View v) {
        EditText inputUserName = findViewById(R.id.input_user_name);
        String username = inputUserName.getText().toString();
        if(username.equalsIgnoreCase("")) {
            Toast.makeText(getApplicationContext(), "Please enter the username.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add user data to the database
        Spinner spinnerCalList = (Spinner)findViewById(R.id.spinner_calendar);
        String calValue = spinnerCalList.getSelectedItem().toString();
        dbManager.update("selectedCalendar", calValue);
        dbManager.update("currentUser", username);

        // Render HomeScreen again ::
        Intent i = new Intent(this,HomeScreen.class);
        startActivity(i);
    }

    // Make sure the database has the default values all the time
    public String initializeDatabase() {
        Cursor c = dbManager.fetch();
        boolean isKeyPresent = false;
        String keyValue = "";

        if (c != null) {
            c.moveToFirst();
            while(true) {
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
                // Adding default value
                dbManager.insert("selectedCalendar", "");
                dbManager.insert("currentUser", "");
            }
        }
        return keyValue;
    }

    public void goHome(View V) throws JSONException {
        // Set the layout
        showEventsOnHome(1);
    }
    public void goProfile(View V){
        // Set the layout
        setContentView(R.layout.layout_profile);

        // Show username on the profile page
        String username = getData("currentUser");
        TextView welcomeUserOnProfileView = findViewById(R.id.text_user_name);
        welcomeUserOnProfileView.setText(username);

        // Show the connected calendar ID on the profile page
        String connectedGmail = getData("selectedCalendar");
        TextView selectedMail = findViewById(R.id.text_connected_gmail);
        selectedMail.setText(connectedGmail);

        // Show the location on home screen
        if(LocationService.isGPSEnabled()) {
            TextView currentLoc = findViewById(R.id.currentLocation);
            currentLoc.setText(locS.getCurrentLocation());
        }
    }

    // Fetch data from the database
    public String getData(String param) {
        String result = "";

        Cursor c = dbManager.fetch();
        if (c != null) {
            c.moveToFirst();

            while(true) {
                // Check if the parameter presents in the database
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

        // Reset all activities
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // Update events to today
    public void changeDateToToday() throws JSONException {
        selectedDay = c.get(Calendar.DAY_OF_MONTH);
        selectedMonth = c.get(Calendar.MONTH);
        selectedYear = c.get(Calendar.YEAR);

        long startTime = Cal.getDayStartTimestampInMilli(selectedYear, selectedMonth, selectedDay);
        long endTime = Cal.getDayEndTimestampInMilli(selectedYear, selectedMonth, selectedDay);

        // Update today's event on the home screen
        PlaceEvent.event_data = Cal.fetchCalEvents(calendarID,startTime,endTime);
        adapter.notifyDataSetChanged();

        // Update date
        formatCurrentDate();
    }
}