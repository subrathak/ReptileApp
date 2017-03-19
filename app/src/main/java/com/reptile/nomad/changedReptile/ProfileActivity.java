package com.reptile.nomad.changedReptile;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

public class ProfileActivity extends AppCompatActivity {

    ImageLoader imageLoader = Reptile.getInstance().getImageLoader();
    @Bind(R.id.imageView1)
    CircularNetworkImageView imageView1;
    @Bind(R.id.profile_username_textview)
    MyTextView profile_username_textview;
    public String userAccountId;
    public String TAG = "ProfileActivity";
    public User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Bundle extras = getIntent().getExtras();
        userAccountId = extras.getString("id");
        JSONObject useridjson = new JSONObject();
        try {
            useridjson.put("id", userAccountId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Reptile.mSocket.emit("fetchuserprofile", useridjson);
        Reptile.mSocket.on("fetchuserprofile", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(TAG,args[0].toString());
                user = User.getUserFromJSONString((String) args[0]);
                populateViews();
            }
        });
    }

    public void populateViews(){
        String imageurl = user.imageURI;
        imageView1.setImageUrl(imageurl,imageLoader);
        profile_username_textview.setText(user.getUserName());
    }
}
