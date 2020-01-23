package com.example.educher_child;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.educher_child.Database.AppLockDatabase;
import com.example.educher_child.Database.PrefManager;
import com.example.educher_child.Model.MySingleton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.example.educher_child.AppConfiguration.APPS;
import static com.example.educher_child.AppConfiguration.CHILD_ID;
import static com.example.educher_child.AppConfiguration.FCM_API;
import static com.example.educher_child.AppConfiguration.PARENT_KEY;
import static com.example.educher_child.AppConfiguration.contentType;
import static com.example.educher_child.AppConfiguration.serverKey;

public class ActiveAdminBroadcast extends DeviceAdminReceiver {

    private static final String TAG = "ActiveAdminBroadcast";
    private String t;
    private AppLockDatabase dataBaseHelper;
    private PrefManager prefManager,pre;
    private DatabaseReference reference;


    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        pre = new PrefManager(context);
        pre.setIsSettingBlock(false);
        pre.setIsLauncherBlock(false);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
        prefManager = new PrefManager(context);
        dataBaseHelper = new AppLockDatabase(context);

        Cursor parent_key = dataBaseHelper.searchInAppDataTable(PARENT_KEY);
        parent_key.moveToFirst();
        t= parent_key.getString(2);
        parent_key.close();
        Cursor child_id = dataBaseHelper.searchInAppDataTable(CHILD_ID);
        child_id.moveToFirst();

        //update firebase rules
        Log.d(TAG, "onDisabled: parent "+t);
        Log.d(TAG, "onDisabled: child"+child_id.getString(2));
        Log.d(TAG, "onDisabled: "+prefManager.getLauncherPackage());
        reference = FirebaseDatabase.getInstance().getReference().child(t).child(child_id.getString(2)).child(APPS);
        reference.child(prefManager.getLauncherPackage().replace(".","_")).child("locked").setValue(true);
        reference.child(prefManager.getSettingPackage().replace(".","_")).child("locked").setValue(true);

        //update sqlite rules
        dataBaseHelper.updateAppStateByPackage(prefManager.getLauncherPackage(),true);
        dataBaseHelper.updateAppStateByPackage(prefManager.getSettingPackage(),true);
        prefManager.setIsLauncherBlock(true);
        prefManager.setIsSettingBlock(true);
        sendNotification(t,context);
    }

    private void sendNotification(String token,Context context){

        String NOTIFICATION_TITLE ="usama";
        String NOTIFICATION_MESSAGE = "Attention!Your child is trying to uninstall the app";

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
        sendNotification(notification,context);
    }

    private void sendNotification(JSONObject notification,Context context) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
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
        MySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

}
