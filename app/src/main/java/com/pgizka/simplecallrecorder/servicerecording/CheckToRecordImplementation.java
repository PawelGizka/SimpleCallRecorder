package com.pgizka.simplecallrecorder.servicerecording;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.pgizka.simplecallrecorder.data.RecorderContract;
import com.pgizka.simplecallrecorder.util.PreferanceStrings;

/**
 * Concrete implementation of CheckToRecord interface. For now it only checks two states: record everything
 * and record only selected. This class follow standard Strategy Pattern.
 */
public class CheckToRecordImplementation implements CheckToRecord {
    static final String TAG = CheckToRecordImplementation.class.getCanonicalName();

    Context context;


    public CheckToRecordImplementation(Context context) {
        this.context = context;
    }

    @Override
    public boolean canRecord(String phoneNumber) {

        SharedPreferences systemPref = context.getSharedPreferences(PreferanceStrings.SYSTEM_PREFERANCE, Context.MODE_PRIVATE);

        boolean recordingEnabled = systemPref.getBoolean(PreferanceStrings.RECORDING_ENABLED, false);
        if(!recordingEnabled){
            Log.d(TAG, "recording is not enabled");
            return false;
        }

        int recordingMode = systemPref.getInt(PreferanceStrings.RECORDING_MODE, 0);
        String selectionContact = RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER + " = ?";
        String[] selectionContactArgs = {phoneNumber};

        Cursor contactCursor = context.getContentResolver().query(RecorderContract.getContentUri(RecorderContract.PATH_CONTACT),
                null, selectionContact, selectionContactArgs, null);

        int recorded = 0, ignored = 0;
        if(contactCursor.getCount() > 0){
            contactCursor.moveToFirst();
            recorded = contactCursor.getInt(contactCursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_RECORDED));
            ignored = contactCursor.getInt(contactCursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_IGNORED));
        }

        boolean canRecord = false;

        if(recordingMode == PreferanceStrings.RECORDING_MODE_ONLY_SELECTED){
            if(contactCursor.getCount() == 0) {
                Log.d(TAG, "recording mode is only selected, and given contact doesn't exist");
                canRecord = false;
            } else if(recorded == 1){
                Log.d(TAG, "recording mode is only selected, and given contact exist and is recorded");
                canRecord = true;
            }
        } else {
            if(contactCursor.getCount() == 0){
                canRecord = true;
            } else if(ignored == 0){
                canRecord = true;
            }
        }

        if(canRecord){
            Log.d(TAG, "can record time to start recordin");
        } else {
            Log.d(TAG, "can not record");
        }
        contactCursor.close();
        // end of checking whether to record

        return canRecord;
    }

}
