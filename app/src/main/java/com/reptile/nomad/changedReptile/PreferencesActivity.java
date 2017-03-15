package com.reptile.nomad.changedReptile;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.reptile.nomad.changedReptile.Adapters.preferenceAdapter;
import com.reptile.nomad.changedReptile.Models.Preference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.emitter.Emitter;

public class PreferencesActivity extends Activity {

    public RecyclerView preferencesListView;
    public Button submitButton;
    public ArrayAdapter<String> listviewAdapter;
    public com.reptile.nomad.changedReptile.Adapters.preferenceAdapter preferenceAdapter;
    public Preference thisPreference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        preferencesListView = (RecyclerView) findViewById(R.id.preferenceListView);
        submitButton = (Button)findViewById(R.id.submitPreferencesButton);
        final String[] listPreferences = new String[]{};
        final List<Preference> listPref = new ArrayList<>();


        Reptile.mSocket.emit("user-preferences",Reptile.mUser.id);
        Reptile.mSocket.on("user-preferences", new Emitter.Listener(){

            @Override
            public void call(Object... args) {
                try {
                    JSONArray inputArray = new JSONArray((String) args[0]);

                    for(int i = 0; i<inputArray.length();i++)
                    {
                        JSONObject input = inputArray.getJSONObject(i);
                        thisPreference = Preference.getPreferenceObject(input);
                        listPref.add(thisPreference);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        });

        listviewAdapter = new ArrayAdapter<String>(this, R.layout.preference_element, listPreferences);
        preferenceAdapter = new preferenceAdapter(listPref);
        preferencesListView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        preferencesListView.setAdapter(preferenceAdapter);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                preferencesListView
            }
        });


    }
}
