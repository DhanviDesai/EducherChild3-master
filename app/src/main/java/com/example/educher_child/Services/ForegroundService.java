package com.example.educher_child.Services;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.educher_child.Database.AppLockDatabase;
import com.example.educher_child.Database.PrefManager;
import com.example.educher_child.LockActivity;
import com.example.educher_child.Model.MySingleton;
import com.example.educher_child.Model.SchModel;
import com.example.educher_child.OverlayDialog;
import com.example.educher_child.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rvalerio.fgchecker.AppChecker;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.educher_child.AppConfiguration.APPS;
import static com.example.educher_child.AppConfiguration.CHILD_ID;
import static com.example.educher_child.AppConfiguration.CHILD_TOKEN;
import static com.example.educher_child.AppConfiguration.FCM_API;
import static com.example.educher_child.AppConfiguration.PARENT_KEY;
import static com.example.educher_child.AppConfiguration.SCHEDULE_SINGLE;
import static com.example.educher_child.AppConfiguration.serverKey;
import static com.example.educher_child.AppConfiguration.contentType;

public class ForegroundService extends Service {
    private static final String TAG = "ForeGround_AppsService_";
    private AppChecker appChecker;
    private AppLockDatabase dbHelper;
    private ArrayList arrayList,childLocks;
    private PackageManager pm;
    private List<PackageInfo> apps;
    private Context context;
    private PrefManager prefManager;
    private Dialog dialog;
    private List<String> from = new ArrayList<>();
    private List<String > to = new ArrayList<>();
    private List<SchModel> packageName = new ArrayList<>();
    private Calendar c;
    private Cursor cursor;
    private AppLockDatabase db;
    private int count,total,percent;
    private String dayOfTheWeek,token;
    private DatabaseReference reference;
    public static final int NOTIFICATION_ID = 1;
    public static final String ID = "1234";
    private NotificationCompat.Builder notification;
    public static final int UniqueId = 123;
    private Handler handler= new Handler();



    @Override
    public void onCreate() {
        super.onCreate();

        pm = getApplicationContext().getPackageManager();
        dbHelper = new AppLockDatabase(this);
        prefManager = new PrefManager(getApplicationContext());
        appChecker = new AppChecker();
        arrayList = new ArrayList();
        childLocks = new ArrayList();
        context = getApplicationContext();
        db = new AppLockDatabase(getApplicationContext());

        c = Calendar.getInstance();

        Cursor parent_key = db.searchInAppDataTable(PARENT_KEY);
        parent_key.moveToFirst();

        Cursor childiD = db.searchInAppDataTable(CHILD_ID);
        childiD.moveToFirst();

        Cursor childtoken = db.searchInAppDataTable(CHILD_TOKEN);
        childtoken.moveToFirst();
        token = childtoken.getString(2);
        childtoken.close();

        //reference to all the apps in the child phone which are uploaded on firebase
        reference = FirebaseDatabase.getInstance().getReference(parent_key.getString(2)).child(childiD.getString(2))
                .child(APPS);

        parent_key.close();
        childiD.close();

        initDialoge();

        setLock();

        fetchScheduleData();

        check();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            buildNotification();
            startForeground(UniqueId, notification.build());
        }else{
            Log.d(TAG, "onCreate: No need");
        }

    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand: ");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void setLock(){
        apps = pm.getInstalledPackages(PackageManager.GET_META_DATA);

