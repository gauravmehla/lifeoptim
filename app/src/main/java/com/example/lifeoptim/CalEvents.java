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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class CalEvents {
    static Context context;
    private static String API_KEY = "6895783046148ae1feff4b96c4772b62caae6842358fee51d0f60c774e60bc94";
    public static String DATE_FORMAT = "dd/MM/yyyy hh:mm:ss.SSS";
    static HashMap<String, HashMap> calendarsList=new HashMap<String, HashMap>();
    static HashMap<String, String> weatherSuggestion = new HashMap<String, String>();


    CalEvents(Context c) {
        this.context = c;
        fetchCalendars();
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

        Log.d("Yoho cursor count - ", cursor.getCount() + "");

        Log.d("-> st timestamp", getDate(stTime, "dd/MM/yyyy hh:mm:ss.SSS"));
        Log.d("-> end timestamp", getDate(endTime, "dd/MM/yyyy hh:mm:ss.SSS"));

        for (int i = 0; i < cursor.getCount(); i++) {
            // Code Break if no events
            if(
                    cursor.getString(3) != null &&
                    cursor.getString(4) != null &&
                    cursor.getString(0).equalsIgnoreCase(calID+"") &&
                    Long.parseLong( cursor.getString(3) ) > stTime &&
                    Long.parseLong( cursor.getString(4) ) < endTime
            ) {
                Log.d("-> here", " 0 " + cursor.getString(0));
                Log.d("-> here", " 1 " + cursor.getString(1));
                Log.d("-> here", " 2 " + cursor.getString(2));

                Log.d("-> here", " 3 " + getDate(cursor.getLong(3), this.DATE_FORMAT));
                Log.d("-> here", " 4 " + getDate(cursor.getLong(4), this.DATE_FORMAT));
                Log.d("-> here", " 5 " + cursor.getString(5));

                events_title.add(cursor.getString(1));
                events_start.add(getDate(cursor.getLong(3), this.DATE_FORMAT));
                events_end.add(getDate(cursor.getLong(4), this.DATE_FORMAT));
                events_loc.add(cursor.getString(5));
            }
            cursor.moveToNext();
        }

        Log.d("Yoho events_title count - ", events_title.size() + "");
        Log.d("Yoho events_loc count - ", events_loc.size() + "");
        Log.d("Yoho events_start count - ", events_start.size() + "");
        Log.d("Yoho events_end count - ", events_end.size() + "");

        events.add(events_title);
        events.add(events_loc);
        events.add(events_start);
        events.add(events_end);
        events.add(suggestions(events_loc));

        return events;
    }

    public static String getCalendarID(String calendar) {
        String calID = "";


        return calID;
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

    public ArrayList<String> suggestions(ArrayList<String> locations) throws JSONException{

        weatherSuggestion.put("Possible Drizzle", "ADVISORY : CARRY UMBRELLA");
        weatherSuggestion.put("Overcast", "ADVISORY : WINTER JACKET");
        weatherSuggestion.put("Rainy", "ALERT : CARRY UMBRELLA");
        weatherSuggestion.put("Snow", "ALERT : WINTER JACKET AND GLOVES");
        weatherSuggestion.put("Possible Light Rain", "ALERT : CARRY UMBRELLA");
        weatherSuggestion.put("High Winds", "ALERT : BRING JACKET");
        String lat = "0.0";
        String lng = "0.0";
        String coordinates[] = new String[2];
        LocationService Loc = new LocationService(context.getApplicationContext());

        int i;
        String temp = "empty";
        String extremeWeathers[] = {"Snow","High Winds","Rainy","Possible Drizzle","Possible Light Rain","Overcast"};
        ArrayList<String> data = new ArrayList<>();
        ArrayList<String> suggestions = new ArrayList<>();


        int flag = 0;
        for(String location : locations) {
            flag =  0;
            coordinates = Loc.getCoordinates(location.split(",")[0]);
            data  = WeatherAPI.get(coordinates[0],coordinates[1]);
//            Log.d("---sug>>",location.split(",")[0] + " " + coordinates[0] + " " + coordinates[1]);
            for(i = 0 ; i < extremeWeathers.length; i++) {
//                Log.d("#Hourly", data.size() + "");
                for (String d : data) {
//                    Log.d("##weather", d + " " + location);
                    if (d.equalsIgnoreCase(extremeWeathers[i])) {
                        Log.d("sugg Extreme Weather", extremeWeathers[i] + " " + weatherSuggestion.get(extremeWeathers[i]));
                        temp = weatherSuggestion.getOrDefault(extremeWeathers[i], "Clear Weather");
                        Log.d("%%", "" + temp);
                        if(flag ==0 ){
                            Log.d("sugg added",temp);
                            suggestions.add(temp);
                            flag = 1;
                            i = extremeWeathers.length - 1;
                        }
//                        break;
                    }
                }
            }
            }
            if(flag == 0){
                suggestions.add("");
            }
//        String str = "{\"time\":1668902400,\"summary\":\"Snow\",\"icon\":\"partly-cloudy-day\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":40.51,\"apparentTemperature\":40.51,\"dewPoint\":34.37,\"humidity\":0.79,\"pressure\":1024.8,\"windSpeed\":2.82,\"windGust\":4.36,\"windBearing\":305,\"cloudCover\":0.83,\"uvIndex\":0,\"visibility\":10,\"ozone\":299},{\"time\":1668906000,\"summary\":\"MostlyCloudy\",\"icon\":\"partly-cloudy-night\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":39.17,\"apparentTemperature\":37.39,\"dewPoint\":33.54,\"humidity\":0.8,\"pressure\":1024.7,\"windSpeed\":3.03,\"windGust\":4.41,\"windBearing\":331,\"cloudCover\":0.82,\"uvIndex\":0,\"visibility\":10,\"ozone\":299.1},{\"time\":1668909600,\"summary\":\"MostlyCloudy\",\"icon\":\"partly-cloudy-night\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":38.55,\"apparentTemperature\":38.55,\"dewPoint\":33.11,\"humidity\":0.81,\"pressure\":1024.8,\"windSpeed\":2.9,\"windGust\":3.56,\"windBearing\":30,\"cloudCover\":0.82,\"uvIndex\":0,\"visibility\":10,\"ozone\":297.8},{\"time\":1668913200,\"summary\":\"MostlyCloudy\",\"icon\":\"partly-cloudy-night\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":37.76,\"apparentTemperature\":35.75,\"dewPoint\":32.84,\"humidity\":0.82,\"pressure\":1024.7,\"windSpeed\":3.07,\"windGust\":3.4,\"windBearing\":37,\"cloudCover\":0.82,\"uvIndex\":0,\"visibility\":10,\"ozone\":297.2},{\"time\":1668916800,\"summary\":\"MostlyCloudy\",\"icon\":\"partly-cloudy-night\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":37.37,\"apparentTemperature\":37.37,\"dewPoint\":32.34,\"humidity\":0.82,\"pressure\":1024.8,\"windSpeed\":2.94,\"windGust\":3.07,\"windBearing\":38,\"cloudCover\":0.85,\"uvIndex\":0,\"visibility\":10,\"ozone\":295.5},{\"time\":1668920400,\"summary\":\"Overcast\",\"icon\":\"cloudy\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":37.15,\"apparentTemperature\":37.15,\"dewPoint\":31.68,\"humidity\":0.8,\"pressure\":1024.9,\"windSpeed\":2.86,\"windGust\":2.89,\"windBearing\":32,\"cloudCover\":0.89,\"uvIndex\":0,\"visibility\":10,\"ozone\":295.5},{\"time\":1668924000,\"summary\":\"Overcast\",\"icon\":\"cloudy\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":36.86,\"apparentTemperature\":36.86,\"dewPoint\":30.86,\"humidity\":0.79,\"pressure\":1025,\"windSpeed\":2.84,\"windGust\":2.9,\"windBearing\":32,\"cloudCover\":0.9,\"uvIndex\":0,\"visibility\":10,\"ozone\":297.9},{\"time\":1668927600,\"summary\":\"Overcast\",\"icon\":\"cloudy\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":36.73,\"apparentTemperature\":36.73,\"dewPoint\":30.6,\"humidity\":0.78,\"pressure\":1024.9,\"windSpeed\":2.58,\"windGust\":2.63,\"windBearing\":52,\"cloudCover\":0.89,\"uvIndex\":0,\"visibility\":10,\"ozone\":298.5},{\"time\":1668931200,\"summary\":\"Overcast\",\"icon\":\"cloudy\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":36.68,\"apparentTemperature\":36.68,\"dewPoint\":30.7,\"humidity\":0.79,\"pressure\":1024.9,\"windSpeed\":2.48,\"windGust\":2.48,\"windBearing\":60,\"cloudCover\":0.92,\"uvIndex\":0,\"visibility\":10,\"ozone\":298.8},{\"time\":1668934800,\"summary\":\"Overcast\",\"icon\":\"cloudy\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":37.01,\"apparentTemperature\":37.01,\"dewPoint\":30.73,\"humidity\":0.78,\"pressure\":1024.7,\"windSpeed\":2.45,\"windGust\":2.53,\"windBearing\":52,\"cloudCover\":0.91,\"uvIndex\":0,\"visibility\":10,\"ozone\":300.8},{\"time\":1668938400,\"summary\":\"Overcast\",\"icon\":\"cloudy\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":37.02,\"apparentTemperature\":37.02,\"dewPoint\":30.54,\"humidity\":0.77,\"pressure\":1024.6,\"windSpeed\":2.83,\"windGust\":2.86,\"windBearing\":53,\"cloudCover\":0.92,\"uvIndex\":0,\"visibility\":10,\"ozone\":302.4},{\"time\":1668942000,\"summary\":\"Overcast\",\"icon\":\"cloudy\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":36.99,\"apparentTemperature\":36.99,\"dewPoint\":29.95,\"humidity\":0.75,\"pressure\":1024.7,\"windSpeed\":2.7,\"windGust\":2.71,\"windBearing\":47,\"cloudCover\":0.9,\"uvIndex\":0,\"visibility\":10,\"ozone\":302.4},{\"time\":1668945600,\"summary\":\"MostlyCloudy\",\"icon\":\"partly-cloudy-night\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":36.77,\"apparentTemperature\":36.77,\"dewPoint\":29.72,\"humidity\":0.75,\"pressure\":1024.7,\"windSpeed\":2.78,\"windGust\":2.78,\"windBearing\":55,\"cloudCover\":0.86,\"uvIndex\":0,\"visibility\":10,\"ozone\":297.9},{\"time\":1668949200,\"summary\":\"MostlyCloudy\",\"icon\":\"partly-cloudy-night\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":36.49,\"apparentTemperature\":34.13,\"dewPoint\":29.65,\"humidity\":0.76,\"pressure\":1024.6,\"windSpeed\":3.21,\"windGust\":3.22,\"windBearing\":61,\"cloudCover\":0.77,\"uvIndex\":0,\"visibility\":10,\"ozone\":295.2},{\"time\":1668952800,\"summary\":\"PartlyCloudy\",\"icon\":\"partly-cloudy-night\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":35.54,\"apparentTemperature\":32.68,\"dewPoint\":29.6,\"humidity\":0.79,\"pressure\":1025.2,\"windSpeed\":3.53,\"windGust\":3.54,\"windBearing\":51,\"cloudCover\":0.57,\"uvIndex\":0,\"visibility\":10,\"ozone\":294.3},{\"time\":1668956400,\"summary\":\"PartlyCloudy\",\"icon\":\"partly-cloudy-night\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":35.47,\"apparentTemperature\":32.52,\"dewPoint\":29.88,\"humidity\":0.8,\"pressure\":1025.5,\"windSpeed\":3.6,\"windGust\":3.66,\"windBearing\":49,\"cloudCover\":0.49,\"uvIndex\":0,\"visibility\":10,\"ozone\":293.7},{\"time\":1668960000,\"summary\":\"MostlyCloudy\",\"icon\":\"partly-cloudy-day\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":35.94,\"apparentTemperature\":33.3,\"dewPoint\":30.19,\"humidity\":0.79,\"pressure\":1025.9,\"windSpeed\":3.39,\"windGust\":3.47,\"windBearing\":52,\"cloudCover\":0.61,\"uvIndex\":0,\"visibility\":10,\"ozone\":292.6},{\"time\":1668963600,\"summary\":\"PartlyCloudy\",\"icon\":\"partly-cloudy-day\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":38.03,\"apparentTemperature\":38.03,\"dewPoint\":31.14,\"humidity\":0.76,\"pressure\":1026.4,\"windSpeed\":2.83,\"windGust\":3.02,\"windBearing\":59,\"cloudCover\":0.55,\"uvIndex\":0,\"visibility\":10,\"ozone\":287.9},{\"time\":1668967200,\"summary\":\"PartlyCloudy\",\"icon\":\"partly-cloudy-day\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":40.82,\"apparentTemperature\":40.82,\"dewPoint\":31.97,\"humidity\":0.71,\"pressure\":1026.5,\"windSpeed\":2.27,\"windGust\":2.51,\"windBearing\":97,\"cloudCover\":0.47,\"uvIndex\":1,\"visibility\":10,\"ozone\":286.2},{\"time\":1668970800,\"summary\":\"PartlyCloudy\",\"icon\":\"partly-cloudy-day\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":44.21,\"apparentTemperature\":44.21,\"dewPoint\":31.64,\"humidity\":0.61,\"pressure\":1026.3,\"windSpeed\":2.25,\"windGust\":2.52,\"windBearing\":106,\"cloudCover\":0.44,\"uvIndex\":1,\"visibility\":10,\"ozone\":282.2},{\"time\":1668974400,\"summary\":\"PartlyCloudy\",\"icon\":\"partly-cloudy-day\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":47.19,\"apparentTemperature\":47.19,\"dewPoint\":31.4,\"humidity\":0.54,\"pressure\":1025.9,\"windSpeed\":2.16,\"windGust\":2.51,\"windBearing\":135,\"cloudCover\":0.46,\"uvIndex\":1,\"visibility\":10,\"ozone\":279.5},{\"time\":1668978000,\"summary\":\"PartlyCloudy\",\"icon\":\"partly-cloudy-day\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":49.42,\"apparentTemperature\":49.42,\"dewPoint\":32.01,\"humidity\":0.51,\"pressure\":1025.5,\"windSpeed\":2.05,\"windGust\":2.55,\"windBearing\":150,\"cloudCover\":0.45,\"uvIndex\":1,\"visibility\":10,\"ozone\":279.4},{\"time\":1668981600,\"summary\":\"PartlyCloudy\",\"icon\":\"partly-cloudy-day\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":49.1,\"apparentTemperature\":49.1,\"dewPoint\":33.31,\"humidity\":0.54,\"pressure\":1025,\"windSpeed\":1.65,\"windGust\":2.23,\"windBearing\":127,\"cloudCover\":0.57,\"uvIndex\":1,\"visibility\":10,\"ozone\":280.7},{\"time\":1668985200,\"summary\":\"MostlyCloudy\",\"icon\":\"partly-cloudy-day\",\"precipIntensity\":0.0017,\"precipProbability\":0.06,\"precipType\":\"rain\",\"temperature\":47.2,\"apparentTemperature\":47.2,\"dewPoint\":34.7,\"humidity\":0.62,\"pressure\":1025.3,\"windSpeed\":1.25,\"windGust\":1.83,\"windBearing\":7,\"cloudCover\":0.67,\"uvIndex\":0,\"visibility\":10,\"ozone\":283.2},{\"time\":1668988800,\"summary\":\"MostlyCloudy\",\"icon\":\"partly-cloudy-day\",\"precipIntensity\":0.0018,\"precipProbability\":0.06,\"precipType\":\"rain\",\"temperature\":44.8,\"apparentTemperature\":44.8,\"dewPoint\":34.69,\"humidity\":0.67,\"pressure\":1025.1,\"windSpeed\":1.44,\"windGust\":2.01,\"windBearing\":326,\"cloudCover\":0.75,\"uvIndex\":0,\"visibility\":10,\"ozone\":284},{\"time\":1668992400,\"summary\":\"Overcast\",\"icon\":\"cloudy\",\"precipIntensity\":0.0012,\"precipProbability\":0.05,\"precipType\":\"rain\",\"temperature\":43.77,\"apparentTemperature\":43.77,\"dewPoint\":34.33,\"humidity\":0.69,\"pressure\":1025,\"windSpeed\":1.78,\"windGust\":2.13,\"windBearing\":351,\"cloudCover\":0.88,\"uvIndex\":0,\"visibility\":10,\"ozone\":283.4},{\"time\":1668996000,\"summary\":\"Overcast\",\"icon\":\"cloudy\",\"precipIntensity\":0.001,\"precipProbability\":0.04,\"precipType\":\"rain\",\"temperature\":42.73,\"apparentTemperature\":42.73,\"dewPoint\":33.99,\"humidity\":0.71,\"pressure\":1025.1,\"windSpeed\":2.35,\"windGust\":2.44,\"windBearing\":14,\"cloudCover\":0.92,\"uvIndex\":0,\"visibility\":10,\"ozone\":283.3},{\"time\":1668999600,\"summary\":\"Overcast\",\"icon\":\"cloudy\",\"precipIntensity\":7.0E-4,\"precipProbability\":0.04,\"precipType\":\"rain\",\"temperature\":42.23,\"apparentTemperature\":42.23,\"dewPoint\":33.89,\"humidity\":0.72,\"pressure\":1025,\"windSpeed\":2.65,\"windGust\":2.65,\"windBearing\":18,\"cloudCover\":0.99,\"uvIndex\":0,\"visibility\":10,\"ozone\":282.6},{\"time\":1669003200,\"summary\":\"Overcast\",\"icon\":\"cloudy\",\"precipIntensity\":5.0E-4,\"precipProbability\":0.04,\"precipType\":\"rain\",\"temperature\":41.88,\"apparentTemperature\":41.88,\"dewPoint\":33.79,\"humidity\":0.73,\"pressure\":1024.9,\"windSpeed\":2.63,\"windGust\":2.63,\"windBearing\":21,\"cloudCover\":0.98,\"uvIndex\":0,\"visibility\":10,\"ozone\":282.6},{\"time\":1669006800,\"summary\":\"Overcast\",\"icon\":\"cloudy\",\"precipIntensity\":4.0E-4,\"precipProbability\":0.03,\"precipType\":\"rain\",\"temperature\":41.97,\"apparentTemperature\":41.97,\"dewPoint\":33.72,\"humidity\":0.72,\"pressure\":1024.8,\"windSpeed\":2.3,\"windGust\":2.3,\"windBearing\":24,\"cloudCover\":1,\"uvIndex\":0,\"visibility\":10,\"ozone\":284.9},{\"time\":1669010400,\"summary\":\"Overcast\",\"icon\":\"cloudy\",\"precipIntensity\":7.0E-4,\"precipProbability\":0.05,\"precipType\":\"rain\",\"temperature\":41.8,\"apparentTemperature\":41.8,\"dewPoint\":34.21,\"humidity\":0.74,\"pressure\":1025.4,\"windSpeed\":2.43,\"windGust\":2.58,\"windBearing\":16,\"cloudCover\":1,\"uvIndex\":0,\"visibility\":10,\"ozone\":284.8},{\"time\":1669014000,\"summary\":\"Overcast\",\"icon\":\"cloudy\",\"precipIntensity\":0.0021,\"precipProbability\":0.08,\"precipType\":\"rain\",\"temperature\":41.58,\"apparentTemperature\":41.58,\"dewPoint\":34.56,\"humidity\":0.76,\"pressure\":1025.6,\"windSpeed\":2.65,\"windGust\":3.13,\"windBearing\":16,\"cloudCover\":1,\"uvIndex\":0,\"visibility\":10,\"ozone\":285.9},{\"time\":1669017600,\"summary\":\"Overcast\",\"icon\":\"cloudy\",\"precipIntensity\":0.002,\"precipProbability\":0.08,\"precipType\":\"rain\",\"temperature\":41.46,\"apparentTemperature\":41.46,\"dewPoint\":35.08,\"humidity\":0.78,\"pressure\":1025.8,\"windSpeed\":2.74,\"windGust\":3.63,\"windBearing\":18,\"cloudCover\":1,\"uvIndex\":0,\"visibility\":10,\"ozone\":287.3},{\"time\":1669021200,\"summary\":\"Overcast\",\"icon\":\"cloudy\",\"precipIntensity\":0.002,\"precipProbability\":0.08,\"precipType\":\"rain\",\"temperature\":41.47,\"apparentTemperature\":41.47,\"dewPoint\":35.72,\"humidity\":0.8,\"pressure\":1025.8,\"windSpeed\":2.95,\"windGust\":4.26,\"windBearing\":27,\"cloudCover\":1,\"uvIndex\":0,\"visibility\":10,\"ozone\":287.6},{\"time\":1669024800,\"summary\":\"Overcast\",\"icon\":\"cloudy\",\"precipIntensity\":0.0015,\"precipProbability\":0.08,\"precipType\":\"rain\",\"temperature\":41.34,\"apparentTemperature\":39.55,\"dewPoint\":35.99,\"humidity\":0.81,\"pressure\":1025.8,\"windSpeed\":3.31,\"windGust\":5.01,\"windBearing\":13,\"cloudCover\":1,\"uvIndex\":0,\"visibility\":10,\"ozone\":288.1},{\"time\":1669028400,\"summary\":\"Overcast\",\"icon\":\"cloudy\",\"precipIntensity\":0.0017,\"precipProbability\":0.08,\"precipType\":\"rain\",\"temperature\":41.34,\"apparentTemperature\":39.09,\"dewPoint\":36.26,\"humidity\":0.82,\"pressure\":1025.6,\"windSpeed\":3.76,\"windGust\":5.86,\"windBearing\":20,\"cloudCover\":0.99,\"uvIndex\":0,\"visibility\":10,\"ozone\":287.5},{\"time\":1669032000,\"summary\":\"Overcast\",\"icon\":\"cloudy\",\"precipIntensity\":0.0024,\"precipProbability\":0.09,\"precipType\":\"rain\",\"temperature\":40.97,\"apparentTemperature\":38.79,\"dewPoint\":36.76,\"humidity\":0.85,\"pressure\":1025.3,\"windSpeed\":3.63,\"windGust\":5.47,\"windBearing\":29,\"cloudCover\":0.98,\"uvIndex\":0,\"visibility\":10,\"ozone\":286.5},{\"time\":1669035600,\"summary\":\"Overcast\",\"icon\":\"cloudy\",\"precipIntensity\":0.0029,\"precipProbability\":0.09,\"precipType\":\"rain\",\"temperature\":40.46,\"apparentTemperature\":38.14,\"dewPoint\":36.81,\"humidity\":0.87,\"pressure\":1025.2,\"windSpeed\":3.7,\"windGust\":5.77,\"windBearing\":34,\"cloudCover\":0.93,\"uvIndex\":0,\"visibility\":10,\"ozone\":286.7},{\"time\":1669039200,\"summary\":\"Overcast\",\"icon\":\"cloudy\",\"precipIntensity\":0.0035,\"precipProbability\":0.09,\"precipType\":\"rain\",\"temperature\":40.36,\"apparentTemperature\":38.13,\"dewPoint\":37.22,\"humidity\":0.88,\"pressure\":1025.1,\"windSpeed\":3.59,\"windGust\":5.88,\"windBearing\":42,\"cloudCover\":0.89,\"uvIndex\":0,\"visibility\":10,\"ozone\":288.2},{\"time\":1669042800,\"summary\":\"MostlyCloudy\",\"icon\":\"partly-cloudy-night\",\"precipIntensity\":0.004,\"precipProbability\":0.1,\"precipType\":\"rain\",\"temperature\":40.27,\"apparentTemperature\":37.98,\"dewPoint\":37.72,\"humidity\":0.91,\"pressure\":1025,\"windSpeed\":3.63,\"windGust\":6.18,\"windBearing\":42,\"cloudCover\":0.78,\"uvIndex\":0,\"visibility\":10,\"ozone\":289.6},{\"time\":1669046400,\"summary\":\"MostlyCloudy\",\"icon\":\"partly-cloudy-day\",\"precipIntensity\":0.0046,\"precipProbability\":0.11,\"precipType\":\"rain\",\"temperature\":40.5,\"apparentTemperature\":38.17,\"dewPoint\":38.01,\"humidity\":0.91,\"pressure\":1025.2,\"windSpeed\":3.71,\"windGust\":6.55,\"windBearing\":43,\"cloudCover\":0.76,\"uvIndex\":0,\"visibility\":10,\"ozone\":290.1},{\"time\":1669050000,\"summary\":\"MostlyCloudy\",\"icon\":\"partly-cloudy-day\",\"precipIntensity\":0.005,\"precipProbability\":0.11,\"precipType\":\"rain\",\"temperature\":41.36,\"apparentTemperature\":39.29,\"dewPoint\":38.28,\"humidity\":0.89,\"pressure\":1025.4,\"windSpeed\":3.58,\"windGust\":6.64,\"windBearing\":39,\"cloudCover\":0.73,\"uvIndex\":0,\"visibility\":10,\"ozone\":289.9},{\"time\":1669053600,\"summary\":\"MostlyCloudy\",\"icon\":\"partly-cloudy-day\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":43.11,\"apparentTemperature\":43.11,\"dewPoint\":38.19,\"humidity\":0.83,\"pressure\":1025.2,\"windSpeed\":2.44,\"windGust\":2.89,\"windBearing\":18,\"cloudCover\":0.74,\"uvIndex\":1,\"visibility\":10,\"ozone\":289.4},{\"time\":1669057200,\"summary\":\"MostlyCloudy\",\"icon\":\"partly-cloudy-day\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":45.34,\"apparentTemperature\":45.34,\"dewPoint\":38.14,\"humidity\":0.76,\"pressure\":1025.1,\"windSpeed\":2.28,\"windGust\":2.96,\"windBearing\":56,\"cloudCover\":0.75,\"uvIndex\":1,\"visibility\":10,\"ozone\":288.6},{\"time\":1669060800,\"summary\":\"MostlyCloudy\",\"icon\":\"partly-cloudy-day\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":47.96,\"apparentTemperature\":47.96,\"dewPoint\":37.79,\"humidity\":0.68,\"pressure\":1024.8,\"windSpeed\":2.36,\"windGust\":3.29,\"windBearing\":80,\"cloudCover\":0.8,\"uvIndex\":1,\"visibility\":10,\"ozone\":286.3},{\"time\":1669064400,\"summary\":\"MostlyCloudy\",\"icon\":\"partly-cloudy-day\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":50.12,\"apparentTemperature\":50.12,\"dewPoint\":38.11,\"humidity\":0.63,\"pressure\":1024.4,\"windSpeed\":2.51,\"windGust\":3.34,\"windBearing\":92,\"cloudCover\":0.8,\"uvIndex\":1,\"visibility\":10,\"ozone\":285.2},{\"time\":1669068000,\"summary\":\"MostlyCloudy\",\"icon\":\"partly-cloudy-day\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":50.46,\"apparentTemperature\":50.46,\"dewPoint\":38.65,\"humidity\":0.64,\"pressure\":1024,\"windSpeed\":2.61,\"windGust\":3.37,\"windBearing\":65,\"cloudCover\":0.79,\"uvIndex\":1,\"visibility\":10,\"ozone\":284},{\"time\":1669071600,\"summary\":\"MostlyCloudy\",\"icon\":\"partly-cloudy-day\",\"precipIntensity\":0,\"precipProbability\":0,\"temperature\":49.25,\"apparentTemperature\":49.25,\"dewPoint\":39.39,\"humidity\":0.69,\"pressure\":1023.6,\"windSpeed\":2.51,\"windGust\":3.55,\"windBearing\":32,\"cloudCover\":0.82,\"uvIndex\":0,\"visibility\":10,\"ozone\":284.7},{\"time\":1669075200,\"summary\":\"MostlyCloudy\",\"icon\":\"partly-cloudy-day\",\"precipIntensity\":0.0017,\"precipProbability\":0.09,\"precipType\":\"rain\",\"temperature\":46.62,\"apparentTemperature\":45.72,\"dewPoint\":39.55,\"humidity\":0.76,\"pressure\":1023.8,\"windSpeed\":3.13,\"windGust\":5.1,\"windBearing\":27,\"cloudCover\":0.85,\"uvIndex\":0,\"visibility\":10,\"ozone\":285.7}]";

        return suggestions;
    }

}
