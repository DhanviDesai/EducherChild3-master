package com.example.educher_child.Services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.educher_child.AppItem;
import com.example.educher_child.DataManager;
import com.example.educher_child.Database.AppLockDatabase;
import com.example.educher_child.Database.PreferenceManager;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import static com.example.educher_child.AppConfiguration.APPS_USAGE;
import static com.example.educher_child.AppConfiguration.CHILD_ID;
import static com.example.educher_child.AppConfiguration.PARENT_KEY;

public class UsageStat extends Service {
    private long mTotal;
    private int mDay;
    private PackageManager mPackageManager;
    private AppLockDatabase dbHelper;
    public String pa,chi;
    private Handler handler= new Handler();
    private static final String TAG = "UsageStatService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");

        //Initialize the database
        dbHelper = new AppLockDatabase(getApplicationContext());

        //Query the database for the parentkey
        Cursor cursor = dbHelper.searchInAppDataTable(PARENT_KEY);
        cursor.moveToFirst();
        pa = cursor.getString(2);
        cursor.close();

        //Query for child_id
        Cursor c = dbHelper.searchInAppDataTable(CHILD_ID);
        c.moveToFirst();
        chi = c.getString(2);
        c.close();


        mPackageManager = getPackageManager();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                initSpinner();
                handler.postDelayed(this,60000);
            }
        };
        handler.post(runnable);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initSpinner() {
        if (DataManager.getInstance().hasPermission(getApplicationContext())) {
            mDay = 0;
            process();
        }
    }

    private void process() {
        if (DataManager.getInstance().hasPermission(getApplicationContext())) {
            int sortInt = PreferenceManager.getInstance().getInt(PreferenceManager.PREF_LIST_SORT);
            new MyAsyncTask().execute(sortInt, mDay);
        }
    }

    @SuppressLint("StaticFieldLeak")
    class MyAsyncTask extends AsyncTask<Integer, Void, List<AppItem>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected List<AppItem> doInBackground(Integer... integers) {
            return DataManager.getInstance().getApps(getApplicationContext(), integers[0], integers[1]);
        }

        @Override
        protected void onPostExecute(List<AppItem> appItems) {
            Log.d(TAG, "onPostExecute: ");
            mTotal = 0;
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(pa).child(chi).child(APPS_USAGE);
            for (AppItem item : appItems) {
                if (item.mUsageTime <= 0) continue;
                reference.child(item.mPackageName.replace(".","_")).setValue(item);
                mTotal += item.mUsageTime;
                item.mCanOpen = mPackageManager.getLaunchIntentForPackage(item.mPackageName) != null;
            }
        }
    }
}
