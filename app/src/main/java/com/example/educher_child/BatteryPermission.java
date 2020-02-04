package com.example.educher_child;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class BatteryPermission extends AppCompatActivity {

    private static final String TAG = "BetteryOptimizationPerm";
    private static final int MY_IGNORE_OPTIMIZATION_REQUEST = 232;
    private PowerManager pm;
    private boolean isIgnoringBatteryOptimizations;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_permission);

        //change status bar color
//        Window window = this.getWindow();
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        window.setStatusBarColor(ContextCompat.getColor(this,R.color.color1));


        pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        isIgnoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(getPackageName());

        Log.d(TAG, "onCreate: "+isIgnoringBatteryOptimizations);

        if (isIgnoringBatteryOptimizations){
            startActivity(new Intent(getApplicationContext(),ChildHidden.class));
            finish();
        }

        findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isIgnoringBatteryOptimizations){
                    startActivity(new Intent(getApplicationContext(),ChildHidden.class));
                    finish();
                }else{
                    Toast.makeText(BatteryPermission.this, "Please Give Permission", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.grant).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                battery_Intent();
            }
        });
    }

    private void battery_Intent(){

        if(!isIgnoringBatteryOptimizations){
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, MY_IGNORE_OPTIMIZATION_REQUEST);

            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_IGNORE_OPTIMIZATION_REQUEST && resultCode == RESULT_OK){
            Log.d(TAG, "onActivityResult: accepted");
            isIgnoringBatteryOptimizations = true;
        }else {
            Log.d(TAG, "onActivityResult: not");
            isIgnoringBatteryOptimizations = false;
        }
    }
}
