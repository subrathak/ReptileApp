package com.reptile.nomad.ReptileApp.Services;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.reptile.nomad.ReptileApp.DetailedViewActivity;
import com.reptile.nomad.ReptileApp.MainActivity;
import com.reptile.nomad.ReptileApp.R;

import java.util.List;

/**
 * Created by sankarmanoj on 30/05/16.
 */
public class MyMessagingService extends FirebaseMessagingService {
    private static final String TAG ="MyMessagingService";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG,remoteMessage.getData().toString());
        if(remoteMessage.getData().get("type").equals("notification"))
        {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setAction("main");
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
            mBuilder.setContentTitle(remoteMessage.getData().get("title")).setContentText(remoteMessage.getData().get("body"));
            mBuilder.setContentIntent(pendingIntent);

            if(!isMyServiceRunning(DeadlineTrackerService.class)){
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent serviceIntetnt = new Intent(MyMessagingService.this,DeadlineTrackerService.class);
                        serviceIntetnt.setAction("track");
                        startService(serviceIntetnt);
                    }
                }, 10000);

            }

            if(DetailedViewActivity.inForeground) {
                NotificationManager mNotifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mBuilder.setSmallIcon(R.drawable.logoreptile);
                mNotifManager.notify(123, mBuilder.build());

                Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notificationSound);
                r.play();
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}


