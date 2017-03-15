package com.reptile.nomad.ReptileApp;

import android.*;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentPagerAdapter;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
//import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.reptile.nomad.ReptileApp.Adapters.NewsFeedFragmentPagerAdapter;
import com.reptile.nomad.ReptileApp.Fragments.BlankFragment;
import com.reptile.nomad.ReptileApp.Models.User;
import com.reptile.nomad.ReptileApp.Fragments.FragmentNewsFeed;
import com.reptile.nomad.ReptileApp.Services.DeadlineTrackerService;
import com.reptile.nomad.ReptileApp.Services.MyFirebaseInstanceIDService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.support.v7.widget.RecyclerView;
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private final int GALLERY_ACTIVITY_CODE=200;
    ViewPager mViewPager;
    public static MainActivity Instance;
    TabLayout tabLayout;
    private long backPressedTime = 0;
    public static List<RecyclerView.Adapter> viewAdapters;
    HashMap<String,User> searchedUsers;
    Timer searchUserTimer;
    public static String TAG = "Main Activity";
    public static GoogleApiClient mGoogleApiClient;
    TextView nameTextView;
    ImageView profilePicture;
    List<FragmentNewsFeed> fragmentList;
    SoftKeyboard softKeyboard;
    static SearchEditText searchingEditText;
    private static boolean searchingToggle;
    private String user_dp_path;
    private final int RESULT_CROP = 400;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2343;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    @Override
    protected void onResume() {
        super.onResume();
        if(!Reptile.mSocket.connected())
        {
            Reptile.mSocket.connect();
        }
        if(Profile.getCurrentProfile()!=null&&Reptile.loginMethod(getApplicationContext())==Reptile.FACEBOOK_LOGIN) {
            nameTextView.setText(Profile.getCurrentProfile().getName());
        }
        else if(Reptile.loginMethod(getApplicationContext())==Reptile.GOOGLE_LOGIN)
        {
            nameTextView.setText(PreferenceManager.getDefaultSharedPreferences(this).getString("fullname"," "));
        }
        if(QuickPreferences.usernameSelected){
            nameTextView.setText(Reptile.mUser.userName);
        }
        ImageLoader imageLoader = ImageLoader.getInstance();
        if(Reptile.mUser==null||Reptile.mUser.imageURI==null)
            imageLoader.displayImage(PreferenceManager.getDefaultSharedPreferences(this).getString("pictureURI"," "),profilePicture);
        else
            imageLoader.displayImage(Reptile.mUser.imageURI,profilePicture);

    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Log.d(TAG, "Got Activity Result with Result Code "+ String.valueOf(resultCode)+ "And Request Code " + String.valueOf(requestCode));
//
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchingToggle=false;
        Instance = this;
        viewAdapters = new ArrayList<>();
        CoordinatorLayout mainLayout =(CoordinatorLayout
                ) findViewById(R.id.main_layout); // You must use your root layout
        InputMethodManager im = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);

        softKeyboard = new SoftKeyboard(mainLayout,im);
        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivityEmail.class));
                    finish();
                }
            }
        };
        try {
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            Log.e(TAG, "Refreshed Token =" + refreshedToken);
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("fireToken", refreshedToken).commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mViewPager = (ViewPager) findViewById(R.id.container2);
        tabLayout = (TabLayout) findViewById(R.id.tabs);



        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        mViewPager = (ViewPager) findViewById(R.id.container2);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position==1)
                {
                    fab.setVisibility(View.VISIBLE);
                }
                else
                {
                    fab .setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setCurrentItem(0);
        fab.setVisibility(View.INVISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),CreateTaskActivity.class));
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
//        drawer.setBackgroundResource(R.drawable.cover_reptile);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        nameTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.NameTextView);
        profilePicture = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.profilePicture);
        fragmentList = new ArrayList<FragmentNewsFeed>();
        fragmentList.add(FragmentNewsFeed.newInstance(FragmentNewsFeed.FEED));
        fragmentList.add(FragmentNewsFeed.newInstance(FragmentNewsFeed.PROFILE));

        NewsFeedFragmentPagerAdapter NewsFeedPagerAdapter = new NewsFeedFragmentPagerAdapter(getSupportFragmentManager(),fragmentList);
        mViewPager.setAdapter(NewsFeedPagerAdapter);
        mViewPager.setCurrentItem(1);
        tabLayout.setupWithViewPager(mViewPager);
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(Instance,
                        Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {


                        ActivityCompat.requestPermissions(Instance,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);


                }
                selectImage();
            }
        });

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                nameTextView.setText(Reptile.mUser.userName);
            }
        }, new IntentFilter("logged-in"));
        setupTabIcons();
        if(Reptile.loginMethod(getApplicationContext())==Reptile.FACEBOOK_LOGIN) {
            String userID = null;
            try {
                userID = Profile.getCurrentProfile().getId();
            } catch (Exception e) {
                SharedPreferences.Editor editor =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putString(QuickPreferences.loginType,"null");
                e.printStackTrace();
            }

            try {
                URL imageURL = new URL("https://graph.facebook.com/" + userID + "/picture?type=large");

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        Intent fcmrefresh = new Intent(this, MyFirebaseInstanceIDService.class);
        startService(fcmrefresh);
        if(!isMyServiceRunning(DeadlineTrackerService.class)){
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent serviceIntetnt = new Intent(MainActivity.this,DeadlineTrackerService.class);
                    serviceIntetnt.setAction("track");
                    startService(serviceIntetnt);
                }
            }, 10000);

        }

        SharedPreferences settings = getSharedPreferences(QuickPreferences.appStatusSharedPreference, 0);
        if(settings.getInt("firstTime",1) == 1){
            startActivity(new Intent(this, Username.class));
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if(searchingToggle)
        {
            final ActionBar actionBar = getSupportActionBar();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchingEditText.getWindowToken(), 0);
            searchingToggle=false;
            actionBar.setDisplayShowCustomEnabled(false); //disable a customRadioButton view inside the actionbar
            actionBar.setDisplayShowTitleEnabled(true); //show the title in the action bar
        }
        else {
            long t = System.currentTimeMillis();
            if (t - backPressedTime > 2000) {    // 2 secs
                backPressedTime = t;
                Toast.makeText(this, "Press back again to exit",
                        Toast.LENGTH_SHORT).show();
            } else {    // this guy is serious
                // clean up
//                super.onBackPressed();       // bye
                ActivityCompat.finishAffinity(this);
                finish();
//                System.exit(0);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        final ActionBar actionBar = getSupportActionBar();
        //noinspection SimplifiableIfStatement
        switch (id)
        {
            case R.id.action_settings:
                return true;
            case R.id.menu_refresh:
                final int CurrentPage = mViewPager.getCurrentItem();
                fragmentList.get(CurrentPage).mSwipeRefresh.setRefreshing(true);
                Reptile.mSocket.emit("addtasks");
                Reptile.mSocket.emit("addusers");
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Instance.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fragmentList.get(CurrentPage).mSwipeRefresh.setRefreshing(false);
                            }
                        });
                    }
                },800);
