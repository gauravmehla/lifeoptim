package com.example.lifeoptim;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.BiometricPrompt;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import org.json.JSONException;

import java.security.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    public static ArrayList<String> nameOfEvent = new ArrayList<String>();
    public static ArrayList<String> startDates = new ArrayList<String>();
    public static ArrayList<String> endDates = new ArrayList<String>();
    public static ArrayList<String> descriptions = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (
            ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR, Manifest.permission.ACCESS_FINE_LOCATION }, 1234);
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();

        }
        else if(
            ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {

            /*
            * Calendar Integration
             */
            CalEvents calendars = new CalEvents(this);
            calendars.showCalendarIDs();

            @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            df.setTimeZone(TimeZone.getTimeZone("America/Vancouver"));
            try {
                String day = "2022-11-20";
                Long stTimestamp = df.parse(day + "T00:00:00.000").getTime();
                Long endTimestamp = df.parse(day + "T23:59:59.999").getTime();

                Log.d("->", stTimestamp + " - " + endTimestamp);

                calendars.fetchCalEvents(17, stTimestamp, endTimestamp);

            } catch (ParseException e) {
                e.printStackTrace();
            }

            //calendars.showCalendarIDs();
            //calendars.fetchCalEvents(14);
            //getCalendarEvents(this);
        }

        /*
        * Weather Integration
         */
        try {
            WeatherAPI.get();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /*
        * Location Integration
         */
        LocationService locS = new LocationService(this);

        if(LocationService.isGPSEnabled()) {
            Log.d(">>", "GPS is ON");
            locS.getCurrentLocation();
        } else {

            LocationService.turnOnGPS();
            Log.d(">>", "GPS is OFF");
        }

    }

    public void goTo(View view) {
        Intent intent = new Intent(this, HomeScreen.class);
        startActivity(intent);
    }
}