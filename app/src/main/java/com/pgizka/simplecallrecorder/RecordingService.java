package com.pgizka.simplecallrecorder;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.pgizka.simplecallrecorder.com.pgizka.simplecallrecorder.data.RecorderContract;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecordingService extends Service {
    static final String TAG = RecordingService.class.getSimpleName();

    MediaRecorder recorder;
    File audiofile;
    boolean recordstarted = false;

    Bundle bundle;
    String state;

    String phoneNumber;
    boolean wasIncoming = false;
    String fileName;
    long startedTime;

    @Override
    public void onCreate() {
        super.onCreate();
        recorder = new MediaRecorder();
        Log.d(TAG, "on created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        processCall(intent);

        return super.onStartCommand(intent, flags, startId);
    }

    private void processCall(Intent intent){

        if (Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction())) {
            phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.d(TAG, "outgoing,ringing:" + phoneNumber);
            Toast.makeText(this, "outgoing,ringing:" + phoneNumber, Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, "it wasnt outgoing call");
        }

        bundle = intent.getExtras();
        if(bundle == null){
            Log.wtf(TAG, "Bundle is null");
            return;
        }

        SharedPreferences systemPref = getSharedPreferences(PreferanceStrings.SYSTEM_PREFERANCE, Context.MODE_PRIVATE);
        SharedPreferences userPref = PreferenceManager.getDefaultSharedPreferences(this);

        state = bundle.getString(TelephonyManager.EXTRA_STATE);
        if(state == null){
            Log.d(TAG, "state is null");
            return;
        } else {
            Log.d(TAG, "state is not null");
        }

        if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            wasIncoming = true;
            phoneNumber = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            Toast.makeText(this, "Icoming ", Toast.LENGTH_LONG).show();
        } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            Log.d(TAG, "Call started");

            Toast.makeText(this, "Call started", Toast.LENGTH_LONG).show();

            fileName = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(System.currentTimeMillis());

            if(wasIncoming){
                fileName += "_Incoming";
            }  else {
                fileName += "_Outgoing";
            }

            File sampleDir = new File(Environment.getExternalStorageDirectory(), "SimpleCallRecorder");
            if (!sampleDir.exists()) {
                sampleDir.mkdirs();
            }

            Log.d(TAG, "file name is: " + fileName);
            try {
                audiofile = File.createTempFile(fileName, ".amr", sampleDir);
            } catch (IOException e) {
                e.printStackTrace();
            }

            recorder = new MediaRecorder();

            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_DOWNLINK);

            recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(audiofile.getAbsolutePath());
            try {
                recorder.prepare();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                Toast.makeText(this, "Recorder threw exception", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Recorder threw exception", Toast.LENGTH_LONG).show();
            }
            recorder.start();
            recordstarted = true;
            startedTime = System.currentTimeMillis();
        } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {

            Toast.makeText(this, "REJECT || DISCO", Toast.LENGTH_LONG).show();
            if (recordstarted) {
                Log.d(TAG, "recordre stoped");
                recorder.stop();
                recorder.release();
                recordstarted = false;
            }

            int conversationDuration = (int) ((System.currentTimeMillis() - startedTime)/1000);
            startedTime = 0;

            String normalizedPhoneNumber;
            if(phoneNumber.charAt(0) == '+'){
                Log.d(TAG, "Normalizing phone number, phone number is " + phoneNumber +
                        " char at 0 is " + phoneNumber.charAt(0) +
                        " normalized phone number is " + phoneNumber.substring(3));
                normalizedPhoneNumber = phoneNumber.substring(3);
            } else {
                normalizedPhoneNumber = phoneNumber;
            }

            String selection = RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER + " = ?";
            String [] selectionArgs = {normalizedPhoneNumber};

            Cursor contactCursor = getContentResolver().query(RecorderContract.getContentUri(RecorderContract.PATH_CONTACT),
                    null, selection, selectionArgs, null);

            int contactId;
            if(contactCursor.getCount() == 0){
                ContentValues contentValues = new ContentValues();
                contentValues.put(RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER, normalizedPhoneNumber);
                contentValues.put(RecorderContract.ContactEntry.COLUMN_IGNORED, false);
                contentValues.put(RecorderContract.ContactEntry.COLUMN_RECORDED, false);
                Uri uri = getContentResolver().insert(
                        RecorderContract.getContentUri(RecorderContract.PATH_CONTACT),
                        contentValues);
                Log.d(TAG, "Last path segment is: " + uri.getLastPathSegment());
                contactId = Integer.parseInt(uri.getLastPathSegment());
            } else {
                contactCursor.moveToFirst();
                contactId = contactCursor.getInt(contactCursor.getColumnIndex(RecorderContract.ContactEntry._ID));
            }

            ContentValues contentValues = new ContentValues();
            contentValues.put(RecorderContract.RecordEntry.COLUMN_CONTACT_KEY, contactId);
            contentValues.put(RecorderContract.RecordEntry.COLUMN_DATE, System.currentTimeMillis());
            contentValues.put(RecorderContract.RecordEntry.COLUMN_LENGTH, conversationDuration);
            contentValues.put(RecorderContract.RecordEntry.COLUMN_PATH, audiofile.getAbsolutePath());
            if(wasIncoming) {
                contentValues.put(RecorderContract.RecordEntry.COLUMN_TYPE, RecorderContract.RecordEntry.TYPE_INCOMING);
            } else {
                contentValues.put(RecorderContract.RecordEntry.COLUMN_TYPE, RecorderContract.RecordEntry.TYPE_OUTGOING);
            }
            getContentResolver().insert(RecorderContract.getContentUri(RecorderContract.PATH_RECORD), contentValues);

            wasIncoming = false;
        }


    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
}
