package com.reptile.nomad.changedReptile;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

import com.android.volley.toolbox.ImageLoader;
import com.reptile.nomad.changedReptile.Models.User;
import com.reptile.nomad.changedReptile.customfonts.MyTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import io.socket.emitter.Emitter;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    ImageLoader imageLoader = Reptile.getInstance().getImageLoader();
//    @Bind(R.id.imageView1)
    CircularNetworkImageView profileImageView1;
//    @Bind(R.id.profile_username_textview)
    MyTextView profile_username_textview;
    ImageView followButtoniv;
    public String userAccountId;
    public String TAG = "ProfileActivity";
    public User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        profileImageView1 = (CircularNetworkImageView)findViewById(R.id.imageView1);
        profile_username_textview = (MyTextView)findViewById(R.id.profile_username_textview);
        followButtoniv = (ImageView)findViewById(R.id.followButton);
        followButtoniv.setOnClickListener(this);
        Bundle extras = getIntent().getExtras();
        if(!Reptile.mSocket.connected())
        {
            Reptile.mSocket.connect();
        }
        userAccountId = extras.getString("id");
        JSONObject useridjson = new JSONObject();
        try {
            useridjson.put("id", userAccountId);
            useridjson.put("me",Reptile.mUser.id);
            Reptile.mSocket.emit("fetchuserprofile", useridjson);
            Log.d(TAG,"Event Sent to fetch profile");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Reptile.mSocket.on("takeuserprofile", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG,args[0].toString());
                user = User.getUserFromJSONString((String) args[0]);
                Reptile.mSocket.off("fetchuserprofile");
                populateViews();
            }
        });
    }

    public void populateViews(){
        String imageurl = user.imageURI;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                profileImageView1.setImageUrl(user.imageURI,imageLoader);
                profile_username_textview.setText(user.getUserName());

            }
        });
//        profileImageView1.setImageUrl(imageurl,imageLoader);
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG,"Follow Clicked");
        JSONObject followObject = new JSONObject();
        try {
            followObject.put("me",Reptile.mUser.accountid);
            followObject.put("user",user.accountid);
            Reptile.mSocket.emit("follow",followObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Reptile.mSocket.on("follow", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String reply = (String)args[0];
                Log.d(TAG,"Reply From Server = "+reply);
                if(reply.equals("success")){
                    followButtoniv.setVisibility(View.GONE);
                }
            }
        });

    }
}