//                fragmentList.get(CurrentPage).mSwipeRefresh.setRefreshing(false);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        //TODO Replace with Switch Case
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            startActivity(new Intent(this, Username.class));

        }
//        else if (id == R.id.nav_following) {
//
//        }
//        else if(id == R.id.nav_groups)
//        {
//            startActivity(new Intent(this,ManageGroups.class));
//        }
//
//        else if (id == R.id.nav_settings) {
//            Intent intent = new Intent(this,PreferencesActivity.class);
//            startActivity(intent);
//
//        }
        else if (id == R.id.nav_create_task) {

            startActivity(new Intent(getApplicationContext(),CreateTaskActivity.class));


        } else if(id == R.id.logout_menu_item)
        {
            Reptile.hasLoggedIn = false;
            if(Reptile.loginMethod(getApplicationContext())==Reptile.FACEBOOK_LOGIN) {
                LoginManager.getInstance().logOut();

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.remove(QuickPreferences.tokenExpiry);
                editor.remove(QuickPreferences.accesstoken);
                editor.remove(QuickPreferences.accountid);
                editor.remove(QuickPreferences.loginType);
                editor.commit();
                Reptile.doRestart();
                startActivity(new Intent(this,splash.class));
            }
            else
            {
                if(Reptile.loginMethod(getApplicationContext())==Reptile.GOOGLE_LOGIN)
                {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                    editor.remove(QuickPreferences.tokenExpiry);
                    editor.remove(QuickPreferences.accesstoken);
                    editor.remove(QuickPreferences.accountid);
                    editor.remove(QuickPreferences.loginType);
                    editor.commit();
                    Auth.GoogleSignInApi.signOut(Reptile.mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                        }
                    });
                }
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        Reptile.mSocket.emit("addusers");
        Reptile.mSocket.emit("addtasks");


    }

    @Override
    public void onStop() {
        super.onStop();

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return BlankFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }

    private void setupTabIcons() {
        int[] tabIcons = {
                R.drawable.ic_public_white_24dp,
                R.drawable.ic_people_outline_white_24dp,
                R.drawable.ic_whatshot_white_24dp
        };

        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[2]);

    }
    public void makeErrorToast(){
        Toast.makeText(getApplicationContext(),"Aw, snap",Toast.LENGTH_LONG).show();
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,30, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }



   public void sendProfilePicture(Bitmap bitmap)
   {
       String imageString = BitMapToString(bitmap);
       JSONObject toSendToServer = new JSONObject();
       try
       {
           toSendToServer.put("image",imageString);
           toSendToServer.put("user",Reptile.mUser.id);

       }catch (JSONException e)
       {
           e.printStackTrace();

       }
       Reptile.mSocket.emit("changePicture",toSendToServer);
   }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private void selectImage() {



        final CharSequence[] options = {  "Choose from Gallery","Cancel" };



        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("Change Profile Picture");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Choose from Gallery"))

                {

                    Intent gallery_Intent=new Intent (getApplicationContext(),GalleryUtil.class);
                    startActivityForResult(gallery_Intent,GALLERY_ACTIVITY_CODE);

                }

                else if (options[item].equals("Cancel")) {

                    dialog.dismiss();

                }

            }

        });

        builder.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(TAG, "Got Activity Result with Result Code "+ String.valueOf(resultCode)+ "And Request Code " + String.valueOf(requestCode));
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if (requestCode == 2) {

                Intent gallery_Intent = new Intent(getApplicationContext(), GalleryUtil.class);
                startActivityForResult(gallery_Intent, GALLERY_ACTIVITY_CODE);
            }
            if (requestCode == GALLERY_ACTIVITY_CODE) {
                if(resultCode == Activity.RESULT_OK){
                    String picturePath = data.getStringExtra("picturePath");
                    //perform Crop on the Image Selected from Gallery
//                    sendImage(picturePath);
                    performCrop(picturePath);
//=======
//                    Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
//                    sendProfilePicture(bitmap);
//>>>>>>> fd72c7d43398ef32730e5ffeefbff47dcd15a3bb
                }
            }

            if (requestCode == RESULT_CROP ) {
                if(resultCode == Activity.RESULT_OK){
                    Bundle extras = data.getExtras();
                    Bitmap selectedBitmap = extras.getParcelable("data");
                    storeImage(selectedBitmap);

                    sendProfilePicture(selectedBitmap);
                    // Set The Bitmap Data To ImageView
                    profilePicture.setImageBitmap(selectedBitmap);
                    Log.d("DP","Got cropped image");

                }
            }
            if(requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
            {
                selectImage();
            }
        }
    }

    private void performCrop(String picUri) {
        try {
            //Start Crop Activity

            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            File f = new File(picUri);
            Uri contentUri = Uri.fromFile(f);

            cropIntent.setDataAndType(contentUri, "image/*");
            // set crop properties
            cropIntent.putExtra("crop", "true");
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 280);
            cropIntent.putExtra("outputY", 280);

            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, RESULT_CROP);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {

            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();



        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    private  File getOutputMediaFile(){
        // might need  Environment.getExternalStorageState()
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");
        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        user_dp_path = mediaStorageDir.getPath() + File.separator + mImageName;
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit()
                .putString("pet_image_name",user_dp_path)
                .apply();
        mediaFile = new File(user_dp_path);
        Log.d("fileSaved",user_dp_path);
//        sendImage(user_dp_path); ////////////////////SENDING TO SERVER HERE
        return mediaFile;
    }

//    public void sendImage(String path)
//    {
//        JSONObject sendData = new JSONObject();
//        try{
//            sendData.put("image", encodeImage(path));
//            sendData.put("user",Reptile.mUser.id);
//            Reptile.mSocket.emit("changePicture",sendData);
//            Log.d("SendImage","sending image");
//        }catch(JSONException e){
//        }
//    }

//    private String encodeImage(String path)
//    {
//        File imagefile = new File(path);
//        FileInputStream fis = null;
//        try{
//            fis = new FileInputStream(imagefile);
//        }catch(FileNotFoundException e){
//            e.printStackTrace();
//        }
//        Bitmap bm = BitmapFactory.decodeStream(fis);
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
//        byte[] b = baos.toByteArray();
//        String encImage = Base64.encodeToString(b, Base64.DEFAULT);
//        //Base64.de
//        return encImage;
//
//    }



}


