package com.example.lifeoptim;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
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
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

//import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    androidx.biometric.BiometricPrompt biometricPrompt;
    BiometricPrompt.PromptInfo promptInfo;
    LinearLayout mMainLayout;

    public static ArrayList<String> nameOfEvent = new ArrayList<String>();
    public static ArrayList<String> startDates = new ArrayList<String>();
    public static ArrayList<String> endDates = new ArrayList<String>();
    public static ArrayList<String> descriptions = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_event);

        // Make sure we have permissions
        if(permissionCheck()) {

            // Ask for biometric
            biometricPrompt();

            /*
             * Calendar Integration
             */
//            CalEvents calendars = new CalEvents(this);
//            calendars.showCalendarIDs();
//
//            @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//            df.setTimeZone(TimeZone.getTimeZone("America/Vancouver"));
//            try {
//                String day = "2022-11-20";
//                Long stTimestamp = df.parse(day + "T00:00:00.000").getTime();
//                Long endTimestamp = df.parse(day + "T23:59:59.999").getTime();
//
//                Log.d("->", stTimestamp + " - " + endTimestamp);
//
//                calendars.fetchCalEvents(17, stTimestamp, endTimestamp);
//
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }

            //calendars.showCalendarIDs();
            //calendars.fetchCalEvents(14);
            //getCalendarEvents(this);

            /*
             * Weather Integration
             */
//            try {
//                WeatherAPI.get();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }

            /*
             * Location Integration
             */
//            LocationService locS = new LocationService(this);
//
//            if(LocationService.isGPSEnabled()) {
//                Log.d(">>", "GPS is ON");
//                locS.getCurrentLocation();
//            } else {
//
//                LocationService.turnOnGPS();
//                Log.d(">>", "GPS is OFF");
//            }

        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean permissionCheck() {

        ActivityCompat.requestPermissions(MainActivity.this, new String[] {
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR,
                Manifest.permission.ACCESS_FINE_LOCATION
        }, 1234);


        if(
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            return true;
        } else {
            return false;
        }
    }

    public void quitApplication() {
        this.finishAffinity();
    }

    public void biometricPrompt() {
        mMainLayout = findViewById(R.id.main_layout);

        Executor executor = ContextCompat.getMainExecutor(this);

        biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);

                Toast.makeText(getApplicationContext(),"Login Failed",Toast.LENGTH_SHORT).show();

                quitApplication();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(),"Login Success",Toast.LENGTH_SHORT).show();


                // Proceed to Home Activity
                goTo();

            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        promptInfo= new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Life Optim")
                .setDescription("Use FingerPrint to Login")
                .setDeviceCredentialAllowed(true)
                .build();


        Log.d(">>", "Asking biometric");
        biometricPrompt.authenticate(promptInfo);
    }

    public void goTo() {
        Intent intent = new Intent(this, HomeScreen.class);
        startActivity(intent);
    }
}