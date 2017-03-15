package com.reptile.nomad.changedReptile;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.reptile.nomad.changedReptile.Adapters.CommentsRecycerAdapterFooter;
import com.reptile.nomad.changedReptile.Models.Task;
import com.reptile.nomad.changedReptile.Models.Comment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import io.socket.emitter.Emitter;

public class DetailedViewActivity extends Activity {

    public TextView NameTextViewDetailed;
    public TextView TaskTextViewDetailed;
    public static boolean inForeground;
    public RelativeTimeTextView DeadlineTextViewDetailed;
    private RoundCornerProgressBar ProgressBarDetailed;
    public RecyclerView CommentsRecyclerViewDetailed;
    public NetworkImageView DPimageViewDetailed;
    public EditText writeComment;
    public ImageButton postComment;
    public String ImageURL;
    public ImageLoader imageLoader;
    public TextView detiledViewTaskStatusTV;

    public String taskID;
    public Task thisTask;

    @Override
    protected void onResume() {
        super.onResume();
        inForeground = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        inForeground = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        inForeground = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        inForeground = true;
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
    }

    public Calendar deadline;
    public String deadlineString;
    public Calendar createdDate;
    public String createdDateString;
    public Date today;
    public HashMap<String,Comment> comments;

    private CommentsRecycerAdapterFooter commentsAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_view);
        comments = new HashMap<>();
        imageLoader = Reptile.getInstance().getImageLoader();
        detiledViewTaskStatusTV = (TextView)findViewById(R.id.detailedViewTaskStatus);
        NameTextViewDetailed = (TextView)findViewById(R.id.NameTextViewDetailed);
        TaskTextViewDetailed = (TextView)findViewById(R.id.TaskTextViewDetailed);
//        writeComment  = (EditText)findViewById(R.id.DetailedViewCommentEntryEditText);
//        postComment = (ImageButton)findViewById(R.id.detailedSubmitCommentImageView);
        ProgressBarDetailed = (RoundCornerProgressBar)findViewById(R.id.progressBarDetailed);
        DPimageViewDetailed = (NetworkImageView) findViewById(R.id.DPimageView);
        DeadlineTextViewDetailed = (RelativeTimeTextView)findViewById(R.id.deadlineTV);
        CommentsRecyclerViewDetailed = (RecyclerView)findViewById(R.id.CommentsRecyclerView);

        Bundle extras = getIntent().getExtras();
        taskID = extras.getString("taskID");
        thisTask = Reptile.mAllTasks.get(taskID); // causing null object error
//        if (thisTask.status.isEmpty()) {
//            detiledViewTaskStatusTV.setText("active");
//        }else {
//            detiledViewTaskStatusTV.setText(thisTask.status);
//        }
        detiledViewTaskStatusTV.setText(thisTask.status);
        TaskTextViewDetailed.setText(thisTask.getTaskString());
        NameTextViewDetailed.setText(thisTask.creator.getUserName());
        deadline = thisTask.getDeadline();
        ImageURL = thisTask.creator.imageURI;
        DPimageViewDetailed.setImageUrl(ImageURL,imageLoader);
        createdDate = thisTask.getCreated();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        SimpleDateFormat sdf = new SimpleDateFormat("E , yyyy-MM-dd HH:mm:ss.SSS");
        TimeZone timeZone= TimeZone.getTimeZone("IST");
        sdf.setTimeZone(timeZone);
        deadlineString = deadline.getTime().toString();
        createdDateString = new SimpleDateFormat("E , d MMM yy", Locale.UK).format(createdDate.getTime());
        DeadlineTextViewDetailed.setReferenceTime(thisTask.getDeadline().getTimeInMillis());//.setText(thisTask.getDeadlineString());
        today = new Date();
        ProgressBarDetailed.setMax(100 );
        ProgressBarDetailed.setProgress(sexo(thisTask.getCreated().getTimeInMillis(),Calendar.getInstance().getTimeInMillis(),thisTask.getDeadline().getTimeInMillis()));
