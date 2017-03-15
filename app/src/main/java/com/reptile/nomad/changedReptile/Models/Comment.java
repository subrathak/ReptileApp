package com.reptile.nomad.changedReptile.Models;

import android.util.Log;

import com.reptile.nomad.changedReptile.Reptile;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by nomad on 11/5/16.
 */
public class Comment {
    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public User author;
    public Task task;
    public String id;
    public String comment;



    public Calendar created;
    public double comment_likes;
    public boolean hidden = false; // If the user deletes the comment of others, change this to true

    public Comment(String comment, User author, Task task){
        this.comment = comment;
        this.task = task;
        this.author = author;
    }
    public Comment(String comment, User author, Task task, String id){
        this.comment = comment;
        this.task = task;
        this.author = author;
        this.id = id;
    }
    public static Comment generateComment(JSONObject input)
    {
     try
     {
//         SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-d'T'k:m:s.S'Z'");
         SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
         simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
         Calendar created = Calendar.getInstance();
         created.setTime(simpleDateFormat.parse(input.getString("created")));
      Comment newComment = new Comment(input.getString("commentstring"), Reptile.knownUsers.get(input.getString("creator")),Reptile.mAllTasks.get(input.getString("task")),input.getString("_id"));
//         Calendar created = Calendar.getInstance();
         created.setTime(simpleDateFormat.parse(input.getString("created")));
         newComment.created = created;
         return newComment;

     }
     catch (JSONException e)
     {
         e.printStackTrace();
         throw new RuntimeException("Error Parsing JSON, Unable to Add Comment");
     }
        catch (ParseException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Error Parsing JSON, Unable to Add Comment");
        }

    }
    public String getComment() {
        return comment;
    }

    public JSONObject getCreationJSON()
    {
        JSONObject toSend = new JSONObject();
        try
        {
            toSend.put("commentstring",comment);
            toSend.put("task",task.id);
            toSend.put("creator",author.id);
            toSend.put("created", Calendar.getInstance().getTime());
            Log.d("commentCreationTime",Calendar.getInstance().getTime().toString());
        }catch (JSONException e)
        {
            e.printStackTrace();
        }

        return toSend;
    }

    public String getAuthorname(){
        return author.getUserName();
    }

    public Calendar getCreated() {
        return created;
    }

}
