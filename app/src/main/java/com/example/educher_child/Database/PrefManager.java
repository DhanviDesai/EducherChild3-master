package com.example.educher_child.Database;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static final String PREF_NAME = "parent_control";
    private static final String PACKAGE = "package";
    private static final String IS_TIMES_UP = "timesup";
    private static final String SUG_COUNT = "suggestion_count";
    private static final String IS_LAUNCHER_BLOCK = "louncher";
    private static final String IS_SETTING_BLOCK = "setting";
    private static final String LAUNCHER_PACKAGE = "launcherpackage";
    private static final String SETTING_PACKAGE = "settingpackage";

    private Context context;

    public PrefManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME,context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setPackage(String p){
        editor.putString(PACKAGE,p);
        editor.commit();
    }

    public String getPackage(){
        return sharedPreferences.getString(PACKAGE,null);
    }

    public void setIsTimesUp(Boolean isTimesUp){
        editor.putBoolean(IS_TIMES_UP,isTimesUp);
        editor.commit();
    }

    public Boolean getIsTimeUp(){
        return sharedPreferences.getBoolean(IS_TIMES_UP,false);
    }

    public void setSugCount(int count){
        editor.putInt(SUG_COUNT,count);
        editor.commit();
    }

    public int getSugCount(){
        return sharedPreferences.getInt(SUG_COUNT,0);
    }

    public void setIsLauncherBlock(Boolean isLauncherBlock){
        editor.putBoolean(IS_LAUNCHER_BLOCK,isLauncherBlock);
        editor.commit();
    }

    public Boolean getIsLauncherBlock(){
        return sharedPreferences.getBoolean(IS_LAUNCHER_BLOCK,false);
    }

    public void setIsSettingBlock(Boolean isSettingBlock){
        editor.putBoolean(IS_SETTING_BLOCK,isSettingBlock);
        editor.commit();
    }

    public Boolean getIsSettingBlock(){
        return sharedPreferences.getBoolean(IS_SETTING_BLOCK,false);
    }

    public void setLauncherPackage(String launcherPackage){
        editor.putString(LAUNCHER_PACKAGE,launcherPackage);
        editor.commit();
    }

    public String getLauncherPackage(){
        return sharedPreferences.getString(LAUNCHER_PACKAGE,null);
    }

    public void setSettingPackage(String settingPackage){
        editor.putString(SETTING_PACKAGE,settingPackage);
        editor.commit();
    }

    public String getSettingPackage(){
        return sharedPreferences.getString(SETTING_PACKAGE,null);
    }
}