//        postComment.setOnClickListener(new View.OnClickListener(){
//
//            @Override
//            public void onClick(View v) {
//                String newComment = writeComment.getText().toString();
//                if(newComment.replace(" ","").length()<3)
//                {
//                    Toast.makeText(getApplicationContext(),"Comment is too small",Toast.LENGTH_LONG).show();
//                    return;
//                }
//                Reptile.mSocket.emit("createcomment",new Comment(newComment,Reptile.mUser,thisTask).getCreationJSON());
//                postComment.setEnabled(false);
//                Reptile.mSocket.on("createcomment", new Emitter.Listener() {
//                    @Override
//                    public void call(Object... args) {
//                        String reply = (String)args[0];
//                        switch (reply)
//                        {
//                            case "success":
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Toast.makeText(getApplicationContext(),"Successfully Created Comment",Toast.LENGTH_SHORT).show();
//                                        postComment.setEnabled(true);
//                                        thisTask.commentCount+=1;
//
//                                        writeComment.setText("");
//                                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(QuickPreferences.tasksUpdated));
//                                    }
//                                });
//                                loadComments();
//                                break;
//                            case "error":
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Toast.makeText(getApplicationContext(),"Error Creating Comment",Toast.LENGTH_SHORT).show();
//                                        postComment.setEnabled(true);
//                                    }
//                                });
//                                break;
//                        }
//                    }
//                });
//
//            }
//        });
        loadComments();
        commentsAdapter = new CommentsRecycerAdapterFooter(new ArrayList<>(comments.values()),thisTask,getApplicationContext());
        Collections.sort(commentsAdapter.taskComments, new Comparator<Comment>() {
            @Override
            public int compare(Comment lhs, Comment rhs) {
                if(lhs.created.after(rhs.created))
                {
                    return 1;
                }
                else
                {
                    return -1;
                }
            }
        });
        CommentsRecyclerViewDetailed.setLayoutManager(new LinearLayoutManager(getApplicationContext())); // not required ?
        CommentsRecyclerViewDetailed.setAdapter(commentsAdapter);

    }



    public long getTimeDifference(Calendar t2, Calendar t1){
        return t2.getTimeInMillis() - t1.getTimeInMillis();
    }
    public void loadComments()
    {
        Reptile.mSocket.emit("loadcomments",thisTask.id);
        Log.d("DetailedViewTaskString",thisTask.getTaskString());
        Reptile.mSocket.on("loadcomments", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONArray commentsJSON = new JSONArray((String) args[0]);
                    comments.clear();
                    for(int i = 0;i<commentsJSON.length();i++)
                    {
                        JSONObject commentJSON = commentsJSON.getJSONObject(i);
                        Comment newComment = Comment.generateComment(commentJSON);
                        if(newComment.task.id==thisTask.id) {
                            comments.put(newComment.id, newComment);
                            Log.w("CommentTask Match",thisTask.getTaskString());
                        }
                        else
                        {
                            Log.e("Comment Task Mismatch",thisTask.getTaskString() + newComment.task.getTaskString());
                        }
                        }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            commentsAdapter.taskComments=new ArrayList<Comment>(comments.values());
                            Collections.sort(commentsAdapter.taskComments, new Comparator<Comment>() {
                                @Override
                                public int compare(Comment lhs, Comment rhs) {
                                    if(lhs.created.after(rhs.created))
                                    {
                                        return 1;
                                    }
                                    else
                                    {
                                        return -1;
                                    }
                                }
                            });
                            commentsAdapter.notifyDataSetChanged();
                        }
                    });
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

//                Reptile.mSocket.off("loadcomments");
            }
        });

    }

    public int sexo(long t1, long t2, long t3){
        Double dick = new Double((((t2 - t1)*1.0)/((t3 - t1)*1.0) * 100));
        Log.d("sexo", dick.intValue() + " ");
        return dick.intValue();
    }


}