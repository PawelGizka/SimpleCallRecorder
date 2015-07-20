package com.pgizka.simplecallrecorder.com.pgizka.simplecallrecorder.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Pawe³ on 2015-07-10.
 */
public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "callRecorder.db";
    public static final int DB_VERSION = 3;


    public DBHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        String contact = "CREATE TABLE " + RecorderContract.ContactEntry.TABLE_NAME + " ( " +
                RecorderContract.ContactEntry._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER + " TEXT NOT NULL, " +
                RecorderContract.ContactEntry.COLUMN_RECORDED + " INTEGER, " +
                RecorderContract.ContactEntry.COLUMN_IGNORED + " INTEGER )";

        String record = "CREATE TABLE " + RecorderContract.RecordEntry.TABLE_NAME + " ( " +
                RecorderContract.RecordEntry._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                RecorderContract.RecordEntry.COLUMN_CONTACT_KEY + " INTEGER NOT NULL, " +
                RecorderContract.RecordEntry.COLUMN_PATH + " TEXT, " +
                RecorderContract.RecordEntry.COLUMN_LENGTH + " INTEGER, " +
                RecorderContract.RecordEntry.COLUMN_DATE + " LONG, " +
                RecorderContract.RecordEntry.COLUMN_TYPE + " INTEGER, " +
                " FOREIGN KEY (" + RecorderContract.RecordEntry.COLUMN_CONTACT_KEY + ") REFERENCES " +
                RecorderContract.ContactEntry.TABLE_NAME + " (" + RecorderContract.ContactEntry._ID + "))";



        db.execSQL(contact);
        db.execSQL(record);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + RecorderContract.RecordEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RecorderContract.ContactEntry.TABLE_NAME);

        onCreate(db);
    }

}
