package com.pgizka.simplecallrecorder.servicerecording;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.pgizka.simplecallrecorder.util.Utils;

public class NoCallState implements State {
    static final String TAG = NoCallState.class.getSimpleName();

    RecordingService recordingService;
    CheckToRecord checkToRecord;

    public NoCallState(RecordingService recordingService, CheckToRecord checkToRecord) {
        this.checkToRecord = checkToRecord;
        this.recordingService = recordingService;
    }

    @Override
    public void newOutgoing(Intent intent) {
        Log.d(TAG, "newOutgoing");

        recordingService.setPhoneNumber(Utils.normalizePhoneNumber(intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)));
        Log.d(TAG, "outgoing,ringing:" + recordingService.getPhoneNumber());

        recordingService.setWasIncoming(false);

        if(checkToRecord.canRecord(recordingService.getPhoneNumber())){
            recordingService.setCurrentState(recordingService.recordingState);
        } else {
            recordingService.setCurrentState(recordingService.notRecordingState);
        }

    }

    @Override
    public void ringing(Intent intent) {
        Log.d(TAG, "ringing");

        String phoneNumber = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
        recordingService.setPhoneNumber(Utils.normalizePhoneNumber(phoneNumber));
        Log.d(TAG, "incoming, set state to ringing, phone number " + phoneNumber);

        recordingService.setWasIncoming(true);

        recordingService.setCurrentState(recordingService.ringingState);
    }

    @Override
    public void offHook(Intent intent) {
        Log.d(TAG, "offHook THIS SHOULDN'T HAPPEN!!!");
    }

    @Override
    public void idle(Intent intent) {
        Log.d(TAG, "idle, THIS SHOULDN'T HAPPEN");
    }
}
