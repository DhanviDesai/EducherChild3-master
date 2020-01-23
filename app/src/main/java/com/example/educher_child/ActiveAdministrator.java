package com.example.educher_child;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class ActiveAdministrator extends AppCompatActivity {

    private static final String TAG = "ActiveAdmin";
    private int REQUEST_CODE_ENABLE_ADMIN = 32323;
    private ComponentName cn;
    private DevicePolicyManager mDevicePolicyManager;

    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_administrator);

//        //change status bar color
//        Window window = this.getWindow();
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        window.setStatusBarColor(ContextCompat.getColor(this,R.color.color1));


        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        //Get the identifier for the ActiveAdminBroadcast receiver
        cn=new ComponentName(getApplicationContext(), ActiveAdminBroadcast.class);

        if (mDevicePolicyManager.isAdminActive(cn)) {
            nextActivity();
        }

        //Upon clicking next button
        findViewById(R.id.next_admin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Check if the permission is already given using the identifier obtained for the broadcast receiver class
                if (mDevicePolicyManager.isAdminActive(cn)) {
                    nextActivity();
                }
                else {

                    //Create an intent to go fetch the device admin permission
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);

                    //add in the receiver class as the intent extra
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cn);

                    //add the explanation for accessing the admin privileges
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                            ActiveAdministrator.this.getString(R.string.device_admin_explanation));

                    //start action with the intent and the request code to catch it later
                    startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
                }
            }
        });

        //Upon clicking the grant button
        findViewById(R.id.grant_admin).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                adminIntent();
            }
        });
    }

    private void adminIntent(){
        if (mDevicePolicyManager.isAdminActive(cn)) {

        }
        else {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cn);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    this.getString(R.string.device_admin_explanation));
            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
        }
    }

    private void nextActivity(){
        startActivity(new Intent(getApplicationContext(), UsagePermission.class));
        finish();
    }
}
