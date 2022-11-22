package com.example.lifeoptim;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {
    private DBHelper dbHelper;
    private Context context;
    private SQLiteDatabase database;

    public DBManager(Context c) {
        context = c;
    }

    // ================================================
    public DBManager open() throws SQLException {
        dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }
    // ================================================

    public void insert(String item, String value) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DBHelper.ITEM, item);
        contentValue.put(DBHelper.VALUE, value);

        database.insert(DBHelper.TABLE_NAME, null, contentValue);
    }

    public Cursor fetch() {
        String[] columns = new String[] { DBHelper.ID, DBHelper.ITEM, DBHelper.VALUE};
        Cursor cursor = database.query(DBHelper.TABLE_NAME, columns, null, null, null, null, null);

        return cursor;
    }

    public int update(String item, String value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.VALUE, value);

        int i = database.update(DBHelper.TABLE_NAME, contentValues, DBHelper.ITEM + " = " + "\"" + item + "\"", null);
        return i;
    }

    public void delete(String item) {
        database.delete(DBHelper.TABLE_NAME, DBHelper.ITEM + " = " + "\"" + item + "\"", null);
    }

}
