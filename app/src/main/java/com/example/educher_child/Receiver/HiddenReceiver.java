package com.example.educher_child.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.educher_child.OverlayDialog;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.example.educher_child.AppConfiguration.APPS;

public class HiddenReceiver extends BroadcastReceiver {

    private Context context;
    String parent_key;
    String child_id;
    String launcherPackage;
    String systemUIPackage;

    public HiddenReceiver(String launcherPackage,String systemUIPackage,String parent_key,String child_id){
        this.parent_key = parent_key;
        this.child_id = child_id;
        this.launcherPackage = launcherPackage;
        this.systemUIPackage = systemUIPackage;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean status = intent.getBooleanExtra("Status",false);
        if(!status) {

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                    .child(parent_key).child(child_id).child(APPS);
            reference.child(launcherPackage).child("locked").setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.i("Locked", "Child hidden");
                }
            });

            if (systemUIPackage != null) {

                reference.child(systemUIPackage).child("locked").setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("Locked", "Child Hidden");
                    }
                });

            }
        }

    }
}
