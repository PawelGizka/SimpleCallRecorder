package com.pgizka.simplecallrecorder.servicerecording;

import android.content.Intent;
import android.util.Log;

public class RingingState implements State {
    static final String TAG = RingingState.class.getSimpleName();

    RecordingService recordingService;
    CheckToRecord checkToRecord;

    public RingingState(RecordingService recordingService, CheckToRecord checkToRecord) {
        this.recordingService = recordingService;
        this.checkToRecord = checkToRecord;
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

        if(checkToRecord.canRecord(recordingService.getPhoneNumber())){
            recordingService.setCurrentState(recordingService.recordingState);
            recordingService.recordingState.offHook(intent);
        } else {
            recordingService.setCurrentState(recordingService.notRecordingState);
        }
    }

    @Override
    public void idle(Intent intent) {
        Log.d(TAG, "idle");

        recordingService.setCurrentState(recordingService.noCallState);
    }
}
