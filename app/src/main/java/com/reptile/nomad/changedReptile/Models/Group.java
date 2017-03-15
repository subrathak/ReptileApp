package com.reptile.nomad.changedReptile.Models;

import android.util.Log;

import java.util.LinkedHashMap;


import com.reptile.nomad.changedReptile.Reptile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sankarmanoj on 04/06/16.
 */
public class Group {
    public String name;
    public String id;
    public User creator;
    public LinkedHashMap<String, User> members;
    public Group(String name)
    {
        this.name = name;
        this.creator = Reptile.mUser;

        members = new LinkedHashMap<>();

    }
    public static Group getGroupFromJSON(JSONObject inputJSON)
    {
        Log.d("New Group",inputJSON.toString());
        try
        {
            Group newGroup = new Group(inputJSON.getString("name"));
            newGroup.id = inputJSON.getString("_id");
            JSONArray membersJSON = inputJSON.getJSONArray("members");
            for (int i = 0; i<membersJSON.length();i++)
            {
                newGroup.members.put(membersJSON.getString(i),Reptile.knownUsers.get(membersJSON.getString(i)));
            }
            return newGroup;

        }catch (JSONException e){e.printStackTrace();}
        return null;
    }

    public JSONObject getJSON()
    {
        JSONObject toReturn = new JSONObject();
        try
        {
            toReturn.put("name",name);
            toReturn.put("creator",creator.id);
            JSONArray Jsonmembers = new JSONArray();
            for(User member : this.members.values())
            {
                Jsonmembers.put(member.id);
            }
            toReturn.put("members",Jsonmembers);
            return toReturn;

        }catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
    }

}
