package com.example.educher_child.Services;

import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.educher_child.Database.AppLockDatabase;
import com.example.educher_child.Model.SchModel;
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

public class Service extends android.app.Service {
    private static final String TAG = "Schedule";
    private Handler handler= new Handler();
    private List<String> from = new ArrayList<>();
    private List<String > to = new ArrayList<>();
    private List<SchModel> packageName = new ArrayList<>();
    private Calendar c;
    private AppLockDatabase db;
    private DatabaseReference reference;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");

        db = new AppLockDatabase(getApplicationContext());

        Cursor parent_key = db.searchInAppDataTable(PARENT_KEY);
        parent_key.moveToFirst();

        Cursor childiD = db.searchInAppDataTable(CHILD_ID);
        childiD.moveToFirst();


        //link to upload app in firebase
        reference = FirebaseDatabase.getInstance().getReference(parent_key.getString(2)).child(childiD.getString(2)).child(APPS);

        parent_key.close();
        childiD.close();

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String pkg = dataSnapshot.getKey();
                Log.d(TAG, "onChildAdded: "+pkg);
                String id = dataSnapshot.child("id").getValue().toString();
                SchModel model = new SchModel(id,pkg);
                packageName.add(model);

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


        parent_key.close();
        childiD.close();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: start service");
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    private void updateDataIntoSQLiteDataBase(String id,Boolean isLock){
        AppLockDatabase helper = new AppLockDatabase(getApplicationContext());
        helper.updateAppState(id,isLock);
    }

}
