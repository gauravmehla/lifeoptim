package com.example.lifeoptim;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;

public class HomeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Spinner spinnerCalList = (Spinner)findViewById(R.id.spinner_calendar_list);

        ArrayList<String> personName = new ArrayList<String>();


        for(String calID: CalEvents.calendarsList.keySet()) {
            HashMap<String, String> cal = CalEvents.calendarsList.get(calID);
            String accountName = cal.get("accountName");

            if(!personName.contains(accountName)) {
                personName.add(accountName);
            }
        }

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, personName);
        spinnerCalList.setAdapter(arrayAdapter);

    }
}