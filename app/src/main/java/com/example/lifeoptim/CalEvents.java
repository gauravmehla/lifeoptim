package com.example.lifeoptim;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class CalEvents {
    static Context context;
    static String DATE_FORMAT = "dd/MM/yyyy hh:mm:ss.SSS";
    static HashMap<String, HashMap> calendarsList=new HashMap<String, HashMap>();

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

    public void fetchCalEvents(int calID, long stTime, long endTime) {
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

        String eventsList[] = new String[cursor.getCount()];


        Log.d("-> st timestamp", getDate(stTime, "dd/MM/yyyy hh:mm:ss.SSS"));
        Log.d("-> end timestamp", getDate(endTime, "dd/MM/yyyy hh:mm:ss.SSS"));

        for (int i = 0; i < eventsList.length; i++) {

            if(
                    cursor.getString(3) != null &&
                    cursor.getString(4) != null &&
                    cursor.getString(0).equalsIgnoreCase(calID+"") &&
                    Long.parseLong( cursor.getString(3) ) > stTime &&
                    Long.parseLong( cursor.getString(4) ) < endTime
            ) {
                Log.d("-> here", " 0" + cursor.getString(0));
                Log.d("-> here", " 1" + cursor.getString(1));
                Log.d("-> here", " 2" + cursor.getString(2));

                Log.d("-> here", " 3" + getDate(cursor.getLong(3), this.DATE_FORMAT));
                Log.d("-> here", " 4" + getDate(cursor.getLong(4), this.DATE_FORMAT));
                Log.d("-> here", " 5" + cursor.getString(5));
            }
            cursor.moveToNext();
        }

    }

    public void showCalendarIDs() {
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

}
