package com.example.educher_child;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;

import com.example.educher_child.Database.AppLockDatabase;
import com.example.educher_child.Database.PrefManager;
import com.example.educher_child.Services.ForegroundService;
import com.example.educher_child.Services.MyService;
import com.example.educher_child.Services.ScheduleService;
import com.example.educher_child.Services.Service;
import com.example.educher_child.Services.UsageStat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.example.educher_child.AppConfiguration.APPS;
import static com.example.educher_child.AppConfiguration.CHILD_ID;
import static com.example.educher_child.AppConfiguration.PARENT_KEY;

public class NetworkBroadcast extends BroadcastReceiver {

    private PrefManager prefManager;
    private AppLockDatabase dataBaseHelper;
    private String parent,child;
    private DatabaseReference reference;
    private Context con;

    @Override
    public void onReceive(Context context, Intent intent) {

        con = context;
        prefManager = new PrefManager(context);
        dataBaseHelper = new AppLockDatabase(context);

        Cursor parent_key = dataBaseHelper.searchInAppDataTable(PARENT_KEY);
        parent_key.moveToFirst();
        parent= parent_key.getString(2);
        parent_key.close();

        Cursor child_id = dataBaseHelper.searchInAppDataTable(CHILD_ID);
        child_id.moveToFirst();
        child = child_id.getString(2);
        child_id.close();

        reference = FirebaseDatabase.getInstance().getReference().child(parent).child(child).child(APPS);


        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            try {


                reference.child(prefManager.getSettingPackage().replace(".","_")).child("locked").setValue(prefManager.getIsSettingBlock());
                reference.child(prefManager.getLauncherPackage().replace(".","_")).child("locked").setValue(prefManager.getIsLauncherBlock()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        con.startService(new Intent(con, UsageStat.class));
                        con.startService(new Intent(con, Service.class));
                        con.startService(new Intent(con, MyService.class));
                        con.startService(new Intent(con, ForegroundService.class));
                        con.startService(new Intent(con, ScheduleService.class));
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

        }

    }
}
