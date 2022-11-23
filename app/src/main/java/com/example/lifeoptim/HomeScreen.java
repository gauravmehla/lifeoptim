package com.example.lifeoptim;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbManager = new DBManager(this);
        dbManager.open();

        // Default values in the database

        dbManager.insert("selectedCalendar", "");

        getData();

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

    public void addData(View v) {

        Spinner spinnerCalList = (Spinner)findViewById(R.id.spinner_calendar_list);

        String value = spinnerCalList.getSelectedItem().toString();

        Log.d("<>", "Inserting data " + value);

        dbManager.update("selectedCalendar", value);
        //dbManager.delete("selectedCalendar");
    }

    // Currently implementing
    public String getData() {
        String result = "";
        Cursor c = dbManager.fetch();
        if (c != null) {
            c.moveToFirst();
            while(true) {
                if(
                        c.getString(1).equals("selectedCalendar") &&
                        !c.getString(2).equals("")
                ) {
                    // Present
                    result = c.getString(2);
                } else {
                    // Absent
                    dbManager.insert("selectedCalendar", "");
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
}