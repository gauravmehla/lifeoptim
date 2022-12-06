package com.example.lifeoptim;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class CalEvents {
    static Context context;
    private static String API_KEY = "6895783046148ae1feff4b96c4772b62caae6842358fee51d0f60c774e60bc94";
    public static String DATE_FORMAT = "hh:mm a";
    static HashMap<String, HashMap> calendarsList=new HashMap<String, HashMap>();
    static HashMap<String, String> weatherSuggestion = new HashMap<String, String>();


    CalEvents(Context c) {
        this.context = c;
        fetchCalendars();


        // Default values for suggestion
        weatherSuggestion.put("Possible Drizzle", "It's advised to carry umbrella before leaving because there is possibility of drizzling.");
        weatherSuggestion.put("Overcast", "It's advised to take winter jacket before leaving.");
        weatherSuggestion.put("Rainy", "It's advised to carry umbrella before leaving because there are chances of rain.");
        weatherSuggestion.put("Snow", "Remember to carry winter jacket and gloves. There will be snow outside.");
        weatherSuggestion.put("Possible Light Rain", "Possibility of light rain. Remember to carry umbrella.");
        weatherSuggestion.put("PartlyCloudy", "It's gonna be partly cloudy.");
        weatherSuggestion.put("MostlyCloudy", "It's gonna be mostly cloudy.");
        weatherSuggestion.put("High Winds", "High winds. Remember to take a jacket.");
    }

    public void fetchCalendars() {
        ContentResolver contentResolver = this.context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        String[] projection =
                new String[]{
                        CalendarContract.Calendars._ID,
                        CalendarContract.Calendars.NAME,
                        CalendarContract.Calendars.ACCOUNT_NAME,
                        CalendarContract.Calendars.ACCOUNT_TYPE};
        Cursor calCursor =
                this.context.getContentResolver().
                        query(CalendarContract.Calendars.CONTENT_URI,
                                projection,
                                CalendarContract.Calendars.VISIBLE + " = 1",
                                null,
                                CalendarContract.Calendars._ID + " ASC");

        if (calCursor.moveToFirst()) {
            do {
                HashMap<String, String> calendar=new HashMap<String, String>();
                calendar.put("_id", calCursor.getString(0) );
                calendar.put("name", calCursor.getString(1) );
                calendar.put("accountName", calCursor.getString(2) );
                calendar.put("accountType", calCursor.getString(3) );
                calendarsList.put(calCursor.getString(0), calendar);

            } while (calCursor.moveToNext());
        }
    }

    public ArrayList<ArrayList<String>> fetchCalEvents(int calID, long stTime, long endTime) throws JSONException {
        ArrayList<ArrayList<String>> events = new ArrayList<ArrayList<String>>();
        ArrayList<String> events_title = new ArrayList<String>();
        ArrayList<String> events_start = new ArrayList<String>();
        ArrayList<String> events_end = new ArrayList<String>();
        ArrayList<String> events_loc = new ArrayList<String>();
        ArrayList<String> events_suggestion = new ArrayList<String>();

        Cursor cursor = this.context.getContentResolver()
                .query(
                        Uri.parse("content://com.android.calendar/events"),
                        new String[] {
                                "calendar_id",
                                "title",
                                "description",
                                "dtstart",
                                "dtend",
                                "eventLocation",
                                "duration"
                        }, null,null, CalendarContract.Events.DTSTART + " ASC");
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            // Code Break if no events
            if(
                    cursor.getString(3) != null &&
                    cursor.getString(4) != null &&
                    cursor.getString(0).equalsIgnoreCase(calID+"") &&
                    Long.parseLong( cursor.getString(3) ) > stTime &&
                    Long.parseLong( cursor.getString(4) ) < endTime
            ) {
                events_title.add(cursor.getString(1));
                events_start.add(getDate(cursor.getLong(3), this.DATE_FORMAT));
                events_end.add(getDate(cursor.getLong(4), this.DATE_FORMAT));
                events_loc.add(cursor.getString(5));
                events_suggestion.add(suggestions(cursor.getString(5), cursor.getLong(3)));
            }
            cursor.moveToNext();
        }

        events.add(events_title);
        events.add(events_loc);
        events.add(events_start);
        events.add(events_end);
        events.add(events_suggestion);

        return events;
    }

    public static void showCalendarIDs() {
        for(String calID: calendarsList.keySet()) {
            HashMap<String, String> cal = calendarsList.get(calID);

            int id = Integer.parseInt(cal.get("_id"));
            String displayName = cal.get("name");
            String accountName = cal.get("accountName");
            String accountType = cal.get("accountType");

            Log.d("-> id", id + "-" + displayName + "-" + accountName + "-" + accountType);

        }
    }

    public static String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public long getDayStartTimestampInMilli(int year, int month, int day) {
        Calendar tempCal = Calendar.getInstance();
        tempCal.set(year, month, day,
                00, 00, 00);
        return tempCal.getTimeInMillis();
    }

    public long getDayEndTimestampInMilli(int year, int month, int day) {
        Calendar tempCal = Calendar.getInstance();
        tempCal.set(year, month, day,
                23, 59, 59);
        return tempCal.getTimeInMillis();
    }

    public String suggestions(String location, long startTime) throws JSONException{
        String suggestion="";

        LocationService Loc = new LocationService(context.getApplicationContext());
        String[] coordinates;
        if(location.length() > 0) {
            coordinates = Loc.getCoordinates(location);
        } else {
            coordinates = new String[]{"0.0","0.0"};
        }

        JSONArray weatherHourlyData  = WeatherAPI.get(coordinates[0],coordinates[1]);

        /*
        * For now, we are selecting the weather using a random function. We can comment this block
        * as soon as the weather API is up.
         */
        for (int x = 1; x < weatherHourlyData.length(); x++) {
            final int random = new Random().nextInt(48);
            if(random%2 == 0) {
                suggestion = weatherHourlyData.getJSONObject(random).getString("summary");
            }
            break;
        }

        return weatherSuggestion.getOrDefault(suggestion, "null");
    }

}
