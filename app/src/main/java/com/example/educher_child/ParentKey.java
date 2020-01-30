package com.example.educher_child;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.educher_child.Database.AppLockDatabase;
import com.example.educher_child.Database.PrefManager;
import com.example.educher_child.Model.AppInfo;
import com.example.educher_child.Model.ChildInfo;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.android.device.DeviceName;

import java.util.ArrayList;
import java.util.List;

import static com.example.educher_child.AppConfiguration.APPS;
import static com.example.educher_child.AppConfiguration.CHILD_ID;
import static com.example.educher_child.AppConfiguration.CONNECT;
import static com.example.educher_child.AppConfiguration.PARENT;
import static com.example.educher_child.AppConfiguration.PARENT_KEY;
import static com.example.educher_child.AppConfiguration.PARENT_TOKEN;
import static com.example.educher_child.AppConfiguration.TOKEN;

public class ParentKey extends AppCompatActivity {


    private boolean flag = false;

    //edittext to enter the parent_key
    private EditText parent_key;

    //submit button
    private Button submit;

    //Firebase Database Reference
    private DatabaseReference databaseReference,reference,ref;

    //String variable to hold the parent phone number
    private String key;

    //Firebase Auth Object
    private FirebaseAuth auth;

    //LinkedList of AppInfo to hold information of all apps
    private List<AppInfo> allApps;

    //Private and offline database
    private AppLockDatabase dataBaseHelper;

    private PackageManager packageManager;

    //SharedPreferences Manager
    private PrefManager prefManager;

    //private IOSDialog iosDialog;


    private static final String TAG = "ParentKey";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //change status bar color
//        Window window = this.getWindow();
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        window.setStatusBarColor(ContextCompat.getColor(this,R.color.color1));

        //Initialize a new PrefManager Object
        prefManager = new PrefManager(getApplicationContext());

        //Initialize connection to the offline database
        dataBaseHelper = new AppLockDatabase(getApplicationContext());

        //If the child is already connected to a parent move to the next activity
        Cursor cursor = dataBaseHelper.searchInAppDataTable(CONNECT);
        if (cursor.getCount() > 0){
            startActivity(new Intent(getApplicationContext(),Home.class));
            finish();
        }

