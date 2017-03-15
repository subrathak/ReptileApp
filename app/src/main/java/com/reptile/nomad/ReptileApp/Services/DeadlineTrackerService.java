package com.reptile.nomad.ReptileApp.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.reptile.nomad.ReptileApp.MainActivity;
import com.reptile.nomad.ReptileApp.Models.Task;
import com.reptile.nomad.ReptileApp.QuickPreferences;
import com.reptile.nomad.ReptileApp.R;
import com.reptile.nomad.ReptileApp.Reptile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.socket.emitter.Emitter;


public class DeadlineTrackerService extends Service {

    String TAG = "DeadlineTrackerService";
    String TAG2 = "TaskStatusTag";
    NotificationManager notificationManager;
    NotificationCompat.Builder builder;
    Notification stateHolderNotification;
    public Task task;

    public DeadlineTrackerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"Service Started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent == null){
            stopSelf();
        }else if (intent.getAction().equals("delete")){
            JSONObject sendToServer = new JSONObject();
            try {
                sendToServer.put("taskID", task.id);
                Reptile.ownTasks.remove(task.id);
                Reptile.mAllTasks.remove(task.id);
                sendToServer.put("status", "deleted");
                Reptile.mSocket.emit("taskDeleted", sendToServer);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Reptile.mSocket.on("taskDeleted", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    String reply = (String)args[0];
                    Log.d(TAG,"Reply From Server = "+reply);
                    switch (reply)
                    {
                        case "success":

                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(QuickPreferences.tasksUpdated));
                            Reptile.mSocket.emit("addtasks");
                            Reptile.mSocket.off("taskDeleted");
                            break;
                        case "error":
                            Log.d("Error","Failed to delete task at sserver");
                            break;
                    }
                }
            });

            stopForeground(true);
//            stopSelf();

        }else if(intent.getAction().equals("done")){

            String IdtaskInNotificationBar = intent.getExtras().getString("task");
            Reptile.ownTasks.containsValue(IdtaskInNotificationBar);
            task.status = "done";
            JSONObject sendToServer = new JSONObject();
            try {
                sendToServer.put("taskID", IdtaskInNotificationBar);
                sendToServer.put("status", "done");
                Reptile.mSocket.emit("taskCompleted", sendToServer);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Reptile.mSocket.on("taskCompleted", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    String reply = (String)args[0];
                    Log.d(TAG,"Reply From Server = "+reply);
                    switch (reply)
                    {
                        case "success":

                            if(Reptile.mSocket.connected()==false)
                            {
                                Reptile.mSocket.connect();
                            }
                            Reptile.mSocket.emit("addtasks");
                            Reptile.mSocket.emit("addusers");
                            Reptile.mSocket.off("taskCompleted");
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(QuickPreferences.tasksUpdated));
                            break;
                        case "error":


                            break;
                    }
                }
            });

            stopForeground(true);
//            stopSelf();
        }

        new TrackThread().start();

        return  START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void notifyUser(Task task){

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction("main");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent doneIntent = new Intent(this,DeadlineTrackerService.class);
        doneIntent.setAction("done");
        doneIntent.putExtra("task",task.id);
        PendingIntent pendingDoneIntent = PendingIntent.getService(this, 0,
                doneIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent deleteIntent = new Intent(this,DeadlineTrackerService.class);
        deleteIntent.setAction("delete");
        deleteIntent.putExtra("task", task.id);
        PendingIntent pendingDeleteIntent = PendingIntent.getService(this, 0,
                deleteIntent,0);


        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.logoreptile);

        builder = new NotificationCompat.Builder(getApplicationContext());

        builder.setContentTitle("Reptile");
        builder.setContentText(task.getTaskString());
        builder.setSmallIcon(R.drawable.logoreptile);
        builder.setLargeIcon(Bitmap.createScaledBitmap(icon, 200, 200, false));
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);
        builder.addAction(R.drawable.ic_done_black_24dp,"Done",pendingDoneIntent);
//        builder.addAction(R.drawable.ic_snooze_black_24dp,"miss",pendingIntent);
        builder.addAction(R.drawable.ic_cancel_black_24dp,"Delete",pendingDeleteIntent);


        builder.setPriority(Notification.PRIORITY_HIGH);
        stateHolderNotification = builder.build();

        startForeground(101,
                stateHolderNotification);

    }

    public class TrackThread extends Thread{

        @Override
        public void run() {
            super.run();

            while (true) {

                Log.d(TAG, "performing pending task check");
                Task c;
                Calendar deadline;
                Calendar currentTime;
                List<Task> toTrack = new ArrayList<>(Reptile.ownTasks.values());
                task = null;
                for (int i = 0; i < toTrack.size(); i++) {
                    c = toTrack.get(i);
                    Log.d(TAG2,"Task: "+c.getTaskString()+" Status: " + c.status);
                    deadline = c.getDeadline();
                    currentTime = Calendar.getInstance();
                    Log.d(TAG,deadline.toString());
                    if (deadline.before(currentTime)) {

                        if (c.status.equals("missed")||c.status.equals("active")) {
                            task = c;
                            Log.d(TAG,"Found unfinished task, reminding user.");
                            Log.d(TAG,"Status of "+c.getTaskString()+ " is "+ c.status+ '.');
                            try {
                                notifyUser(c);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
                if(task==null){
                    stopForeground(true);
                }

                try {
                    Thread.currentThread().sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        }
    }
}


