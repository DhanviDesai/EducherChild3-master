package com.example.educher_child.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.educher_child.MainActivity;

public class PermissionsCheck extends Service {

    private Handler handler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        permissionCheck();
    }

    private void permissionCheck(){
        Runnable code = new Runnable() {
            @Override
            public void run() {
                Log.i("hereinPermissions","Joojo");
                if(!Settings.canDrawOverlays(getApplicationContext())){

                    Log.i("Here in no","Heeeee");

                    Intent i = new Intent();
                    i.setAction("com.example.childhidden");
                    i.putExtra("Status",Settings.canDrawOverlays(getApplicationContext()));
                    sendBroadcast(i);


                }

                handler.removeCallbacks(this);
                handler.postDelayed(this,500);
            }
        };

        handler.removeCallbacks(code);
        handler.post(code);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
