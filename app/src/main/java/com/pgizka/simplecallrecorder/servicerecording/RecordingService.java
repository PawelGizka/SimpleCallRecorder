package com.pgizka.simplecallrecorder.servicerecording;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.pgizka.simplecallrecorder.util.PreferanceStrings;
import com.pgizka.simplecallrecorder.data.RecorderContract;
import com.pgizka.simplecallrecorder.util.Utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

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

        SharedPreferences systemPref = getSharedPreferences(PreferanceStrings.SYSTEM_PREFERANCE, Context.MODE_PRIVATE);
        SharedPreferences userPref = PreferenceManager.getDefaultSharedPreferences(this);

        boolean recordingEnabled = systemPref.getBoolean(PreferanceStrings.RECORDING_ENABLED, false);
        if(!recordingEnabled){
            Log.d(TAG, "recording is not enabled");
            return;
        }


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
        }

        phoneNumber = Utils.normalizePhoneNumber(phoneNumber);

        //Check wheater to record
        int recordingMode = systemPref.getInt(PreferanceStrings.RECORDING_MODE, 0);
        String selectionContact = RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER + " = ?";
        String[] selectionContactArgs = {phoneNumber};

        Cursor contactCursor = getContentResolver().query(RecorderContract.getContentUri(RecorderContract.PATH_CONTACT),
                null, selectionContact, selectionContactArgs, null);

        int recorded = 0, ignored = 0;
        if(contactCursor.getCount() > 0){
            contactCursor.moveToFirst();
            recorded = contactCursor.getInt(contactCursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_RECORDED));
            recorded = contactCursor.getInt(contactCursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_IGNORED));
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
            return;
        }
        contactCursor.close();
        // end of checking whether to record


        if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            int recordingSource = Integer.parseInt(userPref.getString(PreferanceStrings.USER_RECORDING_SOURCE, "0"));
            int recordingFormat = Integer.parseInt(userPref.getString(PreferanceStrings.USER_RECORDING_FORMAT, "0"));
            String recordingPath = userPref.getString(PreferanceStrings.USER_RECORDING_PATH, "/sdcard/simpleCallRecorder");
            boolean turnOnPhone = userPref.getBoolean(PreferanceStrings.USER_TURN_ON_PHONE, false);
            boolean increaseVolume = userPref.getBoolean(PreferanceStrings.USER_TURN_UP_VOLUME, true);

            recorder = new MediaRecorder();

            String audioSufix = null;

            switch (recordingSource){
                case 0:
                    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
                    break;
                case 1:
                    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_DOWNLINK);
                    break;
                case 2:
                    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_UPLINK);
                    break;
                case 3:
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    break;
            }

            switch (recordingFormat){
                case 0:
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    audioSufix = ".amr";
                    break;
                case 1:
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                    audioSufix = ".3gp";
                    break;
                case 2:
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                    audioSufix = ".aac";
                    break;
            }

            AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            if(increaseVolume){
                Log.d(TAG, "volume increaded");
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
            }
            if(turnOnPhone){
                Log.d(TAG, "turn speaker on");
                audioManager.setMode(AudioManager.MODE_IN_CALL);
                audioManager.setSpeakerphoneOn(true);
            }

            Log.d(TAG, "Call started");

            Toast.makeText(this, "Call started", Toast.LENGTH_LONG).show();

            fileName = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(System.currentTimeMillis());

            if(wasIncoming){
                fileName += "_Incoming";
            }  else {
                fileName += "_Outgoing";
            }

            //File sampleDir = new File(Environment.getExternalStorageDirectory(), "SimpleCallRecorder");
            File sampleDir = new File(recordingPath);
            if (!sampleDir.exists()) {
                sampleDir.mkdirs();
            }

            Log.d(TAG, "file name is: " + fileName);
            try {
                audiofile = File.createTempFile(fileName, audioSufix, sampleDir);
            } catch (IOException e) {
                e.printStackTrace();
            }

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

            String selection = RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER + " = ?";
            String [] selectionArgs = {phoneNumber};

            contactCursor = getContentResolver().query(RecorderContract.getContentUri(RecorderContract.PATH_CONTACT),
                    null, selection, selectionArgs, null);

            int contactId;
            if(contactCursor.getCount() == 0){
                ContentValues contentValues = new ContentValues();
                contentValues.put(RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER, phoneNumber);
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

        return null;
    }
}
