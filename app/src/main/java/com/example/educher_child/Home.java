package com.example.educher_child;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.educher_child.Database.AppLockDatabase;
import com.example.educher_child.Database.PrefManager;
import com.example.educher_child.Services.ForegroundService;
import com.example.educher_child.Services.ForegroundTasks;
import com.example.educher_child.Services.MyService;
import com.example.educher_child.Services.PermissionsCheck;
import com.example.educher_child.Services.ScheduleService;
import com.example.educher_child.Services.Service;
import com.example.educher_child.Services.UsageStat;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.example.educher_child.AppConfiguration.CHILD_ID;
import static com.example.educher_child.AppConfiguration.PARENT_KEY;
import static com.example.educher_child.AppConfiguration.SUGGESTION;

public class Home extends AppCompatActivity {


    private ConstraintLayout apps,usage;
    private TextView noti;
    private PrefManager prefManager;
    private DatabaseReference reference;
    private AppLockDatabase db;
    private int count;
    private String parent_key,childiD;
    private static final String TAG = "Dashboard";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //change status bar color

//        Window window = this.getWindow();
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimary));

        //Start the service to check for app usage
        startService(new Intent(getApplicationContext(), UsageStat.class));
        startService(new Intent(getApplicationContext(), Service.class));
        startService(new Intent(getApplicationContext(), MyService.class));
        startService(new Intent(getApplicationContext(), ForegroundTasks.class));
        startService(new Intent(getApplicationContext(), PermissionsCheck.class));

        Receiver r = new Receiver();
        IntentFilter i = new IntentFilter("com.example.educher_child");
        registerReceiver(r,i);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(Home.this, ForegroundService.class));
        }else{
            startService(new Intent(Home.this, ForegroundService.class));
        }
        startService(new Intent(getApplicationContext(), ScheduleService.class));

        count = 0;
        apps = findViewById(R.id.statictics);
        usage = findViewById(R.id.suggestion);
        noti = findViewById(R.id.noti);
        prefManager = new PrefManager(getApplicationContext());
        noti.setVisibility(View.GONE);
        apps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),AppUsageStatistics.class));
            }
        });

        usage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Suggestion.class));
            }
        });

        db = new AppLockDatabase(getApplicationContext());
        Cursor cursor = db.searchInAppDataTable(PARENT_KEY);
        cursor.moveToFirst();
        parent_key = cursor.getString(2);
        cursor.close();
        final Cursor c = db.searchInAppDataTable(CHILD_ID);
        c.moveToFirst();
        childiD = c.getString(2);
        c.close();

        //This method will be called directly by firebase when there is any change in the data that this reference is pointing to
        reference = FirebaseDatabase.getInstance().getReference(parent_key).child(childiD).child(SUGGESTION);
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                count++;
                Log.d(TAG, "onChildAdded: "+count);
                if (prefManager.getSugCount() !=0){
                    if (count>prefManager.getSugCount()){
                        noti.setVisibility(View.VISIBLE);
                        noti.setText(""+(count-prefManager.getSugCount())+"");

                    }
                }else{
                    prefManager.setSugCount(count);
                }

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

   public class Receiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean value = intent.getBooleanExtra("Status",true);
            if(!value){
                Dialog dialog;
                LayoutInflater layoutInflater = LayoutInflater.from(Home.this);
                View promptsView = layoutInflater.inflate(R.layout.activity_lock, null);

                dialog = new OverlayDialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
                    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                }else{
                    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                }
                dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                dialog.setContentView(promptsView);
                dialog.getWindow().setGravity(Gravity.CENTER);
            }
        }
    }
}
