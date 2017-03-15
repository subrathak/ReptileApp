package com.reptile.nomad.ReptileApp.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.reptile.nomad.ReptileApp.DetailedViewActivity;
import com.reptile.nomad.ReptileApp.MainActivity;
import com.reptile.nomad.ReptileApp.Models.Task;
import com.reptile.nomad.ReptileApp.QuickPreferences;
import com.reptile.nomad.ReptileApp.R;
import com.reptile.nomad.ReptileApp.Reptile;
import com.reptile.nomad.ReptileApp.Services.DeadlineTrackerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


import io.socket.emitter.Emitter;

public class MyTasksAdapter extends RecyclerView.Adapter<MyTasksAdapter.TaskViewHolder> {

    public static final String TAG = "MyTasksAdapter";
    public List<Task> Tasks;
    public Activity mActivity;
    public MyTasksAdapter(List<Task> Tasks, Activity mActivity) {
        this.mActivity = mActivity;
        this.Tasks = Tasks;
        if(Tasks==null)
        {
            Tasks = new ArrayList<>();
        }
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder
    {
        public View view;
        public Task currentTask;
        public    TextView NameTextView;
        public CardView cardView;
        public ImageButton doneButton;
        public ImageButton deleteButton;
        public RelativeTimeTextView deadlineTextView;
        public ImageView statusImaveView, tickImageView;
        public RoundCornerProgressBar taskProgressBar;
//        public    ImageView ProfilePictureImageView;
        public TextView TaskTextView;
        public TaskViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mActivity, DetailedViewActivity.class);
                    intent.putExtra("taskID",currentTask.id);
                    intent.putExtra("callerID", "own");
                    mActivity.startActivity(intent);
                }
            });
//            NameTextView = (TextView)itemView.findViewById(R.id.feedNameTextView);
//            ProfilePictureImageView = (ImageView)itemView.findViewById(R.id.feedProfileImageView);
            TaskTextView = (TextView)itemView.findViewById(R.id.feedTaskTextView);
            deadlineTextView = (RelativeTimeTextView) itemView.findViewById(R.id.deadlineTextView);
            doneButton = (ImageButton)itemView.findViewById(R.id.imageButtonDone);
            deleteButton = (ImageButton)itemView.findViewById(R.id.imageButtonDelete);
            taskProgressBar = (RoundCornerProgressBar) itemView.findViewById(R.id.progressBar);
            cardView = (CardView)itemView.findViewById(R.id.card_view);
            tickImageView = (ImageView)itemView.findViewById(R.id.tickImage);
        }

    }

    @Override
    public void onBindViewHolder(final TaskViewHolder holder, final int position) {
        final Task currentTask = Tasks.get(position);
//        String userName = currentTask.creator.getUserName();
//        holder.NameTextView.setText(userName);
        try {
            holder.TaskTextView.setText(currentTask.getTaskString());
            holder.currentTask = Tasks.get(position);
            holder.taskProgressBar.setMax(100);
            holder.taskProgressBar.setProgressColor(Color.parseColor("#ff303f9f"));
            holder.taskProgressBar.setProgress(sexo(currentTask.getCreated().getTimeInMillis(),Calendar.getInstance().getTimeInMillis(),currentTask.getDeadline().getTimeInMillis()));

            if(currentTask.status.equals("done"))
            {
                holder.taskProgressBar.setVisibility(View.INVISIBLE);
                holder.tickImageView.setVisibility(View.VISIBLE);
                holder.doneButton.setVisibility(View.GONE);
                holder.taskProgressBar.setProgressColor(Color.parseColor("#ff2e7d32"));
                holder.cardView.setCardBackgroundColor(Color.WHITE);
            }else{
                holder.cardView.setCardBackgroundColor(Color.WHITE);
                holder.tickImageView.setVisibility(View.INVISIBLE);
                holder.taskProgressBar.setProgressColor(Color.parseColor("#ff64b5f6"));
                holder.taskProgressBar.setVisibility(View.VISIBLE);
                holder.doneButton.setVisibility(View.VISIBLE);
            }
           holder.deadlineTextView.setReferenceTime(currentTask.getDeadline().getTimeInMillis());
            holder.doneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Inform backend + datasetchanged
                    JSONObject sendToServer = new JSONObject();
                    try {
                        sendToServer.put("taskID", currentTask.id);
                        sendToServer.put("status", "done");
                        Reptile.mSocket.emit("taskCompleted", sendToServer);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Reptile.mSocket.on("taskCompleted", new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            String reply = (String)args[0];
                            Reptile.mAllTasks.get(currentTask.id).status="done";
                            LocalBroadcastManager.getInstance(mActivity.getApplicationContext()).sendBroadcast(new Intent(QuickPreferences.tasksUpdated));
                            Log.d(TAG,"Reply From Server = "+reply);
                            switch (reply)
                            {
                                case "success":
                                    mActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            LocalBroadcastManager.getInstance(mActivity.getApplicationContext()).sendBroadcast(new Intent(QuickPreferences.tasksUpdated));
                                        }
                                    });
//                                    Reptile.mSocket.emit("addtasks");
                                    Reptile.mSocket.off("taskCompleted");
                                    break;
                                case "error":


                                    break;
                            }
                        }
                    });
                }
            });
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Inform backend + datasetchanged
                    JSONObject sendToServer = new JSONObject();
                    try {
                        sendToServer.put("taskID", currentTask.id);
                        Reptile.ownTasks.remove(currentTask.id);
                        Reptile.mAllTasks.remove(currentTask.id);
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
                                    mActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Tasks.remove(position);
                                            LocalBroadcastManager.getInstance(mActivity.getApplicationContext()).sendBroadcast(new Intent(QuickPreferences.tasksUpdated));

                                        }
                                    });
                                //    Reptile.mSocket.emit("addtasks");
                                    Reptile.mSocket.off("taskDeleted");
                                    break;
                                case "error":


                                    break;
                            }
                        }
                    });
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    public long getTimeDifference(Calendar t2, Calendar t1){
        return t2.getTimeInMillis() - t1.getTimeInMillis();
    }

    @Override
    public int getItemCount() {
        return Tasks.size();
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_task_card,parent,false);
        return new TaskViewHolder(v);
    }


    public int sexo(long t1, long t2, long t3){
        Double dick = new Double((((t2 - t1)*1.0)/((t3 - t1)*1.0) * 100));
        Log.d("sexo", dick.intValue() + " ");
        return dick.intValue();
    }

}
