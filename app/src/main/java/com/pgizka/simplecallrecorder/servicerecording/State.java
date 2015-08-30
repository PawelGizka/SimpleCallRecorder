package com.pgizka.simplecallrecorder.servicerecording;

import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Paweł on 2015-08-30.
 */
public interface State {
    public void newOutgoing(Intent intent);
    public void ringing(Intent intent);
    public void offHook(Intent intent);
    public void idle(Intent intent);

}
