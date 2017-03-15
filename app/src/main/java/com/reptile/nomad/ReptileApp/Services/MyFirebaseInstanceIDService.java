package com.reptile.nomad.ReptileApp.Services;

import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.reptile.nomad.ReptileApp.Reptile;

/**
 * Created by sankarmanoj on 30/05/16.
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "FirebaseIIDService";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG,"Refreshed Token ="+refreshedToken);
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("fireToken",refreshedToken).commit();
        if (Reptile.hasLoggedIn) {
            Reptile.mSocket.emit("fcmtoken",refreshedToken);
        }
    }
}
