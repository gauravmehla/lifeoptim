package com.example.lifeoptim;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    // Table Name
    public static final String TABLE_NAME = "PREFERENCE";

    // Table columns
    public static final String ID = "_id";
    public static final String ITEM = "item";
    public static final String VALUE = "value";

    // Database Information
    static final String DB_NAME = Constants.DATABASE_NAME;

    // database version
    static final int DB_VERSION = 1;

    // Creating table query
    private static final String CREATE_TABLE = "create table " + TABLE_NAME + "(" + ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + ITEM + " TEXT UNIQUE NOT NULL, "
            + VALUE + " TEXT NOT NULL );";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDB) {
        Log.d("DBHelper","onCreate");
        sqLiteDB.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDB, int oldVersion, int newVerion) {
        Log.d("version change", "oldVersion: " + oldVersion + ", newVersion: " + newVerion);
        sqLiteDB.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDB);
    }
}
