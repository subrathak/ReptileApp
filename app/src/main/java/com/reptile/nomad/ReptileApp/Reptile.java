package com.reptile.nomad.ReptileApp;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
//import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.reptile.nomad.ReptileApp.Models.Group;
import com.reptile.nomad.ReptileApp.Models.Preference;
import com.reptile.nomad.ReptileApp.Models.Task;
import com.reptile.nomad.ReptileApp.Models.User;
import com.reptile.nomad.ReptileApp.volley.LruBitmapCache;
import com.android.volley.toolbox.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Reptile extends Application {
    public static final int FACEBOOK_LOGIN = 594;
    public static final int GOOGLE_LOGIN = 771;
    public static Socket mSocket;
    public static Reptile Instance;
    public static boolean connectedToServer = false;
    public static String DeviceID;
    public final static String TAG = "Reptile Application";
    public static User mUser;
    public static LinkedHashMap<String, Task> mAllTasks;
    public static LinkedHashMap<String, Task> ownTasks;
    public static LinkedHashMap<String, User> knownUsers;
    public static LinkedHashMap<String, Group> mUserGroups;
    public static GoogleApiClient mGoogleApiClient;
    public static GoogleSignInAccount mGoogleAccount;
    LruBitmapCache mLruBitmapCache;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    public static Boolean hasLoggedIn = false;

    @Override
    protected void attachBaseContext(Context base) {
        MultiDex.install(base);
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        Instance = this;
        mAllTasks = new LinkedHashMap<>();
        ownTasks = new LinkedHashMap<>();
        mUserGroups = new LinkedHashMap<>();
        knownUsers = new LinkedHashMap<>();
        DeviceID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        URI serverURI = null;
        FacebookSdk.sdkInitialize(getApplicationContext());
        try {
            serverURI = new URI(getString(R.string.server_uri));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(options)
                .build();
        com.nostra13.universalimageloader.core.ImageLoader.getInstance().init(config);

        mSocket = IO.socket(serverURI);
        mSocket.connect();
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                connectedToServer = true;
                login(getApplicationContext());
            }
        });
        mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                connectedToServer = false;
                mSocket.connect();
                Log.d(TAG, "Socket Disconnected");
            }
        });
        mSocket.on("addmytasks", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONArray inputArray = new JSONArray((String) args[0]);
                    for (int i = 0; i < inputArray.length(); i++) {
                        JSONObject input = inputArray.getJSONObject(i);
                        ownTasks.put(input.getString("_id"), Task.getTaskFromJSON(input));
                    }
                    Log.d("Add My Tasks", "Size = " + ownTasks.size());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        mSocket.on("addusers", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONArray inputArray = new JSONArray((String) args[0]);
                    for (int i = 0; i < inputArray.length(); i++) {
                        JSONObject input = inputArray.getJSONObject(i);
                        User.addToKnownUser(input);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        mSocket.on("addtasks", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONArray inputArray = new JSONArray((String) args[0]);
                    for (int i = 0; i < inputArray.length(); i++) {
                        JSONObject input = inputArray.getJSONObject(i);
                        Task.addTask(input);
                    }
                    Log.d(TAG, "Broadcast Tasks Updated " + String.valueOf(mAllTasks.size()));
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(QuickPreferences.tasksUpdated));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        mSocket.on("addgroups", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONArray inputArray = new JSONArray((String) args[0]);
                    for (int i = 0; i < inputArray.length(); i++) {
                        JSONObject input = inputArray.getJSONObject(i);
                        mUserGroups.put(Group.getGroupFromJSON(input).id, Group.getGroupFromJSON(input));

                    }
                    Log.d(TAG, "Group Size = " + inputArray.length());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        mSocket.on("login", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                mUser = User.getUserFromJSONString((String) args[0]);
                String refreshedToken = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("fireToken", null);
                if (refreshedToken != null)
                    Reptile.mSocket.emit("fcmtoken", refreshedToken);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent("logged-in"));
                hasLoggedIn = true;
            }
        });
        mSocket.on("loginfailed", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.e(TAG, "LOGIN FAILED");
                getApplicationContext().startActivity(new Intent(getApplicationContext(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
        GoogleSignInOptions GSO = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestProfile().requestIdToken(getString(R.string.google_server_client_id)).requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, GSO)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        login(getApplicationContext());
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .build();
        mGoogleApiClient.connect();

    }

    public static void emailSignUp(){
        JSONObject toSendToServer = new JSONObject();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            if(name==null){
                name = email;
            }

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            String uid = user.getUid();
            try {
                toSendToServer.put("firstname", name);
                toSendToServer.put("deviceid", DeviceID);
                toSendToServer.put("type", "email");
                toSendToServer.put("fcmtoken", FirebaseInstanceId.getInstance().getToken());
                toSendToServer.put("accountid", 123);
//                toSendToServer.put("imageuri", photoUrl);
                toSendToServer.put("username",email);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mSocket.emit("signup", toSendToServer);


        }
    }

    public static void googleSignUp(GoogleSignInAccount account) {
        JSONObject toSendToServer = new JSONObject();
        try {
            toSendToServer.put("firstname", account.getDisplayName().split(" ")[0]);
            toSendToServer.put("lastname", account.getDisplayName().split(" ")[account.getDisplayName().split(" ").length - 1]);
            toSendToServer.put("deviceid", DeviceID);
            toSendToServer.put("accesstoken", account.getIdToken());
            toSendToServer.put("type", "google");
            toSendToServer.put("fcmtoken", FirebaseInstanceId.getInstance().getToken());
            toSendToServer.put("accountid", account.getId());
            toSendToServer.put("imageuri", (mGoogleAccount.getPhotoUrl()==null) ? "https://placeimg.com/500/500/people" : mGoogleAccount.getPhotoUrl().toString());
            toSendToServer.put("fcmtoken", FirebaseInstanceId.getInstance().getToken());


        } catch (JSONException e) {
            e.printStackTrace();
        }
        mUser = new User(account.getDisplayName().split(" ")[0], account.getDisplayName().split(" ")[account.getDisplayName().split(" ").length - 1]);
        mUser.accountid = account.getId();
        Log.d(TAG, "ID Token +" + account.getIdToken());
        Log.d(TAG, "Google Login\n " + toSendToServer.toString());
        mSocket.emit("signup", toSendToServer);
    }

    public static void facebookSignUp() {
        if (Profile.getCurrentProfile() != null) {
            JSONObject toSendToServer = new JSONObject();
            try {

                toSendToServer.put("deviceid", DeviceID);
                toSendToServer.put("type", "facebook");
                toSendToServer.put("accountid", Profile.getCurrentProfile().getId());
                toSendToServer.put("accesstoken", AccessToken.getCurrentAccessToken().getToken());
                toSendToServer.put("firstname", Profile.getCurrentProfile().getFirstName());
                toSendToServer.put("lastname", Profile.getCurrentProfile().getLastName());
                toSendToServer.put("fcmtoken", FirebaseInstanceId.getInstance().getToken());
                toSendToServer.put("username", Profile.getCurrentProfile().getFirstName());
                toSendToServer.put("imageuri", Profile.getCurrentProfile().getProfilePictureUri(400, 400));


                Log.i("User Registration", toSendToServer.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mUser = new User(Profile.getCurrentProfile().getFirstName(), Profile.getCurrentProfile().getLastName());
            mUser.accountid = Profile.getCurrentProfile().getId();
            mSocket.emit("signup", toSendToServer);

        }
    }


    public static void login(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        switch (sharedPreferences.getString(QuickPreferences.loginType, "null")) {
            case "email":
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    // Name, email address, and profile photo Url
                    String name = user.getDisplayName();
                    String email = user.getEmail();
                    Uri photoUrl = user.getPhotoUrl();
                    String accessToken;
                    user.getToken(true)
                            .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                @Override
                                public void onComplete(@NonNull com.google.android.gms.tasks.Task<GetTokenResult> task) {
                                    if (task.isSuccessful()) {
                                        String idToken = task.getResult().getToken();
                                        try {
                                            JSONObject loginJSON = new JSONObject();
                                            loginJSON.put("accesstoken", idToken);
                                            loginJSON.put("type", "email");
                                            mSocket.emit("login", loginJSON);

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        // Send token to your backend via HTTPS
                                        // ...
                                    } else {
                                        // Handle error -> task.getException();
                                    }
                                }

                            });

                    // Check if user's email is verified
                    boolean emailVerified = user.isEmailVerified();

                    // The user's ID, unique to the Firebase project. Do NOT use this value to
                    // authenticate with your backend server, if you have one. Use
                    // FirebaseUser.getToken() instead.
                    String uid = user.getUid();

                }
            case "null":
                context.startActivity(new Intent(context, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return;
            case QuickPreferences.facebookLogin:
                String accesstoken = sharedPreferences.getString("accesstoken", null);
                String accountid = sharedPreferences.getString("accountid", null);

                if (accesstoken != null && accountid != null) {
                    try {
                        JSONObject loginJSON = new JSONObject();
                        loginJSON.put("accesstoken", accesstoken);
                        loginJSON.put("accountid", accountid);
                        loginJSON.put("type", "facebook");
                        mSocket.emit("login", loginJSON);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else
                    context.startActivity(new Intent(context, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                return;

            case QuickPreferences.googleLogin:
                accesstoken = sharedPreferences.getString("accesstoken", null);
                accountid = sharedPreferences.getString("accountid", null);

                if (accesstoken != null && accountid != null) {
                    try {
                        JSONObject loginJSON = new JSONObject();
                        loginJSON.put("accesstoken", accesstoken);
                        loginJSON.put("email", accountid);
                        loginJSON.put("type", "google");
                        mSocket.emit("login", loginJSON);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else
                    context.startActivity(new Intent(context, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                return;

            default:
                context.startActivity(new Intent(context, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                return;


        }


    }

    public static int loginMethod(Context context) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        switch (sharedPreferences.getString(QuickPreferences.loginType, "null")) {


            case QuickPreferences.facebookLogin:
                Log.d(TAG, "Facebook Login");
                return FACEBOOK_LOGIN;


            case QuickPreferences.googleLogin:
                Log.d(TAG, "Google Login");
                return GOOGLE_LOGIN;

            default:
                return 0;


        }
    }

    public LruBitmapCache getLruBitmapCache() {
        if (mLruBitmapCache == null)
            mLruBitmapCache = new LruBitmapCache();
        return this.mLruBitmapCache;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            getLruBitmapCache();
            mImageLoader = new ImageLoader(this.mRequestQueue, mLruBitmapCache);
        }

        return this.mImageLoader;
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public static synchronized Reptile getInstance() {
        return Instance;
    }

    public static void doRestart() {
        mSocket.disconnect();
        mAllTasks = new LinkedHashMap<>();
        ownTasks = new LinkedHashMap<>();
        mUserGroups = new LinkedHashMap<>();
        knownUsers = new LinkedHashMap<>();
        mSocket.connect();


    }
}