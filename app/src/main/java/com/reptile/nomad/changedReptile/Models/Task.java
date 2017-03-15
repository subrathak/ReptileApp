package com.reptile.nomad.changedReptile.Models;

import android.util.Log;

import com.reptile.nomad.changedReptile.Reptile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by nomad on 11/5/16.
 */
public class Task {
//    public enum Status { ACTIVE , DONE, MISSED }
    public User creator;
    private String taskString;
    public String id;
    private int likes;
    public LinkedHashMap<String, User> likers;
    public List<Comment> comments;
    private Calendar deadline;
    private Calendar created;
    public String status;
    public int commentCount;
    public List<Group> visibleTo;
    public boolean publictask;

    public Task(User creator, String taskString,    Calendar created, Calendar deadline){
        this.created = created;
        this.deadline = deadline;
        this.taskString = taskString;
        this.creator = creator;
        likers = new LinkedHashMap<>();
    }
    public static Comparator<Task> createdComparator = new Comparator<Task>() {
        @Override
        public int compare(Task lhs, Task rhs) {
            Calendar lhsCreated = lhs.getCreated();
            Calendar rhsCreated = rhs.getCreated();

            return rhsCreated.compareTo(lhsCreated);
        }
    };
    public static Comparator<Task> statusComparator = new Comparator<Task>() {
        @Override
        public int compare(Task lhs, Task rhs) {
            String lhsStatus = lhs.status;
            String rhsStatus = rhs.status;

            return lhsStatus.compareTo(rhsStatus);
        }
    };
    public static Task getTaskFromJSON(JSONObject input)
    {   try
    {
        String id = input.getString("_id");
        String creatordid = input.getString("creator");
        User creator = Reptile.knownUsers.get(creatordid);
        if(creator==null)
        {
           Reptile.mSocket.emit("get-user",creatordid);
            return null;
        }
        else
        {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
            Calendar created = Calendar.getInstance();
            created.setTime(simpleDateFormat.parse(input.getString("created")));
            Calendar deadline = Calendar.getInstance();
//            deadline = (Calendar) input.get("deadline");
            deadline.setTime(simpleDateFormat.parse(input.getString("deadline")));
            Log.d("deadlineParsing",input.getString("deadline"));
            Task newTask = new Task(creator,input.getString("taskstring"),created,deadline);

            JSONArray membersJSON = input.getJSONArray("likers");
            for (int i = 0; i<membersJSON.length();i++)
            {
                newTask.likers.put(membersJSON.getString(i),Reptile.knownUsers.get(membersJSON.getString(i)));
            }
            if(input.has("status")){
                newTask.status = input.getString("status");
            }
            newTask.id=id;
            return newTask;
        }
    }
    catch (JSONException e)
    {
        e.printStackTrace();
        throw new RuntimeException("Error Adding New Task");
    }
    catch (ParseException e)
    {
        e.printStackTrace();
        throw new RuntimeException("Error Adding New Task");
    }

    }
    public static void addTask(JSONObject input)
    {
        try
        {
            String id = input.getString("_id");
//            if(Reptile.mAllTasks.get(id)!=null) return;
            String creatordid = input.getString("creator");
            User creator = Reptile.knownUsers.get(creatordid);
            if(creator==null)
            {
                //TODO : Implement Look up User and Add
            }
            else
            {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
                Calendar created = Calendar.getInstance();
                created.setTime(simpleDateFormat.parse(input.getString("created")));
                Calendar deadline = Calendar.getInstance();
                deadline.setTime(simpleDateFormat.parse(input.getString("deadline")));
                Task newTask = new Task(creator,input.getString("taskstring"),created,deadline);
                JSONArray membersJSON = input.getJSONArray("likers");
                for (int i = 0; i<membersJSON.length();i++)
                {
                    newTask.likers.put(membersJSON.getString(i),Reptile.knownUsers.get(membersJSON.getString(i)));
                }
                newTask.id=id;
                newTask.commentCount=input.getInt("commentcount");
                if(input.has("status")){
                    newTask.status = input.getString("status");
                }
                Reptile.mAllTasks.put(id,newTask);

            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }
    public JSONObject getJSON()
    {
        JSONObject toSend = new JSONObject();
        try
        {
            toSend.put("created",created.getTime());
            toSend.put("creator",creator.id);
            toSend.put("taskstring",taskString);
            toSend.put("deadline",deadline.getTime());
            toSend.put("status",status);
//            toSend.put("likes",likes);
//            JSONArray JsonLikers = new JSONArray();
//            for(User liker : this.likers.values())
//            {
//                JsonLikers.put(liker.id);
//            }
//            toSend.put("members",JsonLikers);
            toSend.put("publictask",publictask);
//            if(publictask==false)
//            {
//                JSONArray visiblegroups = new JSONArray();
//               for(Group group : visibleTo)
//               {
//                   visiblegroups.put(group.id);
//               }
//                toSend.put("visibilegroups",visiblegroups);
//            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return  toSend;
    }

    public String getTaskString() {
        return taskString;
    }

    public void setTaskString(String taskString) {
        this.taskString = taskString;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public Calendar getDeadline() {
        return deadline;
    }

    public void setDeadline(Calendar deadline) {
        this.deadline = deadline;
    }

    public Calendar getCreated() {
        return created;
    }

    public void setCreated(Calendar created) {
        this.created = created;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public boolean likedByCurrentUser(){
        try {
            if (likers != null) {
                return likers.containsKey(Reptile.mUser.id);
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public String getLikes(){
        int likes;
        likes = likers.size();
        Log.d("Number of Likes","= "+likes);
        String likesString = Integer.toString(likes);
        likesString = likesString + " ";
        return likesString;
    }

    public long getMaxTimeGap(){
        return deadline.getTimeInMillis()-created.getTimeInMillis();
    }

    public String getDeadlineString(){
        String deadlineString;
        SimpleDateFormat sdf = new SimpleDateFormat("E , MMM-dd HH:mm");
        TimeZone timeZone= TimeZone.getTimeZone("IST");
//        sdf.setTimeZone(timeZone);
        deadlineString = sdf.format(deadline.getTime());
        if(deadline.get(Calendar.AM_PM)==0)
            deadlineString=deadlineString+" AM";
        else
            deadlineString=deadlineString+" PM";

        return deadlineString;
    }

    public String getTimeAgo(Calendar fromTime){


        return "sss";
    }

}
