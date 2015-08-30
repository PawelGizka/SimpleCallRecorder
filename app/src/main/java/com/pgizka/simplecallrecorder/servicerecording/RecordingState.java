package com.pgizka.simplecallrecorder.servicerecording;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.pgizka.simplecallrecorder.R;
import com.pgizka.simplecallrecorder.data.RecorderContract;
import com.pgizka.simplecallrecorder.main.MainActivity;
import com.pgizka.simplecallrecorder.recordings.RecordingDetailActivity;
import com.pgizka.simplecallrecorder.util.PreferanceStrings;
import com.pgizka.simplecallrecorder.util.UpdateUserData;
import com.pgizka.simplecallrecorder.util.Utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * State to record calls. It implements offHook and idle methods.
 */
public class RecordingState implements State {
    static final String TAG = RecordingState.class.getSimpleName();

    static final int NOTIFICATION_RECORDING = 0;
    static final int NOTIFICATION_CALL_RECORDED = 1;

    Context context;
    MediaRecorder recorder, recorderMic;
    File audioFile, audioFileMic;
    boolean recordStarted = false, recorderThrewException = false;

    NotificationManager notificationManager;
    boolean updateNotification = false;
    Handler handler = new Handler();

    SharedPreferences systemPref;
    SharedPreferences userPref;

    boolean wasIncoming = false;
    String fileName, fileNameMic;
    long startedTime;
    int recordingSource;

    RecordingService recordingService;

