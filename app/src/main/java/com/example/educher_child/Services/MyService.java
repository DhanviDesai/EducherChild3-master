package com.example.educher_child.Services;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.educher_child.Database.AppLockDatabase;
import com.example.educher_child.Model.AppInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.example.educher_child.AppConfiguration.APPS;
import static com.example.educher_child.AppConfiguration.CHILD_ID;
import static com.example.educher_child.AppConfiguration.PARENT_KEY;
import static com.example.educher_child.AppConfiguration.SCHEDULE_SINGLE;

public class MyService extends Service {
    private List<AppInfo> infos;
    private static final String TAG = "MyService";
    private AppLockDatabase dbHelper;
    private AppLockDatabase db;
    private Calendar c;
    private List<String> from = new ArrayList<>();
    private List<String > to = new ArrayList<>();
    private String parent_key,childiD;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        fetchDataFromFireBase();
        fetchSingleschdule();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void init(){
        infos = new ArrayList<>();
        dbHelper = new AppLockDatabase(getApplicationContext());
        c = Calendar.getInstance();
        db = new AppLockDatabase(getApplicationContext());
        Cursor cursor = dbHelper.searchInAppDataTable(PARENT_KEY);
        cursor.moveToFirst();
        parent_key = cursor.getString(2);
        cursor.close();
        Cursor c = dbHelper.searchInAppDataTable(CHILD_ID);
        c.moveToFirst();
        childiD = c.getString(2);
        c.close();

    }

    private void fetchDataFromFireBase(){

        DatabaseReference reference;

        reference = FirebaseDatabase.getInstance().getReference(parent_key).child(childiD).child(APPS);
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String id = dataSnapshot.child("id").getValue().toString();
                String locked = dataSnapshot.child("locked").getValue().toString();
                updateDataIntoSQLiteDataBase(id,Boolean.parseBoolean(locked));

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {


                String id = dataSnapshot.child("id").getValue().toString();
                String locked = dataSnapshot.child("locked").getValue().toString();
                updateDataIntoSQLiteDataBase(id,Boolean.parseBoolean(locked));
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {



            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateDataIntoSQLiteDataBase(String id,Boolean isLock){
        AppLockDatabase helper = new AppLockDatabase(getApplicationContext());
        helper.updateAppState(id,isLock);
    }

    private void fetchSingleschdule(){

        DatabaseReference reference;

        reference = FirebaseDatabase.getInstance().getReference(parent_key).child(childiD).child(SCHEDULE_SINGLE);
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //Get the values from firebase
                String app = dataSnapshot.child("app").getValue().toString();
                String day = dataSnapshot.child("day").getValue().toString();
                String time = dataSnapshot.child("time").getValue().toString();

                //update the values in the local database
                saveToLocalDatabase(app,day,time);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                //Write code here to remove schedule of the app

                String app = dataSnapshot.child("app").getValue().toString();
                deleteFromDatabase(app);



            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveToLocalDatabase(String app,String day,String time){

        //query the current usage of the app
        Cursor cursor = db.getusageApp(app);

        //if the user has used the app
        if (cursor.getCount()>0){
            cursor.moveToFirst();

            //update the allow time to the new value
            db.updateAllowTime(app,time,day);

        //if the user has not opened the app
        }else{

            //insert fresh values in the database for the app
            db.insert(app,""+time,day);
        }

    }

    private void deleteFromDatabase(String appName){
        db.deleteScheduleremove(appName);
    }
}
