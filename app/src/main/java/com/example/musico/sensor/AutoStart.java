package com.example.musico.sensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoStart extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent i) {
        Intent intent = new Intent(context, ServiceSensor.class);
        context.startService(intent);
    }
}
