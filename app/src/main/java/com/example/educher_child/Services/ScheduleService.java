package com.example.educher_child.Services;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.educher_child.Database.AppLockDatabase;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.example.educher_child.AppConfiguration.CHILD_ID;
import static com.example.educher_child.AppConfiguration.PARENT_KEY;
import static com.example.educher_child.AppConfiguration.SCHEDULE;
import static com.example.educher_child.AppConfiguration.SCHEDULE_SINGLE;

public class ScheduleService extends Service {
    private AppLockDatabase dbHelper;
    private AppLockDatabase db;
    private String parent_key,childiD;
    private static final String TAG = "ServiceForSchedule";
    String from=null;
    String to=null;
    String day=null;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new AppLockDatabase(getApplicationContext());
        db = new AppLockDatabase(getApplicationContext());
        Cursor cursor = dbHelper.searchInAppDataTable(PARENT_KEY);
        cursor.moveToFirst();
        parent_key = cursor.getString(2);
        cursor.close();
        Cursor c = dbHelper.searchInAppDataTable(CHILD_ID);
        c.moveToFirst();
        childiD = c.getString(2);
        c.close();
        fetchFromFirebase();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void fetchFromFirebase()
    {
        try{
            dbHelper.deleteall();
        }catch (Exception e){
            Log.d(TAG, "fetchFromFirebase: "+e.getMessage());
        }
        DatabaseReference reference;
        reference = FirebaseDatabase.getInstance().getReference(parent_key).child(childiD).child(SCHEDULE);

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
             day = dataSnapshot.child("day").getValue().toString();
             from = dataSnapshot.child("from").getValue().toString();
             to = dataSnapshot.child("to").getValue().toString();
             insertIntoDatabase(from,to,day);


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void insertIntoDatabase(String from,String to,String day){
        AppLockDatabase helper = new AppLockDatabase(getApplicationContext());
        helper.insertData(from,to,day);
    }
}
