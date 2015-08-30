package com.pgizka.simplecallrecorder.servicerecording;

import android.content.Intent;
import android.util.Log;

/**
 * This state is intended to handle shake to record functionality.
 */
public class NotRecordingState implements State {
    static final String TAG = NotRecordingState.class.getSimpleName();

    RecordingService recordingService;

    public NotRecordingState(RecordingService recordingService) {
        this.recordingService = recordingService;
    }

    @Override
    public void newOutgoing(Intent intent) {

    }

    @Override
    public void ringing(Intent intent) {

    }

    @Override
    public void offHook(Intent intent) {

    }

    @Override
    public void idle(Intent intent) {
        Log.d(TAG, "idle");

        recordingService.setCurrentState(recordingService.noCallState);
    }
}
