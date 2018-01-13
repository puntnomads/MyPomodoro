package com.puntnomads.mypomodoro;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class NotificationReceiver extends WakefulBroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Intent notificationIntent = new Intent(context, TimerService.class);
        startWakefulService(context, notificationIntent);
    }
}
