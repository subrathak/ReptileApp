package com.reptile.nomad.changedReptile.Models;

import android.util.Log;


import com.reptile.nomad.changedReptile.Reptile;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.HashMap;

/**
 * Created by nomad on 12/5/16.
 */
public class User {
    public String userName;
    public String firstName;
    public String lastName;
    public String email;
    public String id="";
    public String accountid = "";
    public String imageURI;
    public int TYPE;
    private String userSessionToken;

    public User(String firstName, String lastName ){
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = firstName + " "+ lastName;
    }

    public static void addToKnownUser(JSONObject input)
    {
        try
        {
            String id = input.getString("_id");
            User newUser = new User(input.getString("firstname"),input.getString("lastname"));
            if(input.has("username")) {
                newUser.userName = input.getString("username");
            }
            newUser.id=input.getString("_id");
            newUser.accountid = input.getString("accountid");
            newUser.imageURI=input.getString("imageuri");
            newUser.email = input.getString("email");
            switch (input.getString("type"))
            {

                case "google":

                    newUser.TYPE = Reptile.GOOGLE_LOGIN;
                    break;
                case "email":
                    newUser.TYPE = Reptile.EMAIL_LOGIN;
                    break;
                default:
                    newUser.TYPE = Reptile.EMAIL_LOGIN;
            }

            Reptile.knownUsers.put(id,newUser);


        }
        catch (JSONException e)
        {
            Log.e("Add User",input.toString());
            e.printStackTrace();
        }

    }
    public static User getUserFromJSON(JSONObject input){
        User newUser = null;
        try {
            String id = input.getString("_id");
            String email = input.getString("email");
            String firstname = input.getString("firstname");
            String accountid = input.getString("accountid");
            String imageuri = input.getString("imageuri");
            newUser = new User(firstname,firstname);
            newUser.accountid =accountid;
            newUser.id = id;
            newUser.email = email;
            newUser.imageURI = imageuri;
            switch (input.getString("type"))
            {
                case "google":

                    newUser.TYPE = Reptile.GOOGLE_LOGIN;
                    break;
                case "email":
                    newUser.TYPE = Reptile.EMAIL_LOGIN;
                    break;
                default:
                    newUser.TYPE = Reptile.EMAIL_LOGIN;
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException("Error Adding New User");
        }
        return newUser;
    }
    public static User getUserFromJSONString ( String inputString)
    {
        try
        {
            JSONObject input = new JSONObject(inputString);
            User newUser = new User(input.getString("firstname"),input.getString("firstname"));
            if(input.has("username")) {
                newUser.userName = input.getString("username");
            }
            newUser.id=input.getString("_id");
            newUser.accountid = input.getString("accountid");
            newUser.imageURI = input.getString("imageuri");
            newUser.email = input.getString("email");
            switch (input.getString("type"))
            {
                case "google":

                    newUser.TYPE = Reptile.GOOGLE_LOGIN;
                    break;
                case "email":
                    newUser.TYPE = Reptile.EMAIL_LOGIN;
                    break;
                default:
                    newUser.TYPE = Reptile.EMAIL_LOGIN;
                    break;
            }
            Log.d("Added user ",newUser.userName);
            return newUser;
        }
        catch (JSONException e)
        {
            Log.e("Add User",inputString);
            e.printStackTrace();
            throw  new RuntimeException("Error Getting User From JSON");
        }
    }
    public static void addUserToHashMap (JSONObject input, HashMap<String,User> userHashMap)
    {

        try
        {
            String id = input.getString("_id");
            User newUser = new User(input.getString("firstname"),input.getString("firstname"));
            if(input.has("username")) {
                newUser.userName = input.getString("username");
            }
            newUser.id=input.getString("_id");
            newUser.accountid = input.getString("accountid");
            newUser.imageURI = input.getString("imageuri");
            newUser.email = input.getString("email");
            switch (input.getString("type"))
            {
//                case "facebook":
//                    newUser.TYPE = Reptile.FACEBOOK_LOGIN;
//                    break;
                case "google":

                    newUser.TYPE = Reptile.GOOGLE_LOGIN;
                    break;
                case "email":
                    newUser.TYPE = Reptile.EMAIL_LOGIN;
                    break;
                default:
                    newUser.TYPE = Reptile.EMAIL_LOGIN;
                    break;
            }
            Log.d("Added user ",newUser.userName);
            userHashMap.put(id,newUser);


        }
        catch (JSONException e)
        {
            Log.e("Add User",input.toString());
            e.printStackTrace();
        }

    }
    public String getUserName() {
        return userName;
    }

    public String getFullName(){
        return firstName + " " + lastName;
    }


}
