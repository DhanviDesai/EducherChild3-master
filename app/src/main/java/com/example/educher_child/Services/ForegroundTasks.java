package com.example.educher_child.Services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.database.core.Context;
import com.rvalerio.fgchecker.AppChecker;

import java.util.List;

public class ForegroundTasks extends Service {

AppChecker appChecker;

    @Override
    public void onCreate() {
        super.onCreate();

        appChecker = new AppChecker();
        appChecker.whenAny(new AppChecker.Listener() {
            @Override
            public void onForeground(String process) {
                Log.i("ForegroundProcess",process);
                ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> appProcess = activityManager.getRunningAppProcesses();
                for (ActivityManager.RunningAppProcessInfo app : appProcess){
                    if(app.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                        Log.i("ForegroundTask",app.processName);
                    }
                }
            }
        }).timeout(1000).start(getApplicationContext());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        Log.d("ForegroundTasks", "onStartCommand: ");
        return START_STICKY;
    }
}