    public RecordingState(Context context, RecordingService recordingService) {
        this.context = context;
        this.recordingService = recordingService;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        systemPref = context.getSharedPreferences(PreferanceStrings.SYSTEM_PREFERANCE, Context.MODE_PRIVATE);
        userPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void newOutgoing(Intent intent) {

    }

    @Override
    public void ringing(Intent intent) {

    }

    @Override
    public void offHook(Intent intent) {
        Log.d(TAG, "offHook");

        recordingSource = Integer.parseInt(userPref.getString(PreferanceStrings.USER_RECORDING_SOURCE, "0"));
        int recordingFormat = Integer.parseInt(userPref.getString(PreferanceStrings.USER_RECORDING_FORMAT, "0"));
        String recordingPath = userPref.getString(PreferanceStrings.USER_RECORDING_PATH, "/sdcard/simpleCallRecorder");
        //boolean turnOnPhone = userPref.getBoolean(PreferanceStrings.USER_TURN_ON_PHONE, false);
        boolean increaseVolume = userPref.getBoolean(PreferanceStrings.USER_TURN_UP_VOLUME, true);

        recorder = new MediaRecorder();

        String audioSuffix = null;

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

        //If recording source is set to voiceCall, voiceUplink or voiceDownlink start recording
        //using microphone in case phone doesn't handle other recordings modes than microphone
        if(recordingSource >= 0 && recordingSource <= 2){
            recorderMic = new MediaRecorder();
            recorderMic.setAudioSource(MediaRecorder.AudioSource.MIC);
        } else {
            recorderMic = null;
        }

        switch (recordingFormat){
            case 0:
                recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                audioSuffix = ".amr";
                if(recorderMic != null){
                    recorderMic.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                    recorderMic.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                }
                break;
            case 1:
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                audioSuffix = ".3gp";
                if(recorderMic != null){
                    recorderMic.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    recorderMic.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                }
                break;
            case 2:
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                audioSuffix = ".mp4";
                if(recorderMic != null){
                    recorderMic.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    recorderMic.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                }
                break;
        }



        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if(increaseVolume){
            Log.d(TAG, "volume increaded");
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
        }
            /*
            if(turnOnPhone){
                Log.d(TAG, "turn speaker on");
                audioManager.setMode(AudioManager.MODE_IN_CALL);
                audioManager.setSpeakerphoneOn(true);
            }*/

        Log.d(TAG, "Call started");

        //Toast.makeText(this, "Call started", Toast.LENGTH_LONG).show();
        startedTime = System.currentTimeMillis();
        showRecordingNotification();

        fileName = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(System.currentTimeMillis());

        if(wasIncoming){
            fileName += "_Incoming";
        }  else {
            fileName += "_Outgoing";
        }

        if(recorderMic != null){
            fileNameMic = fileName + "_MIC";
        }

        //File sampleDir = new File(Environment.getExternalStorageDirectory(), "SimpleCallRecorder");
        File sampleDir = new File(recordingPath);
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }

        Log.d(TAG, "file name is: " + fileName);

        audioFile = new File(sampleDir, fileName + audioSuffix);
        if(recorderMic != null){
            audioFileMic = new File(sampleDir, fileNameMic + audioSuffix);
        }

        recorder.setOutputFile(audioFile.getAbsolutePath());
        if(recorderMic != null){
            recorderMic.setOutputFile(audioFileMic.getAbsolutePath());
        }
        try {
            recorder.prepare();
            if(recorderMic != null){
                recorderMic.prepare();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Toast.makeText(context, "Recorder threw exception", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Recorder threw exception", Toast.LENGTH_LONG).show();
        }
        try {
            recorder.start();
        } catch(Exception e){
            e.printStackTrace();
            recorderThrewException = true;
        }
        if(recorderMic != null){
            recorderMic.start();
        }
        recordStarted = true;
    }

    @Override
    public void idle(Intent intent) {
        Log.d(TAG, "idle");

        if (recordStarted) {
            Log.d(TAG, "recordre stoped");
            if(!recorderThrewException) {
                recorder.stop();
            }
            recorder.release();
            if(recorderMic != null){
                recorderMic.stop();
                recorderMic.release();
            }
            recordStarted = false;
        }

        int conversationDuration = (int) ((System.currentTimeMillis() - startedTime)/1000);

        String selection = RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER + " = ?";
        String [] selectionArgs = {recordingService.getPhoneNumber()};

        Cursor contactCursor = context.getContentResolver().query(RecorderContract.getContentUri(RecorderContract.PATH_CONTACT),
                null, selection, selectionArgs, null);

        int contactId;
        if(contactCursor.getCount() == 0){
            ContentValues contentValues = new ContentValues();
            contentValues.put(RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER, recordingService.getPhoneNumber());
            contentValues.put(RecorderContract.ContactEntry.COLUMN_IGNORED, false);
            contentValues.put(RecorderContract.ContactEntry.COLUMN_RECORDED, false);
            Uri uri = context.getContentResolver().insert(
                    RecorderContract.getContentUri(RecorderContract.PATH_CONTACT),
                    contentValues);
            Log.d(TAG, "Last path segment is: " + uri.getLastPathSegment());
            contactId = Integer.parseInt(uri.getLastPathSegment());
            new UpdateUserData(context).execute();
        } else {
            contactCursor.moveToFirst();
            contactId = contactCursor.getInt(contactCursor.getColumnIndex(RecorderContract.ContactEntry._ID));
        }

        //check wheater recording from source other then mic was successful
        String path;
        int recordingVoiceError;
        if((recorderThrewException || audioFile.length() < 20)  && recorderMic != null){
            path = audioFileMic.getAbsolutePath();
            recordingVoiceError = 1;
            audioFile.delete();
            SharedPreferences.Editor editor = userPref.edit();
            editor.putString(PreferanceStrings.USER_RECORDING_SOURCE, "3");
            editor.commit();
        } else {
            path = audioFile.getAbsolutePath();
            recordingVoiceError = 0;
            if(recorderMic != null) {
                audioFileMic.delete();
            }
        }
        recorderThrewException = false;

        ContentValues contentValues = new ContentValues();
        contentValues.put(RecorderContract.RecordEntry.COLUMN_CONTACT_KEY, contactId);
        contentValues.put(RecorderContract.RecordEntry.COLUMN_DATE, startedTime);
        contentValues.put(RecorderContract.RecordEntry.COLUMN_LENGTH, conversationDuration);
        contentValues.put(RecorderContract.RecordEntry.COLUMN_PATH, path);
        contentValues.put(RecorderContract.RecordEntry.COLUMN_SOURCE, recordingSource);
        contentValues.put(RecorderContract.RecordEntry.COLUMN_SOURCE_ERROR, recordingVoiceError);
        if(wasIncoming) {
            contentValues.put(RecorderContract.RecordEntry.COLUMN_TYPE, RecorderContract.RecordEntry.TYPE_INCOMING);
        } else {
            contentValues.put(RecorderContract.RecordEntry.COLUMN_TYPE, RecorderContract.RecordEntry.TYPE_OUTGOING);
        }
        Uri uri = context.getContentResolver().insert(RecorderContract.getContentUri(RecorderContract.PATH_RECORD), contentValues);

        //Toast.makeText(this, "REJECT || DISCO", Toast.LENGTH_LONG).show();
        hideRecordingNotification(conversationDuration, uri);

        wasIncoming = false;

        recordingService.setCurrentState(recordingService.noCallState);
    }

    private void showRecordingNotification(){
        boolean showNotifiaction = userPref.getBoolean(PreferanceStrings.USER_NOTIFICATION_DURING_CALL, true);
        if(showNotifiaction) {
            updateNotification = true;
            updateNotification();
        }
    }

    private void hideRecordingNotification(int duration, Uri uri){
        updateNotification = false;
        notificationManager.cancel("TAG", NOTIFICATION_RECORDING);

        boolean showNotification = userPref.getBoolean(PreferanceStrings.USER_NOTIFICATION_POST_CALL, true);
        if(!showNotification){
            return;
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_action_call_dark)
                        .setContentTitle(context.getString(R.string.service_notification_call_recorded) + " " + recordingService.getPhoneNumber())
                        .setContentText(Utils.formatDuration(duration))
                        .setAutoCancel(true);

        int id = Integer.parseInt(uri.getLastPathSegment());
        Intent intent = new Intent(context, RecordingDetailActivity.class);
        intent.setData(
                Uri.withAppendedPath(RecorderContract.getContentUri(RecorderContract.PATH_RECORD_WITH_CONTACT),
                        String.valueOf(id)));

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);

        taskStackBuilder.addParentStack(RecordingDetailActivity.class);
        taskStackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(2, PendingIntent.FLAG_ONE_SHOT);

        mBuilder.setContentIntent(pendingIntent);
        //NotificationManager notificationManager =
        //        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // mId allows you to update the notification later on.
        notificationManager.notify("TAG", NOTIFICATION_CALL_RECORDED, mBuilder.build());
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateNotification();
        }
    };

    private void updateNotification(){
        if(updateNotification) {
            int currentDuration = (int) ((System.currentTimeMillis() - startedTime) / 1000);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_recording_icon)
                            .setContentTitle(context.getString(R.string.service_notification_recording) + " " +
                                    Utils.formatDuration(currentDuration))
                            .setContentText(recordingService.getPhoneNumber());
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_ONE_SHOT);
            mBuilder.setContentIntent(pendingIntent);
            notificationManager.notify("TAG", NOTIFICATION_RECORDING, mBuilder.build());
        }
        if(updateNotification){
            handler.postDelayed(runnable, 1000);
        }
    }

}
