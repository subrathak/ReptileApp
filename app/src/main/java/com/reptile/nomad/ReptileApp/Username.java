package com.reptile.nomad.ReptileApp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import io.socket.emitter.Emitter;


public class Username extends Activity {

    EditText usernameEditText;
    Button doneButon;
    String usernameInput;
    int runType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username_request);
        doneButon = (Button) findViewById(R.id.btn_proceed);
        usernameEditText = (EditText) findViewById(R.id.input_username);
        runType = 2;// = checkFirstRun();
        if (runType == 7) {
            Intent proceed = new Intent(Username.this, MainActivity.class);
            startActivity(proceed);
        } else  {
            doneButon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    usernameInput = usernameEditText.getText().toString();
                    JSONObject updateToSend = new JSONObject();
                    try {
                        updateToSend.put("accountid", Reptile.mUser.accountid);
                        updateToSend.put("username", usernameInput);
                        Reptile.mSocket.emit("usernameUpdate", updateToSend);
                        Log.d("updatedUsername", "even has been sent");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Reptile.mSocket.on("usernameUpdate", new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            String reply = (String) args[0];
                            Log.d("updated Username", "Reply From Server = " + reply);
                            switch (reply) {
                                case "success":
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "Username Saved", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    SharedPreferences settings = getSharedPreferences(QuickPreferences.appStatusSharedPreference, 0);
                                    Reptile.mUser.userName = usernameInput;
                                    settings.edit().putInt("firstTime",0).apply();
                                    QuickPreferences.usernameSelected = Boolean.TRUE;
                                    Reptile.mSocket.emit("addusers");
                                    Reptile.mSocket.emit("addtasks");
                                    TimerTask finishActivity = new TimerTask() {
                                        @Override
                                        public void run() {
                                            finish();
                                        }
                                    };
                                    Timer timer = new Timer();
                                    timer.schedule(finishActivity, 1000);
                                    Reptile.mSocket.off("usernameUpdate");
                                    break;
                                case "error":
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "Error Creating Task", Toast.LENGTH_LONG).show();
                                            doneButon.setEnabled(true);
                                        }
                                    });


                                    break;
                            }
                        }
                    });

                }
            });

        }
    }
    private int checkFirstRun() {

        final String PREFS_NAME = "MyPrefsFile";
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;


        // Get current version code
        int currentVersionCode = 0;
        try {
            currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            // handle exception
            e.printStackTrace();
            return 1;
        }

        // Get saved version code
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {

            // This is just a normal run
            return 0;

        } else if (savedVersionCode == DOESNT_EXIST) {

            // TODO This is a new install (or the user cleared the shared preferences)

            return 1;



        } else if (currentVersionCode > savedVersionCode) {

            // TODO This is an upgrade

            return 1;



        }

        // Update the shared preferences with the current version code
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).commit();
        return 1;

    }
}
