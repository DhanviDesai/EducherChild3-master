package com.example.educher_child.Database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
//import android.support.annotation.Nullable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.educher_child.AppItem;
import com.example.educher_child.DataManager;
import com.example.educher_child.Model.AppInfo;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppLockDatabase extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "ParentControl";

    //Table to store all the installed apps
    private static final String TABLE_ALL_APPS = "allApps";

    //Table to store the our app data such as parent_key,child_token etc
    private static final String TABLE_APP_DATA = "appData";

    //Table to store the schedule time of each app for the child by the parent
    private static final String TABLE_SCHEDULE_LOCK = "scheduleChild";

    //Table to store the installed apps usage
    private static final String TABLE_APP_USAGE = "appuse";

    // Common column names
    private static final String KEY_ID = "id";

    // ALL Apps Table - column names
    private static final String APP_PACKAGE = "appPackage";
    private static final String APP_NAME = "appName";
    private static final String LOCKED = "locked";

    // appData Table - column names
    private static final String PROCESS_NAME = "name";
    private static final String PROCESS_VALUE = "value";

    //schedule
    private static final String FROM = "sFrom";
    private static final String TO = "sTo";
    private static final String DAY = "day";

    //app use table
    private static final String ALLOW_TIME = "allowtime";
    private static final String USAGE_TIME = "usagetime";
    private static final String SCHEDULE_DAY = "day";


    private static final String TAG = "AppLockDatabase";



    //CREATE TABLE allapps (id INTEGER PRIMARY KEY AUTOINCREMENT,appName TEXT, appPackage TEXT, locked BOOLEAN)
    private static final String CREATE_TABLE_ALL_APPS = "CREATE TABLE "
            + TABLE_ALL_APPS + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + APP_NAME
            + " TEXT,"+ APP_PACKAGE
            + " TEXT," + LOCKED + " BOOLEAN"+ ")";

    //  table create statement
    //CREATE TABLE appData
    private static final String CREATE_TABLE_APP_DATA = "CREATE TABLE " + TABLE_APP_DATA
            + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"+ PROCESS_NAME + " TEXT,"
            + PROCESS_VALUE + " TEXT" + ")";

    //  table create statement
    private static final String CREATE_TABLE_SCHEDULE = "CREATE TABLE " + TABLE_SCHEDULE_LOCK
            + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"+ FROM + " TEXT,"
            + TO + " TEXT," + DAY + " TEXT" + ")";

    //app usage table
    //CREATE TABLE appuse (id INTEGER PRIMARY KEY AUTOINCREMENT, appPackage TEXT, allowtime TEXT, usagetime TEXT, day TEXT);
    private static final String CREATE_TABLE_APP_USE = "CREATE TABLE " + TABLE_APP_USAGE
            + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"+ APP_PACKAGE + " TEXT,"+ALLOW_TIME + " TEXT,"+USAGE_TIME+" TEXT,"+SCHEDULE_DAY+" TEXT"+")";

    private Context context;

    public AppLockDatabase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        SQLiteDatabase database = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ALL_APPS);
        db.execSQL(CREATE_TABLE_APP_DATA);
        db.execSQL(CREATE_TABLE_SCHEDULE);
        db.execSQL(CREATE_TABLE_APP_USE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALL_APPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APP_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULE_LOCK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APP_USAGE);

    }

    public long insertApps(String name, String packageName, Boolean isLocked){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(APP_NAME,name);
        contentValues.put(APP_PACKAGE,packageName);
        contentValues.put(LOCKED,isLocked);
        long result = database.insert(TABLE_ALL_APPS,null,contentValues);
        return result;
    }

    public ArrayList<AppInfo> getLockedApplication(){
        SQLiteDatabase database = this.getWritableDatabase();
        ArrayList arrayList = new ArrayList();
        Gson gson= new Gson();
        Cursor data = database.rawQuery("SELECT * FROM "+TABLE_ALL_APPS,null);
        while (data.moveToNext()){

            Boolean lock = data.getInt(3) > 0;
            Log.d(TAG, "getLockedApplication: "+lock);
            if (lock){
                arrayList.add(data.getString(2));
            }
        }
        gson.toJson(arrayList);
        data.close();
        return arrayList;
    }

    public Cursor fetchAllAPPFomDataBase(){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor data = database.rawQuery("SELECT * FROM "+TABLE_ALL_APPS,null);
        return data;
    }

    public void updateAppState(String id, Boolean isLocked){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(LOCKED, isLocked);
        String[] arg = {id};
        db.update(TABLE_ALL_APPS,values,KEY_ID + " = ?",arg);
    }

    public void updateAppStateByPackage(String packageName, Boolean isLocked){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(LOCKED, isLocked);
        String[] arg = {packageName};
        db.update(TABLE_ALL_APPS,values,APP_PACKAGE + " = ?",arg);
    }

    public Cursor searchInAllAppsTable(String packageName){
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.query(TABLE_ALL_APPS,new String[]{KEY_ID,APP_NAME,APP_PACKAGE,LOCKED},APP_PACKAGE + "=?",new String[]{packageName},null,null,null);
        return cursor;
    }

    public long insertDataInAppDataTable(String name, String value){
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PROCESS_NAME,name);
        values.put(PROCESS_VALUE,value);

        long res = database.insert(TABLE_APP_DATA,null,values);
        return res;

    }

    public Cursor searchInAppDataTable(String name){
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor =  database.query(TABLE_APP_DATA,new String[]{KEY_ID,PROCESS_NAME,PROCESS_VALUE},PROCESS_NAME + "=?",
                new String[]{name},null,null,null);
        return cursor;
    }

    public void insertData(String from, String to, String day){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FROM,from);
        contentValues.put(TO,to);
        contentValues.put(DAY,day);
        database.insert(TABLE_SCHEDULE_LOCK,null,contentValues);

    }

    public Cursor getTodayTasks(){
        SQLiteDatabase database = this.getReadableDatabase();
        String columns[] = {KEY_ID,FROM,TO,DAY};
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        Date d = new Date();
        String dayOfTheWeek = sdf.format(d);
        Log.d(TAG, "getTodayTasks: "+dayOfTheWeek);
        String args[] = {dayOfTheWeek};
        Cursor cursor = database.query(TABLE_SCHEDULE_LOCK,columns,DAY+"=?",args,null,null,null);
        return cursor;
    }

    public Cursor everyday(){
        SQLiteDatabase database = this.getReadableDatabase();
        String columns[] = {KEY_ID,FROM,TO,DAY};
        String args[] = {"every"};
        Cursor cursor = database.query(TABLE_SCHEDULE_LOCK,columns,DAY+"=?",args,null,null,null);
        return cursor;
    }

    public void deleteall(){
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("delete from "+ TABLE_SCHEDULE_LOCK);
    }

    //Insert the values in the usage table for a fresh app
    public void insert(String packageName, String allowTime, String day){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(APP_PACKAGE,packageName);
        values.put(ALLOW_TIME,allowTime);
        values.put(SCHEDULE_DAY,day);
        values.put(USAGE_TIME,"00:00:00");
        long res = sqLiteDatabase.insert(TABLE_APP_USAGE,null,values);
        if (res>0){
            Toast.makeText(context, "App Added", Toast.LENGTH_SHORT).show();
        }

    }


    public void deleteScheduleremove(String packageName){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_APP_USAGE,APP_PACKAGE +"="+"'"+ packageName+"'",null);

    }

    public void update(String packageName, String usetime){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USAGE_TIME, usetime);
        String[] arg = {packageName};
        db.update(TABLE_APP_USAGE,values,APP_PACKAGE + " = ?",arg);
    }



    public Cursor getusageApp(String packageName){

        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.query(TABLE_APP_USAGE,new String[]{KEY_ID,APP_PACKAGE,ALLOW_TIME,USAGE_TIME,SCHEDULE_DAY},
                APP_PACKAGE + "=?",new String[]{packageName},null,null,null);
        return cursor;
    }

    //Update the allow time for the apps that are already present in the database
    public void updateAllowTime(String packageName, String usetime, String day){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ALLOW_TIME, usetime);
        values.put(SCHEDULE_DAY,day);
        String[] arg = {packageName};
        db.update(TABLE_APP_USAGE,values,APP_PACKAGE + " = ?",arg);
    }
}
