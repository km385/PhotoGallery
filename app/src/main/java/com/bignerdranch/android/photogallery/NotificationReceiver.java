package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "received result" + getResultCode());
        if (getResultCode() != Activity.RESULT_OK){
            // a foreground activity cancelled the broadcast
            return;
        }

        int requestCode = intent.getIntExtra(PollService.REQUEST_CODE,0);
        Notification notification = (Notification)
                intent.getParcelableExtra(PollService.NOTIFICATION);
        NotificationManagerCompat notificationManagerCompat =
                    NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(requestCode, notification);
//        sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION), PERM_PRIVATE);

    }
}