        setContentView(R.layout.activity_parent_key);

//        iosDialog = new IOSDialog.Builder(this)
//                .setCancelable(false)
//                .setSpinnerClockwise(false)
//                .setMessageContentGravity(Gravity.END)
//                .build();
//
       init();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUserUnderParent();
                startActivity(new Intent(getApplicationContext(),Home.class));
                //iosDialog.cancel();
                finish();
            }
        });

    }

    //Initialize all the views
    private void init(){
        parent_key = findViewById(R.id.parent_key);
        submit = findViewById(R.id.parent_submit);
        allApps = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        FirebaseApp.initializeApp(this);
    }

    public void addUserUnderParent(){

        //get the parent phone number
        key = parent_key.getText().toString().trim();
        Toast.makeText(this, ""+key, Toast.LENGTH_SHORT).show();

        if (key.isEmpty()){
            parent_key.setError("Please Enter Key");
            return;
        }

        //Get the reference to monitor value changes under parent
        ref = FirebaseDatabase.getInstance().getReference().child(key).child(TOKEN);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String t = dataSnapshot.child("token").getValue().toString();
                Log.d(TAG, "onDataChange: "+t);
                dataBaseHelper.insertDataInAppDataTable(PARENT_TOKEN,t);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //save parent_key which is the parent phone number in SQlite database
        dataBaseHelper.insertDataInAppDataTable(PARENT_KEY,key);

        //save child_id in the local database
        dataBaseHelper.insertDataInAppDataTable(CHILD_ID,"923046406229");

        Cursor childiD = dataBaseHelper.searchInAppDataTable(CHILD_ID);
        childiD.moveToFirst();

        //link to upload app in firebase
        databaseReference = FirebaseDatabase.getInstance().getReference(key)
                .child(childiD.getString(2)).child(APPS);

        //to upload child key and phone name
        reference = FirebaseDatabase.getInstance().getReference(PARENT).child(key);
        ChildInfo childInfo = new ChildInfo(childiD.getString(2), DeviceName.getDeviceName());
        reference.push().setValue(childInfo);

        allApps = getListOfInstalledApp(getApplicationContext());

        assert allApps != null;
        for (AppInfo apps:allApps){

            //Insert all the apps to the local database
            dataBaseHelper.insertApps(apps.getName(),apps.getPackageName(),false);
        }
        dataBaseHelper.insertApps("Phone Screen",louncherPackage(),false);

        Cursor cursor = dataBaseHelper.fetchAllAPPFomDataBase();
        while (cursor.moveToNext()){
            int id = cursor.getInt(0);
            String appName = cursor.getString(1);
            String package_name = cursor.getString(2);
            Boolean lock = cursor.getInt(3) > 0;
            AppInfo info = new AppInfo(id,appName,package_name,lock);
            databaseReference.child(package_name.replace(".","_")).setValue(info);
        }
        cursor.close();
        childiD.close();

        //Insert connect into the database to show that the child is connected to a parent
        dataBaseHelper.insertDataInAppDataTable(CONNECT,"true");

    }

    //return list of all the installed Applications
    public List<AppInfo> getListOfInstalledApp(Context context) {

        //Retreive an instance of PackageManager
        packageManager = context.getPackageManager();


        List<AppInfo> installedApps = new ArrayList();
        List<PackageInfo> apps = packageManager.getInstalledPackages(PackageManager.GET_SIGNATURES);
        if (apps != null && !apps.isEmpty()) {

            for (int i = 0; i < apps.size(); i++) {
                PackageInfo p = apps.get(i);
                ApplicationInfo appInfo = null;
                try {
                    appInfo = packageManager.getApplicationInfo(p.packageName, 0);

                    //Skip our app
                    if (p.packageName.equals("com.example.educher_child")){

                    }else if((p.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0){
                        if (p.packageName.equals("com.android.browser")){
                            installedApps.add(addAppInList(p));
                        }else if (p.packageName.equals("com.android.camera2")){
                            installedApps.add(addAppInList(p));
                        }else if (p.packageName.equals("com.android.chrome")){
                            installedApps.add(addAppInList(p));
                        }else if (p.packageName.equals("com.android.phone")){
                            installedApps.add(addAppInList(p));
                        }else if (p.packageName.equals("com.android.settings")){
                            prefManager.setSettingPackage("com.android.settings");
                            installedApps.add(addAppInList(p));
                        }else if (p.packageName.equals("com.google.android.youtube")){
                            installedApps.add(addAppInList(p));
                        }else if (p.packageName.equals("com.google.android.videos")){
                            installedApps.add(addAppInList(p));
                        }else if (p.packageName.equals("com.google.android.apps.photos")){
                            installedApps.add(addAppInList(p));
                        }else if (p.packageName.equals("com.huawei.camera")){
                            installedApps.add(addAppInList(p));
                        }else if (p.packageName.equals("com.android.gallery3d")){
                            installedApps.add(addAppInList(p));
                        }else if (p.packageName.equals("com.android.contacts")){
                            installedApps.add(addAppInList(p));
                        }else if (p.packageName.equals("com.oppo.camera")){
                            installedApps.add(addAppInList(p));
                        }else if (p.packageName.equals("com.android.gallery")){
                            installedApps.add(addAppInList(p));
                        }else if (p.packageName.equals("com.android.providers.downloads.ui")){
                            installedApps.add(addAppInList(p));
                        }else if (p.packageName.equals("com.android.vending")){
                            installedApps.add(addAppInList(p));
                        }else if (p.packageName.equals("com.android.camera")){
                            installedApps.add(addAppInList(p));
                        }else if (p.packageName.equals("com.android.incallui")){
                            installedApps.add(addAppInList(p));
                        }else if (p.packageName.equals("com.android.mms")){
                            installedApps.add(addAppInList(p));
                        }else if (p.packageName.equals("com.android.providers.downloads")){
                            installedApps.add(addAppInList(p));
                        }else if (p.packageName.equals("com.google.android.apps.messaging")){
                            installedApps.add(addAppInList(p));
                        }else if (p.packageName.equals("com.google.android.dialer")){
                            installedApps.add(addAppInList(p));
                        }else if (p.packageName.equals("com.android.dialer")){
                            installedApps.add(addAppInList(p));
                        }
                        else if (p.packageName.equals("com.android.systemui")){
                            flag = true;
                            prefManager.setSystemui("com.android.systemui");
                            installedApps.add(addAppInList(p));
                        }else{
                            Log.d(TAG, "getListOfInstalledApp: system");
                        }
                    }
                    else {
                        installedApps.add(addAppInList(p));
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.d(TAG, "getListOfInstalledApp: "+e.getMessage());
                    e.printStackTrace();
                }
            }


            return installedApps;
        }
        return null;
    }

    private AppInfo addAppInList(PackageInfo p){
        AppInfo app = new AppInfo();
        app.setName(p.applicationInfo.loadLabel(packageManager).toString());
        app.setPackageName(p.packageName);
        app.setLocked(false);
        return app;
    }

    private String louncherPackage(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        String currentHomePackage = resolveInfo.activityInfo.packageName;
        prefManager.setLauncherPackage(currentHomePackage);
        return currentHomePackage;
    }
}
