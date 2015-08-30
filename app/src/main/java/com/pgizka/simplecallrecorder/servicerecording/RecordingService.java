package com.pgizka.simplecallrecorder.servicerecording;

import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.pgizka.simplecallrecorder.R;
import com.pgizka.simplecallrecorder.main.MainActivity;
import com.pgizka.simplecallrecorder.recordings.RecordingDetailActivity;
import com.pgizka.simplecallrecorder.util.PreferanceStrings;
import com.pgizka.simplecallrecorder.data.RecorderContract;
import com.pgizka.simplecallrecorder.util.UpdateUserData;
import com.pgizka.simplecallrecorder.util.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.text.SimpleDateFormat;

/**
 * Recording service follow standard Template Pattern. Four different states are encapsulated in their
 * own classes.
 */
public class RecordingService extends Service {
    static final String TAG = RecordingService.class.getSimpleName();

    String state;

    String phoneNumber;
    boolean wasIncoming = false;

    //Four different states
    public State noCallState;
    public State recordingState;
    public State ringingState;
    public State notRecordingState;
    //And current state
    State currentState;

    @Override
    public void onCreate() {
        super.onCreate();

        CheckToRecord checkToRecord = new CheckToRecordImplementation(this);
        noCallState = new NoCallState(this, checkToRecord);
        recordingState = new RecordingState(this, this);
        ringingState = new RingingState(this, checkToRecord);
        notRecordingState = new NotRecordingState(this);

        currentState = noCallState;

        Log.d(TAG, "on created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        processCall(intent);

        return super.onStartCommand(intent, flags, startId);
    }

    private void processCall(Intent intent){

        if(intent == null){
            Log.d(TAG, "intent is null");
            Toast.makeText(this, "intent is null", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle bundle = intent.getExtras();
        if(bundle == null){
            Log.wtf(TAG, "Bundle is null");
            return;
        }

        //intent with action new outgoing call must be checked before testing whether EXTRA STATE is
        //null, because in case this is intent with action new outgoing call EXTRA STATE will
        //always be null
        if (Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction())) {
            Log.d(TAG, "state outgoing");
            currentState.newOutgoing(intent);
        }

        //And now check whether EXTRA STATE is null
        state = bundle.getString(TelephonyManager.EXTRA_STATE);
        if(state == null){
            Log.d(TAG, "state is null");
            return;
        } else {
            Log.d(TAG, "state is not null");
        }

        if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            Log.d(TAG, "state ringing");
            currentState.ringing(intent);
        }

        if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            Log.d(TAG, "state ofHook");
            currentState.offHook(intent);
        } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            Log.d(TAG, "state idle");
            currentState.idle(intent);
        }

    }

    /**
     * This method is intended to change current state in recording service.
     * @param currentState
     */
    public void setCurrentState(State currentState) {
        this.currentState = currentState;
    }


    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean getWasIncoming() {
        return wasIncoming;
    }

    public void setWasIncoming(boolean wasIncoming) {
        this.wasIncoming = wasIncoming;
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}
