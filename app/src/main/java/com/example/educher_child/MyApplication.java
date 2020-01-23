package com.example.educher_child;

import android.app.Application;

import com.example.educher_child.Database.DbIgnoreExecutor;
import com.example.educher_child.Database.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceManager.init(this);
        DbIgnoreExecutor.init(getApplicationContext());
        DataManager.init();
        addDefaultIgnoreAppsToDB();
    }

    private void addDefaultIgnoreAppsToDB() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> mDefaults = new ArrayList<>();
                mDefaults.add("com.android.settings");
                mDefaults.add(BuildConfig.APPLICATION_ID);
                for (String packageName : mDefaults) {
                    AppItem item = new AppItem();
                    item.mPackageName = packageName;
                    item.mEventTime = System.currentTimeMillis();
                    DbIgnoreExecutor.getInstance().insertItem(item);
                }
            }
        }).run();
    }
}
