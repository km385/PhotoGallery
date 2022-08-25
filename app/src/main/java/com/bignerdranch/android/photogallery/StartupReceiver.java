package com.bignerdranch.android.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class StartupReceiver extends BroadcastReceiver {
    private static final String TAG = "StartupReceiver";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received broadcast intent: " + intent.getAction());

//       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
//           if(QueryPreferences.isAlarmOn(context)){
//               PollJobService.setUpService(context, 1);
//           } else {
//               PollJobService.cancelJob(context, 1);
//           }
//
//       } else {
           boolean isOn = QueryPreferences.isAlarmOn(context);
           PollService.setServiceAlarm(context, isOn);
//       }

    }
}