        appChecker.whenAny(new AppChecker.Listener() {
            @Override

            //Called when a new app is opened on the phone
            public void onForeground(String process) {


                //Get today's day
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
                SimpleDateFormat sdf1 = new SimpleDateFormat("H:m");

                //Get today's date
                Date d = new Date();

                //Format it for today's day
                dayOfTheWeek = sdf.format(d);

                //get the list of all the locked apps from the local database
                arrayList = dbHelper.getLockedApplication();

                //get time from date object
                String time = sdf1.format(d);
                Log.i("Time",time);

                //App unlocks once the day is over by updating its usage time to zero
                if (time.equals("23:59")){
                    dbHelper.update(process,"00:00:00");
                    prefManager.setIsTimesUp(false);
                }

                //query the local database for the opened app's usage time
                cursor = dbHelper.getusageApp(process);
                if (cursor.getCount()>0){

                    //The condition is true if the user has used the app atleast once
                    setSchedule(process);
                }else{
                    if (arrayList.contains(process)) {
//                        Intent intent1 = new Intent(getApplicationContext(), LockActivity.class);
//                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent1);
                        showDialog();
                    }else {
                        hideDialoge();
                    }
                }

            }
        }).timeout(500).start(getApplicationContext());
    }

    private void buildNotification(){
        notification = new NotificationCompat.Builder(this,ID);

        notification.setSmallIcon(R.drawable.ic_lock)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("App Locker")
                .setContentText("Lock service running");

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            notification.setChannelId(ID);
        }
    }

    private void check(){

        final Runnable runnableCode = new Runnable() {
            @Override
            public void run() {

                try{
                    Cursor cursor = db.getTodayTasks();
                    Cursor cursor1 = db.everyday();
                    Log.d(TAG, "run: "+cursor.getCount());
                    while (cursor.moveToNext()){
                        from.add(cursor.getString(1));
                        to.add(cursor.getString(2));
                    }

                    cursor.close();
                    if(cursor1.getCount()>0){
                        cursor1.moveToNext();
                        from.add(cursor1.getString(1));
                        to.add(cursor1.getString(2));
                    }

                }catch (Exception e){
                    Log.d(TAG, "run: "+e.getMessage());
                }
                for (int i = 0;i<from.size();i++){
                    int hour = c.get(Calendar.HOUR_OF_DAY);
                    int mint = c.get(Calendar.MINUTE);
                    String time = hour + ":" + mint;

                    if (time.equals(from.get(i))){
                        for (int f=0;f<packageName.size();f++){
                            Log.d(TAG, "run: from");
                            updateDataIntoSQLiteDataBase(packageName.get(f).getId(),false);
                            reference.child(packageName.get(f).getPkg()).child("locked").setValue(false);
                        }
                    }else if(time.equals(to.get(i))){
                        for (int f=0;f<packageName.size();f++){
                            Log.d(TAG, "run: to");
                            updateDataIntoSQLiteDataBase(packageName.get(f).getId(),true);
                            reference.child(packageName.get(f).getPkg()).child("locked").setValue(true);
                        }
                    }
                }
                handler.removeCallbacks(this);
                handler.postDelayed(this, 10000);
            }
        };
        handler.removeCallbacks(runnableCode);
        handler.post(runnableCode);
    }

    private void updateDataIntoSQLiteDataBase(String id,Boolean isLock){
        AppLockDatabase helper = new AppLockDatabase(getApplicationContext());
        helper.updateAppState(id,isLock);
    }

    private void fetchScheduleData(){
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
    }

    private void setupsendNotification(int percent){

        String NOTIFICATION_TITLE ="usama";
        String NOTIFICATION_MESSAGE = "Attention!Your used "+percent+" schedule time for this app ";

        JSONObject notification = new JSONObject();
        JSONObject notifcationBody = new JSONObject();
        try {
            notifcationBody.put("title", NOTIFICATION_TITLE);
            notifcationBody.put("message", NOTIFICATION_MESSAGE);

            notification.put("to", token);
            notification.put("data", notifcationBody);
        } catch (JSONException e) {
            Log.e("loc", "onCreate: " + e.getMessage() );
        }
        sendNotification(notification);

    }

    private void sendNotification(JSONObject notification) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,FCM_API, notification,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "onResponse: " + response.toString());

                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onErrorResponse: "+error);
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    private void initDialoge(){
        if (context == null)
            context = getApplicationContext();
        LayoutInflater layoutInflater = LayoutInflater.from(this);
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
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                showDialog();
            }
        });

    }

    private void showDialog() {
        dialog.show();
    }

    private void hideDialoge(){
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }



    private void setSchedule(String process){
        if (cursor.getCount()>0){

            Log.i("Ininside","In here");

            //Move to the first row of the query result
            cursor.moveToFirst();

            //Get the day from the
            String allowDay = cursor.getString(4);
            Log.i("Allowday",allowDay);
            Log.i("Dayofweek",dayOfTheWeek);
            if (allowDay.equals(dayOfTheWeek) || allowDay.equals("everyday")){

                AppLockDatabase db = new AppLockDatabase(context);
                db = new AppLockDatabase(getApplicationContext());
                Cursor cursor = db.searchInAppDataTable(PARENT_KEY);
                cursor.moveToFirst();
               String parent_key = cursor.getString(2);
                cursor.close();
                final Cursor c = db.searchInAppDataTable(CHILD_ID);
                c.moveToFirst();
                String childiD = c.getString(2);
                c.close();

                //Columnindex 3 is for usage time of the app
                Log.i("hi",this.cursor.getString(3));

                //Split the usage time
                Log.i("Split", Arrays.toString(this.cursor.getString(3).split(":")));
                String[] use = this.cursor.getString(3).split(":",3);

                //Columnindex 2 is for allow time
                Log.i("Allow",this.cursor.getString(2));

                //Columnindex 1 is for app name
                Log.i("Appname",this.cursor.getString(1));

                //Split the allow time
                String allow = this.cursor.getString(2);
                count = (Integer.parseInt(use[0])*3600)+(Integer.parseInt(use[1])*60)+(Integer.parseInt(use[2]));
                total = (Integer.parseInt(allow)*60);
                Log.i("Childid",childiD);
//                percent = Math.round(((count / (float)total) * 100));
//                if (percent == 50){
//                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(parent_key).child(childiD)
//                            .child(SCHEDULE_SINGLE).child(process.replace('.','_')).child("progress");
//                    ref.setValue(50);
//                    setupsendNotification(50);
//                }
//                if (percent>=90 && percent<=92){
//                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(parent_key).child(childiD)
//                            .child(SCHEDULE_SINGLE).child(process.replace('.','_')).child("progress");
//                    ref.setValue(90);
//                    setupsendNotification(90);
//                }
//

                if(count<=total) {
                    count++;
                    DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference().child(parent_key).child(childiD)
                            .child(SCHEDULE_SINGLE).child(process.replace('.','_')).child("progress");
                    ref1.setValue(count);
                }
                int mint = count/60;
                int sec = count%60;
                dbHelper.update(process,"0:"+mint+":"+sec);
                if (count>=total){
                    Toast.makeText(context, "Time up", Toast.LENGTH_SHORT).show();
                    showDialog();
                }

            }else{

//                prefManager.setPackage(process);
//                Intent intent1 = new Intent(getApplicationContext(), LockActivity.class);
//                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent1);
               hideDialoge();
            }
        }
    }


}
