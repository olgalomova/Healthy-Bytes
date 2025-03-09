package com.example.diaguard.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RegularReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper.sendNotification(context,
                "Reminder",
                "Do not forget to check glucose level today!",
                1001);
    }
}
