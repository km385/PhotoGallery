package com.bignerdranch.android.photogallery;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class PollJobService extends JobService {
    private static final String TAG = "PollJobService";

    private PollTask mCurrentTask;

    @Override
    public boolean onStartJob(JobParameters params) {
        mCurrentTask = new PollTask();
        mCurrentTask.execute(params);
        return true;
    }

    private class PollTask extends AsyncTask<JobParameters,Void,Void>{

        @Override
        protected Void doInBackground(JobParameters... jobParameters) {
            JobParameters jobParams = jobParameters[0];

            handleJob();

            jobFinished(jobParams, false);
            return null;
        }
    }

    public static boolean isServiceScheduled(Context context, int JOB_ID){
        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        boolean hasBeenScheduled = false;

        for(JobInfo job: jobScheduler.getAllPendingJobs()){

            if (job.getId() == JOB_ID){
                hasBeenScheduled = true;
                Log.i(TAG, "isServiceScheduled: has been set");
            }
        }
        return hasBeenScheduled;
    }

    public static void setUpService(Context context, int JOB_ID){
        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        JobInfo jobInfo = new JobInfo.Builder(
                JOB_ID, new ComponentName(context, PollJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setPeriodic(1000*15*60)
                .setPersisted(true)
                .build();
        jobScheduler.schedule(jobInfo);

        QueryPreferences.setAlarmOn(context, true);
    }

    public static void cancelJob(Context context, int JOB_ID){
        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(JOB_ID);

        QueryPreferences.setAlarmOn(context, false);
        Log.i(TAG, "Service has been canceled: " + JOB_ID);
    }



    private void handleJob(){
        String query = QueryPreferences.getStoredQuery(this);
        String lastResultId = QueryPreferences.getLastResultId(this);
        List<GalleryItem> items;

        if (query == null){
            items = new FlickrFetchr().fetchRecentPhotos("1");
        } else {
            items = new FlickrFetchr().searchPhotos("1", query);
        }

        if (items.size() == 0){
            return;
        }

        String resultId = items.get(0).getId();
        if (resultId.equals(lastResultId)){
            Log.i(TAG, "onHandleIntent: got an old result " + resultId);
        } else{
            Log.i(TAG, "onHandleIntent: got a new result " + resultId);

            Resources resources = getResources();
            Intent i = PhotoGalleryActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getActivity(this, 0,i,0);

            Notification notification = new NotificationCompat.Builder(this, "0")
                    .setTicker(resources.getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resources.getString(R.string.new_pictures_title))
                    .setContentText(resources.getString(R.string.new_pictures_text))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build();

            NotificationManagerCompat notificationManagerCompat =
                    NotificationManagerCompat.from(this);
            notificationManagerCompat.notify(0, notification);
        }

        QueryPreferences.setLastResultId(this, resultId);
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (mCurrentTask != null){
            mCurrentTask.cancel(true);
        }
        return false;
    }
}
