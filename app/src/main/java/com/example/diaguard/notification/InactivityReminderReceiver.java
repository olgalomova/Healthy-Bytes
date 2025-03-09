package com.example.diaguard.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InactivityReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper.sendNotification(context,
                "Reminder",
                "You haven't entered glucose data for a long time. Check the glucose level.",
                1002);
    }
}
