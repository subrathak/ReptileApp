package com.reptile.nomad.changedReptile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import com.facebook.AccessToken;
//import com.facebook.CallbackManager;
//import com.facebook.FacebookSdk;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    GoogleSignInOptions GSO;
    GoogleApiClient mGoogleApiClient;
    GoogleSignInAccount mGoogleAccount = null;



//    @Bind(R.id.login_button)
//    LoginButton loginButton;

//    CallbackManager callbackManager;

//    @Bind(R.id.fb_sign_in_button)
//    ImageView fb;

    TextView emailTextView;

    TextView passwordTextView;

    Button loginButton, manualLogin;

    ImageView signInButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add code to print out the key hash
        FirebaseAuth.getInstance();
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.reptile.nomad.ReptileApp",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }


        GSO = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestProfile().requestIdToken(getString(R.string.google_server_client_id)).requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {

                    }
                }).addApi(Auth.GOOGLE_SIGN_IN_API,GSO).build();


        if (!FirebaseApp.getApps(this).isEmpty()) {
//            FacebookSdk.sdkInitialize(getApplicationContext());
        }
//        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_login);
        emailTextView = (TextView)findViewById(R.id.email);
        passwordTextView = (TextView)findViewById(R.id.passwordEt);
        loginButton = (Button) findViewById(R.id.button2);
        manualLogin = (Button)findViewById(R.id.button3);

        manualLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, LoginActivityEmail.class));

            }
        });

//        ButterKnife.bind(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, password;
                email = emailTextView.getText().toString();
                password = passwordTextView.getText().toString();
                JSONObject toSend = new JSONObject();
                try {
                    toSend.put("email",email);
                    toSend.put("password",password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Reptile.mSocket.emit("login",toSend);
                Log.d("guru","guru");
            }
        });

//        callbackManager = CallbackManager.Factory.create();
//        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//            private ProfileTracker mProfileTracker;
//            @Override
//            public void onSuccess(final LoginResult loginResult) {
//                if(Profile.getCurrentProfile()==null)
//                {
//                    mProfileTracker = new ProfileTracker() {
//                        @Override
//                        protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
//                            facebookProfileSignIn(currentProfile,loginResult);
//                        }
//                    };
//                }
//                else
//                {
//                    facebookProfileSignIn(Profile.getCurrentProfile(),loginResult);
//                }
//                Log.e(TAG, "Login Success!");
//
//
//            }
//
//            @Override
//            public void onCancel() {
//                Log.e(TAG,"Login Cancelled");
//            }
//
//            @Override
//            public void onError(FacebookException error) {
//                error.printStackTrace();
//                Log.e(TAG,"Login Error");
//            }
//            private void facebookProfileSignIn(Profile profile,LoginResult loginResult)
//            {
//                SharedPreferences.Editor editor =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
//                editor.putString(QuickPreferences.accesstoken,loginResult.getAccessToken().getToken());
//                editor.putString(QuickPreferences.accountid, loginResult.getAccessToken().getUserId());
//                editor.putString(QuickPreferences.tokenExpiry,loginResult.getAccessToken().getExpires().toString());
//                editor.putString(QuickPreferences.loginType,QuickPreferences.facebookLogin);
//                editor.putString("pictureURI",profile.getProfilePictureUri(400,400).toString());
//                editor.apply();
//                Reptile.facebookSignUp();
//                startActivity(new Intent(getApplicationContext(), MainActivity.class));
//            }
//        });
//        signInButton = (ImageView) findViewById(R.id.google_sign_in_button);


//        signInButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
//                SharedPreferences.Editor editor =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
//                editor.putString(QuickPreferences.loginType,QuickPreferences.googleLogin);
//
//                editor.apply();
//                startActivityForResult(signInIntent,9001);
//            }
//        });

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        if(FacebookSdk.isInitialized())
//        {
//            if(AccessToken.getCurrentAccessToken()!=null)
//            {
//                startActivity(new Intent(getApplicationContext(),MainActivity.class));
//            }
//        }else if(mGoogleAccount != null && sharedPreferences.getString(QuickPreferences.accesstoken,null) != null ){
//            startActivity(new Intent(getApplicationContext(),MainActivity.class));
//        }else {
//            Intent intent = getIntent();
//            finish();
//            startActivity(intent);
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Activity Result", String.valueOf(requestCode)+"!"+ resultCode);

        if(requestCode==9001)
        {

                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result.isSuccess()) {
                    mGoogleAccount = result.getSignInAccount();
                    firebaseAuthWithGoogle(mGoogleAccount);
                    Reptile.mGoogleAccount = mGoogleAccount;
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                    sharedPreferences.edit()
                            .putString(QuickPreferences.accountid, mGoogleAccount.getId())
                            .putString(QuickPreferences.accesstoken, mGoogleAccount.getIdToken())
                            .putString("pictureURI", (mGoogleAccount.getPhotoUrl() == null) ? "https://placeimg.com/500/500/people" : mGoogleAccount.getPhotoUrl().toString())
                            .putString("fullname", mGoogleAccount.getDisplayName())
                            .apply();
                    Reptile.googleSignUp(mGoogleAccount);
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                } else {
                    Log.e("Err Login", result.getStatus().toString() + result.getStatus().getStatusMessage());
                    Toast.makeText(getApplicationContext(),"Login Failed",Toast.LENGTH_LONG).show();
                }



        }
        else {
//            callbackManager.onActivityResult(requestCode, resultCode, data);
//            if (AccessToken.getCurrentAccessToken() != null) {
//                startActivity(new Intent(getApplicationContext(), MainActivity.class));
//            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onBackPressed() {

                ActivityCompat.finishAffinity(this);
                finish();
    }

    public void onClick(View v) {

//        loginButton.performClick();

    }

    public void manualAuth(View view) {
        Intent loginManually = new Intent(this,loginManually.class);
        startActivity(loginManually);
    }
}